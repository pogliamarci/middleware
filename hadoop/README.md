Power monitoring with Hadoop
============================

The [data-generator.jar](http://corsi.dei.polimi.it/distsys/pub/data-generator.jar)
Java program generates a dataset of a random set of smart plugs, each being
part of a household, which is, in turn, part of a house.
Each smart plug records the actual load (in Watts) at each second. The
generated dataset is inspired by the [DEBS 2014
challenge](http://www.cse.iitb.ac.in/debs2014/?page_id=42) and follow a similar
format: a sequence of 6 comma separated values for each line (i.e., for each
reading)

* a unique identifier of the measurement [64 bit unsigned integer value]
* a timestamp of measurement (number of seconds since January 1, 1970, 00:00:00
  GMT) [64 bit unsigned integer value]
* a unique identifier (within a household) of the smart plug [32 bit unsigned
  integer value]
* unique identifier of a household (within a house) where the plug is located
  [32 bit unsigned integer value]
* unique identifier of a house where the household with the plug is located [32
  bit unsigned integer value]
* the measurement [32 bit unsigned integer]

Use Hadoop to calculate the outliers of the energy consumption measurements.
For each house and for each hour in the dataset (start from the first timestamp
and "slice" the dataset in blocks of 3600 seconds each), calculate the
percentage of plugs in that house that have a median load, during the hour,
greater than the median load of all the plugs (in the same house, in the same
hour). Notice that this is a slight variation of "Query 2" on the DEBS Website,
essentially without the sliding window and focusing on houses and percentages
instead of identifiers.

### Notes

* The attached Java program takes one parameter, the number of seconds for the
  dataset to be generated in that specific run; you can generate small datasets
  to test your application, but when you will present your project you will
  need to show it running at least on a 24 hour dataset (which is about 1 GB of
  data). Notice that each run will generate a different dataset (randomly)
* The program prints the CSV file to stdout. At the beginning of the run it
  prints on stderr the set of houses with IDs, for each house the set of
  households with IDs, for each household the set of plugs with IDs. Indeed,
  this data changes at each run, being generated randomly (see point above)
* You have to use the Java interface for Hadoop (optionally you can implement
  the same solution also using Pig)
* If you need to pass a value to a Job, for example a constant value valid for
  a MR run, you can use the Configuration class (Mapper and Reducer instances
  can access the current configuration)
* The code has to be demonstrated using at least two physical machines
  connected in a LAN (with or without additional virtual machines to emulate a
  larger cluster), with a replication value for HDFS of at least 2


## Installation

This project uses Apache Maven to compile and it has been tested with Maven 3.1.1 and Hadoop 2.2.0 under a Unix environment.
Java SE 7 or later is also required.
The pig version has been tested with Pig 0.12.0. Notice that, in order to work with Hadoop 2.x, pig could need to be recompiled
with the `-Dhadoopversion=23` option. The relevant sources are already included in the pig binary distribution and can be compiled using `maven`.

To compile the code and produce the JAR file, install Maven, put the `bin` directory in your `PATH` and execute

```
$ mvn package
```

This command will download all the necessary dependencies and produce the `hadoop-prj-0.1.jar` file.

## Execution

Install Apache Hadoop 2.2.0, then configure the `JAVA_HOME` variable in the `hadoop-2.2.0/etc/hadoop/hadoop-env.sh` script.
Make sure that the `hadoop-2.2.0/logs` directory is writable by the user who executes the Hadoop jobs.

To run the application in standalone mode, the default configuration is enough; to run it, put the `bin` and `sbin` directories of Hadoop in your `PATH` and execute
```
$ java -jar data/data-generator.jar <number of seconds> > /tmp/dataset.csv
$ hadoop jar hadoop-prj-0.1.jar -fs file:/// /tmp/dataset.csv output
```
The `output` directory should not exist.

### Pig

The pig script is contained in the `script/` folder. To execute it, the `hadoop-prj-0.1.jar` file and the `dataset.csv` file (input file) need
to be readable in the same directory where the script is being run, otherwise you need to change the paths in the `pig` script.

For example, to run the script locally:
```
$ pig -x local script/outliers.pig
```

### Cluster mode

The steps required to setup the cluster are the following:
* Install Hadoop on all the machines in the same directory
* Install and activate a `ssh` daemon on every machine
* Create the same user (e.g., `cluster`) in the same machines
* Generate a `ssh` key for the user you created on each machine
  and distribute the public keys (`.pub` files) copying all of them
  in the `~/.ssh/authorized_keys` file on each node. Every node should
  be able to connect via `ssh` without to every host, itself included
* Customize the configuration file in the `conf/` directory. The files assume
  that the master node's hostname is `hadoop-master.lan` and that there is only a
  single slave node, whose hostname is `hadoop-slave1.lan`.
* Configure the `JAVA_HOME` variable in the `conf/hadoop-env.sh`
* The configuration can either be installed
  in the `etc/` folder of the Hadoop installation, or can be used locally
  passing the argument `--config <path_to_the_config>` to every Hadoop command.

Finally, on the master node, format the filesystem and start the daemons
```
$ hdfs [--config <conf>] namenode -format
$ start-dfs.sh [--config <conf>]
$ start-yarn.sh [--config <conf>]
$ mr-jobhistory-daemon.sh [--config <conf>] start historyserver
```
The `jps` command shows all the daemons that are running: in the master node, there should be the `NameNode`, a `DataNode`, the `NodeManager`, the `ResourceManager` and the `HistoryServer`.
If some of them are missing, check the logs under the Hadoop installation directory.

Now you can copy the input and execute the demo
```
$ java -jar data/data-generator.jar <number of seconds> > /tmp/dataset.csv
$ hadoop [--config <conf>] fs -mkdir -p /user/<username>
$ hadoop [--config <conf>] fs -copyFromLocal /tmp/dataset.csv .
$ hadoop [--config <conf>] jar hadoop-prj-0.1.jar dataset.csv output
```
To see the result of the job execute the following
```
$ hadoop [--config <conf>] fs -cat output/part-r-00000
```
In order to run again the program, you need to delete the `output` directory and its contents:
```
$ hadoop [--config <conf>] fs -rm output/*
$ hadoop [--config <conf>] fs -rmdir output
```

Note: the number of mappers (and, thus, how the work is split among the nodes
in the cluster) depends upon the HDFS block size of the input file. If the file is
small, you can experience that all the mappers are run by a single host. To overcome
this problem, explicitly set the HDFS block size to a smaller value than the default one.

To copy the input file to HDFS setting the block size, run the following command:
```
hadoop fs -D dfs.block.size=$BLOCK_SIZE -copyFromLocal dataset.csv .
```
where `$BLOCK_SIZE` is the size (in bytes) you want to set for a block, 
and should be a multple of 512 bytes. For example, `$BLOCK_SIZE=41943040` sets the 
block size to about 40 MB, which produces 19 mappers for a dataset of about 700 MB.

To change also the number of reducers, pass the `-D mapred.reduce.tasks=<reducers>` parameter 
to `hadoop` when running the job.

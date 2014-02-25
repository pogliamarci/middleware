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


Setup instructions
==================

Requirements:
* Java 7 or later
* Apache Hadoop
* Maven (to compile and package the sources)

Setup:
* Download and install the Hadoop release on each machine in the cluster. This
  project has been tested with Apache Hadoop 2.2.0 under a Unix system. In
  order to ease administration, it is advised to install Hadoop on the same
  location in each machine of the cluster. From now on, this directory will be
  called `$HADOOP_DIR`.
* To ease administration, it is advised to create a common user on every
  cluster node (e.g., the `hadoop` user). Notice that the user executing hadoop
  should be able to write to the `$HADOOP_DIR/log`.
* To run the control scripts, SSH needs to be set up on each machine to allow
  password-less login (login with public-private keypair) from itself and any
  other node in the cluster.

Configuration:
An example of configuration files is included in the conf/ directory. The
provider configuration assumes two nodes, a master node called
`hadoop-master.lan` and a single slave node called `hadoop-slave1.lan`.

Run:
...

See Sivieri's readme. (TBC)

Additional notes:
* specify --conf <path to the conf directory> to each Hadoop command to use a
local site configuration instead of the one stored in $HADOOP_DIR/etc.
* there is no need to specify the main class, as it is already specified in the
  jar's manifest.
* ...
To compile the project: `mvn package`. This command should generate a `hadoop-prj-0.1.jar` file containing the compiled Java classes.

Single host: standalone mode
============================
export HADOOP_CLASSPATH=hadoop-prj-0.1.jar
hadoop it.polimi.distsys.hadoop.PowerOutliersJob -fs file:/// data/data.csv output


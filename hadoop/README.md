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

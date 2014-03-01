--
-- TemperatureOutliersJob - Apache Pig version
--
-- Middleware Technologies for Distributed Systems project, February 2014
-- Marcello Pogliani, Alessandro Riva
--

REGISTER hadoop-prj-0.1.jar;
DEFINE MEDIAN it.polimi.distsys.pig.Median;
DEFINE OUTLIERS it.polimi.distsys.pig.OutliersPercentage;

raw = LOAD 'dataset.csv' USING PigStorage(',') AS (id:long,timestamp:long,house:int,household:int,plug:int,measurement:int);
mintimestamp = FOREACH (GROUP raw ALL) GENERATE MIN(raw.timestamp) as val;
dataset = FOREACH raw GENERATE (timestamp - mintimestamp.val) / 3600 AS hour, house, household, plug, measurement;

plugMedianLoad = FOREACH (GROUP dataset BY (hour, house, household, plug)) {
  sorted = ORDER dataset BY measurement;
  GENERATE group.hour AS hour, group.house AS house, MEDIAN(sorted.measurement) AS median;
}

houseMedianLoad = FOREACH (GROUP dataset BY (hour, house)) {
  sorted = ORDER dataset BY measurement;
  GENERATE group.hour AS hour, group.house AS house, MEDIAN(sorted.measurement) AS aver;
}

grouped = GROUP(JOIN houseMedianLoad by (hour, house), plugMedianLoad BY (hour, house)) BY ($0, $1, $2);

outliersPercentage = FOREACH grouped {
  GENERATE group.hour, group.house, ROUND(OUTLIERS($1.$2, $1.$5)*100f)/100f;
}

DUMP outliersPercentage;

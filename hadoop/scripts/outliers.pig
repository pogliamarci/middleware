--
-- TemperatureOutliersJob - Apache Pig version
--
-- Middleware Technologies for Distributed Systems project, February 2014
-- Marcello Pogliani, Alessandro Riva <{marcello.pogliani, alessandro10.riva}@mail.polimi.it>
--

REGISTER hadoop-prj-0.1.jar;
DEFINE MEDIAN it.polimi.distsys.pig.Median;

raw = LOAD 'dataset.csv' USING PigStorage(',') AS (id:long,timestamp:long,house:int,household:int,plug:int,measurement:int);
firstline = (LIMIT(ORDER raw BY id) 1);

dataset = FOREACH raw GENERATE (timestamp - firstline.timestamp) / 3600 AS hour, house, household, plug, measurement;

byPlug = GROUP dataset BY (hour, house, household, plug);
byHouse = GROUP dataset BY (hour, house);

plugMedianLoad = FOREACH byPlug {
  sorted = ORDER dataset BY measurement;
  GENERATE group.hour AS hour, group.house AS house, MEDIAN(sorted.measurement) AS median;
}

houseMedianLoad = FOREACH byHouse {
  sorted = ORDER dataset BY measurement;
  GENERATE group.hour AS hour, group.house AS house, MEDIAN(sorted.measurement) AS aver;
}

grouped = GROUP(JOIN houseMedianLoad by (hour, house), plugMedianLoad BY (hour, house)) BY ($0, $1, $2);

outliersPercentage = FOREACH grouped {
  outliers = FILTER $1 BY $2 < $5;
  GENERATE group.hour, group.house, ROUND(COUNT(outliers) * 10000f / COUNT($1))/100f;
}

DUMP outliersPercentage;
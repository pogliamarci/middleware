#!/bin/bash

JARFILE=jmscluster-1.0.jar

echo $1
java -cp $JARFILE:$JORAM_HOME/ship/bundle/* it.polimi.distsys.jmscluster.worker.Server $1


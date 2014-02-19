#!/bin/bash

JARFILE=jmscluster-1.0.jar

java -cp $JARFILE:$JORAM_HOME/ship/bundle/* it.polimi.distsys.jmscluster.utils.LocalAdmin


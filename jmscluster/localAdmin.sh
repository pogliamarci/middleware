#!/bin/bash

JARFILE=jmscluster-1.0.jar

host=192.168.60.10
port=160100

java -cp $JARFILE:$JORAM_HOME/ship/bundle/* it.polimi.distsys.jmscluster.utils.LocalAdmin $host $port


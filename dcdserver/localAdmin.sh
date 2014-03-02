#!/bin/bash

JARFILE=dcdserver-1.0.jar

host=localhost
port=16010
jndiHost=localhost
jndiPort=16400

if [ "$1" != "" ]; then
	host=$1
fi

if [ "$2" != "" ]; then
	port=$2
fi

if [ "$3" != "" ]; then
    jndiHost=$3
fi

if [ "$4" != "" ]; then
    jndiPort=$4
fi

# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/samples/bin/clean.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

exec "${JAVA_HOME}"/bin/java -cp $JARFILE:$JORAM_HOME/ship/bundle/* it.polimi.distsys.dcd.utils.LocalAdmin $host $port $jndiHost $jndiPort

#!/bin/bash

JARFILE=jmscluster-1.0.jar


# Verify if JORAM_HOME is well defined
if [ ! -r "$JORAM_HOME"/samples/bin/clean.sh ]; then
  echo "The JORAM_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

exec "${JAVA_HOME}"/bin/java -cp $JARFILE:$JORAM_HOME/ship/bundle/* it.polimi.distsys.jmscluster.client.Client


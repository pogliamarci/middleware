Grid computing with JMS
=======================

#### (version with dynamic code downloading)

Implement an infrastructure to manage jobs submitted to a cluster of hosts.
Each client may submit a job, represented as an object that offers the
following interface:

```java
interface Job extends Serializable {
    public Serializable run();
}
```

the result produced by the job is returned to the client at the end of
execution. Hosts coordinate to execute the submitted jobs in order to share
load in such a way that at each time every host is running the same number of
jobs (or a number as close as possible to that). Use JMS (the point-to-point
and/or the publish-subscribe domain) to implement both the code running on
hosts and the clients.

For simplicity you may assume that hosts already hold the code of submitted
jobs (i.e., you are not expected to struggle with dynamic code downloading, but
if you do this will be appreciated).

## Pre-requirements

The project is managed using [Apache Maven](https://maven.apache.org/), that
needs to be installed in order to compile the sources. To run the project, each
host needs a JDK compatible with Java 7 (or later) and the
[JORAM](http://joram.ow2.org/) middleware.

This document assumes that JORAM is already installed in the `$JORAM_HOME`
directory (just unzip the file downloaded from JORAM's website).

## Execution

* Make sure that the `$JORAM_HOME` and `$JAVA_HOME` environment variables are
  set to the right locations.
* The source code is divided in three different project: one for the
  client-specific components, one for the server-side components and a third
  one for the shared classes and interfaces. The three components can be built
  and packaged at once just building the top-level project:
```
mvn package
```
This will generate two `.jar` files:
  * `jmsgrid-client/jmsgrid-client-1.0.jar` (contains the client)
  * `jmsgrid-worker/jmsgrid-worker-1.0-jar` (the worker and the localAdmin)
Notice that those files already contain the `util` project but not the `JMS`
and `JORAM` related classes.
* Start JORAM. The `single_server.sh` script starts the middleware (listening
  to `localhost:16010`) and the JNDI server (on `localhost:16400`). For
  distributed configurations, refer to the JORAM documentation.
* Create the administered objects. On one host only, run (from the directory
  where the jar file is present): 
```
./localAdmin.sh [host] [port] [jndiHost] [jndiPort]
```
where all parameters are optional (if they are omitted, a connection to a
middleware and a JNDI server running on `localhost` will be attempted).
* On each server (worker), run (from the directory where the jar file is
  present):
```
./server.sh id [jndiHost] [jndiPort]
```
where `id` must be an unique numeric identifier for the server. The other
parameters are optional and specify the host where the JNDI server is running.
* To run the sample client, execute (from the directory where the jar file is
  present):
```
./client.sh [jndiHost] [jndiPort]
```
The sample client will try to run on the available server(s) some `Job`s of
variable duration. The code of the classes that are submitted is not in the
server's classpath: it will be dynamically requested via a JMS message to the
client and subsequently loaded on the server's JVM.

Note: the provided scripts to run the client and the servers are just a wrapper
to:
```
java -cp $JARFILE:$JORAM_HOME/ship/bundle/* <className> <parameters>
```
`className` is `it.polimi.jmsgrid.worker.Server` for a worker,
`it.polimi.jmsgrid.client.Client` for a server and
`it.polimi.jmsgrid.admin.LocalAdmin` for the utility to create the administered
objects.

*Important notice* This is only a proof-of-concept project, intended only to
demonstrate the use of JMS. The administration class will create JMS topics and
queue without authentication, and the server will load and run source code from
an untrusted (and not authenticated) client, without any sandboxing. Be
careful.


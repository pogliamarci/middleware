Grid computing with JMS
=======================

Implement an infrastructure to manage jobs submitted to a cluster of hosts.
Each client may submit a job, represented as an object that offers the
following interface:


    interface Job extends Serializable {
        public Serializable run();
    }

the result produced by the job is returned to the client at the end of
execution. Hosts coordinate to execute the submitted jobs in order to share
load in such a way that at each time every host is running the same number of
jobs (or a number as close as possible to that). Use JMS (the point-to-point
and/or the publish-subscribe domain) to implement both the code running on
hosts and the clients.

For simplicity you may assume that hosts already hold the code of submitted
jobs (i.e., you are not expected to struggle with dynamic code downloading, but
if you do this will be appreciated).

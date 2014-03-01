/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcdserver.worker;

public interface JobsSignalListener {

	void signalJobStart();
	void signalJobEnd();

}

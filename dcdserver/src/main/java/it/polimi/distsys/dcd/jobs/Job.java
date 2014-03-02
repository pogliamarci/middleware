/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcd.jobs;

import java.io.Serializable;

public interface Job extends Serializable {

	Serializable run();

}

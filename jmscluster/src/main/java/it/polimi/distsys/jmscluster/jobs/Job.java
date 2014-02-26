package it.polimi.distsys.jmscluster.jobs;

import java.io.Serializable;

public interface Job extends Serializable {

	Serializable run();

}

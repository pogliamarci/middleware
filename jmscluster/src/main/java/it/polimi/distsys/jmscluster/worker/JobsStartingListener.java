package it.polimi.distsys.jmscluster.worker;

public interface JobsStartingListener {

	void signalJobStart();
	void signalJobEnd();

}

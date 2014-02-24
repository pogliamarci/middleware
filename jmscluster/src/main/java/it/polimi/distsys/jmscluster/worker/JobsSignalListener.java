package it.polimi.distsys.jmscluster.worker;

public interface JobsSignalListener {

	void signalJobStart();
	void signalJobEnd();

}

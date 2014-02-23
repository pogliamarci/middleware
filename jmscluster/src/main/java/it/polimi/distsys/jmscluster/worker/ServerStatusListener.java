package it.polimi.distsys.jmscluster.worker;

public interface ServerStatusListener {
	
	void canAccept(boolean status);
	
}

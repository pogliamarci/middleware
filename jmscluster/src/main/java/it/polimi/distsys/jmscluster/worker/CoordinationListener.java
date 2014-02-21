package it.polimi.distsys.jmscluster.worker;

import javax.jms.Message;
import javax.jms.MessageListener;

public class CoordinationListener implements MessageListener {

	@Override
	public void onMessage(Message arg0) {
		System.out.println("Received coordination message");
	}

}

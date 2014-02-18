package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class ReplyListener implements MessageListener {

	Map<String, Serializable> results;
	
	public ReplyListener() {
		results = new HashMap<String, Serializable>();
	}

	@Override
	public void onMessage(Message msg) {
		if(!(msg instanceof ObjectMessage))
			return;
		ObjectMessage om = (ObjectMessage) msg;
		try {
			synchronized(this)
			{
				results.put(msg.getJMSCorrelationID(), om.getObject());
				notifyAll();
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}		
	}


	public synchronized Serializable get(String corrId) {
		while(results.get(corrId) == null)
		{
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Serializable ret = results.get(corrId);
		results.remove(corrId);
		return ret;
	}
	
}

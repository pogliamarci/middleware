package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

public class ReplyProcesser {

	private Map<String, Serializable> results;
	private Set<String> outstandingReplies;
	private QueueSession session;
	private QueueReceiver recv;
	
	public ReplyProcesser() {
		results = new HashMap<String, Serializable>();
		outstandingReplies = new HashSet<String>();
	}

	public void listen(QueueConnection conn, Queue tempQueue) throws JMSException {
		disconnect();
		session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		recv = session.createReceiver(tempQueue);
		recv.setMessageListener(new ReplyMessageListener());
	}
	
	private synchronized void onMessage(ObjectMessage msg) {
		try {
			outstandingReplies.remove(msg.getJMSCorrelationID());
			results.put(msg.getJMSCorrelationID(), msg.getObject());
			notifyAll();
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
	
	public void disconnect() throws JMSException {
		if(session != null)
			session.close();
		if(recv != null)
		{
			recv.close();
		}
	}
	
	private class ReplyMessageListener implements MessageListener {
		@Override
		public void onMessage(Message msg) {
			if (!(msg instanceof ObjectMessage))
				return;
			ReplyProcesser.this.onMessage((ObjectMessage) msg);
		}
	}

	public synchronized void jobPosted(String id) {
		outstandingReplies.add(id);
	}

	public synchronized void waitForOutstandingReplies() {
		while(!outstandingReplies.isEmpty())
		{
			try {
				wait();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}

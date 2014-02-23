package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private final static Logger LOGGER = Logger.getLogger(ReplyProcesser.class.getName());
	
	private Map<String, Serializable> results;
	private Set<String> outstandingReplies;
	private Set<String> toBeDiscarded;
	private QueueSession session;
	private QueueReceiver recv;
	
	public ReplyProcesser() {
		results = new HashMap<String, Serializable>();
		outstandingReplies = new HashSet<String>();
		toBeDiscarded = new HashSet<String>();
	}

	public void listen(QueueConnection conn, Queue tempQueue) throws JMSException {
		disconnect();
		session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		recv = session.createReceiver(tempQueue);
		recv.setMessageListener(new ReplyMessageListener());
	}

	public synchronized Serializable get(String corrId)
			throws InterruptedException {
		while(!results.containsKey(corrId))
			wait();
		return getAndRemove(corrId);
	}

	public synchronized Serializable get(String corrId, long millis) 
			throws InterruptedException, TimeoutException {
		long initial = System.currentTimeMillis();
		if(millis == 0)
			return get(corrId);
		while(!results.containsKey(corrId))
		{
			long now = System.currentTimeMillis();
			if(now < initial || (now - initial) >= millis)
				wait(millis - (now - initial));
			else throw new TimeoutException();
		}
		return getAndRemove(corrId);
	}
	
	public synchronized boolean isReady(String corrId) {
		return results.containsKey(corrId);
	}
	
	public void disconnect() throws JMSException {
		if(session != null)
			session.close();
		if(recv != null)
		{
			recv.close();
		}
	}

	public synchronized void jobPosted(String id) {
		outstandingReplies.add(id);
	}

	public synchronized void waitForOutstandingReplies() 
			throws InterruptedException {
		while(!outstandingReplies.isEmpty())
			wait();
	}

	// In order not to waste resource, if the future is cancelled 
	// we make sure to discard the result of the call...
	public synchronized void markAsCancelled(String corrId) {
		if(results.containsKey(corrId))
			results.remove(corrId);
		else toBeDiscarded.add(corrId);
	}
	
	private synchronized void onMessage(ObjectMessage msg) {
		try {
			String corrId = msg.getJMSCorrelationID();
			outstandingReplies.remove(msg.getJMSCorrelationID());
			if(toBeDiscarded.contains(corrId))
			{
				toBeDiscarded.remove(corrId);
			} else {
				results.put(msg.getJMSCorrelationID(), msg.getObject());
			}
			notifyAll();
		} catch (JMSException e) {
			LOGGER.log(Level.WARNING, "Error receiving message: " + e.getMessage());
		}		
	}
	
	private Serializable getAndRemove(String corrId) {
		Serializable ret = results.get(corrId);
		results.remove(corrId);
		return ret;
	}
	
	private class ReplyMessageListener implements MessageListener {
		@Override
		public void onMessage(Message msg) {
			if (!(msg instanceof ObjectMessage))
				return;
			ReplyProcesser.this.onMessage((ObjectMessage) msg);
		}
	}
	
}

/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.client;

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

/**
 * Manager for the temporary queue to receive the replies. An object of this
 * class listens on the temporary queue where replies are posted, and renders the
 * results that are received available for consumption by the client.
 * It also keeps track of the messages that have been sent, so that the client can
 * be shut down gracefully only when all replies have been received.
 */
public class ReplyManager {

	private static final Logger LOGGER = Logger.getLogger(ReplyManager.class.getName());
	
	private Map<String, Serializable> results;
	private Set<String> outstandingReplies;
	private Set<String> toBeDiscarded;
	private QueueSession session;
	private QueueReceiver recv;
	
	public ReplyManager() {
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
		while(!results.containsKey(corrId)) {
			wait();
		}
		return getAndRemove(corrId);
	}

	public synchronized Serializable get(String corrId, long millis) 
			throws InterruptedException, TimeoutException {
		long initial = System.currentTimeMillis();
		if(millis == 0) {
			return getAndRemove(corrId);
		}
		while(!results.containsKey(corrId))
		{
			long now = System.currentTimeMillis();
			if(now < initial) {
				now = initial; // sanity check...
			}
			if((now - initial) < millis) {
				wait(millis - (now - initial));
			} else {
				throw new TimeoutException();
			}
		}
		return getAndRemove(corrId);
	}
	
	public synchronized boolean isReady(String corrId) {
		return results.containsKey(corrId);
	}
	
	public synchronized void signalJobPosted(String id) {
		outstandingReplies.add(id);
	}
	
	/**
	 * Stop the listener thread and close the JMS session.
	 */
	public void disconnect() throws JMSException {
		if(session != null) {
			session.close();
		}
		if(recv != null)
		{
			recv.setMessageListener(null);
			recv.close();
		}
	}

	/**
	 * Waits that all the jobs have been completed by the server, and that
	 * all the replies have been received by the client. This is needed so
	 * that when the server sends a reply to the temporary queue, the queue
	 * exists. Another approach (not implemented) could be to send to the server 
	 * a job cancellation request.
	 */
	public synchronized void waitForOutstandingReplies() 
			throws InterruptedException {
		while(!outstandingReplies.isEmpty()) {
			wait();
		}
	}

	// In order not to waste resource, if the future is cancelled 
	// we make sure to discard the result of the call...
	/**
	 * When the AsyncResult future is cancelled, we need to make
	 * sure that the result of the call is never saved in the HashMap, 
	 * otherwise resources are wasted.
	 */
	public synchronized void markAsCancelled(String corrId) {
		if(results.containsKey(corrId)) {
			results.remove(corrId);
		} else {
			toBeDiscarded.add(corrId);
		}
	}
	
	private synchronized void onMessage(ObjectMessage msg) {
		try {
			String corrId = msg.getJMSCorrelationID();
			outstandingReplies.remove(corrId);
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
			if (!(msg instanceof ObjectMessage)) {
				return;
			}
			ReplyManager.this.onMessage((ObjectMessage) msg);
		}
	}
	
}

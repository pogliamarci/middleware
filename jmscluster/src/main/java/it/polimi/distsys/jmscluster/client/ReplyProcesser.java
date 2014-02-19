package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

	Map<String, Serializable> results;
	
	public ReplyProcesser() {
		results = new HashMap<String, Serializable>();
	}

	public void listen(QueueConnection conn, Queue tempQueue) throws JMSException {
		QueueSession session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		QueueReceiver recv = session.createReceiver(tempQueue);
		recv.setMessageListener(new ReplyMessageListener());
	}
	
	private synchronized void onMessage(ObjectMessage msg) {
		try {
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
	
	private class ReplyMessageListener implements MessageListener {

		@Override
		public void onMessage(Message msg) {
			if (!(msg instanceof ObjectMessage))
				return;
			ReplyProcesser.this.onMessage((ObjectMessage) msg);
		}
	}
	
}

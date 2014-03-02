package it.polimi.distsys.dcd.worker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

public class CommunicationHandler {
	private QueueSession session;
	private TemporaryQueue classesQueue;
	private Map<String, byte[]> classes;

	private static final long RESOURCE_REQUEST_TIMEOUT_MILLIS = 10000;

	public CommunicationHandler(QueueConnection jobsConn) throws JMSException {
		
		classes = new HashMap<String, byte[]>();
		
		session = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		QueueSession locSession = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		classesQueue = locSession.createTemporaryQueue();
		QueueReceiver classesRecv = locSession.createReceiver(classesQueue);
		classesRecv.setMessageListener(new ClassesListener());
	}

	public synchronized byte[] lookupClass(String className, Destination ReplyTo)
			throws ClassNotFoundException {
		TextMessage reply;
		String msgId;
		try {
			reply = session.createTextMessage();
			Queue tempQueue = (Queue) ReplyTo;

			reply.setJMSReplyTo(classesQueue);
			reply.setText(className);

			MessageProducer prod = session.createProducer(tempQueue);
			prod.send(reply);
			msgId = reply.getJMSMessageID();

			return getAndRemove(msgId);
		} catch (JMSException e) {
			throw new ClassNotFoundException("JMS error " + e.getMessage());
		}
	}

	public synchronized void addClass(ObjectMessage msg) {
		try {
			classes.put(msg.getJMSCorrelationID(), (byte[]) msg.getObject());
			notifyAll();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public synchronized void sendResult(Serializable ret, String msgId, Destination ReplyTo) throws JMSException {
		ObjectMessage reply = session.createObjectMessage();
		reply.setJMSCorrelationID(msgId);
		Queue tempQueue = (Queue) ReplyTo;
		reply.setObject(ret);
		MessageProducer prod = session.createProducer(tempQueue);
		prod.send(reply);
	}
	
	private synchronized byte[] getAndRemove(String msgId) throws ClassNotFoundException {
		long initial = System.currentTimeMillis();
		while (!classes.containsKey(msgId)) {
			long now = System.currentTimeMillis();
			if (now < initial) {
				now = initial; // sanity check...
			}
			if ((now - initial) < RESOURCE_REQUEST_TIMEOUT_MILLIS) {
				try {
					wait(RESOURCE_REQUEST_TIMEOUT_MILLIS - (now - initial));
				} catch (InterruptedException e) {
					throw new ClassNotFoundException(e.getMessage());
				}
			} else {
				throw new ClassNotFoundException("Timeout elapsed requesting class from client");
			}
		}

		byte[] ret = classes.get(msgId);
		classes.remove(msgId);
		return ret;
	}

	private class ClassesListener implements MessageListener {
		@Override
		public void onMessage(Message msg) {

			if (!(msg instanceof ObjectMessage))
				return;

			addClass((ObjectMessage) msg);
		}
	}
}

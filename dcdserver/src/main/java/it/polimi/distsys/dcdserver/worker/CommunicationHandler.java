package it.polimi.distsys.dcdserver.worker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	private Map<String, Serializable> classes;
	private Set<String> requests;
	
	public CommunicationHandler(QueueConnection jobsConn) throws JMSException {
		classes = new HashMap<String, Serializable>();
		requests = new HashSet<String>();
		
		session = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		
		QueueSession locSession = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		classesQueue = locSession.createTemporaryQueue();
		QueueReceiver classesRecv = locSession.createReceiver(classesQueue);
		
		classesRecv.setMessageListener(new ClassesListener());
	}
	
	public synchronized void lookupClass(String className, String msgId, Destination ReplyTo) {
		TextMessage reply;
		try {
			reply = session.createTextMessage();
			reply.setJMSCorrelationID(msgId);
			Queue tempQueue = (Queue) ReplyTo;
			
			reply.setJMSReplyTo(classesQueue);
			reply.setText(className);

			MessageProducer prod = session.createProducer(tempQueue);
			prod.send(reply);

		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		requests.add(msgId);
	}
	
	public synchronized void sendResult(Serializable ret, String msgId, Destination ReplyTo)
			throws JMSException {
		
		ObjectMessage reply = session.createObjectMessage();
		reply.setJMSCorrelationID(msgId);
		Queue tempQueue = (Queue) ReplyTo;
		reply.setObject(ret);
		MessageProducer prod = session.createProducer(tempQueue);
		prod.send(reply);
	}

	private class ClassesListener implements MessageListener {
		
		public ClassesListener() {
		}
		
		@Override
		public void onMessage(Message msg) {
			
			if(!(msg instanceof ObjectMessage))
				return;
			
			ObjectMessage objMsg = (ObjectMessage) msg;
			
			synchronized(CommunicationHandler.this) {
				try {
					classes.put(objMsg.getJMSMessageID(), objMsg.getObject());
					System.out.println(objMsg.getObject().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			
		}
	}
}

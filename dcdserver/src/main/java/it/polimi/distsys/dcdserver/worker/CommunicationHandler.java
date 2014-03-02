package it.polimi.distsys.dcdserver.worker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

public class CommunicationHandler {
	private QueueSession locSession;
	private Map<String, Serializable> classes;
	private Set<String> requests;
	
	public CommunicationHandler(QueueSession locSession) {
		this.locSession = locSession;
		classes = new HashMap<String, Serializable>();
	}
	
	public synchronized void lookupClass(String className, String msgId, Destination ReplyTo) {
		TextMessage reply;
		try {
			reply = locSession.createTextMessage();
			reply.setJMSCorrelationID(msgId);
			Queue tempQueue = (Queue) ReplyTo;
		
			reply.setText(className);
		
			MessageProducer prod = locSession.createProducer(tempQueue);
			prod.send(reply);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
		requests.add(msgId);
	}
	
	public synchronized void sendResult(Serializable ret, String msgId, Destination ReplyTo)
			throws JMSException {
		
		ObjectMessage reply = locSession.createObjectMessage();
		reply.setJMSCorrelationID(msgId);
		Queue tempQueue = (Queue) ReplyTo;
		reply.setObject(ret);
		MessageProducer prod = locSession.createProducer(tempQueue);
		prod.send(reply);
	}
}

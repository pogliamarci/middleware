package it.polimi.distsys.dcdserver.worker;

import it.polimi.distsys.dcdserver.jobs.Job;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	public synchronized void sendResult(Job job, String msgId, Destination ReplyTo)
			throws JMSException {
		
		ObjectMessage reply = locSession.createObjectMessage();
		reply.setJMSCorrelationID(msgId);
		Queue tempQueue = (Queue) ReplyTo;
		try {
			//Serializable ret = job.run();
			//reply.setObject(ret);
		} catch(Exception e) {
			reply.setObject(new ExecutionException(e));
		}
		MessageProducer prod = locSession.createProducer(tempQueue);
		prod.send(reply);
	}
}

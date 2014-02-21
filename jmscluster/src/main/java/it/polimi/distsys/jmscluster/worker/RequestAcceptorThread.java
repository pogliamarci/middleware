package it.polimi.distsys.jmscluster.worker;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.polimi.distsys.jmscluster.jobs.Job;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

public class RequestAcceptorThread extends Thread {
	
	static final long RECV_TIMEOUT = 1000;
	
	private QueueConnection jobsConn;
	private Queue jobsQueue;
	private CoordinationManager manager;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public RequestAcceptorThread(QueueConnection jqc, Queue q, CoordinationManager manager) {
		jobsConn = jqc;
		jobsQueue = q;
		this.manager = manager;
	}

	@Override
	public void run() {
		try {
			QueueSession qs = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueReceiver jobsRecv = qs.createReceiver(jobsQueue);
			
			while(true)
			{
				while(!manager.okToAccept()) {
					try {
						manager.wait();
					} catch(InterruptedException ie) {
						ie.printStackTrace();
						break;
					}
				}
				Message newJob = jobsRecv.receive(RECV_TIMEOUT);
				
				if(newJob != null)
					processJobMessage(newJob);

			}
		} catch(JMSException e) {
			System.err.println("Error: trouble connecting with JMS...");
		}
		
	}

	private void processJobMessage(Message receive) {
		if(receive instanceof ObjectMessage)
		{
			final ObjectMessage msg = (ObjectMessage) receive;
			manager.addJob();
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Job job = (Job) msg.getObject();
						Serializable ret = job.run();
						QueueSession locSession = 
								jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						ObjectMessage reply = locSession.createObjectMessage();
						reply.setJMSCorrelationID(msg.getJMSMessageID());
						reply.setObject(ret);
						Queue tempQueue = (Queue) msg.getJMSReplyTo();
						MessageProducer prod = locSession.createProducer(tempQueue);
						prod.send(reply);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
		}	
	}

}

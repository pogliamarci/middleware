package it.polimi.distsys.jmscluster.client;

import it.polimi.distsys.jmscluster.jobs.Job;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GridClient {

	private boolean connected;
	private QueueSession session;
	private Queue jobsQueue;
	private Queue tempQueue;
	private QueueConnection conn;
	private MessageProducer jobQueuePublisher;
	private ReplyListener listener;
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public GridClient() {
		connected = false;
	}
	
	public void connect() throws ConnectionException {
		if(connected)
			return;

		listener = new ReplyListener();

		try {
			InitialContext ictx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			jobsQueue = (Queue) ictx.lookup("jobsQueue");
			ictx.close();

			
			conn = qcf.createQueueConnection();
			try {
				conn.start();
			} catch (JMSException e) {
				throw new ConnectionException("can't start connection");
			}
			
			
			session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

			QueueSession session2 = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			tempQueue = session2.createTemporaryQueue();
			QueueReceiver recv = session2.createReceiver(tempQueue);
			recv.setMessageListener(listener);
			
			jobQueuePublisher = session.createSender(jobsQueue);

		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
		
		
		connected = true;
	}
	
	public Serializable submitJob(Job j) throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");
		
		try {
			return submitJobAsync(j).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new JobSubmissionFailedException(e);
		}
	}
	
	public Future<Serializable> submitJobAsync(Job j) throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");
		
		try {
			ObjectMessage msg = session.createObjectMessage();
			msg.setObject(j);
			msg.setJMSReplyTo(tempQueue);
			jobQueuePublisher.send(msg);
			final String corrId = msg.getJMSMessageID();
			
			return pool.submit(new Callable<Serializable>() {
				@Override
				public Serializable call() {
					synchronized(listener) {
						return listener.get(corrId);
					}
				}
			});
		} catch (JMSException e) {
			throw new JobSubmissionFailedException(e);
		}
	}
	
	public void disconnect() throws ConnectionException {
		connected = false;
		try {
			conn.stop();
		} catch (JMSException e) {
			throw new ConnectionException("can't stop connection");
		}
	}
}

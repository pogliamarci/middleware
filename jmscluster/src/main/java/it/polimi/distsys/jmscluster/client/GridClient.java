package it.polimi.distsys.jmscluster.client;

import it.polimi.distsys.jmscluster.jobs.Job;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class GridClient {

	private boolean connected;
	private QueueConnection conn;
	private QueueSession session;
	private Queue jobsQueue;
	private Queue tempQueue;
	private MessageProducer jobQueuePublisher;
	private ReplyProcesser listener;
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public GridClient() {
		connected = false;
	}
	
	public void connect() throws ConnectionException {
		if(connected)
			return;
		
		QueueConnectionFactory qcf;

		try {
			InitialContext ictx = new InitialContext();
			qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			jobsQueue = (Queue) ictx.lookup("jobsQueue");
			ictx.close();
		} catch (NamingException e) {
				throw new ConnectionException("can't look up items (naming error)");
		}

		try {
			conn = qcf.createQueueConnection();
			session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			tempQueue = session.createTemporaryQueue();
			listener = new ReplyProcesser();
			listener.listen(conn, tempQueue);
			jobQueuePublisher = session.createSender(jobsQueue);
			conn.start();
		}  catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
		connected = true;
	}
	
	public Serializable submitJob(Job j) throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");	
		String cid = postJob(j);
		listener.jobPosted(cid);
		return listener.get(postJob(j));
	}
	
	public Future<Serializable> submitJobAsync(Job j) throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");
		
		//TODO for simplicity, here we call postJob from within this thread.
		//A better approach would be waking up a poster thread that does 
		//the operation asynchronously.
		//Can't just place postJob() inside the callable because JMS wants that every
		//operation belonging to the same session is performed by the same thread.
		//Also, the use of Callable is not very scalable, because it spawns a thread
		//just to check whether a result is finished. Maybe it is better to do something
		//finer, such as building an object similar to the Futures but that allows a single
		//thread to wait for everyone (and then waking up only the "right" future). Maybe this
		//is already implemented in the JMS APIs, maybe not. Need to check!
		final String corrId = postJob(j);
		listener.jobPosted(corrId);
		return pool.submit(new Callable<Serializable>() {
				@Override
				public Serializable call() {
					synchronized(listener) {
						System.out.println("chiamato");
						Serializable ret = listener.get(corrId);
						System.out.println("ritornato");
						return ret;
					}
				}
			});
		
	}
	
	public void disconnect() throws ConnectionException {
		connected = false;
		listener.waitForOutstandingReplies();
		try {
			listener.disconnect();
			conn.stop();
		} catch (JMSException e) {
			throw new ConnectionException("can't stop connection");
		}
	}
	
	private String postJob(Job j) throws JobSubmissionFailedException {
		try {
			ObjectMessage msg = session.createObjectMessage();
			msg.setObject(j);
			msg.setJMSReplyTo(tempQueue);
			jobQueuePublisher.send(msg);
			return msg.getJMSMessageID();
		} catch (JMSException e) {
			throw new JobSubmissionFailedException(e);
		}	
	}

	public void submitJobNoReply(Job j) throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");	
		postJob(j);
	}
}

/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcd.client;

import it.polimi.distsys.dcd.jobs.Job;
import it.polimi.distsys.dcd.utils.ConnectionException;
import it.polimi.distsys.dcd.utils.JobSubmissionFailedException;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
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

/**
 * Client API for the remote job execution.
 * 
 * This class is not thread-safe, and the methods of an object belonging to this class
 * are intended to be called always by the same thread (in the JMS model there can be 
 * a single thread associated to a session: this class holds a single JMS session and
 * the messages are posted by the calling thread itself).
 * 
 * If you need to submit jobs from multiple threads, create multiple ClusterClient
 * classes. The Futures returned holding the asynchronous results that are returned when
 * a job is submitted are thread safe. 
 *
 */
public class ClusterClient {

	private boolean connected;
	private QueueConnection conn;
	private QueueSession session;
	private Queue tempQueue;
	private MessageProducer jobQueuePublisher;
	private ReplyManager listener;
	private InitialContext ictx;
	private ReplyDispatcher dispatcher;
	
	public ClusterClient(InitialContext ictx) {
		connected = false;
		this.ictx = ictx;
	}
	
	public void connect() throws ConnectionException {
		if(connected) {
			return;
		}
		QueueConnectionFactory qcf;
		Queue jobsQueue;
		try {
			qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			jobsQueue = (Queue) ictx.lookup("jobsQueue");
			ictx.close();
		} catch (NamingException e) {
				throw new ConnectionException("can't look up items (naming error)", e);
		}
		try {
			conn = qcf.createQueueConnection();
			session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			tempQueue = session.createTemporaryQueue();
			listener = new ReplyManager();
			dispatcher = new ReplyDispatcher(listener, new CodeOnDemandClient(conn));
			dispatcher.listen(conn, tempQueue);
			jobQueuePublisher = session.createSender(jobsQueue);
			conn.start();
		}  catch (JMSException e) {
			throw new ConnectionException("can't create connection", e);
		}
		connected = true;
	}
	
	public Serializable submitJob(Job j) 
			throws JobSubmissionFailedException, InterruptedException, ExecutionException {
		if(!connected) {
			throw new JobSubmissionFailedException("Client is not connected");
		}
		String cid = postJob(j);
		listener.signalJobPosted(cid);
		Serializable obj = listener.get(cid);
		if(obj instanceof Throwable) {
			throw new ExecutionException((Throwable) obj);
		} else return obj;
	}
	
	public Future<Serializable> submitJobAsync(Job j) 
			throws JobSubmissionFailedException {
		if(!connected) {
			throw new JobSubmissionFailedException("Client is not connected");
		}
		String corrId = postJob(j);
		return new AsyncResult(corrId, listener);
	}
	
	public void disconnect() throws ConnectionException {
		connected = false;
		try {
			listener.waitForOutstandingReplies();
		} catch(InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
		try {
			dispatcher.disconnect();
			conn.stop();
		} catch (JMSException e) {
			throw new ConnectionException("can't stop connection", e);
		}
	}
	
	private String postJob(Job j) throws JobSubmissionFailedException {
		try {
			ObjectMessage msg = session.createObjectMessage();
			msg.setObject(j);
			msg.setJMSReplyTo(tempQueue);
			/* This thing is HORRIBLE, but useful because I know the message ID only after the send()... */
			synchronized(listener) {
				jobQueuePublisher.send(msg);
				listener.signalJobPosted(msg.getJMSMessageID());
			}
			return msg.getJMSMessageID();
		} catch (JMSException e) {
			throw new JobSubmissionFailedException("can't post job", e);
		}	
	}

}

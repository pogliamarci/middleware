package it.polimi.distsys.jmscluster.client;

import it.polimi.distsys.jmscluster.jobs.Job;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

import java.io.Serializable;
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

//The methods of an object of this class are intended to be called always by the
//same thread, because in the JMS model there could be only a single thread
//that uses one session, and this class holds a JMS session.
//A more general approach would be for this class to spawn a thread that 
//holds the session, listening to job post requests. However, for simplicity,
//we limit ourselves to ask the caller to take care about this.
public class ClusterClient {

	private boolean connected;
	private QueueConnection conn;
	private QueueSession session;
	private Queue jobsQueue;
	private Queue tempQueue;
	private MessageProducer jobQueuePublisher;
	private ReplyProcesser listener;
	
	public ClusterClient() {
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
	
	public Serializable submitJob(Job j) 
			throws JobSubmissionFailedException, InterruptedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");	
		String cid = postJob(j);
		listener.jobPosted(cid);
		return listener.get(cid);
	}
	
	public Future<Serializable> submitJobAsync(Job j) 
			throws JobSubmissionFailedException {
		if(!connected)
			throw new JobSubmissionFailedException("Client is not connected");

		String corrId = postJob(j);
		listener.jobPosted(corrId);
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

}
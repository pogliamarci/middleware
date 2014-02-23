package it.polimi.distsys.jmscluster.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.polimi.distsys.jmscluster.jobs.Job;

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

/**
 * Implements a thread listening for jobs in the jobsQueue and executes them using a thread pool.
 * It is enabled or disabled toggling the canAccept method. When the canAccept method
 * is toggled, the action is guaranteed to take place at most after 500ms (RECV_TIMEOUT ms).
 * 
 * The thread signals to any registered ServerStatusListener when it starts to executes a
 * job or when a job is completed.
 *
 */
public class JobsListener extends Thread implements ServerStatusListener, MessageListener {
	
	static final long RECV_TIMEOUT = 500;
	
	private QueueConnection jobsConn;
	private Queue jobsQueue;
	private boolean isOk;
	
	private QueueReceiver jobsRecv;
	
	private List<JobsStartingListener> lsts;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public JobsListener(QueueConnection jqc, Queue q) {
		jobsConn = jqc;
		jobsQueue = q;
		isOk = false;
		lsts = new ArrayList<JobsStartingListener>();
	}
	
	public void addListener(JobsStartingListener lst) {
		lsts.add(lst);
	}
	
	@Override
	public synchronized void canAccept(boolean status) {
		this.isOk = status;
		notifyAll();
	}
	
	@Override
	public void run() {
		try {
			QueueSession qs = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			jobsRecv = qs.createReceiver(jobsQueue);
			jobsConn.start();
			while(!interrupted())
			{
				waitForAcceptanceCondition();
				Message newJob = jobsRecv.receive(RECV_TIMEOUT);
				
				if(newJob != null)
					onMessage(newJob);
			}
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error with JMS setup: " + e.getMessage());
		}
		
	}

	@Override
	public void onMessage(Message receive) {
		if(receive instanceof ObjectMessage)
		{
			ObjectMessage msg = (ObjectMessage) receive;
			signalJobStart();
			pool.execute(new JobExecutor(msg, jobsConn));
		}
	}
	
	private synchronized void waitForAcceptanceCondition()
	{
		while(!isOk)
		{
			try {
				wait();
			} catch(InterruptedException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
						"JobsListener thread interrupted while waiting!");
			}
		}
	}
	
	private void signalJobStart() {
		for(JobsStartingListener lst : lsts) {
			lst.signalJobStart();
		}
	}
	
	private void signalJobEnd() {
		for(JobsStartingListener lst : lsts) {
			lst.signalJobEnd();
		}
	}
	
	private class JobExecutor implements Runnable {
		private ObjectMessage msg;
		
		JobExecutor(ObjectMessage msg, QueueConnection conn) {
			this.msg = msg;
		}
		
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
				Logger l = Logger.getLogger(this.getClass().getName());
				l.log(Level.WARNING, "Error sending reply: " + e.getMessage());
			} finally {
				signalJobEnd();
			}
		}
	}

}

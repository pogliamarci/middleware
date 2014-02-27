package it.polimi.distsys.jmscluster.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
	
	static final long RECV_TIMEOUT = 2500;
	
	private QueueConnection jobsConn;
	private Queue jobsQueue;
	private boolean isOk;
	private boolean pleaseStop = false;
	private List<JobsSignalListener> lsts;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public JobsListener(QueueConnection jqc, Queue q) {
		jobsConn = jqc;
		jobsQueue = q;
		isOk = false;
		lsts = new ArrayList<JobsSignalListener>();
	}
	
	public void addListener(JobsSignalListener lst) {
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
			QueueReceiver jobsRecv = qs.createReceiver(jobsQueue);
			jobsConn.start();
			while(!shouldStop()) {
				try {
					waitForAcceptanceCondition();
				} catch(InterruptedException e) {
					break;
				}
				Message newJob = jobsRecv.receive(RECV_TIMEOUT);
				
				if(newJob != null) {
					onMessage(newJob);
				}
			}
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error with JMS setup: " + e.getMessage());
		}
	}
	
	private boolean shouldStop() {
		return interrupted() || pleaseStop;
	}

	@Override
	public void onMessage(Message receive) {
		if(receive instanceof ObjectMessage) {
			ObjectMessage msg = (ObjectMessage) receive;
			signalJobStart();
			pool.execute(new JobExecutor(msg));
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		pleaseStop = true;
	}
	
	private synchronized void waitForAcceptanceCondition() 
			throws InterruptedException {
		while(!isOk) {
			wait();
		}
	}
	
	private void signalJobStart() {
		for(JobsSignalListener lst : lsts) {
			lst.signalJobStart();
		}
	}
	
	private void signalJobEnd() {
		for(JobsSignalListener lst : lsts) {
			lst.signalJobEnd();
		}
	}
	
	private class JobExecutor implements Runnable {
		private ObjectMessage msg;
		
		JobExecutor(ObjectMessage msg) {
			this.msg = msg;
		}
		
		@Override
		public void run() {
			try {
				QueueSession locSession = 
						jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				ObjectMessage reply = locSession.createObjectMessage();
				reply.setJMSCorrelationID(msg.getJMSMessageID());
				Queue tempQueue = (Queue) msg.getJMSReplyTo();
				try {
					Job job = (Job) msg.getObject();
					Serializable ret = job.run();
					reply.setObject(ret);
				} catch(Exception e) {
					reply.setObject(new ExecutionException(e));
				}
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

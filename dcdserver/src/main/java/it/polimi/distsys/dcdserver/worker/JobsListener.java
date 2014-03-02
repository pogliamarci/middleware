/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcdserver.worker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.polimi.distsys.dcdserver.jobs.Job;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
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
	private CommunicationHandler handler;
	private boolean isOk;
	private boolean pleaseStop = false;
	private List<JobsSignalListener> lsts;
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public JobsListener(QueueConnectionFactory qcf, Queue q) throws JMSException {
		jobsConn = qcf.createQueueConnection();
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
			
			try {
				handler = new CommunicationHandler(jobsConn);
			} catch (JMSException e) {
				Logger l = Logger.getLogger(this.getClass().getName());
				l.log(Level.WARNING, "Error sending reply: " + e.getMessage());
			}
			
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
			pool.shutdown();
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
			pool.execute(new JobExecutor(handler, msg));
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		pleaseStop = true;
	}
	
	public void closeConnection() throws JMSException {
		jobsConn.close();
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
		private CommunicationHandler handler;
		
		JobExecutor(CommunicationHandler handler, ObjectMessage msg) {
			this.msg = msg;
			this.handler = handler;
			setContextClassLoader(new CustomClassLoader(handler, msg));
		}
		
		@Override
		public void run() {
			try {
				Serializable ret;
				try {
					Job job = (Job) msg.getObject();
					ret = null; //= job.run();
				} catch(Exception e) {
					ret = new ExecutionException(e);
				}
				handler.sendResult(ret, msg.getJMSMessageID(), msg.getJMSReplyTo());
			} catch (JMSException e) {
				Logger l = Logger.getLogger(this.getClass().getName());
				l.log(Level.WARNING, "Error sending reply: " + e.getMessage());
			} finally {
				signalJobEnd();
			}
		}
	}

}

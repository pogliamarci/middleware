package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.client.ConnectionException;
import it.polimi.distsys.jmscluster.jobs.Job;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import javax.jms.MessageProducer;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Server implements MessageListener {
	
	private ExecutorService pool = Executors.newCachedThreadPool();
	private QueueSession session;
	private QueueConnection conn;

	@Override
	public void onMessage(Message msg) {
		if (msg instanceof ObjectMessage) {
			final ObjectMessage om = (ObjectMessage) msg;
			pool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Job job = (Job) om.getObject();
						Serializable ret = job.run();
						QueueSession locSession = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
						ObjectMessage reply = locSession.createObjectMessage();
						reply.setJMSCorrelationID(om.getJMSMessageID());
						reply.setObject(ret);
						Queue tempQueue = (Queue) om.getJMSReplyTo();
						MessageProducer prod = locSession.createProducer(tempQueue);
						prod.send(reply);
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public void go() throws ConnectionException {
		try {
			InitialContext ictx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			Queue jobsQueue = (Queue) ictx.lookup("jobsQueue");
			ictx.close();
			conn = qcf.createQueueConnection();
			session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
			conn.start();
			
			QueueReceiver recv = session.createReceiver(jobsQueue);
			recv.setMessageListener(this);
		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
		
	}
	
	public void stop() throws ConnectionException {
		try {
			conn.close();
		} catch (JMSException e) {
			throw new ConnectionException("can't close connection");
		}
	}
	
	public static void main(String[] args) {
		Server svr = new Server();
		try {
			svr.go();
			System.in.read(); //TODO orribile
			System.out.println("waiting");
			svr.stop();
			System.out.println("stopped");
		} catch (ConnectionException | IOException e) {
			e.printStackTrace();
		}
	}
	
}

package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.jobs.Job;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;

public class JobsListener implements MessageListener {

	private ExecutorService pool = Executors.newCachedThreadPool();
	private QueueConnection conn;
	
	public JobsListener(QueueConnection conn) {
		this.conn = conn;
	}
	
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
}

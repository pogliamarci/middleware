package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.client.ConnectionException;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Server {

	private int serverId;
	private QueueSession queueSession;
	private QueueConnection queueConn;
	private TopicSession topicSession;
	private TopicConnection topicConn;
	
	public Server(int id) {
		serverId = id;
	}
		
	
	public void go() throws ConnectionException {
		try {
			InitialContext ictx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			Queue jobsQueue = (Queue) ictx.lookup("jobsQueue");
			ictx.close();
			queueConn = qcf.createQueueConnection();
			queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
			queueConn.start();
			
			QueueReceiver recv = queueSession.createReceiver(jobsQueue);
			recv.setMessageListener(new JobsListener(queueConn));
			
		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
	}
	
	public void subscribe() throws ConnectionException {
		try {
			InitialContext ictx = new InitialContext();
			TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
			Topic coordinationTopic = (Topic) ictx.lookup("coordinationTopic");
			ictx.close();
			topicConn = tcf.createTopicConnection();
			topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			TopicSubscriber subs = topicSession.createSubscriber(coordinationTopic);
			subs.setMessageListener(new CoordinationListener());
		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
	}
	
	public static void main(String[] args) {
		//TODO usage
		
		final Server svr = new Server(Integer.parseInt(args[1]));
		try {
			svr.go();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
	
}

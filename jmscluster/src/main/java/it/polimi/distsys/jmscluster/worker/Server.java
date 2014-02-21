package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.client.ConnectionException;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
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
	private QueueConnection queueConn;
	private TopicSession topicSession;
	private TopicConnection topicConn;
	
	private JobsTracker tracker;
	
	public Server(int id) {
		serverId = id;
		tracker = new JobsTracker();
	}
		
	
	public void go() throws ConnectionException {
		try {
			InitialContext ictx = new InitialContext();
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			Queue jobsQueue = (Queue) ictx.lookup("jobsQueue");
			TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
			Topic coordinationTopic = (Topic) ictx.lookup("coordinationTopic");
			ictx.close();

			queueConn = qcf.createQueueConnection();
			
			topicConn = tcf.createTopicConnection();
			topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			
			CoordinationManager manager = 
					new CoordinationManager(tracker, topicSession, coordinationTopic, serverId);
			
			TopicSubscriber subs = topicSession.createSubscriber(coordinationTopic);
			subs.setMessageListener(manager);
			
			RequestAcceptorThread acceptor = new RequestAcceptorThread(queueConn, jobsQueue, manager);
			
			acceptor.run();
			
			topicConn.start();
			queueConn.start();
		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage: [id]");
			return;
		}
		
		final Server svr = new Server(Integer.parseInt(args[1]));
		try {
			svr.go();
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
	}
	
}

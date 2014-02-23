package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.utils.ConnectionException;

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
	
	public Server(int id) {
		serverId = id;
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
			
			JobsTracker tracker = new JobsTracker();
			CoordinationManager manager = 
					new CoordinationManager(topicConn, coordinationTopic, serverId, tracker);
			
			topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			TopicSubscriber subs = topicSession.createSubscriber(coordinationTopic);
			topicConn.start();
			subs.setMessageListener(manager);
			
			manager.sendJoin(); // join after the listener is subscribed...
			
			JobsListener acceptor = new JobsListener(queueConn, jobsQueue);
			
			acceptor.addListener(manager);
			tracker.addObserver(acceptor);
			
			acceptor.run();

		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: server [id]");
			System.exit(1);
		}
		
		Server svr = new Server(Integer.parseInt(args[0]));
		try {
			svr.go();
		} catch (ConnectionException e) {
			System.err.println("Connection error: " + e.getMessage() + ".");
			System.err.println("Quitting.");
			System.exit(1);
		}
	}
	
}
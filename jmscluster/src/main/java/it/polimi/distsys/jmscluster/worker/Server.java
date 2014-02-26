package it.polimi.distsys.jmscluster.worker;

import java.util.Hashtable;

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
	private Coordinator manager;
	private JobsListener acceptor;
	
	private static String DEFAULT_JNDI_HOST = "localhost";
	private static int DEFAULT_JNDI_PORT = 16400;
	
	public Server(int id) {
		serverId = id;
	}
	
	public void go(InitialContext ictx) throws ConnectionException {
		try {
			QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
			Queue jobsQueue = (Queue) ictx.lookup("jobsQueue");
			TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
			Topic coordinationTopic = (Topic) ictx.lookup("coordinationTopic");
			ictx.close();

			queueConn = qcf.createQueueConnection();
			
			topicConn = tcf.createTopicConnection();
			
			JobsTracker tracker = new JobsTracker();
			manager =  new Coordinator(topicConn, coordinationTopic, serverId, tracker);
			
			topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			TopicSubscriber subs = topicSession.createSubscriber(coordinationTopic);
			topicConn.start();
			subs.setMessageListener(manager);
			
			manager.sendJoin(); // join after the listener is subscribed...
			
			acceptor = new JobsListener(queueConn, jobsQueue);
			CommandLine cmd = new CommandLine(manager, acceptor);
			
			acceptor.addListener(manager);
			tracker.addObserver(acceptor);
			
			acceptor.start();
			
			cmd.start();

		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)");
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection");
		}
	}
	
	public static void main(String[] args) throws JMSException {
		if(args.length != 1) {
			System.out.println("Usage: server [id]");
			System.exit(1);
		}
		
		String host = DEFAULT_JNDI_HOST;
		String port = Integer.toString(DEFAULT_JNDI_PORT);
		
		if(args.length == 3)
		{
			host = args[1];
			port = args[2];
		}
		
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put("java.naming.factory.host", host);
		env.put("java.naming.factory.port", port);
		try {
			InitialContext ictx = new InitialContext(env);
			Server svr = new Server(Integer.parseInt(args[0]));
			svr.go(ictx);
		} catch (NamingException e) {
			e.printStackTrace();
			System.err.println("Can't create JNDI connection to " + host + ":" + port);
		} catch (ConnectionException e) {
			System.err.println("Connection error: " + e.getMessage() + ".");
			System.err.println("Quitting.");
			System.exit(1);
		}
	}
	
}
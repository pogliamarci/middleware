/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcdserver.worker;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.polimi.distsys.dcdserver.utils.ConnectionException;
import it.polimi.distsys.dcdserver.utils.InitialContextFactory;

import javax.jms.JMSException;
import javax.jms.Queue;
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
	private Coordinator manager;
	private JobsListener listener;
	private TopicSubscriber subs;
	private TopicConnection topicConn;
	private TopicSession topicSession;
	
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

			topicConn = tcf.createTopicConnection();
			
			JobsTracker tracker = new JobsTracker();
			manager =  new Coordinator(topicConn, coordinationTopic, serverId, tracker);
			
			topicSession = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			subs = topicSession.createSubscriber(coordinationTopic);
			topicConn.start();
			subs.setMessageListener(manager);
			
			// N.B.: join should be sent *ONLY* after the listener is subscribed
			manager.sendJoin();
			listener = new JobsListener(qcf, jobsQueue);
			listener.addListener(manager);
			tracker.addObserver(listener);
			listener.start();
		} catch (NamingException e) {
			throw new ConnectionException("can't look up items (naming error)", e);
		} catch (JMSException e) {
			throw new ConnectionException("can't create connection", e);
		}
	}
	
	public void leave() {
		listener.interrupt();				// don't listen anymore...
		try {
			listener.join();				// wait the listener quits....
			manager.shutdown();				// leave and wait for pending operations...
			listener.closeConnection();		// kills the connection (the threads in the threadpool should be done now)
			topicConn.close();
		} catch (InterruptedException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Thread interrupted during shutdown: " + e.getMessage());
		} catch (JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error during shutdown: " + e.getMessage());
		}
	}
	
	public static void main(String[] args) throws JMSException {
		if(args.length != 1 && args.length != 3) {
			System.out.println("Usage: server id [host] [port]");
			System.exit(1);
		}
		
		Server svr = new Server(Integer.parseInt(args[0]));
		
		try {
			InitialContext ictx = init(args);
			svr.go(ictx);
		}  catch (ConnectionException e) {
			System.err.println("Connection error: " + e.getMessage() + ".");
			System.exit(1);
		}
		
		(new CommandLine(svr)).start();
		System.out.println("Server started. Type 'leave' to complete the running jobs and shut down the server.");
	}
	
	private static InitialContext init(String[] args) {
		try {
			if (args.length == 3) {
				String host = args[1];
				int port = Integer.parseInt(args[2]);
				return InitialContextFactory.generate(host, port);
			} 
			return InitialContextFactory.generate();
		} catch (NamingException e) {
			System.err.println("Can't create JNDI connection");
			System.exit(1);
			return null;
		}
	}
	
}
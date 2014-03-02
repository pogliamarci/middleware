/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.admin;

import it.polimi.jmsgrid.utils.InitialContextFactory;

import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Set up the required queues and topics on the Joram server.
 * For simplicity, user authentication is disabled and the Joram server
 * is set up with the default administrator username and password.
 * Of course, in a real case, we should set up a user for the queue
 * and the topic (and the client and the workers should need to authenticate).
 */
public class LocalAdmin {
	
	private static final String ADMIN_USER = "root";
	private static final String ADMIN_PASS = "root";
	private static final String USERNAME = "anonymous";
	private static final String PASSWORD = "anonymous";
	
	
	public void createJobsQueue(String host, int port, InitialContext jndiCtx)
			throws AdminException, ConnectException, NamingException {
		try {
			AdminModule.connect(host, port, ADMIN_USER, ADMIN_PASS);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host. Can't connect to " + host + ":" + port);
		}

		QueueConnectionFactory qcf = TcpConnectionFactory.create(host, port);
		User.create(USERNAME, PASSWORD);

		Queue jobsQueue = Queue.create("jobsQueue");
		jobsQueue.setFreeReading();
		jobsQueue.setFreeWriting();

		jndiCtx.bind("qcf", qcf);
		jndiCtx.bind("jobsQueue", jobsQueue);

		AdminModule.disconnect();		
	}
	
	public void createCoordinationTopic(String host, int port, InitialContext jndiCtx)
			throws AdminException, ConnectException, NamingException {
		try {
			AdminModule.connect(host, port, ADMIN_USER, ADMIN_PASS);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host. Can't connect to " + host + ":" + port);
		}
		
		TopicConnectionFactory tcf = TcpConnectionFactory.create(host, port);
		
		User.create(USERNAME, PASSWORD);

		Topic coordinationTopic = Topic.create("coordinationTopic");
		coordinationTopic.setFreeReading();
		coordinationTopic.setFreeWriting();

		jndiCtx.bind("tcf", tcf);
		jndiCtx.bind("coordinationTopic", coordinationTopic);

		AdminModule.disconnect();
	}
	
	public static void main(String[] args) {
		LocalAdmin admin = new LocalAdmin();
		
		if(args.length < 2) {
			printUsage();
		}
		
		String host = args[0];
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			printUsage();
		}
		
		try {
			InitialContext ictx;
			if(args.length == 4)
			{
				String jndiHost = args[2];
				int jndiPort = Integer.parseInt(args[3]);
				ictx = InitialContextFactory.generate(jndiHost, jndiPort);
			} else {
				ictx = InitialContextFactory.generate();
			}
			admin.createJobsQueue(host, port, ictx);
			admin.createCoordinationTopic(host, port, ictx);
			ictx.close();
		} catch (ConnectException | AdminException | NamingException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void printUsage() {
		System.out.println("Usage: java localAdmin host port [jndiHost] [jndiPort]");
		System.exit(1);
	}
}
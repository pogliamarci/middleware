package it.polimi.distsys.jmscluster.utils;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Hashtable;

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

public class LocalAdmin {
	
	private static String DEFAULT_JNDI_HOST = "localhost";
	private static int DEFAULT_JNDI_PORT = 16400;
	
	public void createJobsQueue(String host, int port, InitialContext jndiCtx)
			throws AdminException, ConnectException, NamingException {
		try {
			AdminModule.connect(host, port, "root", "root");
		} catch (UnknownHostException e) {
			System.err.println("Unknown host. Can't connect to " + host + ":" + port);
		}

		QueueConnectionFactory qcf = TcpConnectionFactory.create(host, port);
		User.create("anonymous", "anonymous");

		Queue jobsQueue = Queue.create("jobsQueue");
		jobsQueue.setFreeReading();
		jobsQueue.setFreeWriting();

		jndiCtx.bind("qcf", qcf);
		jndiCtx.bind("jobsQueue", jobsQueue);
		jndiCtx.close();

		AdminModule.disconnect();		
	}
	
	public void createCoordinationTopic(String host, int port, InitialContext jndiCtx)
			throws AdminException, ConnectException, NamingException {
		try {
			AdminModule.connect(host, port, "root", "root");
		} catch (UnknownHostException e) {
			System.err.println("Unknown host. Can't connect to " + host + ":" + port);
		}
		
		TopicConnectionFactory tcf = TcpConnectionFactory.create(host, port);
		
		User.create("anonymous", "anonymous");

		Topic coordinationTopic = Topic.create("coordinationTopic");
		coordinationTopic.setFreeReading();
		coordinationTopic.setFreeWriting();

		jndiCtx.bind("tcf", tcf);
		jndiCtx.bind("coordinationTopic", coordinationTopic);
		jndiCtx.close();

		AdminModule.disconnect();
	}
	
	public static void main(String[] args) throws Exception {
		LocalAdmin admin = new LocalAdmin();
		
		if(args.length < 2)
			printUsage();
		
		String host = args[0];
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			printUsage();
		}
		String jndiHost = DEFAULT_JNDI_HOST;
		String jndiPort = Integer.toString(DEFAULT_JNDI_PORT);
		if(args.length == 4)
		{
			jndiHost = args[2];
			jndiPort = args[3];
		}
		
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put("java.naming.factory.host", jndiHost);
		env.put("java.naming.factory.port", jndiPort);
		
		InitialContext ictx = null;
		try {
			ictx = new InitialContext(env);
		} catch (NamingException e) {
			System.err.println("Can't create JNDI connection to " + host + ":" + port);
			System.exit(1);
		}
		
		admin.createJobsQueue(host, port, ictx);
		admin.createCoordinationTopic(host, port, ictx);
	}
	
	private static void printUsage() {
		System.out.println("Usage: java localAdmin host port [jndiHost] [jndiPort]");
		System.exit(1);
	}
}
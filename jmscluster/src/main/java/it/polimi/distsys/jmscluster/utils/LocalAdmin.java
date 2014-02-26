package it.polimi.distsys.jmscluster.utils;

import java.net.ConnectException;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class LocalAdmin {
	
	public void createJobsQueue(String host, int port)
			throws AdminException, ConnectException, NamingException {
		QueueConnectionFactory qcf = TcpConnectionFactory.create(host, port);
		
		AdminModule.connect(qcf, "root", "root");
		
		User.create("anonymous", "anonymous");

		Queue jobsQueue = Queue.create("jobsQueue");
		jobsQueue.setFreeReading();
		jobsQueue.setFreeWriting();
		
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		
		jndiCtx.bind("qcf", qcf);
		jndiCtx.bind("jobsQueue", jobsQueue);
		jndiCtx.close();

		AdminModule.disconnect();		
	}
	
	@SuppressWarnings("deprecation")
	public void createCoordinationTopic(String host, int port)
			throws AdminException, ConnectException, NamingException {
		TopicConnectionFactory tcf = TcpConnectionFactory.create(host, port);
		
		AdminModule.connect(tcf, "root", "root");
		
		User.create("anonymous", "anonymous");

		Topic coordinationTopic = Topic.create("coordinationTopic");
		coordinationTopic.setFreeReading();
		coordinationTopic.setFreeWriting();
		
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();

		jndiCtx.bind("tcf", tcf);
		jndiCtx.bind("coordinationTopic", coordinationTopic);
		jndiCtx.close();

		AdminModule.disconnect();
	}
	
	public static void main(String[] args) throws Exception {
		LocalAdmin admin = new LocalAdmin();
		
		if(args.length != 2)
			printUsage();
		
		String host = args[0];
		int port = 0;
		
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			printUsage();
		}
		admin.createJobsQueue(host, port);
		admin.createCoordinationTopic(host, port);
	}
	
	private static void printUsage() {
		System.out.println("Usage: java localAdmin host port");
		System.exit(1);
	}
}
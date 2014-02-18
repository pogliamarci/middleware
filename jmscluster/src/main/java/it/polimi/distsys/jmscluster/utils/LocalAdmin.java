package it.polimi.distsys.jmscluster.utils;

import javax.jms.QueueConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class LocalAdmin {
	public static void main(String[] args) throws Exception {
		QueueConnectionFactory qcf = TcpConnectionFactory.create("localhost", 16010);
		
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
}
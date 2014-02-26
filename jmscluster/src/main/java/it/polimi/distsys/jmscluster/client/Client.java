package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.jobs.PauseJob;
import it.polimi.distsys.jmscluster.utils.ConnectionException;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public class Client {
	
	private static String DEFAULT_JNDI_HOST = "localhost";
	private static int DEFAULT_JNDI_PORT = 16400;

	public static void main(String[] args) {
		String host = DEFAULT_JNDI_HOST;
		String port = Integer.toString(DEFAULT_JNDI_PORT);
		
		if(args.length == 2) {
			host = args[0];
			port = args[1];
		}
		
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put("java.naming.factory.host", host);
		env.put("java.naming.factory.port", port);
		
		InitialContext ictx = null;
		try {
			ictx = new InitialContext(env);
		} catch (NamingException e) {
			System.err.println("Can't create JNDI connection to " + host + ":" + port);
			System.exit(1);
		}
		
		ClusterClient client = new ClusterClient(ictx);
		Serializable ret;
		
		try {
			client.connect();
			Future<Serializable> r1 = client.submitJobAsync(new HelloWorldJob("JOB 1!"));
			Future<Serializable> r2 = client.submitJobAsync(new PauseJob("JOB 2"));
			Future<Serializable> r3 = client.submitJobAsync(new PauseJob("JOB 3"));
			try {
				ret = (String) client.submitJob(new HelloWorldJob("JOB 4"));
				System.out.println(ret);
				System.out.println(r1.get());
				System.out.println(r2.get());
				System.out.println(r3.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			client.disconnect();
			System.exit(0);
		} catch (JobSubmissionFailedException | ConnectionException e) {
			e.printStackTrace();
		}
	}

}

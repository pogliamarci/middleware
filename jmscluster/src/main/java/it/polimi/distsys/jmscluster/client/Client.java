package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.jobs.PauseJob;
import it.polimi.distsys.jmscluster.utils.ConnectionException;
import it.polimi.distsys.jmscluster.utils.InitialContextFactory;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public final class Client {
	
	private Client() {
		
	}
	
	public static void main(String[] args) {

		InitialContext ictx;
		try {
			
			if (args.length == 2) {
				String host = args[0];
				int port = Integer.parseInt(args[1]);
				ictx = InitialContextFactory.generate(host, port);
			} else {
				ictx = InitialContextFactory.generate();
			}
		} catch (NamingException e) {
			System.err.println("Can't create JNDI connection");
			System.exit(1);
			return;
		}
		
		ClusterClient client = new ClusterClient(ictx);
		Serializable ret;
		
		try {
			client.connect();
			Future<Serializable> r1 = client.submitJobAsync(new HelloWorldJob("JOB 1!"));
			Future<Serializable> r2 = client.submitJobAsync(new PauseJob("JOB 2"));
			Future<Serializable> r3 = client.submitJobAsync(new PauseJob("JOB 3"));
			ret = (String) client.submitJob(new HelloWorldJob("JOB 4"));
			System.out.println(ret);
			System.out.println(r1.get());
			System.out.println(r2.get());
			System.out.println(r3.get());
			client.disconnect();
			System.exit(0);
		} catch (JobSubmissionFailedException | ConnectionException
				| InterruptedException | ExecutionException e) {
			Logger l = Logger.getLogger(Client.class.getName());
			l.log(Level.WARNING, "Error running the cline: " + e.getMessage());
			System.exit(1);
		}
	}

}

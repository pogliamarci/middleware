package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.jobs.McmJob;
import it.polimi.distsys.jmscluster.jobs.PauseJob;
import it.polimi.distsys.jmscluster.jobs.PrimeJob;
import it.polimi.distsys.jmscluster.utils.ConnectionException;
import it.polimi.distsys.jmscluster.utils.InitialContextFactory;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public final class Client {
	
	static final long JOB_WAITING_TIME = 300;

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
		
		try {
			client.connect();
			List<Future<Serializable>> results = new ArrayList<Future<Serializable>>();
			
			results.add(client.submitJobAsync(new HelloWorldJob("JOB 1")));
			results.add(client.submitJobAsync(new McmJob("JOB 2", 253620135, 540465346)));
			results.add(client.submitJobAsync(new PauseJob("JOB 3", 5000)));
			results.add(client.submitJobAsync(new PrimeJob("JOB 4", 17920157)));
			results.add(client.submitJobAsync(new PauseJob("JOB 5", 5000)));
			results.add(client.submitJobAsync(new HelloWorldJob("JOB 6")));
			
			while(!results.isEmpty()) {
				for(int i = 0; i < results.size(); i++)
					try {
						Serializable out = results.get(i).get(JOB_WAITING_TIME, TimeUnit.MILLISECONDS);
						System.out.println(out);
						results.remove(i);
					}
					catch(TimeoutException e) { }
			}
			
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

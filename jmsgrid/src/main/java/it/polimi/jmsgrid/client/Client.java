/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.polimi.jmsgrid.client.Client;
import it.polimi.jmsgrid.client.ClusterClient;
import it.polimi.jmsgrid.jobs.*;
import it.polimi.jmsgrid.utils.ConnectionException;
import it.polimi.jmsgrid.utils.InitialContextFactory;
import it.polimi.jmsgrid.utils.JobSubmissionFailedException;

/**
 * A sample client to showcase the usage of the ClusterClient API.
 */
public final class Client {
	
	static final long JOB_WAITING_TIME = 300;

	private Client() {
		
	}

	public static void main(String[] args) {
		InitialContext ictx = init(args);
		ClusterClient client = new ClusterClient(ictx);
		try {
			client.connect();
			List<Future<Serializable>> results = new ArrayList<Future<Serializable>>(Arrays.asList(
					client.submitJobAsync(new HelloWorldJob("JOB 1")),
					client.submitJobAsync(new McmJob("JOB 2", 2535, 54534)),
					client.submitJobAsync(new PauseJob("JOB 3", 5000)),
					client.submitJobAsync(new PrimeJob("JOB 4", 17920157)),
					client.submitJobAsync(new PauseJob("JOB 5", 5000)),
					client.submitJobAsync(new HelloWorldJob("JOB 6"))));

			while(!results.isEmpty()) {
				getResults(results, JOB_WAITING_TIME);
			}
			client.disconnect();
		} catch (JobSubmissionFailedException | InterruptedException |
				ConnectionException e) {
			Logger l = Logger.getLogger(Client.class.getName());
			l.log(Level.WARNING, "Error running the client: " + e.getMessage());
			System.exit(1);
		}
	}
	
	private static void getResults(List<Future<Serializable>> results, long timeout) 
			throws InterruptedException {
		Iterator<Future<Serializable>> it = results.iterator();
		while(it.hasNext()) {
			try {
				Serializable out = it.next().get(timeout, TimeUnit.MILLISECONDS);
				it.remove();
				System.out.println(out);
			} catch(ExecutionException e) {
				System.out.println("A job failed to run: " + e.getMessage());
				it.remove();
			} catch(TimeoutException e) {
				//nothing to do..
			}
		}
	}
	
	private static InitialContext init(String[] args) {
		try {
			if (args.length == 2) {
				String host = args[0];
				int port = Integer.parseInt(args[1]);
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

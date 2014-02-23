package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.jobs.PauseJob;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public class Client {

	public static void main(String[] args) {
		ClusterClient client = new ClusterClient();
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

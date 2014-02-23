package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.jobs.PauseJob;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public class Client {
	
	public static void main(String[] args) {
		GridClient client = new GridClient();
		Serializable ret;
		try {
			client.connect();
			client.submitJobAsync(new HelloWorldJob("JOB 1!"));
			client.submitJobAsync(new PauseJob("JOB 2"));
			client.submitJobAsync(new PauseJob("JOB 3"));
			ret = (String) client.submitJob(new HelloWorldJob("JOB 4"));
			System.out.println(ret);
			client.disconnect();
			System.exit(0);
		} catch (JobSubmissionFailedException | ConnectionException e) {
			e.printStackTrace();
		}
	}
	
}

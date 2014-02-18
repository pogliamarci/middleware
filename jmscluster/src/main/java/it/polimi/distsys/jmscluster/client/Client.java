package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;

import it.polimi.distsys.jmscluster.jobs.HelloWorldJob;
import it.polimi.distsys.jmscluster.utils.JobSubmissionFailedException;

public class Client {
	
	public static void main(String[] args) {
		GridClient client = new GridClient();
		Serializable ret;
		try {
			client.connect();
			ret = (String) client.submitJob(new HelloWorldJob("Hello, World!"));
			ret = (String) client.submitJob(new HelloWorldJob("Ciao, Mondo!"));
			System.out.println(ret);
			client.disconnect();
		} catch (JobSubmissionFailedException | ConnectionException e) {
			e.printStackTrace();
		}
	}
	
}

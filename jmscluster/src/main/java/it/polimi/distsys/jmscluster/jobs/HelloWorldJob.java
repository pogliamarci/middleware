package it.polimi.distsys.jmscluster.jobs;

import java.io.Serializable;

public class HelloWorldJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	
	public HelloWorldJob(String str) {
		this.str = str;
	}
	
	@Override
	public Serializable run() {
		System.out.println(str);
		return str;
	}

}

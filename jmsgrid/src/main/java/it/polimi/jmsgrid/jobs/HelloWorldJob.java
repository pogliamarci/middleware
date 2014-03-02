/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.jobs;

import java.io.Serializable;

public class HelloWorldJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	
	public HelloWorldJob(String str) {
		this.str = str;
	}
	
	@Override
	public Serializable run() {
		System.out.println(str + "begin");
		
		System.out.println(str + "end");
		return str+" says Hello World!";
	}

}

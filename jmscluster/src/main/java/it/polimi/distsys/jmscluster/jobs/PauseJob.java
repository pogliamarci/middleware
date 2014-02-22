package it.polimi.distsys.jmscluster.jobs;

import java.io.Serializable;

public class PauseJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	
	public PauseJob(String str) {
		this.str = str;
	}
	
	@Override
	public synchronized Serializable run() {
		System.out.println(str + "begin");
		try {
			wait(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(str + "end");
		return str;
	}

}

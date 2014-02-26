package it.polimi.distsys.jmscluster.jobs;

import java.io.Serializable;

public class PauseJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	private int time;
	
	public PauseJob(String str, int time) {
		this.str = str;
		this.time = time;
	}
	
	@Override
	public synchronized Serializable run() {
		System.out.println(str + "begin");
		try {
			wait(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(str + "end");
		return str+" has been completed.";
	}

}
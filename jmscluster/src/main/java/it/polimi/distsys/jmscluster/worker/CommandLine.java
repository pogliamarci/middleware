package it.polimi.distsys.jmscluster.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLine extends Thread {
	
	private Coordinator manager;
	private JobsListener acceptor;
	
	public CommandLine(Coordinator manager, JobsListener acceptor) {
		this.manager = manager;
		this.acceptor = acceptor;
	}
	
	@Override
	public void run() {
		InputStreamReader is = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(is);
		String cmd = null;
		
		do {
			try {
				cmd = br.readLine();
			} catch (IOException e) { }
		} while(!cmd.equals(new String("leave")));
		
		acceptor.interrupt();
		manager.sendLeave();
		
		System.out.println("Waiting for pending jobs completion...");
		
		try {
			acceptor.join();
			manager.emptyQueue();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Quitting.");
		
		System.exit(0);
	}
}
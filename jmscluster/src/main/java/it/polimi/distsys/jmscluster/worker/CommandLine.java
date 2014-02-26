package it.polimi.distsys.jmscluster.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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
			} catch (IOException e) {
				Logger l = Logger.getLogger(this.getClass().getName());
				l.log(Level.WARNING, "Exception reading line: " + e.getMessage());
			}
		} while(!cmd.equals("leave"));
		
		acceptor.interrupt();
		manager.shutdown();
		
		System.out.println("Waiting for pending jobs completion...");
		
		try {
			acceptor.join();
			manager.emptyQueue();
		} catch (InterruptedException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Thread interrupted during shutdown: " + e.getMessage());
		}
		
		System.exit(0);
	}
}
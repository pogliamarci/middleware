/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.jmscluster.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just wait for the "leave" command to be issued...
 */
public class CommandLine extends Thread {
	
	private Server server;
	
	public CommandLine(Server server) {
		this.server = server;
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
		} while(!cmd.trim().equals("leave"));

		System.out.println("Waiting for all the jobs to complete...");
		server.leave();
		System.out.println("Quitting.");
	}
}
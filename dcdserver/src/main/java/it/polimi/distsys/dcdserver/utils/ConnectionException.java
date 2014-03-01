/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.dcdserver.utils;

public class ConnectionException extends Exception {

	private static final long serialVersionUID = -3254998407404113012L;
	
	public ConnectionException(Exception e) {
		super(e);
	}
	
	public ConnectionException(String string) {
		super(string);
	}

	public ConnectionException(String string, Exception e) {
		super(string, e);
	}
	
}

package it.polimi.distsys.jmscluster.utils;

public class ConnectionException extends Exception {

	private static final long serialVersionUID = -3254998407404113012L;
	
	public ConnectionException(String string) {
		super(string);
	}

	public ConnectionException(String string, Exception e) {
		super(string, e);
	}
	
}

/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.utils;

public class JobSubmissionFailedException extends Exception {

	private static final long serialVersionUID = -7250727137847153588L;
	
	public JobSubmissionFailedException(Exception e) {
		super(e);
	}

	public JobSubmissionFailedException(String string) {
		super(string);
	}

	public JobSubmissionFailedException(String string, Exception e) {
		super(string, e);
	}

}

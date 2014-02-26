package it.polimi.distsys.jmscluster.utils;

public class JobSubmissionFailedException extends Exception {

	public JobSubmissionFailedException(Exception e) {
		super(e);
	}

	public JobSubmissionFailedException(String string) {
		super(string);
	}

	public JobSubmissionFailedException(String string, Exception e) {
		super(string, e);
	}

	private static final long serialVersionUID = -7250727137847153588L;

}

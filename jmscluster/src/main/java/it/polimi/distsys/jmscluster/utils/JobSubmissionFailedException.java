package it.polimi.distsys.jmscluster.utils;

public class JobSubmissionFailedException extends Exception {

	public JobSubmissionFailedException(Exception e) {
		super(e);
	}

	public JobSubmissionFailedException() {
		// TODO Auto-generated constructor stub
	}

	public JobSubmissionFailedException(String string) {
		super(string);
	}

	private static final long serialVersionUID = -7250727137847153588L;

}

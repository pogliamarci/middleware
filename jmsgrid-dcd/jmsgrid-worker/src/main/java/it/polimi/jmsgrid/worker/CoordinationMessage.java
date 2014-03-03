/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.worker;

import java.io.Serializable;

/**
 * The message using to coordinate between workers in order to share load.
 */
public class CoordinationMessage implements Serializable {

	private static final long serialVersionUID = -3397173605723850254L;

	enum Type {
		UPDATE, JOIN, LEAVE
	}
	
	private Type type;
	private int n;
	private int jobs;
	
	public CoordinationMessage(Type type) {
		this.type = type;
	}
	
	public CoordinationMessage(Type type, int n, int jobs) {
		this.type = type;
		this.n = n;
		this.jobs = jobs;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public int getJobs() {
		return jobs;
	}

	public void setJobs(int jobs) {
		this.jobs = jobs;
	}
}

package it.polimi.distsys.jmscluster.worker;

import java.io.Serializable;

public class CoordinationMessage implements Serializable {

	private static final long serialVersionUID = 5671750865047787526L;

	enum Type {
		UPDATE, JOIN, LEAVE
	}
	
	public Type type;
	public int n;
	public int jobs;
}

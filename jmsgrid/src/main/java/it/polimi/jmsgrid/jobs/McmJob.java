/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.jobs;

import java.io.Serializable;

public class McmJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	private long num1;
	private long num2;
	
	public McmJob(String str, long num1, long num2) {
		this.str = str;
		this.num1 = num1;
		this.num2 = num2;
	}
	
	@Override
	public Serializable run() {
		System.out.println(str + "begin");
		
		long mcm = 0;
		long base = num1;
		
		if(num1 != 0 && num2 != 0) {
			if(num1 < num2)
				base = num2;
			else
				base = num1;

			for(long i = base; i <= num1 * num2; i++)
				if(i % num1 == 0 && i % num2 == 0) {
					mcm = i;
					break;
				}
		}
		
		System.out.println(str + "end");
		return str+": the mcm between "+num1+" and "+num2+" is "+mcm;
	}

}

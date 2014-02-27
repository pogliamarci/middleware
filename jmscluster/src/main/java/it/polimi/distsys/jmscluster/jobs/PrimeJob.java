/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.jmscluster.jobs;

import java.io.Serializable;

public class PrimeJob implements Job {

	private static final long serialVersionUID = -5005037609999207121L;
	private String str;
	private long num;
	
	public PrimeJob(String str, long num) {
		this.str = str;
		this.num = num;
	}
	
	@Override
	public Serializable run() {
		System.out.println(str + "begin");
		
		String result = null;
		long i;
		
		for(i = 2; i <= num / 2; i++)
			if(num % i == 0) {
				result = new String("is not prime");
			}
		if(i > num/2)
			result = new String("is prime");
		
		System.out.println(str + "end");
		return str+": "+num+" "+result;
	}

}
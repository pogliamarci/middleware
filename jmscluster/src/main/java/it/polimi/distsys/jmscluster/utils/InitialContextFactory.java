/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.jmscluster.utils;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class InitialContextFactory {
	
	private static final String DEFAULTHOST = "localhost";
	private static final int DEFAULTPORT = 16400;
	
	private InitialContextFactory() {
		
	}
	
	public static InitialContext generate() throws NamingException {
		return generate(DEFAULTHOST, DEFAULTPORT);
	}
	
	public static InitialContext generate(String host, int port) throws NamingException {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
		env.put("java.naming.factory.host", host);
		env.put("java.naming.factory.port", Integer.toString(port));
		return new InitialContext(env);
	}

}

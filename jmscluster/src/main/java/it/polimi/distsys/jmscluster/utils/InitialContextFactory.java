package it.polimi.distsys.jmscluster.utils;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class InitialContextFactory {
	
	private static String DEFAULT_JNDI_HOST = "localhost";
	private static int DEFAULT_JNDI_PORT = 16400;
	
	private InitialContextFactory() {
		
	}
	
	public static InitialContext generate() throws NamingException {
		return generate(DEFAULT_JNDI_HOST, DEFAULT_JNDI_PORT);
	}
	
	public static InitialContext generate(String host, int port) throws NamingException {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
		env.put("java.naming.factory.host", host);
		env.put("java.naming.factory.port", Integer.toString(port));
		return new InitialContext(env);
	}

}

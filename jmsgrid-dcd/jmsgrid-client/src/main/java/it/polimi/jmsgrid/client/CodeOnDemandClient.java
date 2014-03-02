/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

public class CodeOnDemandClient {

	private Session session;
	
	public CodeOnDemandClient(Connection conn) throws JMSException {
		session = conn.createSession();
	}
	
	public void onMessage(TextMessage msg) throws JMSException {
		ObjectMessage reply;
		reply = session.createObjectMessage();
		reply.setJMSCorrelationID(msg.getJMSMessageID());
		
		try {
			String resourceName = msg.getText().replace('.', '/') + ".class";
			reply.setObject(loadResource(resourceName));
		} catch (IOException e) {  
			System.err.println("Unable to load class " + msg.getText()); 
		}

		Queue tempQueue = (Queue) msg.getJMSReplyTo();
		MessageProducer prod = session.createProducer(tempQueue);
		prod.send(reply);
	}
	
	private byte[] loadResource(String res) throws IOException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(res);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		
        byte[] chunk = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(chunk)) > 0) {
	            byteStream.write(chunk, 0, bytesRead);
	    }
        return byteStream.toByteArray();
	}
}

/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

public class ReplyDispatcher implements MessageListener {

	private static final Logger LOGGER = Logger.getLogger(ReplyManager.class.getName());
	
	private ReplyManager rm;
	private CodeOnDemandClient cod;
	
	private QueueSession session;
	private QueueReceiver recv;

	
	public ReplyDispatcher(ReplyManager rm, CodeOnDemandClient cod) {
		this.rm = rm;
		this.cod = cod;
	}

	@Override
	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				cod.onMessage((TextMessage) msg);
			} else if (msg instanceof ObjectMessage) {
				rm.onMessage((ObjectMessage) msg);
			}
		} catch (JMSException e) {
			LOGGER.log(Level.WARNING,
					"Error processing message: " + e.getMessage());
		}
	}
	
	public void listen(QueueConnection conn, Queue tempQueue) throws JMSException {
		disconnect();
		session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		recv = session.createReceiver(tempQueue);
		recv.setMessageListener(this);
	}
	
	/**
	 * Stop the listener thread and close the JMS session.
	 */
	public void disconnect() throws JMSException {
		if(session != null) {
			session.close();
		}
		if(recv != null)
		{
			recv.setMessageListener(null);
			recv.close();
		}
	}

}

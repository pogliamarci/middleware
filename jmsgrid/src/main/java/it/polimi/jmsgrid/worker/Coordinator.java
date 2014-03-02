/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.jmsgrid.worker;

import it.polimi.jmsgrid.worker.CoordinationMessage.Type;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

public class Coordinator implements MessageListener, JobsSignalListener {

	private JobsTracker tracker;
	private TopicConnection connection;
	private final ThreadLocal<TopicSession> session;
	private final ThreadLocal<TopicPublisher> pub;
	private int serverId;
	private boolean isLeaving;
	
	public Coordinator(TopicConnection conn, 
			Topic coord, int sid, JobsTracker tracker) throws JMSException {
		final Topic topic = coord;
		this.serverId = sid;
		this.connection = conn;
		this.tracker = tracker;
		isLeaving = false;

		session = new ThreadLocal<TopicSession>() {
            @Override protected TopicSession initialValue() {
                try {
					return connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					Logger l = Logger.getLogger(this.getClass().getName());
					l.log(Level.WARNING, "Error creating session: " + e.getMessage());
					System.exit(1);
					return null;
				}
            }
		};
        pub = new ThreadLocal<TopicPublisher>() {
        	@Override protected TopicPublisher initialValue() {
        		try {
					return session.get().createPublisher(topic);
				} catch (JMSException e) {
					Logger l = Logger.getLogger(this.getClass().getName());
					l.log(Level.WARNING, "Error creating publisher: " + e.getMessage());
					System.exit(1);
					return null;
				}
        	}
        };
	}

	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage om = (ObjectMessage) message;
			CoordinationMessage content = (CoordinationMessage) om.getObject();
			onMessage(content);
		} catch(ClassCastException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Received unknown message on the coordination topic");
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error receiving message: " + e.getMessage());
		}
	}
	
	private void onMessage(CoordinationMessage cm)
	{
		/* drop messages sent by the process itself */
		if(cm.getN() == serverId)
		{
			return;
		}
		switch(cm.getType()) {
		case UPDATE:
			tracker.update(cm.getN(), cm.getJobs());
			break;
		case JOIN:
			if(!tracker.exists(cm.getN())) {
				/* inform the existing processes of my existence... */
				sendJoin();
			}
			tracker.join(cm.getN(), cm.getJobs());
			break;
		case LEAVE:
			tracker.leave(cm.getN());
			break;
		default:
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Received coordination message with unknown type");
		}
	}

	@Override
	public void signalJobStart() {
		tracker.addJob();
		
		if(!isLeaving) {
			sendUpdate();
		}
	}

	@Override
	public void signalJobEnd() {
		tracker.removeJob();
		
		if(!isLeaving) {
			sendUpdate();
		} else {
			synchronized(this) {
				this.notifyAll();
			}
		}
	}
	
	public void shutdown() throws JMSException, InterruptedException {
		isLeaving = true;
		sendLeave();
		synchronized(this) {
			while(tracker.getJobs() != 0) {
				wait();
			}
		}
	}
	
	private void sendUpdate() {
		CoordinationMessage msg = new CoordinationMessage(Type.UPDATE, serverId, tracker.getJobs());
		send(msg);
	}
	
	public void sendJoin() {
		CoordinationMessage msg = 
				new CoordinationMessage(Type.JOIN, serverId, tracker.getJobs());
		send(msg);
	}
	
	private void sendLeave() {
		CoordinationMessage msg = 
				new CoordinationMessage(Type.LEAVE, serverId, 0);
		send(msg);
	}
	
	private void send(CoordinationMessage m) {
		try {
			pub.get().publish(session.get().createObjectMessage(m));
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error publishing message: " + e.getMessage());
		}
	}
	
}

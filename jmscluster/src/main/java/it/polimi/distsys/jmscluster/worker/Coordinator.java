package it.polimi.distsys.jmscluster.worker;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.polimi.distsys.jmscluster.worker.CoordinationMessage.Type;

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
		if(message instanceof ObjectMessage) {
			ObjectMessage om = (ObjectMessage) message;
			try {
				Object o = om.getObject();
				if(o instanceof CoordinationMessage) {
					onMessage((CoordinationMessage) o);
				} else {
					Logger l = Logger.getLogger(this.getClass().getName());
					l.log(Level.WARNING, "Received unknown message on the coordination topic");
				}
			} catch(JMSException e) {
				Logger l = Logger.getLogger(this.getClass().getName());
				l.log(Level.WARNING, "Error receiving message: " + e.getMessage());
			}
		} else {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Received unknown message on the coordination topic");
		}
	}
	
	private void onMessage(CoordinationMessage cm)
	{
		/* drop messages sent by the process itself */
		if(cm.n == serverId)
		{
			return;
		}
		switch(cm.type) {
		case UPDATE:
			tracker.update(cm.n, cm.jobs);
			break;
		case JOIN:
			if(!tracker.exists(cm.n))
			{
				sendJoin(); // inform the existing processes of my existence...
			}
			tracker.join(cm.n, cm.jobs);
			break;
		case LEAVE:
			tracker.leave(cm.n);
			break;
		default:
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Received coordination message with unknown type");
		}
	}

	@Override
	public void signalJobStart() {
		tracker.addJob();
		
		if(!isLeaving)
			sendUpdate();
	}

	@Override
	public void signalJobEnd() {
		tracker.removeJob();
		
		if(!isLeaving)
			sendUpdate();
		else
			synchronized(this) {
				this.notifyAll();
		}
	}
	
	public boolean queueIsEmpty() {
		return tracker.getJobs() == 0;
	}
	
	private void sendUpdate() {
		CoordinationMessage msg = new CoordinationMessage();
		msg.type = Type.UPDATE;
		msg.n = serverId;
		msg.jobs = tracker.getJobs();
		try {
			pub.get().publish(session.get().createObjectMessage(msg));
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error publishing message: " + e.getMessage());
		}
	}
	
	public void sendJoin() { //TODO ugly this should not be public, but for now it works :)
		CoordinationMessage msg = new CoordinationMessage();
		msg.type = Type.JOIN;
		msg.n = serverId;
		msg.jobs = tracker.getJobs();
		try {
			pub.get().publish(session.get().createObjectMessage(msg));
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error publishing message: " + e.getMessage());
		}
	}
	
	public void sendLeave() { //TODO ugly this should not be public, but for now it works :)
		isLeaving = true;
		CoordinationMessage msg = new CoordinationMessage();
		msg.type = Type.LEAVE;
		msg.n = serverId;
		msg.jobs = 0;
		try {
			pub.get().publish(session.get().createObjectMessage(msg));
		} catch(JMSException e) {
			Logger l = Logger.getLogger(this.getClass().getName());
			l.log(Level.WARNING, "Error publishing message: " + e.getMessage());
		}
	}
	
}

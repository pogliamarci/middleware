package it.polimi.distsys.jmscluster.worker;

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

public class CoordinationManager implements MessageListener {

	private JobsTracker tracker;
	private TopicConnection connection;
	private final ThreadLocal<TopicSession> session;
	private final ThreadLocal<TopicPublisher> pub;
	private int id;
	
	public CoordinationManager(JobsTracker tracker, TopicConnection conn, 
			Topic coord, int sid) throws JMSException {
		final Topic topic = coord;
		this.tracker = tracker;
		this.id = sid;
		this.connection = conn;
		session = new ThreadLocal<TopicSession>() {
            @Override protected TopicSession initialValue() {
                try {
					return connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					e.printStackTrace();
					return null;
				}
            }
		};
        pub = new ThreadLocal<TopicPublisher>() {
        	@Override protected TopicPublisher initialValue() {
        		try {
					return session.get().createPublisher(topic);
				} catch (JMSException e) {
					e.printStackTrace();
					return null;
				}
        	}
        };
		/*
		 *this.pub = session.createPublisher(coord); 
		 */
		
		sendJoin();
	}
	
	private void sendJoin() {
		CoordinationMessage msg = new CoordinationMessage();
		msg.type = Type.JOIN;
		msg.n = id;
		msg.jobs = 0;
		try {
			pub.get().publish(session.get().createObjectMessage(msg));
		} catch(JMSException e) {
			e.printStackTrace();
			System.err.println("Error publishing the coordination message.");
		}
	}
	
	public void addJob() {
		tracker.addJob();
		CoordinationMessage msg = new CoordinationMessage();
		msg.type = Type.UPDATE;
		msg.n = id;
		msg.jobs = tracker.getJobs();
		try {
			pub.get().publish(session.get().createObjectMessage(msg));
		} catch(JMSException e) {
			e.printStackTrace();
			System.err.println("Error publishing the coordination message.");
		}
	}

	public boolean okToAccept() {
		return tracker.canAccept();
	}

	@Override
	public void onMessage(Message message) {
		if(message instanceof ObjectMessage) {
			ObjectMessage om = (ObjectMessage) message;
			CoordinationMessage cm;
			try {
				Object o = om.getObject();
				if(o instanceof CoordinationMessage) {
					cm = (CoordinationMessage) o;
				} else return;
			} catch(JMSException e) {
				return;
			}
			/* drop messages sent by the process itself */
			if(cm.n == id)
				return;
			switch(cm.type) {
			case UPDATE:
				tracker.update(cm.n, cm.jobs);
				break;
			case JOIN:
				System.err.println("Worker " + cm.n + " joined");
				if(!tracker.exists(cm.n))
					sendJoin(); // inform the existing processes of my existence...
				tracker.join(cm.n);
				break;
			case LEAVE:
				System.err.println("Worker " + cm.n + " left");
				tracker.leave(cm.n);
				break;
			}
		}
		synchronized(this)
		{
			notifyAll();
		}
	}

	public synchronized void waitForAcceptanceCondition() {
			while(!okToAccept()) {
				System.err.println("Not accepting jobs...");
				try {
					wait();
				} catch(InterruptedException ie) {
					ie.printStackTrace();
					break;
				}
				System.err.println("Going back to accept jobs...");
			}
	}
	
}

package it.polimi.distsys.jmscluster.worker;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

public class CoordinationManager implements MessageListener {

	private JobsTracker tracker;
	private TopicSession session;
	private TopicPublisher pub;
	private int id;
	
	public CoordinationManager(JobsTracker tracker, TopicSession session, 
			Topic coord, int sid) throws JMSException {
		this.tracker = tracker;
		this.id = sid;
		this.session = session;
		this.pub = session.createPublisher(coord);
	}
	
	public void addJob() {
		tracker.addJob();
		CoordinationMessage msg = new CoordinationMessage();
		msg.n = id;
		msg.jobs = tracker.getJobs();
		try {
			pub.publish(session.createObjectMessage(msg));
		} catch(JMSException e) {
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
			if(om instanceof CoordinationMessage) {
				CoordinationMessage cm = (CoordinationMessage)om;
				tracker.update(cm.n, cm.jobs);
				
				if(tracker.canAccept())
					this.notifyAll();
			}
		}
	}
	
}

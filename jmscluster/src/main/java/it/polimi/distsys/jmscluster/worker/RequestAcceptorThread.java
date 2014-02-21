package it.polimi.distsys.jmscluster.worker;

import it.polimi.distsys.jmscluster.jobs.Job;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

public class RequestAcceptorThread extends Thread implements MessageListener {
	
	private QueueConnection jobsConn;
	private TopicConnection coordConn;
	private Queue jobsQueue;
	private Topic coordTopic;
	private int jobCount = 0;
	
	public RequestAcceptorThread(QueueConnection jqc, TopicConnection ctc, Queue q, Topic t) {
		coordConn = ctc;
		jobsConn = jqc;
		jobsQueue = q;
		coordTopic = t;
	}
	
	@Override
	public void onMessage(Message message) {
		// TODO callback fopr the reception of messages from the coordination queue...

	}
	
	@Override
	public void run() {
		try {
			QueueSession qs = jobsConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			TopicSession ts = coordConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			
			QueueReceiver jobsRecv = qs.createReceiver(jobsQueue);
			TopicPublisher coordPublisher = ts.createPublisher(coordTopic);
			
			while(true)
			{
				while(!okToListen()) {
					try {
						wait();
					} catch(InterruptedException ie) {
						ie.printStackTrace();
						break;
					}
				}
				processJobMessage(jobsRecv.receive()); //TODO how to interrupt the receive???
				// invia messaggi sul topic coordination...
			}
		} catch(JMSException e) {
			System.err.println("Error: trouble connecting with JMS...");
		}
		
	}

	private void processJobMessage(Message receive) {
		Job j;
		Destination replyTo;
		String correlationId;
		if(receive instanceof ObjectMessage)
		{
			ObjectMessage msg = (ObjectMessage) receive;
			try {
				j = (Job) msg.getObject();
				replyTo = msg.getJMSReplyTo();
				correlationId = msg.getJMSCorrelationID();
			} catch(JMSException e) {
				return;
			}
		}
		this.jobCount += 1;
		// send updated job count
		// dispatch job
		// set up a callback for when the job finishes to execute...	
	}

	private boolean okToListen() {
		// if my number of jobs is <= the one of the other servers...
		return false;
	}

}

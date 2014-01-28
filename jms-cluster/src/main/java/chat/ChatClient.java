package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ChatClient {

	static Context ictx;
	private MsgListener msgListener;
	private TopicSession pubSession;
	private TopicSession subSession;
	private Map<String, TopicPublisher> publishers;
	private Map<String, TopicSubscriber> subscribers;
	TopicConnectionFactory tcf;
	private TopicConnection subConn;
	private TopicConnection pubConn;

	public static void main(String args[]) {
		new ChatClient();
	}

	public ChatClient() {
		this.publishers = new HashMap<String, TopicPublisher>();
		this.subscribers = new HashMap<String, TopicSubscriber>();
		try {
			init();
		}
		catch (NamingException ne) {
			ne.printStackTrace();
			System.exit(-1);
		}
		catch (JMSException je) {
			je.printStackTrace();
			System.exit(-1);
		}
		parseInput();
	}

	private void init() throws JMSException, NamingException {
		ictx = new InitialContext();
		this.tcf = (TopicConnectionFactory) ictx.lookup("tcf");
		ictx.close();
		this.subConn = this.tcf.createTopicConnection();
		this.pubConn = this.tcf.createTopicConnection();
		this.pubSession = this.pubConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		this.subSession = this.subConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		this.msgListener = new MsgListener();
	}

	private void subscribe(String topic) throws JMSException, NamingException {
		Topic t = null;
		ictx = new InitialContext();
		t = (Topic) ictx.lookup(topic);
		ictx.close();
		TopicPublisher pub = this.pubSession.createPublisher(t);
		this.publishers.put(topic, pub);
		TopicSubscriber sub = this.subSession.createSubscriber(t);
		this.subscribers.put(topic, sub);
		// Notice: during this operation we may loose messages!
		// If we want all messages to be delivered,
		// we can use durable subscriptions, or multiple sessions
		this.subConn.stop();
		sub.setMessageListener(this.msgListener);
		this.subConn.start();
	}

	private void unsubscribe(String topic) throws JMSException {
		if (!this.subscribers.containsKey(topic)) {
			System.out.println("No subscription is set for the given topic!");
		}
		else {
			this.subscribers.get(topic).close();
			this.subscribers.remove(topic);
			this.publishers.get(topic).close();
			this.publishers.remove(topic);
		}
	}

	private void publish(String text) throws JMSException {
		Set<TopicPublisher> publishersCache;
		publishersCache = new HashSet(this.publishers.values());
		TextMessage msg = this.pubSession.createTextMessage();
		msg.setText(text);
		for (TopicPublisher pub : this.publishers.values()) {
			pub.send(msg);
		}
	}

	private void showSubscriptions() {
		System.out.print("Current subscriptions: ");
		for (String s : this.subscribers.keySet()) {
			System.out.print(s + " ");
		}
		System.out.println();
	}

	private void exit() throws JMSException {
		try {
			this.subConn.close();
			this.pubConn.close();
		}
		catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void parseInput() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String s;
		while (true) {
			try {
				System.out.println("\nWaiting new command: ");
				s = in.readLine();
				String command[] = s.split(" ");
				if (command[0].equals("sub")) {
					if (command.length >= 2) {
						subscribe(command[1]);
					}
					else {
						usage();
					}
				}
				else if (command[0].equals("unsub")) {
					if (command.length >= 2) {
						unsubscribe(command[1]);
					}
					else {
						usage();
					}
				}
				else if (command[0].equals("pub")) {
					if (command.length >= 2) {
						publish(command[1]);
					}
					else {
						usage();
					}
				}
				else if (command[0].equals("list")) {
					showSubscriptions();
				}
				else if (command[0].equals("exit")) {
					exit();
					break;
				}
				else {
					usage();
				}
			}
			catch (NamingException ne) {
				System.out.println("Wrong topic!");
			}
			catch (JMSException je) {
				je.printStackTrace();
				break;
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				break;
			}
		}
	}

	private void usage() {
		System.out.println("Usage: sub <topic> | unsub <topic> | pub <text> | list | exit");
	}
}

class MsgListener implements MessageListener {

	public MsgListener() {
	}

	@Override
	public void onMessage(Message msg) {
		TextMessage tmsg = (TextMessage) msg;
		try {
			System.out.println(" --- Msg received: " + tmsg.getText() + " ---");
		}
		catch (JMSException jE) {
			jE.printStackTrace();
		}
	}
}

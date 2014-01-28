package chat;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

public class Admin {
	public static void main(String[] args) throws Exception {
		AdminModule.connect("root", "root", 60);
		javax.jms.TopicConnectionFactory tcf = TopicTcpConnectionFactory.create("localhost", 16010);
		Topic topic1 = Topic.create("topic1");
		Topic topic2 = Topic.create("topic2");
		Topic topic3 = Topic.create("topic3");
		Topic topic4 = Topic.create("topic4");
		Topic topic5 = Topic.create("topic5");
		Topic topic6 = Topic.create("topic6");
		Topic topic7 = Topic.create("topic7");
		Topic topic8 = Topic.create("topic8");
		User.create("anonymous", "anonymous");
		topic1.setFreeReading();
		topic1.setFreeWriting();
		topic2.setFreeReading();
		topic2.setFreeWriting();
		topic3.setFreeReading();
		topic3.setFreeWriting();
		topic4.setFreeReading();
		topic4.setFreeWriting();
		topic5.setFreeReading();
		topic5.setFreeWriting();
		topic6.setFreeReading();
		topic6.setFreeWriting();
		topic7.setFreeReading();
		topic7.setFreeWriting();
		topic8.setFreeReading();
		topic8.setFreeWriting();
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("tcf", tcf);
		jndiCtx.bind("topic1", topic1);
		jndiCtx.bind("topic2", topic2);
		jndiCtx.bind("topic3", topic3);
		jndiCtx.bind("topic4", topic4);
		jndiCtx.bind("topic5", topic5);
		jndiCtx.bind("topic6", topic6);
		jndiCtx.bind("topic7", topic7);
		jndiCtx.bind("topic8", topic8);
		jndiCtx.close();
		AdminModule.disconnect();
		System.out.println("Done");
	}
}

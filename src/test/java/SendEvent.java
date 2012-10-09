import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.felix.eventadmin.bridge.jms.JmsSender;
import org.junit.Test;
import org.osgi.service.event.Event;


public class SendEvent {

	@Test
	public void sendEvent() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		JmsSender sender = new JmsSender();
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		Event event = new Event("my.topic", props );
		sender.setConnectionFactory(connectionFactory);
		sender.handleEvent(event);
	}
}

package org.apache.felix.eventadmin.bridge.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

public class JmsSupport {
	static final String FROM_REMOTE = "fromRemote";
	static final String TOPIC_PREFIX = "osgi.event.";
	
	ConnectionFactory connectionFactory = null;
	
	void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (JMSException e) {
			}
		}
	}

	void closeSession(Session session) {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
			}
		}
	}

	void closeProducer(MessageProducer prod) {
		if (prod != null) {
			try {
				prod.close();
			} catch (JMSException e) {
			}
		}
	}
	
	void closeConsumer(MessageConsumer consumer) {
		if (consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
			}
		}
	}
	
	void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		
	}
}

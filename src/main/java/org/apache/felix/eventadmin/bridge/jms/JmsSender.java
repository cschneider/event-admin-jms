/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.eventadmin.bridge.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsSender extends JmsSupport implements EventHandler {
	Logger LOG = LoggerFactory.getLogger(JmsSender.class);

	public JmsSender() {
	}

	public void handleEvent(Event event) {
		if (connectionFactory == null) {
			return;
		}
		Boolean fromRemote = (Boolean) event.getProperty(FROM_REMOTE);
		if (fromRemote != null) {
			return;
		}
		if (!shouldSend(event)) {
			LOG.info("Ignoring event" + event.toString());
			return;
		}
		Connection con = null;
		Session session = null;
		MessageProducer prod = null;
		try {
			con = connectionFactory.createConnection();
			session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			con.start();
			Message message = mapEventToMessage(event, session);
			Topic dest = mapEventTopicToJmsTopic(event, session);
			prod = session.createProducer(dest);
			LOG.info("Sending event" + event.toString());
			prod.send(message);
		} catch (JMSException e) {
			throw new RuntimeException("Error forwarding event to jms topic " + event.getTopic() +": " + e.getMessage(), e);
		} finally {
			closeProducer(prod);
			closeSession(session);
			closeConnection(con);
		}
	}

	private boolean shouldSend(Event event) {
		return !event.getTopic().startsWith("org/osgi/service/log/LogEntry");
	}

	private Topic mapEventTopicToJmsTopic(Event event, Session session)
			throws JMSException {
		String topicPart = event.getTopic().replace("/", ".");
		return session.createTopic(TOPIC_PREFIX + topicPart);
	}

	private Message mapEventToMessage(Event event, Session session) throws JMSException {
		MapMessage message = session.createMapMessage();
		for (String key : event.getPropertyNames()) {
			Object value = event.getProperty(key);
			if (canTransmit(value)) {
				message.setObjectProperty(key, value);
			}
		}
		message.setBoolean(FROM_REMOTE, true);
		return message;
	}
	
	private boolean canTransmit(Object value) {
		return (value instanceof String || value instanceof Long || value instanceof Integer);
	}



	public void close() {
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		
	}

}

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

import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsReceiver extends JmsSupport implements MessageListener {
	Logger LOG = LoggerFactory.getLogger(JmsReceiver.class);

	private Connection con;
	private Session session;
	private MessageConsumer consumer;
	private EventAdmin eventAdmin;

	public JmsReceiver() {

	}

	@Override
	public void onMessage(Message message) {
		try {
			Event event = mapMessageToEvent(message);
			LOG.info("Received event" + event.toString());
			if (eventAdmin != null) {
				eventAdmin.postEvent(event);
			}
		} catch (JMSException e) {
		}
	}

	private Event mapMessageToEvent(Message message) throws JMSException {
		Topic jmsTopic = (Topic) message.getJMSDestination();
		String topic = mapJmsTopicToEventTopic(jmsTopic);
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		@SuppressWarnings("unchecked")
		Enumeration<String> keys = message.getPropertyNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			Object value = message.getObjectProperty(key);
			props.put(key, value);
		}
		props.put(FROM_REMOTE, new Boolean(true));
		return new Event(topic, props);
	}

	private String mapJmsTopicToEventTopic(Topic jmsTopic) {
		String strippedTopic = jmsTopic.toString().substring(
				"topic://".length() + TOPIC_PREFIX.length());
		return strippedTopic.replace(".", "/");
	}

	public void close() {
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		closeConsumer(consumer);
		closeSession(session);
		closeConnection(con);
		this.connectionFactory = connectionFactory;
		try {
			connect(connectionFactory);
		} catch (JMSException e) {
			closeConsumer(consumer);
			closeSession(session);
			closeConnection(con);
		}
	}

	private void connect(ConnectionFactory connectionFactory)
			throws JMSException {
		if (connectionFactory == null) {
			return;
		}
		con = connectionFactory.createConnection();
		session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic(TOPIC_PREFIX + ">");
		consumer = session.createConsumer(topic);
		consumer.setMessageListener(this);
		con.start();
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

}

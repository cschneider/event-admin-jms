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

import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

/**
 *

 */
public class Activator implements BundleActivator {
	private ServiceTracker cfTracker;
	private JmsSender sender = null;
	private JmsReceiver receiver = null;
	private ServiceTracker eventAdminTracker;

	/**
     */
	public void start(final BundleContext context) throws Exception {
		sender = new JmsSender();
		receiver  = new JmsReceiver();
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put(EventConstants.EVENT_TOPIC, "*");
		context.registerService(EventHandler.class.getName(), sender, dict);
		
		//ServiceReference cfRef = context.getServiceReference(ConnectionFactory.class.getName());
		cfTracker = new ServiceTracker(context, ConnectionFactory.class.getName(), null) {

			@Override
			public Object addingService(ServiceReference reference) {
				ConnectionFactory cf = (ConnectionFactory) context.getService(reference);
				sender.setConnectionFactory(cf);
				receiver.setConnectionFactory(cf);
				return super.addingService(reference);
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				sender.setConnectionFactory(null);
				receiver.setConnectionFactory(null);
				super.removedService(reference, service);
			}

		};
		cfTracker.open();
		eventAdminTracker = new ServiceTracker(context, EventAdmin.class.getName(), null) {

			@Override
			public Object addingService(ServiceReference reference) {
				EventAdmin eventAdmin = (EventAdmin) context.getService(reference);
				receiver.setEventAdmin(eventAdmin);
				return super.addingService(reference);
			}

			@Override
			public void removedService(ServiceReference reference,
					Object service) {
				sender.setConnectionFactory(null);
				receiver.setConnectionFactory(null);
				super.removedService(reference, service);
			}

		};
		eventAdminTracker.open();
	}

	/**

     */
	public void stop(final BundleContext context) throws Exception {
		cfTracker.close();
		// Services are unregistered by the framework
	}
}

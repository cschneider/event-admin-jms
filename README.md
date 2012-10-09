event-admin-jms
===============

Allows OSGi events to be distributed across containers using a jms provider

Build
-----

mvn clean install


Installation
------------

Load and extract ActiveMQ 5.6.0
Start ActiveMQ with: 
> bin/activemq console

Load and extract Karaf 2.2.9
Start Karaf:
> bin/karaf

Install bundles:
> features:chooseurl activemq 5.6.0
> features:install activemq

Copy localhost-broker.xml to deploy folder. This will create a OSGi service for the ConnectionFactory
> install -s mvn:org.apache.felix/org.apache.felix.eventadmin/1.2.14
> install -s install -s mvn:org.apache.felix/org.apache.felix.eventadmin.bridge.jms/0.9.0-SNAPSHOT

> log:display 
This should show at least one event that was sent and received


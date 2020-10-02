package org.monguzzi.examples;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class BasicClient {

	public static final void main(String[] args) throws JMSException, IOException {
	String brokerUrl = args[0];
	ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(brokerUrl);
	cf.createConnection("admin", "admin");
	
	System.in.read();
	
	cf.close();
	}
}

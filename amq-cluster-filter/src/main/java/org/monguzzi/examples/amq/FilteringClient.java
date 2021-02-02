package org.monguzzi.examples.amq;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class FilteringClient {

	public static final void main(String[] args) throws JMSException, IOException, NamingException {

		Connection connection1 = null;
		Connection connection2 = null;
		InitialContext initialContext = null;
		try {

			// Step 1. Create an initial context to perform the JNDI lookup.
			initialContext = new InitialContext();

			// Step 2. Look-up the JMS queue
			Queue queue = (Queue) initialContext.lookup("queue/filteredQueue");

			// Step 3. Look-up the JMS connection factory
			ConnectionFactory cf1 = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
			// Step 4. Create a JMS connection
			connection1 = cf1.createConnection();
			
			ConnectionFactory cf2 = (ConnectionFactory) initialContext.lookup("ConnectionFactory2");
			connection2 = cf2.createConnection();

			// Step 5. Create a JMS session
			Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Step 6. Create a JMS message producer
			MessageProducer producer = session1.createProducer(queue);

			// Step 7. Create one subscription with a specific Filter for someID=1
			MessageConsumer messageConsumer1 = session1.createConsumer(queue, "someID=1", false);

			// Step 8. Create another subscription with a specific Filter for someID=2
			MessageConsumer messageConsumer2 = session1.createConsumer(queue, "someID=2", false);

			MessageConsumer messageConsumer3 = session2.createConsumer(queue, "someID=3", false);

			// Step 10. Send 30 messages, 10 with someID=1, 10 with someID=2, 10 with someID=3

			for (int i = 1; i <= 10; i++) {
				for (int someID = 1; someID <= 3; someID++) {
					// Step 10.1 Create a text message
					TextMessage message1 = session1
							.createTextMessage("This is a text message " + i + " sent for someID=" + someID);

					// Step 10.1 Set a property
					message1.setIntProperty("someID", someID);

					// Step 10.2 Send the message
					producer.send(message1);

					System.out.println("Sent message: " + message1.getText());
				}
			}

			// Step 11. Start the JMS Connection. This step will activate the subscribers to
			// receive messages.
			connection1.start();
			connection2.start();

			// Step 12. Consume the messages from MessageConsumer1, filtering out someID=2

			System.out.println("*************************************************************");
			System.out.println("MessageConsumer1 will only receive messages where someID=1:");
			for (;;) {
				TextMessage messageReceivedA = (TextMessage) messageConsumer1.receive(1000);
				if (messageReceivedA == null) {
					break;
				}

				System.out.println("messageConsumer1 received " + messageReceivedA.getText() + " someID = "
						+ messageReceivedA.getIntProperty("someID"));
			}

			// Step 13. Consume the messages from MessageConsumer2, filtering out someID=2
			System.out.println("*************************************************************");
			System.out.println("MessageConsumer2 will only receive messages where someID=2:");
			for (;;) {
				TextMessage messageReceivedB = (TextMessage) messageConsumer2.receive(1000);
				if (messageReceivedB == null) {
					break;
				}

				System.out.println("messageConsumer2 received " + messageReceivedB.getText() + " someID = "
						+ messageReceivedB.getIntProperty("someID"));
			}

			// Step 14. Consume the messages from MessageConsumer3, receiving the complete
			// set of messages
			System.out.println("*************************************************************");
			System.out.println("MessageConsumer3 will receive every message:");
			for (;;) {
				TextMessage messageReceivedC = (TextMessage) messageConsumer3.receive(1000);
				if (messageReceivedC == null) {
					break;
				}
				System.out.println("messageConsumer3 received " + messageReceivedC.getText() + " someID = "
						+ messageReceivedC.getIntProperty("someID"));
			}

			// Step 15. Close the subscribers
			messageConsumer1.close();
			messageConsumer2.close();
			messageConsumer3.close();
		} finally {
			// Step 15. Be sure to close our JMS resources!
			if (connection1 != null) {
				connection1.close();
			}
			if (connection2 != null) {
				connection2.close();
			}
		}
	}
}

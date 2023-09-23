package de.demo.amqp.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

//@Component
public class Consumer implements MessageListener{

	private static final Logger log = LoggerFactory.getLogger("Consumer");
	
	public void onMessage(Message message) {
		log.info("basic consumer got a message");
	}
}

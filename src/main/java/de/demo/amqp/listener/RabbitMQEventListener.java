package de.demo.amqp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventListener {
	
	private static final Logger log = LoggerFactory.getLogger("RabbitMQEventListener");

	@EventListener
	public void handler(ListenerContainerConsumerFailedEvent container){
		log.info("RabbitMQEventListener is listening!");

	}
}

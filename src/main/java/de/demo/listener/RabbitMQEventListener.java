package de.demo.listener;

import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQEventListener {

	@EventListener
	public void handler(ListenerContainerConsumerFailedEvent container){

	}
}

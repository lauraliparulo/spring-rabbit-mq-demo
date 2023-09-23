package de.demo.amqp.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnnotatedConsumer {
	
	private static final Logger log = LoggerFactory.getLogger("Consumer");

	@RabbitListener(queues="${rabbitmq.queue}")
	public void process(String message){
		log.info("annotaded consumer got a message");
	}
}

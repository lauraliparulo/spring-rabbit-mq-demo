package de.demo.amqp.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import de.demo.domain.Rate;


//@Component
public class RateConsumer {

	@RabbitListener(queues="${rabbitmq.rate-queue}")
	public void messageHandler(Rate rate){
		
	}
	
}

package de.demo.amqp.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

//@Component
public class SimpleProducer {

	private RabbitTemplate template;
	
	@Autowired
	public SimpleProducer(RabbitTemplate template){
		this.template = template;
	}
	

	@Transactional
	public void sendMessage(String exchange,String routingKey, String message){
		this.template.convertAndSend(exchange,routingKey, message);
	}
}

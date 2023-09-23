package de.demo.amqp.rpc;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcRequestClient {

	private RabbitTemplate template;

	@Autowired
	public RpcRequestClient(RabbitTemplate template) {
		this.template = template;
	}

	public Object sendMessage(String exchange,
      String routingKey, String message) {
      Object response =
             this.template              
                    .convertSendAndReceive(exchange, routingKey, message);              
           return response;
	}
}

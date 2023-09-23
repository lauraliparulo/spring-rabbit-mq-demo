package de.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import de.demo.amqp.producer.Producer;


@SpringBootApplication
public class AmqpDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}
	
	@Bean
	CommandLineRunner simple(@Value("${rabbitmq.exchange}")String exchange, @Value("${rabbitmq.routingkey}")String routingKey, Producer producer){
		return args -> {
			producer.sendMessage(exchange, routingKey, "HELLO, AMQP!");
			producer.sendMessage(exchange, routingKey, "HELLO, AMQP!");
			producer.sendMessage(exchange, routingKey, "HELLO, AMQP!");
			producer.sendMessage(exchange, routingKey, "HELLO, AMQP!");
			
		};
	}

}

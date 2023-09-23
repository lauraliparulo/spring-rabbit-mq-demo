package de.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import de.demo.amqp.producer.SimpleProducer;
import de.demo.amqp.rpc.RpcRequestClient;


@SpringBootApplication
public class AmqpDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}
	
	@Bean
    CommandLineRunner
    simple(@Value("${rabbitqm.exchange:}")String exchange,
           @Value("${rabbitmq.queue}")String routingKey,
           RpcRequestClient client){
                return args -> {
                    Object result = client
                             .sendMessage(exchange,
                                        routingKey,
                                     "HELLO AMQP/RPC!");
                        assert result!=null;
           };
    }

}

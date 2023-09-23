package de.demo;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import de.demo.amqp.producer.RateProducer;
import de.demo.config.AMQPProperties;
import de.demo.domain.Rate;


@SpringBootApplication
public class AmqpDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmqpDemoApplication.class, args);
	}
	
	@Bean
	CommandLineRunner process(AMQPProperties props, RateProducer producer){
		return args -> {
			producer.sendRate(props.getRateExchange(),props.getRateQueue(), new Rate("EUR",0.88857F,new Date()));
			producer.sendRate(props.getRateExchange(),props.getRateQueue(), new Rate("JPY",102.17F,new Date()));
			producer.sendRate(props.getRateExchange(),props.getRateQueue(), new Rate("MXN",19.232F,new Date()));
			producer.sendRate(props.getRateExchange(),props.getRateQueue(), new Rate("GBP",0.75705F,new Date()));
		};
	}

}

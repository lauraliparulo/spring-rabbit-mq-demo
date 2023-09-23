package de.demo.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.demo.aop.AMQPAudit;

@Configuration
@EnableConfigurationProperties(AMQPProperties.class)
@EnableRabbit
public class AMQPConfig {

	   @Value("${rabbitmq.queue}")
	    private String queueName;
	    @Value("${rabbitmq.exchange}")
	    private String exchange;
	    @Value("${rabbitmq.routingkey}")
	    private String routingkey;
	    @Value("${rabbitmq.username}")
	    private String username;
	    @Value("${rabbitmq.password}")
	    private String password;
	    @Value("${rabbitmq.host}")
	    private String host;
	    @Value("${rabbitmq.virtualhost}")
	    private String virtualHost;
	    @Value("${rabbitmq.reply.timeout}")
	    private Integer replyTimeout;
	    @Value("${rabbitmq.concurrent.consumers}")
	    private Integer concurrentConsumers;
	    @Value("${rabbitmq.max.concurrent.consumers}")
	    private Integer maxConcurrentConsumers;

	@Bean
	AMQPAudit messageAspect() {
		return new AMQPAudit();
	}

	   @Bean
	    public Queue queue() {
	        return new Queue(queueName, false);
	    }
	    @Bean
	    public DirectExchange exchange() {
	        return new DirectExchange(exchange);
	    }
	    @Bean
	    public Binding binding(Queue queue, DirectExchange exchange) {
	        return BindingBuilder.bind(queue).to(exchange).with(routingkey);
	    }

	    @Bean
	    public Jackson2JsonMessageConverter jsonMessageConverter() {
	        ObjectMapper objectMapper = new ObjectMapper();
	        return new Jackson2JsonMessageConverter(objectMapper);
	    }	    
	    
	// to customize
	    
	    @Bean
	    public ConnectionFactory connectionFactory() {
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
	        connectionFactory.setVirtualHost(virtualHost);
	        connectionFactory.setHost(host);
	        connectionFactory.setUsername(username);
	        connectionFactory.setPassword(password);
	        return connectionFactory;
	    }
//	    @Bean
//	    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//	        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//	        rabbitTemplate.setDefaultReceiveQueue(queueName);
//	        rabbitTemplate.setMessageConverter(jsonMessageConverter());
//	        rabbitTemplate.setReplyAddress(queue().getName());
//	        rabbitTemplate.setReplyTimeout(replyTimeout);
//	        rabbitTemplate.setUseDirectReplyToContainer(false);
//	        return rabbitTemplate;
//	    }

//	    @Bean
//	    public SimpleMessageListenerContainer              
//	                 container(ConnectionFactory connectionFactory,
//	                        MessageListener consumer,
//	              @Value("${rabbitmq.queue}")String queueName) {
//
//	        SimpleMessageListenerContainer container = new
//	                             SimpleMessageListenerContainer();
//	                container.setConnectionFactory(connectionFactory);
//	                container.setQueueNames(queueName);
//	                container.setMessageListener(consumer);
//	                return container;
//	    }
	    
	    @Bean
	    public AmqpAdmin amqpAdmin() {
	        return new RabbitAdmin(connectionFactory());
	    }
	
	@Bean
	MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter();
	}
	
	
	  @Bean
	    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
	        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	        factory.setConnectionFactory(connectionFactory());
	        factory.setMessageConverter(jsonMessageConverter());
	        factory.setConcurrentConsumers(concurrentConsumers);
	        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
	        factory.setErrorHandler(errorHandler());
	        return factory;
	    }
	    @Bean
	    public ErrorHandler errorHandler() {
	        return new ConditionalRejectingErrorHandler(new MyFatalExceptionStrategy());
	    }
	
	    public static class MyFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
	        private final Logger logger = LogManager.getLogger(getClass());
	        @Override
	        public boolean isFatal(Throwable t) {
	            if (t instanceof ListenerExecutionFailedException) {
	                ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
	                logger.error("Failed to process inbound message from queue "
	                        + lefe.getFailedMessage().getMessageProperties().getConsumerQueue()
	                        + "; failed message: " + lefe.getFailedMessage(), t);
	            }
	            return super.isFatal(t);
	        }
	    }


}

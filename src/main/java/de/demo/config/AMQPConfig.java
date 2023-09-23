package de.demo.config;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP.Channel;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

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

	// for RPC

	@Value("${rabbitmq.reply-queue}")
	private String replyQueueName;

	@Bean
	AMQPAudit messageAspect() {
		return new AMQPAudit();
	}

	@Bean
	public Queue queue() {
		return new Queue(queueName, true);
	}

	@Bean
	public Queue replyQueue() {
		return new Queue(replyQueueName, true);
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

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.execute(new ChannelCallback<Object>() {

			@Override
			public Object doInRabbit(com.rabbitmq.client.Channel channel) throws Exception {

				((com.rabbitmq.client.Channel) channel).getConnection().addBlockedListener(new BlockedListener() {

					public void handleUnblocked() throws IOException {
						// Resume business logic
					}

					public void handleBlocked(String reason) throws IOException {
						// FlowControl -> Logic to handle block
					}
				});

				((com.rabbitmq.client.Channel) channel).getConnection().addShutdownListener(new ShutdownListener() {

					@Override
					public void shutdownCompleted(ShutdownSignalException cause) {
						// TODO Auto-generated method stub

					};

				});

				return null;
			}

		});
		return template;
	}

//	@Bean
//	public SimpleMessageListenerContainer replyListenerContainer() {
//
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//		container.setConnectionFactory(connectionFactory());
//		container.setQueues(replyQueue());
//		container.setMessageListener(fixedReplyQueueRabbitTemplate());
//		return container;
//	}

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
						+ lefe.getFailedMessage().getMessageProperties().getConsumerQueue() + "; failed message: "
						+ lefe.getFailedMessage(), t);
			}
			return super.isFatal(t);
		}
	}

}

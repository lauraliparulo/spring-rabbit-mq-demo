package de.demo.config;

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
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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

import de.demo.aop.AMQPAudit;

@Configuration
@EnableConfigurationProperties(AMQPProperties.class)
//@EnableAspectJAutoProxy(proxyTargetClass = true)
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
	    
	    @Bean
	    public AmqpAdmin amqpAdmin() {
	        return new RabbitAdmin(connectionFactory());
	    }
	
//	@Bean
//	ConnectionFactory connectionFactory() {
//		return new ConnectionFactory();
//	}
	
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

//
//	@Bean
//	public SimpleMessageListenerContainer container(
//			org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory, MessageListener consumer,
//			@Value("${rabbitmq.queue}") String queueName) {
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//		container.setConnectionFactory((connectionFactory));
//		container.setQueueNames(queueName);
//		container.setMessageListener(consumer);
//		return container;
//	}

	/*
	 * Uncomment this out for a RPC with a fixed reply-to queue
	 * 
	 * 
	 * 
	 * @Value("${de.demo.amqp.reply-queue}") String replyQueueName;
	 * 
	 * @Bean public RabbitTemplate fixedReplyQueueRabbitTemplate() { RabbitTemplate
	 * template = new RabbitTemplate(connectionFactory);
	 * template.setReplyAddress(replyQueueName); template.setReplyTimeout(60000L);
	 * return template; }
	 * 
	 * @Bean public SimpleMessageListenerContainer replyListenerContainer() {
	 * SimpleMessageListenerContainer container = new
	 * SimpleMessageListenerContainer();
	 * container.setConnectionFactory(connectionFactory);
	 * container.setQueues(replyQueue());
	 * container.setMessageListener(fixedReplyQueueRabbitTemplate()); return
	 * container; }
	 * 
	 * @Bean public Queue replyQueue(){ return new Queue(replyQueueName,false); }
	 */

	// Converters

//	/*
//	 * //Producer
//	 * 
//	 * @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory
//	 * connectionFactory){ RabbitTemplate template = new
//	 * RabbitTemplate(connectionFactory); template.setMessageConverter(new
//	 * Jackson2JsonMessageConverter()); return template; }
//	 * 
//	 * //Consumer
//	 * 
//	 * @Bean public SimpleRabbitListenerContainerFactory
//	 * rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//	 * SimpleRabbitListenerContainerFactory factory = new
//	 * SimpleRabbitListenerContainerFactory();
//	 * factory.setConnectionFactory(connectionFactory);
//	 * factory.setMessageConverter(new Jackson2JsonMessageConverter()); return
//	 * factory; }
//	 */

	// Template with Blocked/UnBlocked/Shutdown Listeners

	/*
	 * @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory
	 * connectionFactory){ RabbitTemplate template = new
	 * RabbitTemplate(connectionFactory); template.execute(new
	 * ChannelCallback<Object>() {
	 * 
	 * public Object doInRabbit(Channel channel) throws Exception {
	 * 
	 * channel.getConnection().addShutdownListener(new ShutdownListener() { public
	 * void shutdownCompleted(ShutdownSignalException cause) { // Process the
	 * shutdown } });
	 * 
	 * channel.getConnection().addBlockedListener(new BlockedListener() {
	 * 
	 * public void handleUnblocked() throws IOException { // Resume business logic }
	 * 
	 * @Override public void handleBlocked(String reason) throws IOException { //
	 * FlowControl -> Logic to handle block } });
	 * 
	 * return null; }
	 * 
	 * }); return template; }
	 */

	// Template with ShutdownListener and Java 8 Lambda
	/*
	 * @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory
	 * connectionFactory){ RabbitTemplate template = new
	 * RabbitTemplate(connectionFactory); template.execute( channel -> {
	 * channel.getConnection().addShutdownListener( cause -> {
	 * 
	 * //Logic Here
	 * 
	 * }); return null; });
	 * 
	 * return template; }
	 */

	// Retry for the consumer, normally this needs to be set in the container:
	// container.setAdviceChain(new Advice[] { interceptor() });
	//
	/*
	 * @Bean public StatefulRetryOperationsInterceptor interceptor() { return
	 * RetryInterceptorBuilder.stateful() .maxAttempts(3) .backOffOptions(1000, 2.0,
	 * 10000) // initialInterval, multiplier, maxInterval .build(); }
	 */

	// Retry for the Consumer. After the 3 attempts, the next will be the
	// RepublishMessageRecoverer
	/*
	 * @Bean RetryOperationsInterceptor interceptor(RabbitTemplate
	 * template,@Value("${de.demo.amqp.error-exchange:}")String
	 * errorExchange, @Value("${de.demo.amqp.error-routing-key}")String
	 * errorExchangeRountingKey) { return RetryInterceptorBuilder.stateless()
	 * .maxAttempts(3) .recoverer(new RepublishMessageRecoverer(template,
	 * errorExchange, errorExchangeRountingKey)) .build(); }
	 */
}

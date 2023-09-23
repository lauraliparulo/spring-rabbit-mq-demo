package de.demo.amqp.rpc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class RpcResponseServer {

@RabbitListener(queues="${rabbitmq.queue}")              
public Message<String> process(String message){              

///More Processing here...              
    return MessageBuilder              
            .withPayload("PROCESSED:OK")              
            .setHeader("PROCESSED", new              
                                 SimpleDateFormat("yyyy-MM-dd")              
                                                         .format(new Date()))              
            .setHeader("CODE", UUID.randomUUID().toString())              
            .build();
        }
}
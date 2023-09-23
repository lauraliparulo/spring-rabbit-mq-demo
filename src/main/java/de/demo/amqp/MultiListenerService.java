package de.demo.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;

import de.demo.domain.Invoice;
import de.demo.domain.InvoiceWithTax;
import de.demo.domain.Item;
import de.demo.domain.Order;


//@Component
@RabbitListener(id="multi", queues = "${rabbitmq.queue}")
public class MultiListenerService {

	@RabbitHandler
    @SendTo("${rabbitmq.reply-exchange-queue}")
    public Order processInvoice(Invoice invoice) {
        Order order = new Order();
        
        //Process Invoice here...
        
        order.setInvoice(invoice);      	return order;
    }

    @RabbitHandler
    public Order processInvoiceWithTax(InvoiceWithTax invoiceWithTax) {
    		Order order = new Order();
        
        //Process Invoice with Tax here...
    		    		    		
    		return order;
    }

    @RabbitHandler
    public String itemProcess(@Header("amqp_receivedRoutingKey") String routingKey, @Payload Item item) {
        //Some Process here...
    		return "{\"message\": \"OK\"}"; 
    }

}

package com.pedidos360.backend.messaging.consumer.tickets;

import com.pedidos360.backend.messaging.config.RabbitMQConfig;
import com.pedidos360.backend.messaging.event.PedidoEvent;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TicketConsumer {

    @RabbitListener(queues = RabbitMQConfig.TICKETS_QUEUE)
    public void procesar(PedidoEvent evento, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (evento.producto().toLowerCase().contains("error")) {
                throw new IllegalStateException("Producto inválido");
            }
            System.out.println("[TICKET] Ticket TCK-" + evento.pedidoId() + " generado");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.out.println("[TICKET] Falló pedido " + evento.pedidoId() + " -> DLQ");
            channel.basicNack(tag, false, false);
        }
    }
}
package com.pedidos360.backend.messaging.consumer.notificaciones;

import com.pedidos360.backend.messaging.config.RabbitMQConfig;
import com.pedidos360.backend.messaging.event.PedidoEvent;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificacionConsumer {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICACIONES_QUEUE)
    public void procesar(PedidoEvent evento, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (evento.producto().toLowerCase().contains("error")) {
                throw new IllegalStateException("Producto inválido");
            }
            System.out.println("[NOTIFICACION] Aviso enviado a " + evento.cliente()
                    + " - pedido " + evento.pedidoId() + " " + evento.tipoEvento());
            channel.basicAck(tag, false);               // OK -> se borra de la cola
        } catch (Exception e) {
            System.out.println("[NOTIFICACION] Falló pedido " + evento.pedidoId() + " -> DLQ");
            channel.basicNack(tag, false, false);       // Error -> va a la DLQ
        }
    }
}
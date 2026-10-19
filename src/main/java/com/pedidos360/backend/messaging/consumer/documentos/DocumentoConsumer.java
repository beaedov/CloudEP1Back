package com.pedidos360.backend.messaging.consumer.documentos;

import com.pedidos360.backend.messaging.config.RabbitMQConfig;
import com.pedidos360.backend.messaging.event.PedidoEvent;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DocumentoConsumer {

    @RabbitListener(queues = RabbitMQConfig.DOCUMENTOS_QUEUE)
    public void procesar(PedidoEvent evento, Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            if (evento.producto().toLowerCase().contains("error")) {
                throw new IllegalStateException("Producto inválido");
            }
            System.out.println("[DOCUMENTO] Comprobante del pedido " + evento.pedidoId() + " generado");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            System.out.println("[DOCUMENTO] Falló pedido " + evento.pedidoId() + " -> DLQ");
            channel.basicNack(tag, false, false);
        }
    }
}

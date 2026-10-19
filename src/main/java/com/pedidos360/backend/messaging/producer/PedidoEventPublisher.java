package com.pedidos360.backend.messaging.producer;

import com.pedidos360.backend.messaging.config.RabbitMQConfig;
import com.pedidos360.backend.messaging.event.PedidoEvent;
import com.pedidos360.backend.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public PedidoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void pedidoCreado(Pedido p)      { publicar(RabbitMQConfig.RK_PEDIDO_CREADO, p, "CREADO"); }
    public void pedidoActualizado(Pedido p) { publicar(RabbitMQConfig.RK_PEDIDO_ACTUALIZADO, p, "ACTUALIZADO"); }
    public void pedidoEliminado(Pedido p)   { publicar(RabbitMQConfig.RK_PEDIDO_ELIMINADO, p, "ELIMINADO"); }

    private void publicar(String routingKey, Pedido p, String tipo) {
        PedidoEvent evento = new PedidoEvent(p.getId(), p.getCliente(), p.getProducto(),
                p.getCantidad(), p.getEstado(), tipo, LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PEDIDOS_EXCHANGE, routingKey, evento);
            log.info("[PUBLISHER] {} publicado, pedido {}", routingKey, p.getId());
        } catch (AmqpException e) {
            // Si RabbitMQ falla, el pedido igual queda guardado
            log.error("[PUBLISHER] No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}
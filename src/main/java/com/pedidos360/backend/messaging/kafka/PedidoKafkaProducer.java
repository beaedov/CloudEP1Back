package com.pedidos360.backend.messaging.kafka;

import com.pedidos360.backend.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * PRODUCTOR Kafka del backend Pedidos360.
 *
 * Se llama desde PedidoService JUNTO a la publicación en RabbitMQ (no la reemplaza).
 * El envío es asíncrono y cualquier error solo se registra en el log:
 * si Kafka está caído, el pedido igual se guarda y RabbitMQ sigue funcionando.
 */
@Component
public class PedidoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate;
    private final String topico;

    public PedidoKafkaProducer(KafkaTemplate<String, PedidoKafkaEvent> kafkaTemplate,
                               @Value("${app.kafka.topico-eventos}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.topico = topico;
    }

    public void pedidoCreado(Pedido p)      { publicar("CREADO", p); }
    public void pedidoActualizado(Pedido p) { publicar("ACTUALIZADO", p); }
    public void pedidoEliminado(Pedido p)   { publicar("ELIMINADO", p); }

    private void publicar(String tipo, Pedido p) {
        PedidoKafkaEvent evento = new PedidoKafkaEvent(
                UUID.randomUUID().toString(), tipo, p.getId(), p.getCliente(), p.getProducto(),
                p.getCantidad(), p.getEstado(), usuarioActual(), System.currentTimeMillis());
        try {
            // KEY = id del pedido -> todos los eventos de un pedido van a la misma partición (orden)
            kafkaTemplate.send(topico, String.valueOf(p.getId()), evento)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("[KAFKA] {} publicado | particion={} | offset={} | pedido {}",
                                    tipo, result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(), p.getId());
                        } else {
                            log.error("[KAFKA] No se pudo publicar {} del pedido {}: {}", tipo, p.getId(), ex.getMessage());
                        }
                    });
        } catch (Exception ex) {
            // Ej.: broker no disponible dentro de max.block.ms. El pedido NO se ve afectado.
            log.error("[KAFKA] Envío rechazado {} del pedido {}: {}", tipo, p.getId(), ex.getMessage());
        }
    }

    /** Usuario autenticado según el JWT de Entra ID (para auditoría). */
    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            String nombre = jwt.getToken().getClaimAsString("preferred_username");
            if (nombre == null) nombre = jwt.getToken().getClaimAsString("name");
            return nombre != null ? nombre : jwt.getName();
        }
        return auth != null ? auth.getName() : "sistema";
    }
}

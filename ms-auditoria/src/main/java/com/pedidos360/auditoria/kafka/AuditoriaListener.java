package com.pedidos360.auditoria.kafka;

import com.pedidos360.auditoria.dto.MetricaPedidos;
import com.pedidos360.auditoria.dto.PedidoEvento;
import com.pedidos360.auditoria.service.AuditoriaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMIDORES Kafka de auditoría (capa de mensajería). Escucha LOS DOS tópicos:
 *  - pedidos-eventos  : cada acción de negocio sobre un pedido
 *  - pedidos-metricas : cada métrica recalculada por reportería
 * Ambos usan el grupo auditoria-group. Errores -> KafkaErrorConfig (reintentos + DLT).
 */
@Component
public class AuditoriaListener {

    private final AuditoriaService auditoriaService;

    public AuditoriaListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @KafkaListener(topics = "${app.kafka.topico-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPedidoEvento(ConsumerRecord<String, PedidoEvento> record) {
        auditoriaService.auditarPedido(record.topic(), record.partition(), record.offset(),
                record.key(), record.value());
    }

    // "properties" cambia el tipo JSON SOLO para este listener
    @KafkaListener(topics = "${app.kafka.topico-metricas}", groupId = "${spring.kafka.consumer.group-id}",
            properties = "spring.json.value.default.type=com.pedidos360.auditoria.dto.MetricaPedidos")
    public void onMetrica(ConsumerRecord<String, MetricaPedidos> record) {
        auditoriaService.auditarMetrica(record.topic(), record.partition(), record.offset(),
                record.key(), record.value());
    }
}

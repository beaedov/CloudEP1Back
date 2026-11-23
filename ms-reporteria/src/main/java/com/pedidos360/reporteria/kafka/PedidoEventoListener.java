package com.pedidos360.reporteria.kafka;

import com.pedidos360.reporteria.dto.MetricaPedidos;
import com.pedidos360.reporteria.dto.PedidoEvento;
import com.pedidos360.reporteria.service.ReporteService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMIDOR Kafka (capa de mensajería).
 * Solo recibe el mensaje, registra de dónde vino y delega al servicio de dominio.
 * Si el servicio lanza una excepción, la maneja KafkaErrorConfig (reintentos + DLT).
 */
@Component
public class PedidoEventoListener {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventoListener.class);

    private final ReporteService reporteService;
    private final MetricaProducer metricaProducer;

    public PedidoEventoListener(ReporteService reporteService, MetricaProducer metricaProducer) {
        this.reporteService = reporteService;
        this.metricaProducer = metricaProducer;
    }

    @KafkaListener(topics = "${app.kafka.topico-eventos}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPedidoEvento(ConsumerRecord<String, PedidoEvento> record) {
        log.info("REPORTERIA | recibido | particion={} | offset={} | key={} | {}",
                record.partition(), record.offset(), record.key(), record.value());

        MetricaPedidos metrica = reporteService.procesar(record.value());

        if (metrica == null) {
            log.warn("REPORTERIA | evento duplicado ignorado | eventoId={}", record.value().getEventoId());
            return;
        }
        // Reacción en tiempo real: la métrica nueva se publica en el 2º tópico
        metricaProducer.publicar(metrica);
    }
}

package com.pedidos360.reporteria.kafka;

import com.pedidos360.reporteria.dto.MetricaPedidos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** PRODUCTOR Kafka: publica cada métrica actualizada en "pedidos-metricas" (key = tipoEvento). */
@Component
public class MetricaProducer {

    private static final Logger log = LoggerFactory.getLogger(MetricaProducer.class);

    private final KafkaTemplate<String, MetricaPedidos> kafkaTemplate;
    private final String topico;

    public MetricaProducer(KafkaTemplate<String, MetricaPedidos> kafkaTemplate,
                           @Value("${app.kafka.topico-metricas}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.topico = topico;
    }

    public void publicar(MetricaPedidos metrica) {
        kafkaTemplate.send(topico, metrica.getTipoEvento(), metrica)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("REPORTERIA | métrica publicada | particion={} | offset={} | {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(), metrica);
                    } else {
                        log.error("REPORTERIA | error publicando métrica {} : {}", metrica, ex.getMessage());
                    }
                });
    }
}

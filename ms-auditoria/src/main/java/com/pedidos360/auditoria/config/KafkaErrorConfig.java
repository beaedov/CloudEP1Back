package com.pedidos360.auditoria.config;

import com.pedidos360.auditoria.service.EventoInvalidoException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MANEJO DE ERRORES del consumidor (equivalente a la DLQ de RabbitMQ).
 *
 *  1. Error temporal (ej. una excepción inesperada): se REINTENTA 3 veces, esperando 1 s entre intentos.
 *  2. Si sigue fallando, o si el payload es inválido / el JSON viene roto (no tiene sentido reintentar):
 *     se registra en el log y se envía al tópico <topico>.DLT (Dead Letter Topic).
 *  3. El consumidor SIGUE funcionando con los mensajes siguientes: un mensaje malo no bloquea la partición.
 *
 * Spring Boot detecta este bean (CommonErrorHandler) y lo aplica a todos los @KafkaListener.
 */
@Configuration
public class KafkaErrorConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaErrorConfig.class);

    @Bean
    public DefaultErrorHandler errorHandler(@Value("${spring.kafka.bootstrap-servers}") String bootstrap,
                                            @Value("${app.kafka.reintentos}") long reintentos,
                                            @Value("${app.kafka.espera-reintento-ms}") long espera) {

        // Template SOLO para el DLT: sabe enviar bytes (JSON roto) y objetos (payload inválido)
        Map<Class<?>, Serializer<?>> serializers = new LinkedHashMap<>();
        serializers.put(byte[].class, new ByteArraySerializer());
        serializers.put(Object.class, new JsonSerializer<>());
        Map<String, Object> props = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        KafkaTemplate<String, Object> dltTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(props, new StringSerializer(),
                        new DelegatingByTypeSerializer(serializers, true)));

        // Por defecto publica en "<topico-original>.DLT", misma partición
        DeadLetterPublishingRecoverer dlt = new DeadLetterPublishingRecoverer(dltTemplate);

        DefaultErrorHandler handler = new DefaultErrorHandler((record, ex) -> {
            log.error("DLT | enviando a {}.DLT | particion={} | offset={} | key={} | causa={}",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
            dlt.accept(record, ex);
        }, new FixedBackOff(espera, reintentos));

        // Log de cada reintento
        handler.setRetryListeners((record, ex, intento) ->
                log.warn("REINTENTO {} | topico={} | offset={} | causa={}",
                        intento, record.topic(), record.offset(), ex.getMessage()));

        // Estos errores no se arreglan reintentando -> directo al DLT
        // (DeserializationException ya viene incluida por defecto)
        handler.addNotRetryableExceptions(EventoInvalidoException.class);

        return handler;
    }
}

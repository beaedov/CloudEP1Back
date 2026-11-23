package com.pedidos360.kafkaadmin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Declaración de tópicos "como código".
 * Al iniciar, KafkaAdmin (de Spring) crea los tópicos que no existan con estos parámetros.
 * Es replicable: cualquier ambiente nuevo queda igual con solo levantar este microservicio.
 */
@Configuration
public class TopicosConfig {

    /** Cliente de administración de Kafka (crear, listar, modificar, borrar). */
    @Bean(destroyMethod = "close")
    public AdminClient adminClient(KafkaAdmin kafkaAdmin) {
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }

    @Bean
    public NewTopic topicoEventos(@Value("${app.topicos.eventos.nombre}") String nombre,
                                  @Value("${app.topicos.eventos.particiones}") int particiones,
                                  @Value("${app.topicos.eventos.replicas}") int replicas,
                                  @Value("${app.topicos.eventos.retention-ms}") String retencion,
                                  @Value("${app.topicos.eventos.cleanup-policy}") String cleanup,
                                  @Value("${app.topicos.eventos.min-insync-replicas}") String minIsr) {
        return TopicBuilder.name(nombre)
                .partitions(particiones)
                .replicas(replicas)
                .config(TopicConfig.RETENTION_MS_CONFIG, retencion)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanup)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minIsr)
                .build();
    }

    @Bean
    public NewTopic topicoMetricas(@Value("${app.topicos.metricas.nombre}") String nombre,
                                   @Value("${app.topicos.metricas.particiones}") int particiones,
                                   @Value("${app.topicos.metricas.replicas}") int replicas,
                                   @Value("${app.topicos.metricas.cleanup-policy}") String cleanup,
                                   @Value("${app.topicos.metricas.min-insync-replicas}") String minIsr) {
        return TopicBuilder.name(nombre)
                .partitions(particiones)
                .replicas(replicas)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, cleanup)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minIsr)
                .build();
    }

    // Dead Letter Topics: mismas particiones que el original (el DLT usa la misma partición)
    @Bean
    public NewTopic topicoEventosDlt(@Value("${app.topicos.eventos.nombre}") String nombre,
                                     @Value("${app.topicos.dlt.retention-ms}") String retencion) {
        return TopicBuilder.name(nombre + ".DLT").partitions(3).replicas(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, retencion).build();
    }

    @Bean
    public NewTopic topicoMetricasDlt(@Value("${app.topicos.metricas.nombre}") String nombre,
                                      @Value("${app.topicos.dlt.retention-ms}") String retencion) {
        return TopicBuilder.name(nombre + ".DLT").partitions(3).replicas(3)
                .config(TopicConfig.RETENTION_MS_CONFIG, retencion).build();
    }
}

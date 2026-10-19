package com.pedidos360.backend.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== Exchanges =====
    public static final String PEDIDOS_EXCHANGE = "pedidos.topic";
    public static final String DLX_EXCHANGE = "pedidos.dlx";

    // ===== Colas de negocio =====
    public static final String NOTIFICACIONES_QUEUE = "pedidos.notificaciones.queue";
    public static final String TICKETS_QUEUE = "pedidos.tickets.queue";
    public static final String DOCUMENTOS_QUEUE = "pedidos.documentos.queue";

    // ===== Dead Letter Queues =====
    public static final String NOTIFICACIONES_DLQ = "pedidos.notificaciones.dlq";
    public static final String TICKETS_DLQ = "pedidos.tickets.dlq";
    public static final String DOCUMENTOS_DLQ = "pedidos.documentos.dlq";

    // ===== Routing keys de eventos (exchange topic) =====
    public static final String RK_PEDIDO_CREADO = "pedido.creado";
    public static final String RK_PEDIDO_ACTUALIZADO = "pedido.actualizado";
    public static final String RK_PEDIDO_ELIMINADO = "pedido.eliminado";
    public static final String RK_TODOS_LOS_PEDIDOS = "pedido.*";

    // ===== Routing keys de dead-lettering (exchange direct) =====
    public static final String DLK_NOTIFICACIONES = "notificaciones.dead";
    public static final String DLK_TICKETS = "tickets.dead";
    public static final String DLK_DOCUMENTOS = "documentos.dead";

    // ---------- Exchanges ----------

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(PEDIDOS_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ---------- Colas de negocio (cada una declara su DLX) ----------

    @Bean
    public Queue notificacionesQueue() {
        return colaConDlq(NOTIFICACIONES_QUEUE, DLK_NOTIFICACIONES);
    }

    @Bean
    public Queue ticketsQueue() {
        return colaConDlq(TICKETS_QUEUE, DLK_TICKETS);
    }

    @Bean
    public Queue documentosQueue() {
        return colaConDlq(DOCUMENTOS_QUEUE, DLK_DOCUMENTOS);
    }

    private Queue colaConDlq(String nombre, String deadLetterRoutingKey) {
        return QueueBuilder.durable(nombre)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }

    // ---------- DLQs ----------

    @Bean
    public Queue notificacionesDlq() {
        return QueueBuilder.durable(NOTIFICACIONES_DLQ).build();
    }

    @Bean
    public Queue ticketsDlq() {
        return QueueBuilder.durable(TICKETS_DLQ).build();
    }

    @Bean
    public Queue documentosDlq() {
        return QueueBuilder.durable(DOCUMENTOS_DLQ).build();
    }

    // ---------- Bindings de negocio (topic) ----------

    @Bean
    public Binding notificacionesBinding() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(RK_TODOS_LOS_PEDIDOS);
    }

    /** Tickets solo se generan para pedidos nuevos. */
    @Bean
    public Binding ticketsBinding() {
        return BindingBuilder.bind(ticketsQueue()).to(pedidosExchange()).with(RK_PEDIDO_CREADO);
    }

    /** Documentos (comprobante) solo para pedidos nuevos. */
    @Bean
    public Binding documentosBinding() {
        return BindingBuilder.bind(documentosQueue()).to(pedidosExchange()).with(RK_PEDIDO_CREADO);
    }

    // ---------- Bindings de dead-lettering (direct) ----------

    @Bean
    public Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(deadLetterExchange()).with(DLK_NOTIFICACIONES);
    }

    @Bean
    public Binding ticketsDlqBinding() {
        return BindingBuilder.bind(ticketsDlq()).to(deadLetterExchange()).with(DLK_TICKETS);
    }

    @Bean
    public Binding documentosDlqBinding() {
        return BindingBuilder.bind(documentosDlq()).to(deadLetterExchange()).with(DLK_DOCUMENTOS);
    }

    // ---------- Conversión JSON ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
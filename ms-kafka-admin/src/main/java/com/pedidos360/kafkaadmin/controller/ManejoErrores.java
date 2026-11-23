package com.pedidos360.kafkaadmin.controller;

import org.apache.kafka.common.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/** Traduce los errores de Kafka a códigos HTTP entendibles. */
@RestControllerAdvice
public class ManejoErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejoErrores.class);

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<Map<String, String>> kafka(ExecutionException ex) {
        Throwable causa = ex.getCause();
        HttpStatus status;
        if (causa instanceof TopicExistsException) {
            status = HttpStatus.CONFLICT;                          // 409 ya existe
        } else if (causa instanceof UnknownTopicOrPartitionException
                || causa instanceof GroupIdNotFoundException) {
            status = HttpStatus.NOT_FOUND;                         // 404 no existe
        } else if (causa instanceof InvalidPartitionsException
                || causa instanceof InvalidReplicationFactorException
                || causa instanceof InvalidConfigurationException
                || causa instanceof InvalidRequestException
                || causa instanceof GroupNotEmptyException) {
            status = HttpStatus.BAD_REQUEST;                       // 400 datos inválidos
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        log.warn("KAFKA-ADMIN | {} | {}", status, causa != null ? causa.getMessage() : ex.getMessage());
        return ResponseEntity.status(status).body(Map.of(
                "error", causa != null ? causa.getClass().getSimpleName() : "ExecutionException",
                "mensaje", String.valueOf(causa != null ? causa.getMessage() : ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumento(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Solicitud inválida", "mensaje", String.valueOf(ex.getMessage())));
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String, String>> timeout(TimeoutException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Kafka no responde", "mensaje", "revise que los brokers estén arriba"));
    }
}

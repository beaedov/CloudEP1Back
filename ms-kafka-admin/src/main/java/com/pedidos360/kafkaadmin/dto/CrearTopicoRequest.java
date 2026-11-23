package com.pedidos360.kafkaadmin.dto;

import java.util.Map;

/**
 * Body para crear un tópico:
 * { "nombre":"pedidos-prueba", "particiones":3, "replicas":3,
 *   "configuraciones": { "retention.ms":"86400000", "cleanup.policy":"delete" } }
 */
public record CrearTopicoRequest(
        String nombre,
        Integer particiones,
        Short replicas,
        Map<String, String> configuraciones
) {}

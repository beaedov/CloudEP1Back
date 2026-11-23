package com.pedidos360.auditoria.dto;

/**
 * Registro de auditoría: QUÉ pasó, QUIÉN, CUÁNDO y DÓNDE quedó en Kafka (tópico/partición/offset).
 * Con tópico+partición+offset se puede ubicar el mensaje original en Kafka UI.
 */
public record RegistroAuditoria(
        String topico,
        int particion,
        long offset,
        String key,
        String tipo,
        String usuario,
        String detalle,
        long registradoEn
) {}

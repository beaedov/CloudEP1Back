package com.pedidos360.backend.messaging.event;

import java.time.LocalDateTime;

public record PedidoEvent(
        Long pedidoId,
        String cliente,
        String producto,
        Integer cantidad,
        String estado,
        String tipoEvento,
        LocalDateTime fecha
) {
}
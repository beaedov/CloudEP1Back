package com.pedidos360.backend.messaging.kafka;

/**
 * DTO del evento de negocio que se publica en el tópico Kafka "pedidos-eventos".
 * (Distinto de PedidoEvent, que es el mensaje de RabbitMQ: así cada integración evoluciona por separado.)
 *
 *  - eventoId   : id único -> auditoría e idempotencia en los consumidores
 *  - tipoEvento : CREADO, ACTUALIZADO o ELIMINADO
 *  - pedidoId   : id del pedido; se usa como KEY -> mismo pedido, misma partición, orden garantizado
 *  - cliente, producto, cantidad, estado : datos del pedido para reportería
 *  - usuario    : quién hizo la acción (sale del JWT) -> auditoría
 *  - timestamp  : cuándo ocurrió (epoch millis)
 */
public class PedidoKafkaEvent {

    private String eventoId;
    private String tipoEvento;
    private Long pedidoId;
    private String cliente;
    private String producto;
    private Integer cantidad;
    private String estado;
    private String usuario;
    private Long timestamp;

    public PedidoKafkaEvent() {}

    public PedidoKafkaEvent(String eventoId, String tipoEvento, Long pedidoId, String cliente,
                            String producto, Integer cantidad, String estado, String usuario, Long timestamp) {
        this.eventoId = eventoId;
        this.tipoEvento = tipoEvento;
        this.pedidoId = pedidoId;
        this.cliente = cliente;
        this.producto = producto;
        this.cantidad = cantidad;
        this.estado = estado;
        this.usuario = usuario;
        this.timestamp = timestamp;
    }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "PedidoKafkaEvent{" + tipoEvento + ", pedidoId=" + pedidoId + ", usuario=" + usuario + "}";
    }
}

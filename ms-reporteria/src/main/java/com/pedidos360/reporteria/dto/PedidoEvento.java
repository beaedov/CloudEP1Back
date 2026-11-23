package com.pedidos360.reporteria.dto;

/**
 * Evento de negocio que llega por el tópico "pedidos-eventos" (lo publica el backend Pedidos360).
 * Es una copia del contrato JSON: los microservicios no comparten código, comparten el formato del evento.
 *
 *  - eventoId   : id único del evento (auditoría e idempotencia: evita contar dos veces)
 *  - tipoEvento : CREADO, ACTUALIZADO o ELIMINADO (reportería agrupa por esto)
 *  - pedidoId   : id del pedido; es la KEY del mensaje (mismo pedido = misma partición = orden)
 *  - cliente, producto, cantidad, estado : datos del pedido
 *  - usuario    : quién hizo la acción (sale del JWT de Entra ID)
 *  - timestamp  : cuándo ocurrió (epoch millis)
 */
public class PedidoEvento {

    private String eventoId;
    private String tipoEvento;
    private Long pedidoId;
    private String cliente;
    private String producto;
    private Integer cantidad;
    private String estado;
    private String usuario;
    private Long timestamp;

    public PedidoEvento() {}

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
        return "PedidoEvento{" + tipoEvento + ", pedidoId=" + pedidoId + ", cliente=" + cliente
                + ", producto=" + producto + ", cantidad=" + cantidad + ", estado=" + estado
                + ", usuario=" + usuario + "}";
    }
}

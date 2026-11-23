package com.pedidos360.auditoria.dto;

/**
 * Métrica que se publica en el tópico "pedidos-metricas" (compactado).
 * KEY = tipoEvento -> Kafka conserva el ÚLTIMO valor de cada métrica (cleanup.policy=compact).
 *
 *  - tipoEvento          : CREADO, ACTUALIZADO o ELIMINADO
 *  - totalEventos        : cuántos eventos de ese tipo van
 *  - unidadesAcumuladas  : suma de "cantidad" de los pedidos de ese tipo
 *  - ultimoPedidoId      : último pedido que actualizó la métrica
 *  - ultimaActualizacion : cuándo se recalculó (epoch millis)
 */
public class MetricaPedidos {

    private String tipoEvento;
    private Long totalEventos;
    private Long unidadesAcumuladas;
    private Long ultimoPedidoId;
    private Long ultimaActualizacion;

    public MetricaPedidos() {}

    public MetricaPedidos(String tipoEvento, Long totalEventos, Long unidadesAcumuladas,
                          Long ultimoPedidoId, Long ultimaActualizacion) {
        this.tipoEvento = tipoEvento;
        this.totalEventos = totalEventos;
        this.unidadesAcumuladas = unidadesAcumuladas;
        this.ultimoPedidoId = ultimoPedidoId;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Long getTotalEventos() { return totalEventos; }
    public void setTotalEventos(Long totalEventos) { this.totalEventos = totalEventos; }
    public Long getUnidadesAcumuladas() { return unidadesAcumuladas; }
    public void setUnidadesAcumuladas(Long unidadesAcumuladas) { this.unidadesAcumuladas = unidadesAcumuladas; }
    public Long getUltimoPedidoId() { return ultimoPedidoId; }
    public void setUltimoPedidoId(Long ultimoPedidoId) { this.ultimoPedidoId = ultimoPedidoId; }
    public Long getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(Long ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    @Override
    public String toString() {
        return "MetricaPedidos{" + tipoEvento + ", totalEventos=" + totalEventos
                + ", unidadesAcumuladas=" + unidadesAcumuladas + "}";
    }
}

package com.pedidos360.reporteria.service;

import com.pedidos360.reporteria.dto.MetricaPedidos;
import com.pedidos360.reporteria.dto.PedidoEvento;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LÓGICA DE NEGOCIO de reportería (no sabe nada de Kafka).
 * Calcula métricas en tiempo real por tipo de evento: cuántos pedidos se crearon,
 * actualizaron o eliminaron y cuántas unidades suman.
 */
@Service
public class ReporteService {

    private static final Set<String> TIPOS_VALIDOS = Set.of("CREADO", "ACTUALIZADO", "ELIMINADO");

    /** tipoEvento -> métrica actual. ConcurrentHashMap porque hay 3 hilos consumidores. */
    private final Map<String, MetricaPedidos> metricas = new ConcurrentHashMap<>();

    /** eventoIds ya procesados: si Kafka re-entrega un mensaje, no se cuenta dos veces. */
    private final Set<String> procesados = ConcurrentHashMap.newKeySet();

    /** Devuelve la métrica actualizada, o null si el evento es duplicado. */
    public MetricaPedidos procesar(PedidoEvento evento) {
        validar(evento);

        if (!procesados.add(evento.getEventoId())) {
            return null;
        }

        return metricas.compute(evento.getTipoEvento(), (tipo, actual) -> {
            long total = (actual == null ? 0 : actual.getTotalEventos()) + 1;
            long unidades = (actual == null ? 0 : actual.getUnidadesAcumuladas()) + evento.getCantidad();
            return new MetricaPedidos(tipo, total, unidades, evento.getPedidoId(), System.currentTimeMillis());
        });
    }

    /** Validación del payload: si no cumple, se lanza una excepción que NO se reintenta (va al DLT). */
    private void validar(PedidoEvento e) {
        if (e == null) throw new EventoInvalidoException("evento nulo");
        if (e.getEventoId() == null || e.getEventoId().isBlank())
            throw new EventoInvalidoException("eventoId obligatorio");
        if (e.getPedidoId() == null)
            throw new EventoInvalidoException("pedidoId obligatorio");
        if (e.getTipoEvento() == null || !TIPOS_VALIDOS.contains(e.getTipoEvento()))
            throw new EventoInvalidoException("tipoEvento desconocido: " + e.getTipoEvento());
        if (e.getCantidad() == null || e.getCantidad() < 0)
            throw new EventoInvalidoException("cantidad inválida: " + e.getCantidad());
    }

    public Map<String, Object> resumen() {
        long totalEventos = metricas.values().stream().mapToLong(MetricaPedidos::getTotalEventos).sum();
        return Map.of(
                "totalEventos", totalEventos,
                "porTipo", new TreeMap<>(metricas));
    }
}

package com.pedidos360.auditoria.service;

import com.pedidos360.auditoria.dto.MetricaPedidos;
import com.pedidos360.auditoria.dto.PedidoEvento;
import com.pedidos360.auditoria.dto.RegistroAuditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * LÓGICA DE NEGOCIO de auditoría (no sabe nada de Kafka: recibe datos ya extraídos).
 * Guarda los últimos 200 registros en memoria y deja cada uno en el log.
 * (Si quieres persistirlo, aquí es donde llamarías a un repositorio JPA de tu BD cloud.)
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);
    private static final int MAX_REGISTROS = 200;

    private final Deque<RegistroAuditoria> registros = new ConcurrentLinkedDeque<>();

    public void auditarPedido(String topico, int particion, long offset, String key, PedidoEvento e) {
        if (e == null || e.getEventoId() == null || e.getTipoEvento() == null) {
            throw new EventoInvalidoException("evento de pedido sin eventoId o tipoEvento");
        }
        guardar(new RegistroAuditoria(topico, particion, offset, key,
                "PEDIDO_" + e.getTipoEvento(), e.getUsuario(),
                "pedido " + e.getPedidoId() + " (" + e.getCliente() + ", " + e.getProducto()
                        + " x" + e.getCantidad() + ") -> " + e.getEstado(),
                System.currentTimeMillis()));
    }

    public void auditarMetrica(String topico, int particion, long offset, String key, MetricaPedidos m) {
        if (m == null || m.getTipoEvento() == null) {
            throw new EventoInvalidoException("métrica sin tipoEvento");
        }
        guardar(new RegistroAuditoria(topico, particion, offset, key,
                "METRICA_" + m.getTipoEvento(), "ms-reporteria",
                "totalEventos=" + m.getTotalEventos() + ", unidadesAcumuladas=" + m.getUnidadesAcumuladas(),
                System.currentTimeMillis()));
    }

    private void guardar(RegistroAuditoria r) {
        registros.addFirst(r);
        while (registros.size() > MAX_REGISTROS) {
            registros.pollLast();
        }
        log.info("AUDITORIA | {} | usuario={} | {} | {}-{}@{}",
                r.tipo(), r.usuario(), r.detalle(), r.topico(), r.particion(), r.offset());
    }

    public List<RegistroAuditoria> ultimos() {
        return new ArrayList<>(registros);
    }
}

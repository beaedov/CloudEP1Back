package com.pedidos360.backend.service;

import com.pedidos360.backend.exception.PedidoNoEncontradoException;
import com.pedidos360.backend.messaging.kafka.PedidoKafkaProducer;
import com.pedidos360.backend.messaging.producer.PedidoEventPublisher;
import com.pedidos360.backend.model.Pedido;
import com.pedidos360.backend.model.PedidoRequest;
import com.pedidos360.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher eventPublisher;
    private final PedidoKafkaProducer kafkaProducer;

    public PedidoService(PedidoRepository pedidoRepository, PedidoEventPublisher eventPublisher,
                         PedidoKafkaProducer kafkaProducer) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
        this.kafkaProducer = kafkaProducer;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
    }

    public Pedido crear(PedidoRequest request) {
        Pedido pedido = new Pedido(
                request.getCliente(),
                request.getProducto(),
                request.getCantidad(),
                request.getEstado()
        );
        Pedido guardado = pedidoRepository.save(pedido);
        eventPublisher.pedidoCreado(guardado);          // RabbitMQ: notificación, ticket y documento
        kafkaProducer.pedidoCreado(guardado);           // Kafka: evento de negocio (reportería y auditoría)
        return guardado;
    }

    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarPorId(id);
        pedido.setCliente(request.getCliente());
        pedido.setProducto(request.getProducto());
        pedido.setCantidad(request.getCantidad());
        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            pedido.setEstado(request.getEstado());
        }
        Pedido actualizado = pedidoRepository.save(pedido);
        eventPublisher.pedidoActualizado(actualizado);  // RabbitMQ
        kafkaProducer.pedidoActualizado(actualizado);   // Kafka
        return actualizado;
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
        eventPublisher.pedidoEliminado(pedido);         // RabbitMQ
        kafkaProducer.pedidoEliminado(pedido);          // Kafka
    }
}


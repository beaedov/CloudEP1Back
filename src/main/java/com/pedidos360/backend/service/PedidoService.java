package com.pedidos360.backend.service;

import com.pedidos360.backend.exception.PedidoNoEncontradoException;
import com.pedidos360.backend.model.Pedido;
import com.pedidos360.backend.model.PedidoRequest;
import com.pedidos360.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, PedidoRequest request) {
        Pedido pedido = buscarPorId(id);
        pedido.setCliente(request.getCliente());
        pedido.setProducto(request.getProducto());
        pedido.setCantidad(request.getCantidad());
        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            pedido.setEstado(request.getEstado());
        }
        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }
}

package com.pedidos360.backend.exception;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(Long id) {
        super("No se encontró el pedido con id " + id);
    }
}

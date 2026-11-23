package com.pedidos360.auditoria.service;

/** Payload que no cumple el contrato. NO se reintenta: va directo al DLT. */
public class EventoInvalidoException extends RuntimeException {
    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }
}

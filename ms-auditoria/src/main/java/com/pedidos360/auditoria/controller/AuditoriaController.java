package com.pedidos360.auditoria.controller;

import com.pedidos360.auditoria.dto.RegistroAuditoria;
import com.pedidos360.auditoria.service.AuditoriaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET http://localhost:8097/api/auditoria/registros  -> últimos registros (más reciente primero) */
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/registros")
    public List<RegistroAuditoria> registros() {
        return auditoriaService.ultimos();
    }
}

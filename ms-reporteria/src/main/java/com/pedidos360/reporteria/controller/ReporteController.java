package com.pedidos360.reporteria.controller;

import com.pedidos360.reporteria.service.ReporteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Consulta de métricas en tiempo real: GET http://localhost:8096/api/reportes/metricas */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/metricas")
    public Map<String, Object> metricas() {
        return reporteService.resumen();
    }
}

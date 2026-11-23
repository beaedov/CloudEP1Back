package com.pedidos360.kafkaadmin.controller;

import com.pedidos360.kafkaadmin.dto.CrearTopicoRequest;
import com.pedidos360.kafkaadmin.service.KafkaAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * API de administración de Kafka (base: http://localhost:8095/api/kafka-admin)
 *
 *  GET    /cluster                          brokers y controlador
 *  GET    /topicos                          listar tópicos
 *  GET    /topicos/{nombre}                 particiones, líder, réplicas, ISR y configuraciones
 *  POST   /topicos                          crear tópico
 *  PUT    /topicos/{nombre}/configuracion   modificar configuraciones (retention.ms, cleanup.policy...)
 *  PUT    /topicos/{nombre}/particiones     aumentar particiones  { "total": 6 }
 *  DELETE /topicos/{nombre}                 eliminar tópico
 *  GET    /grupos                           listar consumer groups
 *  GET    /grupos/{grupo}                   miembros, offsets y lag
 *  DELETE /grupos/{grupo}                   eliminar grupo (solo si está vacío)
 */
@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {

    private final KafkaAdminService service;

    public KafkaAdminController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping("/cluster")
    public Map<String, Object> cluster() throws Exception {
        return service.cluster();
    }

    // -------- Tópicos --------

    @GetMapping("/topicos")
    public Set<String> listarTopicos() throws Exception {
        return service.listarTopicos();
    }

    @GetMapping("/topicos/{nombre}")
    public Map<String, Object> describirTopico(@PathVariable String nombre) throws Exception {
        return service.describirTopico(nombre);
    }

    @PostMapping("/topicos")
    public ResponseEntity<Map<String, Object>> crearTopico(@RequestBody CrearTopicoRequest req) throws Exception {
        service.crearTopico(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.describirTopico(req.nombre()));
    }

    @PutMapping("/topicos/{nombre}/configuracion")
    public Map<String, String> modificarConfiguracion(@PathVariable String nombre,
                                                      @RequestBody Map<String, String> cambios) throws Exception {
        return service.modificarConfiguracion(nombre, cambios);
    }

    @PutMapping("/topicos/{nombre}/particiones")
    public Map<String, Object> aumentarParticiones(@PathVariable String nombre,
                                                   @RequestBody Map<String, Integer> body) throws Exception {
        Integer total = body.get("total");
        if (total == null) throw new IllegalArgumentException("envía { \"total\": N }");
        service.aumentarParticiones(nombre, total);
        return service.describirTopico(nombre);
    }

    @DeleteMapping("/topicos/{nombre}")
    public ResponseEntity<Void> eliminarTopico(@PathVariable String nombre) throws Exception {
        service.eliminarTopico(nombre);
        return ResponseEntity.noContent().build();
    }

    // -------- Consumer groups --------

    @GetMapping("/grupos")
    public List<Map<String, Object>> listarGrupos() throws Exception {
        return service.listarGrupos();
    }

    @GetMapping("/grupos/{grupo}")
    public Map<String, Object> describirGrupo(@PathVariable String grupo) throws Exception {
        return service.describirGrupo(grupo);
    }

    @DeleteMapping("/grupos/{grupo}")
    public ResponseEntity<Void> eliminarGrupo(@PathVariable String grupo) throws Exception {
        service.eliminarGrupo(grupo);
        return ResponseEntity.noContent().build();
    }
}

package com.pedidos360.kafkaadmin.service;

import com.pedidos360.kafkaadmin.dto.CrearTopicoRequest;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Operaciones de administración de Kafka usando AdminClient.
 * Todas esperan como máximo 10 s la respuesta del cluster.
 */
@Service
public class KafkaAdminService {

    private static final long TIMEOUT = 10;
    /** Configuraciones que siempre mostramos aunque tengan el valor por defecto. */
    private static final Set<String> CONFIGS_CLAVE =
            Set.of("retention.ms", "cleanup.policy", "min.insync.replicas", "segment.bytes");

    private final AdminClient admin;

    public KafkaAdminService(AdminClient admin) {
        this.admin = admin;
    }

    // ===================== CLUSTER =====================

    public Map<String, Object> cluster() throws Exception {
        DescribeClusterResult r = admin.describeCluster();
        var brokers = r.nodes().get(TIMEOUT, TimeUnit.SECONDS).stream()
                .map(n -> Map.of("id", n.id(), "host", n.host(), "puerto", n.port()))
                .toList();
        var controller = r.controller().get(TIMEOUT, TimeUnit.SECONDS);
        return Map.of(
                "clusterId", r.clusterId().get(TIMEOUT, TimeUnit.SECONDS),
                "controlador", controller.id(),
                "brokers", brokers);
    }

    // ===================== TÓPICOS =====================

    public Set<String> listarTopicos() throws Exception {
        return new TreeSet<>(admin.listTopics().names().get(TIMEOUT, TimeUnit.SECONDS));
    }

    public Map<String, Object> describirTopico(String nombre) throws Exception {
        TopicDescription d = admin.describeTopics(List.of(nombre))
                .allTopicNames().get(TIMEOUT, TimeUnit.SECONDS).get(nombre);

        // Por cada partición: líder, réplicas e ISR (réplicas sincronizadas)
        var particiones = d.partitions().stream().map(p -> Map.of(
                "particion", p.partition(),
                "lider", p.leader() != null ? p.leader().id() : -1,
                "replicas", p.replicas().stream().map(n -> n.id()).toList(),
                "isr", p.isr().stream().map(n -> n.id()).toList()
        )).toList();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("nombre", d.name());
        resp.put("cantidadParticiones", d.partitions().size());
        resp.put("factorReplicacion", d.partitions().get(0).replicas().size());
        resp.put("particiones", particiones);
        resp.put("configuraciones", configuraciones(nombre));
        return resp;
    }

    public void crearTopico(CrearTopicoRequest req) throws Exception {
        if (req.nombre() == null || req.nombre().isBlank()) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        int particiones = req.particiones() != null ? req.particiones() : 3;
        short replicas = req.replicas() != null ? req.replicas() : 3;
        if (particiones < 1 || replicas < 1) {
            throw new IllegalArgumentException("particiones y replicas deben ser >= 1");
        }
        NewTopic topico = new NewTopic(req.nombre(), particiones, replicas);
        if (req.configuraciones() != null) {
            topico.configs(req.configuraciones());
        }
        admin.createTopics(List.of(topico)).all().get(TIMEOUT, TimeUnit.SECONDS);
    }

    /** Modifica configuraciones (ej. retention.ms) sin recrear el tópico. */
    public Map<String, String> modificarConfiguracion(String nombre, Map<String, String> cambios) throws Exception {
        if (cambios == null || cambios.isEmpty()) {
            throw new IllegalArgumentException("envía al menos una configuración, ej. {\"retention.ms\":\"86400000\"}");
        }
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Collection<AlterConfigOp> ops = cambios.entrySet().stream()
                .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
                .toList();
        admin.incrementalAlterConfigs(Map.of(recurso, ops)).all().get(TIMEOUT, TimeUnit.SECONDS);
        return configuraciones(nombre);
    }

    /** Aumenta particiones (Kafka NO permite disminuirlas). */
    public void aumentarParticiones(String nombre, int total) throws Exception {
        admin.createPartitions(Map.of(nombre, NewPartitions.increaseTo(total)))
                .all().get(TIMEOUT, TimeUnit.SECONDS);
    }

    public void eliminarTopico(String nombre) throws Exception {
        admin.deleteTopics(List.of(nombre)).all().get(TIMEOUT, TimeUnit.SECONDS);
    }

    private Map<String, String> configuraciones(String nombre) throws Exception {
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = admin.describeConfigs(List.of(recurso)).all()
                .get(TIMEOUT, TimeUnit.SECONDS).get(recurso);
        // Mostramos las claves importantes + todo lo que se cambió respecto al default
        return config.entries().stream()
                .filter(e -> CONFIGS_CLAVE.contains(e.name()) || !e.isDefault())
                .collect(Collectors.toMap(ConfigEntry::name, ConfigEntry::value, (a, b) -> a, TreeMap::new));
    }

    // ===================== CONSUMER GROUPS =====================

    public List<Map<String, Object>> listarGrupos() throws Exception {
        return admin.listConsumerGroups().all().get(TIMEOUT, TimeUnit.SECONDS).stream()
                .map(g -> Map.<String, Object>of(
                        "grupo", g.groupId(),
                        "estado", g.state().map(Enum::name).orElse("DESCONOCIDO")))
                .toList();
    }

    /**
     * Estado de un grupo: miembros, particiones asignadas, offset confirmado, último offset y LAG.
     * LAG = mensajes que el grupo todavía no procesa (LOG-END-OFFSET - CURRENT-OFFSET).
     */
    public Map<String, Object> describirGrupo(String grupo) throws Exception {
        ConsumerGroupDescription desc = admin.describeConsumerGroups(List.of(grupo))
                .all().get(TIMEOUT, TimeUnit.SECONDS).get(grupo);

        var miembros = desc.members().stream().map(m -> Map.of(
                "clientId", m.clientId(),
                "host", m.host(),
                "particiones", m.assignment().topicPartitions().stream()
                        .map(TopicPartition::toString).sorted().toList()
        )).toList();

        Map<TopicPartition, OffsetAndMetadata> confirmados = admin.listConsumerGroupOffsets(grupo)
                .partitionsToOffsetAndMetadata().get(TIMEOUT, TimeUnit.SECONDS);

        Map<TopicPartition, OffsetSpec> pedirUltimos = new HashMap<>();
        confirmados.keySet().forEach(tp -> pedirUltimos.put(tp, OffsetSpec.latest()));
        var ultimos = admin.listOffsets(pedirUltimos).all().get(TIMEOUT, TimeUnit.SECONDS);

        long lagTotal = 0;
        List<Map<String, Object>> offsets = new ArrayList<>();
        for (var e : confirmados.entrySet()) {
            if (e.getValue() == null) continue;
            long actual = e.getValue().offset();
            long fin = ultimos.get(e.getKey()).offset();
            long lag = Math.max(0, fin - actual);
            lagTotal += lag;
            offsets.add(Map.of(
                    "topico", e.getKey().topic(),
                    "particion", e.getKey().partition(),
                    "currentOffset", actual,
                    "logEndOffset", fin,
                    "lag", lag));
        }
        offsets.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("topico"))
                .thenComparing(m -> (Integer) m.get("particion")));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("grupo", grupo);
        resp.put("estado", desc.state().name());
        resp.put("coordinador", desc.coordinator().id());
        resp.put("miembros", miembros);
        resp.put("offsets", offsets);
        resp.put("lagTotal", lagTotal);
        return resp;
    }

    /** Solo se puede borrar un grupo sin consumidores activos (estado EMPTY). */
    public void eliminarGrupo(String grupo) throws Exception {
        admin.deleteConsumerGroups(List.of(grupo)).all().get(TIMEOUT, TimeUnit.SECONDS);
    }
}

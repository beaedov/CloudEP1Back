#!/usr/bin/env bash
# =====================================================================
# Administración de Kafka por CLI (alternativa replicable al ms-kafka-admin)
# Uso (Git Bash, dentro de infra/kafka):
#   ./kafka-admin.sh crear       -> crea los tópicos con sus parámetros
#   ./kafka-admin.sh describir   -> particiones, réplicas, ISR y configuración
#   ./kafka-admin.sh grupos      -> consumer groups: offsets y lag
#   ./kafka-admin.sh retencion   -> ejemplo de modificar retention.ms
# =====================================================================
export MSYS_NO_PATHCONV=1
BS=kafka1:29092
K="docker compose exec kafka1"

case "$1" in
  crear)
    # Eventos de negocio: 3 particiones, 3 réplicas, 7 días, borrado por tiempo
    $K kafka-topics --bootstrap-server $BS --create --if-not-exists --topic pedidos-eventos \
      --partitions 3 --replication-factor 3 \
      --config retention.ms=604800000 --config cleanup.policy=delete --config min.insync.replicas=2
    # Métricas: 3 particiones, 3 réplicas, compactado (último valor por key)
    $K kafka-topics --bootstrap-server $BS --create --if-not-exists --topic pedidos-metricas \
      --partitions 3 --replication-factor 3 \
      --config cleanup.policy=compact --config min.insync.replicas=2
    # Dead Letter Topics: 14 días para revisar mensajes fallidos
    $K kafka-topics --bootstrap-server $BS --create --if-not-exists --topic pedidos-eventos.DLT \
      --partitions 3 --replication-factor 3 --config retention.ms=1209600000
    $K kafka-topics --bootstrap-server $BS --create --if-not-exists --topic pedidos-metricas.DLT \
      --partitions 3 --replication-factor 3 --config retention.ms=1209600000
    $K kafka-topics --bootstrap-server $BS --list
    ;;
  describir)
    $K kafka-topics --bootstrap-server $BS --describe --topic pedidos-eventos
    $K kafka-topics --bootstrap-server $BS --describe --topic pedidos-metricas
    ;;
  grupos)
    $K kafka-consumer-groups --bootstrap-server $BS --list
    $K kafka-consumer-groups --bootstrap-server $BS --describe --group reporteria-group
    $K kafka-consumer-groups --bootstrap-server $BS --describe --group auditoria-group
    ;;
  retencion)
    $K kafka-configs --bootstrap-server $BS --entity-type topics --entity-name pedidos-eventos \
      --alter --add-config retention.ms=259200000
    ;;
  *)
    echo "Uso: ./kafka-admin.sh [crear | describir | grupos | retencion]"
    ;;
esac

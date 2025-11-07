#!/bin/bash
# Script pour exporter les métriques depuis Prometheus et InfluxDB

set -e

RESULTS_DIR="${RESULTS_DIR:-results/exports}"
mkdir -p "$RESULTS_DIR"

echo "📊 Export des métriques..."

# Export depuis Prometheus (métriques JVM)
echo "Exporting JVM metrics from Prometheus..."
PROMETHEUS_URL="http://localhost:9090"

# CPU usage
curl -s "$PROMETHEUS_URL/api/v1/query?query=process_cpu_usage" > "$RESULTS_DIR/jvm_cpu.json"

# Heap memory
curl -s "$PROMETHEUS_URL/api/v1/query?query=jvm_memory_used_bytes{area=\"heap\"}" > "$RESULTS_DIR/jvm_heap.json"

# GC time
curl -s "$PROMETHEUS_URL/api/v1/query?query=rate(jvm_gc_pause_seconds_sum[1m])" > "$RESULTS_DIR/jvm_gc.json"

# Threads
curl -s "$PROMETHEUS_URL/api/v1/query?query=jvm_threads_live_threads" > "$RESULTS_DIR/jvm_threads.json"

# HikariCP
curl -s "$PROMETHEUS_URL/api/v1/query?query=hikaricp_connections_active" > "$RESULTS_DIR/hikaricp_active.json"
curl -s "$PROMETHEUS_URL/api/v1/query?query=hikaricp_connections_max" > "$RESULTS_DIR/hikaricp_max.json"

echo "✅ Métriques JVM exportées dans $RESULTS_DIR"

# Note: Pour InfluxDB, utiliser l'interface web ou l'API directement
echo "📝 Pour exporter les métriques JMeter depuis InfluxDB:"
echo "   1. Accéder à http://localhost:8086"
echo "   2. Aller dans Data Explorer"
echo "   3. Exécuter les requêtes et exporter en CSV"
echo ""
echo "Ou utiliser l'API InfluxDB:"
echo "   curl -X POST http://localhost:8086/api/v2/query \\"
echo "     -H 'Authorization: Token token' \\"
echo "     -H 'Content-Type: application/vnd.flux' \\"
echo "     -d 'from(bucket:\"jmeter\") |> range(start:-1h)'"


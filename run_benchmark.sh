#!/usr/bin/env bash
set -euo pipefail

# Configurable parameters
INFLUX_URL=${INFLUX_URL:-http://localhost:8086}
INFLUX_TOKEN=${INFLUX_TOKEN:-token}
INFLUX_ORG=${INFLUX_ORG:-perf}
INFLUX_BUCKET=${INFLUX_BUCKET:-jmeter}
JMETER_BIN=${JMETER_BIN:-jmeter}
RESULTS_DIR=${RESULTS_DIR:-results}
GIT_AUTO_PUSH=${GIT_AUTO_PUSH:-0}

# Compose service host ports (using function for bash 3.2 compatibility)
get_service_port() {
  case "$1" in
    varianta) echo "8081" ;;
    variantc) echo "8082" ;;
    variantd) echo "8083" ;;
    *) echo "8080" ;;
  esac
}

SCENARIOS=(read-heavy join-filter mixed heavy-body)

log() { echo "[$(date +'%F %T')] $*"; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "Missing required command: $1" >&2; exit 1; }
}

wait_http() {
  local url="$1" max_tries="${2:-60}"
  for i in $(seq 1 "$max_tries"); do
    if curl -fsS "$url" >/dev/null 2>&1; then return 0; fi
    sleep 2
  done
  return 1
}

build_services() {
  log "Building Java artifacts (A, C, D)"
  (cd variantA-jersey && mvn -q -DskipTests package)
  (cd variantC-springmvc && mvn -q -DskipTests package)
  (cd variantD-springdatarest && mvn -q -DskipTests package)
}

start_stack() {
  log "Starting Docker stack (database and monitoring)"
  docker compose up -d postgres influxdb prometheus grafana

  log "Building and starting REST services"
  docker compose build varianta variantc variantd
  docker compose up -d varianta variantc variantd varianta-jmx

  log "Waiting for services to be ready"
  wait_http "http://localhost:$(get_service_port variantc)/actuator/health" || true
  wait_http "http://localhost:$(get_service_port variantd)/actuator/health" || true
  wait_http "http://localhost:$(get_service_port varianta)/" || true

  log "Waiting for Prometheus and Grafana"
  wait_http "http://localhost:9090/-/ready" || true
  wait_http "http://localhost:3000/api/health" || true

  log "Waiting for InfluxDB"
  wait_http "${INFLUX_URL}/health" || true
}

run_scenarios_for_service() {
  local service="$1"
  local port=$(get_service_port "$1")
  local baseUrl="http://localhost:${port}"
  mkdir -p "${RESULTS_DIR}/${service}"
  log "Running JMeter scenarios against ${service} (${baseUrl})"
  for s in "${SCENARIOS[@]}"; do
    local jmx="jmeter/${s}.jmx" out="${RESULTS_DIR}/${service}/${s}.jtl" logf="${RESULTS_DIR}/${service}/${s}.log"
    if [[ ! -f "$jmx" ]]; then echo "Missing $jmx" >&2; exit 1; fi
    log "Executing: $s"
    "${JMETER_BIN}" -n -t "$jmx" \
      -JbaseUrl="${baseUrl}" \
      -JinfluxUrl="${INFLUX_URL}" -JinfluxToken="${INFLUX_TOKEN}" -JinfluxOrg="${INFLUX_ORG}" -JinfluxBucket="${INFLUX_BUCKET}" \
      -l "$out" > "$logf" 2>&1 || true
  done
}

summarize_results() {
  local summary="${RESULTS_DIR}/summary.md"
  mkdir -p "${RESULTS_DIR}"
  python3 - "$RESULTS_DIR" > "$summary" << 'PY'
import csv, glob, os, sys, math
from statistics import median

results_dir = sys.argv[1]

def percentiles(values, ps=(50,95,99)):
    if not values:
        return {p: float('nan') for p in ps}
    vs = sorted(values)
    out = {}
    for p in ps:
        k = (p/100)*(len(vs)-1)
        f = math.floor(k); c = math.ceil(k)
        if f == c: out[p] = vs[int(k)]
        else:
            d0 = vs[f]*(c-k)
            d1 = vs[c]*(k-f)
            out[p] = d0 + d1
    return out

print("## Benchmark Summary\n")
print("| Service | Scenario | Samples | RPS | p50(ms) | p95(ms) | p99(ms) | Errors(%) |")
print("|---|---|---:|---:|---:|---:|---:|---:|")
for service in sorted(os.listdir(results_dir)):
    service_dir = os.path.join(results_dir, service)
    if not os.path.isdir(service_dir):
        continue
    for jtl in sorted(glob.glob(os.path.join(service_dir, '*.jtl'))):
        scenario = os.path.splitext(os.path.basename(jtl))[0]
        elapsed=[]; ok=0; total=0
        start_ts=None; end_ts=None
        try:
            with open(jtl, newline='') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    try:
                        total += 1
                        if row.get('success','true').lower() in ('true','1','yes'):
                            ok += 1
                        e = float(row['elapsed'])
                        elapsed.append(e)
                        ts = int(row.get('timeStamp', '0'))
                        if ts:
                            if start_ts is None or ts < start_ts: start_ts = ts
                            if end_ts is None or ts > end_ts: end_ts = ts
                    except Exception:
                        pass
        except FileNotFoundError:
            continue
        duration_s = max(1.0, ((end_ts or 0) - (start_ts or 0))/1000.0)
        rps = ok / duration_s if duration_s > 0 else 0.0
        pct = percentiles(elapsed)
        err_pct = 0.0 if total==0 else (100.0*(total-ok)/total)
        print(f"| {service} | {scenario} | {total} | {rps:.1f} | {pct[50]:.1f} | {pct[95]:.1f} | {pct[99]:.1f} | {err_pct:.2f} |")
PY
  log "Wrote summary to ${summary}"
}

update_readme_and_commit() {
  local summary="${RESULTS_DIR}/summary.md"
  if [[ -f "$summary" ]]; then
    printf "# benchmark-rest\n\n" > README.md
    printf "This README contains the latest benchmark summary generated by run_benchmark.sh.\n\n" >> README.md
    cat "$summary" >> README.md
    log "Updated README.md with summary"
  fi

  if [[ "$GIT_AUTO_PUSH" == "1" ]]; then
    git add README.md "$summary" || true
    git -c user.name="benchmark-bot" -c user.email="bot@example.com" commit -m "chore: update README with latest benchmark summary" || true
    if git remote get-url origin >/dev/null 2>&1; then
      git push || true
    fi
  fi
}

main() {
  require_cmd docker
  require_cmd curl
  require_cmd "${JMETER_BIN}"

  mkdir -p "${RESULTS_DIR}"

  build_services
  start_stack

  for svc in varianta variantc variantd; do
    run_scenarios_for_service "$svc"
  done

  summarize_results
  update_readme_and_commit

  log "Done. See results/summary.md and README.md."
}

main "$@"

#!/usr/bin/env bash
# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.
#
# Runs the python client smoke tests against a locally launched ACL app
# using the dev profile (in-memory H2, no external services required).
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$MODULE_DIR/../../../../../.." && pwd)"

CLIENT_DIR="$MODULE_DIR/target/generated-sources/openapi"
VENV_DIR="$MODULE_DIR/target/venv"
APP_LOG="$MODULE_DIR/target/acl-app.log"
HEALTH_URL="http://localhost:8081/actuator/health"

jar_candidates=("$ROOT_DIR"/src/infrastructure/app-main/target/gs-acl-app-*-bin.jar)
APP_JAR="${jar_candidates[0]}"

if [ ! -f "$APP_JAR" ] || [ ! -f "$CLIENT_DIR/pyproject.toml" ]; then
  echo "Missing build artifacts. Run 'make package' first." >&2
  exit 1
fi

python3 -m venv "$VENV_DIR"
"$VENV_DIR/bin/pip" install --quiet "$CLIENT_DIR" pytest

java -jar "$APP_JAR" --spring.profiles.active=dev >"$APP_LOG" 2>&1 &
APP_PID=$!
trap 'kill "$APP_PID" 2>/dev/null || true; wait "$APP_PID" 2>/dev/null || true' EXIT

echo "Waiting for the ACL app to become healthy at $HEALTH_URL ..."
healthy=no
for _ in $(seq 1 90); do
  if curl -fsS "$HEALTH_URL" >/dev/null 2>&1; then
    healthy=yes
    break
  fi
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    break
  fi
  sleep 1
done

if [ "$healthy" != "yes" ]; then
  echo "The ACL app did not become healthy in time. Last log lines:" >&2
  tail -50 "$APP_LOG" >&2
  exit 1
fi

"$VENV_DIR/bin/python" -m pytest "$MODULE_DIR/tests" -v --color=yes

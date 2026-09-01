#!/usr/bin/env bash
# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.
#
# Runs the python client example against the locally built wheel and
# docker image. Consumers following the example instead install the
# published packages per requirements.txt and README.md.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

DIST_DIR="$ROOT_DIR/src/infrastructure/web-api/v1/clients/python/target/dist"
VENV_DIR="$SCRIPT_DIR/.venv"

wheel_candidates=("$DIST_DIR"/geoserver_acl_client-*.whl)
WHEEL="${wheel_candidates[0]}"

if [ ! -f "$WHEEL" ]; then
  echo "Missing python client wheel. Run 'make dist-python-client' first." >&2
  exit 1
fi

python3 -m venv "$VENV_DIR"
"$VENV_DIR/bin/pip" install --quiet "$WHEEL" pytest testcontainers

"$VENV_DIR/bin/python" -m pytest "$SCRIPT_DIR" -v --color=yes

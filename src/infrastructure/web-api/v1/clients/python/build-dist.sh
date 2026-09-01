#!/usr/bin/env bash
# (c) 2026 Open Source Geospatial Foundation - all rights reserved
# This code is licensed under the GPL 2.0 license, available at the root
# application directory.
#
# Builds the sdist and wheel for the generated python client into target/dist.
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "$0")" && pwd)"
CLIENT_DIR="$MODULE_DIR/target/generated-sources/openapi"
VENV_DIR="$MODULE_DIR/target/venv-build"
DIST_DIR="$MODULE_DIR/target/dist"

if [ ! -f "$CLIENT_DIR/pyproject.toml" ]; then
  echo "Missing generated client. Run 'make package' first." >&2
  exit 1
fi

python3 -m venv "$VENV_DIR"
"$VENV_DIR/bin/pip" install --quiet build

rm -rf "$DIST_DIR"
"$VENV_DIR/bin/python" -m build --outdir "$DIST_DIR" "$CLIENT_DIR"
ls -l "$DIST_DIR"

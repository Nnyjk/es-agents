#!/usr/bin/env bash
#
# es-agents 开发环境快捷脚本
# 等同于 dev-start.sh 的快捷方式
#

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
exec "$ROOT_DIR/scripts/dev-start.sh" "$@"
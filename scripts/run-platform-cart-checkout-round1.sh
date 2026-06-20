#!/usr/bin/env bash
# 平台购物车/checkout Round1 验收 Runner（需本地 spring-jdbc 可连库）
set -euo pipefail
cd "$(dirname "$0")/.."
mvn -q test-compile exec:java \
  -Dexec.mainClass=com.nongxinle.platform.PlatformCartCheckoutRound1Runner

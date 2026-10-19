#!/bin/bash
# Sonic Docker 镜像导出脚本
# 用法: ./export-images.sh <版本号> [跳过的镜像...]
# 示例:
#   ./export-images.sh v1.0.2
#   ./export-images.sh v1.0.2 sonic-server-eureka sonic-server-gateway

set -e

if [ -z "$1" ]; then
  echo "用法: $0 <版本号> [跳过的镜像...]"
  echo "示例:"
  echo "  $0 v1.0.2"
  echo "  $0 v1.0.2 sonic-server-eureka sonic-server-gateway"
  exit 1
fi

VERSION="$1"
shift
SKIP=("$@")

# 所有镜像列表
IMAGES=(
  sonic-server-controller
  sonic-server-eureka
  sonic-server-folder
  sonic-server-gateway
  sonic-client-web
)

# 输出目录
OUTPUT_DIR="."

echo "========================================"
echo " Sonic Docker 镜像导出"
echo " 版本: ${VERSION}"
echo "========================================"

for IMAGE in "${IMAGES[@]}"; do
  # 检查是否跳过
  SKIP_FLAG=false
  for S in "${SKIP[@]}"; do
    if [ "${IMAGE}" = "${S}" ]; then
      SKIP_FLAG=true
      break
    fi
  done

  if [ "${SKIP_FLAG}" = true ]; then
    echo ">>> 跳过: ${IMAGE}"
    continue
  fi

  FULL_IMAGE="tg/${IMAGE}:${VERSION}"
  OUTPUT_FILE="${OUTPUT_DIR}/tg-${IMAGE}-${VERSION}.tar"

  echo ">>> 导出: ${FULL_IMAGE} -> ${OUTPUT_FILE}"
  docker save -o "${OUTPUT_FILE}" "${FULL_IMAGE}"
  echo "    [完成] ${OUTPUT_FILE}"
done

echo ""
echo "========================================"
echo " 导出完成"
echo "========================================"

# 列出导出文件
ls -lh ${OUTPUT_DIR}/tg-sonic-*-${VERSION}.tar 2>/dev/null

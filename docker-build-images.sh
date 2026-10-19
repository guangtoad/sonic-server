#!/bin/bash
# Sonic Server Docker 镜像批量构建脚本
# 用法: ./build-images.sh <版本号>
# 示例: ./build-images.sh v1.0.2

set -e

# 版本号参数（必填）
if [ -z "$1" ]; then
  echo "用法: $0 <版本号>"
  echo "示例: $0 v1.0.2"
  exit 1
fi

VERSION="$1"
PREFIX="tg"

MODULES=(
  sonic-server-controller
  sonic-server-eureka
  sonic-server-folder
  sonic-server-gateway
)

echo "========================================"
echo " Sonic Server Docker 镜像构建"
echo " 版本: ${VERSION}"
echo " 镜像数: ${#MODULES[@]}"
echo "========================================"

for MODULE in "${MODULES[@]}"; do
  DOCKERFILE="./${MODULE}/src/main/docker/Dockerfile"
  IMAGE="${PREFIX}/${MODULE}:${VERSION}"

  echo ""
  echo ">>> 构建镜像: ${IMAGE}"

  if [ ! -f "${DOCKERFILE}" ]; then
    echo "    [错误] Dockerfile 不存在: ${DOCKERFILE}"
    exit 1
  fi

  docker build -f "${DOCKERFILE}" . -t "${IMAGE}"
  echo "    [完成] ${IMAGE}"
done

echo ""
echo "========================================"
echo " 全部构建完成 (${#MODULES[@]} 个镜像)"
echo "========================================"

docker images | grep "${PREFIX}/sonic-server" | grep "${VERSION}"
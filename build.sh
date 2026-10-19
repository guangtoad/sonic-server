#!/bin/bash
# Sonic Server Docker 镜像批量构建脚本 (增强日志版)
# 用法: ./build-images.sh <版本号>
# 示例: ./build-images.sh v1.0.2

# 开启严格模式：遇到错误立即退出，未定义变量报错，管道错误传递
set -euo pipefail

# --- 辅助函数：带时间戳的日志输出 ---
log_info() {
    echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo "[ERROR] $(date '+%Y-%m-%d %H:%M:%S') - $1" >&2
}

log_success() {
    echo "[SUCCESS] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# --- 1. 参数校验 ---
if [ -z "${1:-}" ]; then
    log_error "缺少版本号参数！"
    echo "用法: $0 <版本号>"
    echo "示例: $0 v1.0.2"
    exit 1
fi

VERSION="$1"
PREFIX="tg"

# --- 2. 项目清理与构建 ---
log_info "开始清理并构建 Maven 项目 (mvn clean package)..."
# 注意：原脚本 'mvn clean && package' 语法有误，已修正为 'mvn clean package'
# 增加 -q (quiet) 减少非必要的 Maven 刷屏日志，若需查看详细构建日志可去掉 -q
if mvn clean package -q -DskipTests; then
    log_success "Maven 构建成功。"
else
    log_error "Maven 构建失败！请检查控制台输出。"
    exit 1
fi

# --- 3. 模块配置 ---
MODULES=(
    sonic-server-controller
    sonic-server-eureka
    sonic-server-folder
    sonic-server-gateway
)

log_info "========================================"
log_info "Sonic Server Docker 镜像构建任务启动"
log_info "版本: ${VERSION}"
log_info "镜像前缀: ${PREFIX}"
log_info "待构建模块数: ${#MODULES[@]}"
log_info "========================================"

# --- 4. 循环构建 Docker 镜像 ---
SUCCESS_COUNT=0
FAIL_COUNT=0

for MODULE in "${MODULES[@]}"; do
    DOCKERFILE="./${MODULE}/src/main/docker/Dockerfile"
    IMAGE="${PREFIX}/${MODULE}:${VERSION}"

    echo ""
    log_info ">>> 正在处理模块: ${MODULE}"

    # 检查 Dockerfile 是否存在
    if [ ! -f "${DOCKERFILE}" ]; then
        log_error "Dockerfile 不存在: ${DOCKERFILE}，跳过该模块。"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        continue
    fi

    log_info "开始构建镜像: ${IMAGE}"
    log_info "执行命令: docker build -f ${DOCKERFILE} . -t ${IMAGE}"
    
    # 执行构建并捕获输出
    if docker build -f "${DOCKERFILE}" . -t "${IMAGE}" 2>&1; then
        log_success "镜像构建成功: ${IMAGE}"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        log_error "镜像构建失败: ${IMAGE} (退出码: $?)"
        FAIL_COUNT=$((FAIL_COUNT + 1))
        # 如果希望一个模块构建失败就立即停止整个脚本，请取消下面这行的注释
        # exit 1
    fi
done

# --- 5. 构建结果汇总 ---
echo ""
log_info "========================================"
log_info "构建任务结束"
log_info "成功: ${SUCCESS_COUNT} | 失败: ${FAIL_COUNT}"
log_info "========================================"

if [ ${FAIL_COUNT} -gt 0 ]; then
    log_error "存在构建失败的模块，请检查上方日志！"
fi

# --- 6. 展示构建结果 ---
if [ ${SUCCESS_COUNT} -gt 0 ]; then
    log_info "当前系统存在的目标镜像列表："
    # 增加 || true 防止 grep 没找到匹配项时导致脚本因 set -e 报错退出
    docker images | grep "${PREFIX}/sonic-server" | grep "${VERSION}" || true
else
    log_info "没有成功构建任何镜像。"
fi
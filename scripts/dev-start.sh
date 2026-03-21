#!/usr/bin/env bash
#
# es-agents 本地开发环境一键启动脚本
# 支持启动/停止前端、服务端、Host Agent 三个服务
#

set -euo pipefail

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
PID_DIR="$ROOT_DIR/.dev-pids"
LOG_DIR="$ROOT_DIR/.dev-logs"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志前缀
PREFIX_SERVER="${CYAN}[SERVER]${NC}"
PREFIX_FRONTEND="${GREEN}[FRONTEND]${NC}"
PREFIX_AGENT="${MAGENTA}[AGENT]${NC}"

# 打印带颜色的消息
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 检查依赖
check_dependencies() {
    local missing=()
    
    log_info "检查依赖..."
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        missing+=("Java 21.x")
    else
        java_version=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$java_version" -lt 21 ]; then
            missing+=("Java 21.x (当前版本: $java_version)")
        fi
    fi
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        missing+=("Node.js 20.x")
    else
        node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
        if [ "$node_version" -lt 20 ]; then
            missing+=("Node.js 20.x (当前版本: $node_version)")
        fi
    fi
    
    # 检查 Go
    if ! command -v go &> /dev/null; then
        missing+=("Go 1.23+")
    else
        go_version=$(go version | grep -oP 'go\K[0-9.]+' | cut -d'.' -f1,2)
        log_info "Go 版本: $go_version"
    fi
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        missing+=("Maven 3.8+")
    fi
    
    if [ ${#missing[@]} -ne 0 ]; then
        log_error "缺少以下依赖:"
        for dep in "${missing[@]}"; do
            echo "  - $dep"
        done
        echo ""
        echo "请参考 docs/07-development/LOCAL-DEV-ENV.md 安装依赖"
        exit 1
    fi
    
    log_success "所有依赖已安装"
}

# 创建目录
setup_dirs() {
    mkdir -p "$PID_DIR" "$LOG_DIR"
}

# 启动服务端
start_server() {
    log_info "启动服务端..."
    
    local pid_file="$PID_DIR/server.pid"
    local log_file="$LOG_DIR/server.log"
    
    if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
        log_info "服务端已在运行 (PID: $(cat "$pid_file"))"
        return
    fi
    
    cd "$ROOT_DIR/server"
    
    export DB_URL="${DB_URL:-jdbc:postgresql://127.0.0.1:5432/easy_station}"
    export DB_USER="${DB_USER:-postgres}"
    export DB_PASSWORD="${DB_PASSWORD:-postgres}"
    
    nohup mvn quarkus:dev > "$log_file" 2>&1 &
    echo $! > "$pid_file"
    
    log_success "服务端已启动 (PID: $(cat "$pid_file"))"
}

# 启动前端
start_frontend() {
    log_info "启动前端..."
    
    local pid_file="$PID_DIR/frontend.pid"
    local log_file="$LOG_DIR/frontend.log"
    
    if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
        log_info "前端已在运行 (PID: $(cat "$pid_file"))"
        return
    fi
    
    cd "$ROOT_DIR/frontend"
    
    if [ ! -d node_modules ]; then
        log_info "安装前端依赖..."
        npm ci --legacy-peer-deps
    fi
    
    nohup npm run dev -- --host 0.0.0.0 > "$log_file" 2>&1 &
    echo $! > "$pid_file"
    
    log_success "前端已启动 (PID: $(cat "$pid_file"))"
}

# 启动 Host Agent
start_agent() {
    log_info "启动 Host Agent..."
    
    local pid_file="$PID_DIR/agent.pid"
    local log_file="$LOG_DIR/agent.log"
    local config_path="$ROOT_DIR/agent/.dev/config.yaml"
    
    if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
        log_info "Host Agent 已在运行 (PID: $(cat "$pid_file"))"
        return
    fi
    
    if [ ! -f "$config_path" ]; then
        log_info "生成 Host Agent 配置..."
        "$ROOT_DIR/scripts/dev-host-agent-config.sh" "$config_path"
    fi
    
    cd "$ROOT_DIR/agent"
    
    nohup go run ./cmd/host-agent --config "$config_path" > "$log_file" 2>&1 &
    echo $! > "$pid_file"
    
    log_success "Host Agent 已启动 (PID: $(cat "$pid_file"))"
}

# 停止所有服务
stop_all() {
    log_info "停止所有服务..."
    
    for service in server frontend agent; do
        local pid_file="$PID_DIR/${service}.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                kill "$pid" 2>/dev/null || true
                log_info "已停止 $service (PID: $pid)"
            fi
            rm -f "$pid_file"
        fi
    done
    
    log_success "所有服务已停止"
}

# 查看日志
view_logs() {
    log_info "实时查看日志 (Ctrl+C 退出)..."
    
    tail -f "$LOG_DIR"/*.log 2>/dev/null | while IFS= read -r line; do
        if echo "$line" | grep -q "server.log"; then
            echo -e "$PREFIX_SERVER $line"
        elif echo "$line" | grep -q "frontend.log"; then
            echo -e "$PREFIX_FRONTEND $line"
        elif echo "$line" | grep -q "agent.log"; then
            echo -e "$PREFIX_AGENT $line"
        else
            echo "$line"
        fi
    done
}

# 查看状态
show_status() {
    echo ""
    echo "服务状态:"
    echo "----------------------------------------"
    
    for service in server frontend agent; do
        local pid_file="$PID_DIR/${service}.pid"
        local name=""
        local port=""
        
        case $service in
            server) name="服务端"; port=":8080" ;;
            frontend) name="前端"; port=":5173" ;;
            agent) name="Host Agent"; port=":9090" ;;
        esac
        
        if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
            echo -e "  ${GREEN}●${NC} $name ${port} (PID: $(cat "$pid_file"))"
        else
            echo -e "  ${RED}○${NC} $name ${port} (未运行)"
        fi
    done
    
    echo "----------------------------------------"
    echo ""
}

# 显示帮助
show_help() {
    echo "es-agents 本地开发环境管理脚本"
    echo ""
    echo "用法: $0 <命令>"
    echo ""
    echo "命令:"
    echo "  start    启动所有服务"
    echo "  stop     停止所有服务"
    echo "  restart  重启所有服务"
    echo "  status   查看服务状态"
    echo "  logs     实时查看日志"
    echo "  help     显示帮助信息"
    echo ""
    echo "环境变量:"
    echo "  DB_URL      数据库连接 URL (默认: jdbc:postgresql://127.0.0.1:5432/easy_station)"
    echo "  DB_USER     数据库用户名 (默认: postgres)"
    echo "  DB_PASSWORD 数据库密码 (默认: postgres)"
}

# 主函数
main() {
    local command="${1:-help}"
    
    case $command in
        start)
            setup_dirs
            check_dependencies
            start_server
            start_frontend
            start_agent
            show_status
            log_success "开发环境启动完成!"
            echo ""
            echo "访问地址:"
            echo "  前端:    http://localhost:5173"
            echo "  服务端:  http://localhost:8080"
            echo "  API文档: http://localhost:8080/q/swagger-ui"
            echo ""
            echo "使用 '$0 logs' 查看实时日志"
            echo "使用 '$0 stop' 停止所有服务"
            ;;
        stop)
            stop_all
            ;;
        restart)
            stop_all
            sleep 2
            setup_dirs
            check_dependencies
            start_server
            start_frontend
            start_agent
            show_status
            ;;
        status)
            show_status
            ;;
        logs)
            view_logs
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
#!/bin/bash

# ============================================
# Notebook 项目部署脚本
# ============================================

set -e

# 配置
APP_NAME="notebook"
BACKEND_JAR="notebook-server-1.0.0.jar"
BACKEND_PORT=8080
NGINX_CONF="nginx.conf"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为 root 用户
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_warn "建议使用 root 用户运行此脚本"
    fi
}

# 检查 Java
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装，请先安装 JDK 17+"
        exit 1
    fi
    java -version
}

# 检查 Maven
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装"
        exit 1
    fi
}

# 停止旧服务
stop_old_service() {
    log_info "停止旧服务..."

    # 停止 Spring Boot 应用
    if pgrep -f "$BACKEND_JAR" > /dev/null; then
        log_info "停止后端服务..."
        pkill -f "$BACKEND_JAR" || true
        sleep 2
    fi

    # 停止 Nginx
    if command -v nginx &> /dev/null; then
        log_info "停止 Nginx..."
        nginx -s stop 2>/dev/null || systemctl stop nginx 2>/dev/null || true
    fi
}

# 部署后端
deploy_backend() {
    log_info "部署后端服务..."

    # 复制 JAR 文件
    cp ../server/target/$BACKEND_JAR /opt/notebook/

    # 设置权限
    chmod 755 /opt/notebook/$BACKEND_JAR

    # 创建启动脚本
    cat > /opt/notebook/start-backend.sh << EOF
#!/bin/bash
export JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=utf-8"
export SPRING_PROFILES_ACTIVE=prod
java \$JAVA_OPTS -jar /opt/notebook/$BACKEND_JAR --server.port=$BACKEND_PORT
EOF
    chmod +x /opt/notebook/start-backend.sh

    # 启动后端
    log_info "启动后端服务..."
    nohup /opt/notebook/start-backend.sh > /var/log/notebook-backend.log 2>&1 &

    # 等待服务启动
    sleep 5

    # 检查服务是否启动成功
    if curl -s http://127.0.0.1:$BACKEND_PORT/api/ > /dev/null 2>&1 || curl -s http://127.0.0.1:$BACKEND_PORT/ > /dev/null 2>&1; then
        log_info "后端服务启动成功"
    else
        log_warn "后端服务可能未正常启动，请检查日志"
    fi
}

# 部署前端
deploy_frontend() {
    log_info "部署前端静态文件..."

    # 创建目录
    mkdir -p /var/www/notebook-web
    mkdir -p /data/notebook/uploads

    # 复制前端文件
    rm -rf /var/www/notebook-web/dist
    cp -r ../web/dist /var/www/notebook-web/

    # 设置权限
    chmod -R 755 /var/www/notebook-web
    chmod -R 755 /data/notebook/uploads
}

# 配置 Nginx
config_nginx() {
    log_info "配置 Nginx..."

    # 复制 Nginx 配置
    cp $NGINX_CONF /etc/nginx/sites-available/notebook
    ln -sf /etc/nginx/sites-available/notebook /etc/nginx/sites-enabled/notebook

    # 测试 Nginx 配置
    if nginx -t; then
        log_info "Nginx 配置正确"
    else
        log_error "Nginx 配置错误"
        exit 1
    fi

    # 启动 Nginx
    nginx -s reload || systemctl restart nginx
}

# 主函数
main() {
    log_info "开始部署 Notebook 项目..."
    echo ""

    check_root
    check_java

    stop_old_service
    deploy_backend
    deploy_frontend
    config_nginx

    echo ""
    log_info "========================================"
    log_info "部署完成！"
    log_info "========================================"
    log_info "前端地址: http://your-server-ip/"
    log_info "后端地址: http://your-server-ip/api/"
    log_info ""
    log_info "日志位置:"
    log_info "  后端日志: /var/log/notebook-backend.log"
    log_info ""
    log_info "常用命令:"
    log_info "  查看后端日志: tail -f /var/log/notebook-backend.log"
    log_info "  重启后端: /opt/notebook/start-backend.sh"
    log_info "  重载 Nginx: nginx -s reload"
}

main "$@"

#!/bin/bash

# Java环境检查工具构建脚本
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "===== 开始构建Java环境检查工具 ====="

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven"
    exit 1
fi

# 检查Java版本
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "使用Java版本: $java_version"

# 构建项目
cd "$PROJECT_ROOT"
echo "正在构建项目..."
mvn clean package -DskipTests

# 检查构建结果
if [ -f "$PROJECT_ROOT/target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    echo "===== 构建成功! ====="
    echo "可执行文件: $PROJECT_ROOT/target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar"
    echo "使用方法: java -jar target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar -p <项目路径> -v"
else
    echo "===== 构建失败! ====="
    exit 1
fi 
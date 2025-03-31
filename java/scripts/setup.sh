#!/bin/bash
set -e
echo "Java环境设置脚本启动..."

# 检查SDKMAN是否安装
if ! command -v sdk &> /dev/null; then
    echo "安装SDKMAN..."
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
else
    echo "SDKMAN已安装，跳过..."
fi

echo "安装JDK和Maven..."
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17-open
sdk install maven 3.8.7

echo "安装完成！"

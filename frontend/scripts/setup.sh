#!/bin/bash

# 前端环境检查工具安装脚本
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

NODE_VERSION="18.16.1"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
  case $1 in
    --node-version)
      NODE_VERSION="$2"
      shift 2
      ;;
    *)
      echo "未知参数: $1"
      exit 1
      ;;
  esac
done

echo "===== 开始安装前端环境检查工具 ====="
echo "Node.js版本: $NODE_VERSION"

# 检查Node.js是否已安装
if ! command -v node &> /dev/null; then
    echo "Node.js未安装，尝试安装..."
    
    # 检查nvm是否安装
    if ! command -v nvm &> /dev/null; then
        echo "nvm未安装，尝试安装nvm..."
        curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash
        
        # 加载nvm
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    fi
    
    # 安装指定版本的Node.js
    nvm install "$NODE_VERSION"
    nvm use "$NODE_VERSION"
else
    current_version=$(node -v | cut -d 'v' -f 2)
    echo "当前Node.js版本: $current_version"
    
    # 检查版本是否符合要求
    if [[ "$(printf '%s\n' "$NODE_VERSION" "$current_version" | sort -V | head -n1)" != "$NODE_VERSION" ]]; then
        echo "当前Node.js版本低于要求，尝试安装新版本..."
        
        # 检查nvm是否安装
        if command -v nvm &> /dev/null; then
            nvm install "$NODE_VERSION"
            nvm use "$NODE_VERSION"
        else
            echo "警告: 未找到nvm，无法自动切换Node.js版本"
            echo "请手动安装Node.js $NODE_VERSION 或更高版本"
        fi
    fi
fi

# 进入项目目录
cd "$PROJECT_ROOT"

# 安装依赖
echo "安装项目依赖..."
npm install

# 更新.nvmrc文件
echo "$NODE_VERSION" > "$PROJECT_ROOT/.nvmrc"
echo "已更新.nvmrc文件"

# 检查package.json中是否包含所需依赖
if ! grep -q "commander" package.json; then
    echo "添加commander依赖..."
    npm install --save commander
fi

if ! grep -q "chalk" package.json; then
    echo "添加chalk依赖..."
    npm install --save chalk
fi

# 修改脚本权限
chmod +x "$SCRIPT_DIR/check.js"
echo "已设置check.js为可执行文件"

echo "===== 前端环境检查工具安装完成 ====="
echo "使用方法: node $SCRIPT_DIR/check.js -p <前端项目路径> -v [-m]"

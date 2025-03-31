#!/bin/bash

# Python环境自动化设置脚本
# 用于配置pyenv和Poetry环境

set -e

echo "开始设置Python环境..."

# 检查操作系统
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    echo "检测到macOS系统"
    
    # 安装Homebrew（如果未安装）
    if ! command -v brew &> /dev/null; then
        echo "安装Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        
        # 确保Homebrew在PATH中可用
        if [[ -f /opt/homebrew/bin/brew ]]; then
            echo "配置Homebrew环境..."
            eval "$(/opt/homebrew/bin/brew shellenv)"
            # 添加到shell配置，但不覆盖现有内容
            if ! grep -q "eval \"\$(/opt/homebrew/bin/brew shellenv)\"" ~/.zshrc 2>/dev/null; then
                echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
                echo "已将Homebrew添加到~/.zshrc"
            fi
            if ! grep -q "eval \"\$(/opt/homebrew/bin/brew shellenv)\"" ~/.bash_profile 2>/dev/null; then
                echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.bash_profile
                echo "已将Homebrew添加到~/.bash_profile"
            fi
        elif [[ -f /usr/local/bin/brew ]]; then
            echo "配置Homebrew环境..."
            eval "$(/usr/local/bin/brew shellenv)"
            # 添加到shell配置
            if ! grep -q "eval \"\$(/usr/local/bin/brew shellenv)\"" ~/.zshrc 2>/dev/null; then
                echo 'eval "$(/usr/local/bin/brew shellenv)"' >> ~/.zshrc
                echo "已将Homebrew添加到~/.zshrc"
            fi
            if ! grep -q "eval \"\$(/usr/local/bin/brew shellenv)\"" ~/.bash_profile 2>/dev/null; then
                echo 'eval "$(/usr/local/bin/brew shellenv)"' >> ~/.bash_profile
                echo "已将Homebrew添加到~/.bash_profile"
            fi
        else
            echo "警告: 无法找到brew命令。请手动将Homebrew添加到PATH后重新运行此脚本。"
            exit 1
        fi
    fi
    
    # 使用Homebrew安装pyenv
    echo "安装pyenv..."
    brew install pyenv
    
    # 配置pyenv
    if ! grep -q "pyenv init" ~/.zshrc 2>/dev/null; then
        echo '# pyenv配置' >> ~/.zshrc
        echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.zshrc
        echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.zshrc
        echo 'eval "$(pyenv init --path)"' >> ~/.zshrc
        echo 'eval "$(pyenv init -)"' >> ~/.zshrc
        echo "已将pyenv配置添加到~/.zshrc"
    fi
    
    # 临时添加pyenv到PATH
    export PYENV_ROOT="$HOME/.pyenv"
    export PATH="$PYENV_ROOT/bin:$PATH"
    eval "$(pyenv init --path)" || true
    eval "$(pyenv init -)" || true
    
    # 安装Poetry
    echo "安装Poetry..."
    brew install poetry
    
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    echo "检测到Linux系统"
    
    # 安装pyenv依赖
    echo "安装pyenv依赖..."
    sudo apt-get update
    sudo apt-get install -y make build-essential libssl-dev zlib1g-dev \
    libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm \
    libncursesw5-dev xz-utils tk-dev libxml2-dev libxmlsec1-dev libffi-dev liblzma-dev
    
    # 安装pyenv
    echo "安装pyenv..."
    curl https://pyenv.run | bash
    
    # 添加pyenv到PATH
    if ! grep -q "pyenv init" ~/.bashrc 2>/dev/null; then
        echo '# pyenv配置' >> ~/.bashrc
        echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.bashrc
        echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.bashrc
        echo 'eval "$(pyenv init --path)"' >> ~/.bashrc
        echo 'eval "$(pyenv init -)"' >> ~/.bashrc
        echo "已将pyenv配置添加到~/.bashrc"
    fi
    
    # 临时添加到当前会话
    export PYENV_ROOT="$HOME/.pyenv"
    export PATH="$PYENV_ROOT/bin:$PATH"
    eval "$(pyenv init --path)" || true
    eval "$(pyenv init -)" || true
    
    # 安装Poetry
    echo "安装Poetry..."
    curl -sSL https://install.python-poetry.org | python3 -
    
    # 添加Poetry到PATH
    if ! grep -q '$HOME/.local/bin' ~/.bashrc 2>/dev/null; then
        echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
        echo "已将Poetry添加到~/.bashrc"
    fi
    export PATH="$HOME/.local/bin:$PATH"
else
    echo "不支持的操作系统: $OSTYPE"
    exit 1
fi

# 检查pyenv是否可用
if ! command -v pyenv &> /dev/null; then
    echo "警告: pyenv安装可能未完成。请检查上述输出中的错误信息，并手动完成pyenv安装。"
    echo "然后重新运行此脚本。"
    exit 1
fi

# 使用pyenv安装Python 3.11
echo "安装Python 3.11..."
pyenv install 3.11 -s || echo "警告: Python 3.11安装失败，请查看错误信息"

# 设置全局Python版本
echo "设置Python 3.11为默认版本..."
pyenv global 3.11 || echo "警告: 设置Python 3.11为默认版本失败"

# 验证安装
echo "验证Python版本..."
python --version || echo "警告: 无法获取Python版本"
poetry --version || echo "警告: Poetry可能未正确安装"

# 配置Poetry使用pyenv Python
echo "配置Poetry..."
poetry config virtualenvs.in-project true || echo "警告: Poetry配置失败"

echo "设置完成！"
echo "请关闭并重新打开终端，或运行以下命令使更改立即生效:"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "  source ~/.zshrc"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "  source ~/.bashrc"
fi 
#!/usr/bin/env python3
"""
Python环境检查脚本
用于检查系统中的Python版本和环境管理工具
"""

import os
import platform
import shutil
import subprocess
import sys
from pathlib import Path

def print_header(title):
    """打印带格式的标题"""
    print("\n" + "=" * 60)
    print(f" {title}")
    print("=" * 60)

def run_command(cmd, display=True):
    """运行命令并返回输出"""
    try:
        result = subprocess.run(
            cmd, 
            shell=True, 
            check=False,
            capture_output=True,
            text=True
        )
        output = result.stdout.strip()
        if display and output:
            print(output)
        return output
    except Exception as e:
        return f"错误: {e}"

def check_system_info():
    """检查系统信息"""
    print_header("系统信息")
    print(f"操作系统: {platform.system()} {platform.release()}")
    print(f"系统版本: {platform.version()}")
    print(f"Python解释器: {sys.executable}")
    print(f"Python版本: {platform.python_version()}")

def check_python_versions():
    """检查系统中安装的Python版本"""
    print_header("系统中的Python版本")
    
    # 检查系统Python
    print("系统Python路径:")
    run_command("which python python3 python3.8 python3.9 python3.10 python3.11 python3.12 2>/dev/null || echo '未找到'")
    
    # 检查pyenv
    print("\npyenv状态:")
    if shutil.which("pyenv"):
        run_command("pyenv versions")
    else:
        print("未安装pyenv")

def check_dependency_tools():
    """检查依赖管理工具"""
    print_header("依赖管理工具")
    
    tools = {
        "pip": "pip --version",
        "pipenv": "pipenv --version",
        "poetry": "poetry --version",
        "conda": "conda --version",
        "pdm": "pdm --version"
    }
    
    for tool, cmd in tools.items():
        version = run_command(cmd, display=False) if shutil.which(tool.split()[0]) else "未安装"
        print(f"{tool}: {version}")

def check_virtual_envs():
    """检查虚拟环境"""
    print_header("虚拟环境管理")
    
    # 检查venv/virtualenv
    print("标准库venv:", "可用" if hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix else "未使用")
    
    # 检查当前环境
    if os.environ.get('VIRTUAL_ENV'):
        print(f"当前活动的虚拟环境: {os.environ['VIRTUAL_ENV']}")
    else:
        print("当前没有活动的虚拟环境")
    
    # 检查Conda环境
    if shutil.which("conda"):
        print("\nConda环境:")
        run_command("conda env list")

def check_project_files():
    """检查项目中的依赖文件"""
    print_header("项目依赖文件")
    
    files_to_check = [
        "requirements.txt",
        "pyproject.toml",
        "Pipfile",
        "Pipfile.lock",
        "poetry.lock",
        "environment.yml",
        "setup.py"
    ]
    
    found_files = []
    for file in files_to_check:
        if Path(file).exists():
            found_files.append(file)
    
    if found_files:
        print("找到以下依赖文件:")
        for file in found_files:
            print(f"- {file}")
    else:
        print("未找到任何依赖管理文件")

def main():
    """主函数"""
    print("Python环境检查工具")
    print("此工具将收集系统中的Python环境信息\n")
    
    check_system_info()
    check_python_versions()
    check_dependency_tools()
    check_virtual_envs()
    check_project_files()
    
    print_header("检查完成")
    print("建议使用pyenv + Poetry进行环境管理")
    print("运行 ./scripts/setup.sh 安装推荐的环境管理工具")

if __name__ == "__main__":
    main() 
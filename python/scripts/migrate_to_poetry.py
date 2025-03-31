#!/usr/bin/env python3
"""
迁移辅助脚本：将requirements.txt转换为Poetry依赖格式
使用方法: python migrate_to_poetry.py <requirements_file>
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path


def parse_requirements(file_path):
    """从requirements.txt文件中解析依赖"""
    dependencies = []
    with open(file_path, 'r') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            
            # 处理 -r 包含其他文件的情况
            if line.startswith('-r '):
                included_file = line[3:].strip()
                included_path = Path(file_path).parent / included_file
                dependencies.extend(parse_requirements(included_path))
                continue
            
            # 移除注释部分
            if '#' in line:
                line = line.split('#')[0].strip()
            
            # 跳过-e editable安装
            if line.startswith('-e '):
                print(f"警告: 跳过可编辑安装: {line}")
                continue
            
            # 跳过其他参数
            if line.startswith('-'):
                print(f"警告: 跳过不支持的参数: {line}")
                continue
            
            dependencies.append(line)
    
    return dependencies


def is_poetry_initialized():
    """检查项目是否已初始化Poetry"""
    return Path('pyproject.toml').exists()


def init_poetry_if_needed():
    """如果需要，初始化Poetry项目"""
    if not is_poetry_initialized():
        print("初始化Poetry项目...")
        subprocess.run(['poetry', 'init', '--no-interaction'], check=True)


def add_dependencies(dependencies):
    """添加依赖到Poetry项目"""
    success_count = 0
    failed_deps = []
    
    for dep in dependencies:
        print(f"添加依赖: {dep}")
        try:
            result = subprocess.run(
                ['poetry', 'add', dep],
                capture_output=True,
                text=True,
                check=False
            )
            
            if result.returncode == 0:
                success_count += 1
            else:
                print(f"警告: 添加 {dep} 失败: {result.stderr}")
                failed_deps.append(dep)
        except Exception as e:
            print(f"错误: 添加 {dep} 时发生异常: {e}")
            failed_deps.append(dep)
    
    return success_count, failed_deps


def main():
    parser = argparse.ArgumentParser(description='将requirements.txt转换为Poetry依赖')
    parser.add_argument('requirements_file', help='requirements.txt文件路径')
    args = parser.parse_args()
    
    requirements_file = args.requirements_file
    
    if not Path(requirements_file).exists():
        print(f"错误: 文件不存在: {requirements_file}")
        sys.exit(1)
    
    dependencies = parse_requirements(requirements_file)
    print(f"从 {requirements_file} 解析到 {len(dependencies)} 个依赖")
    
    init_poetry_if_needed()
    
    success_count, failed_deps = add_dependencies(dependencies)
    
    print("\n迁移摘要:")
    print(f"成功添加: {success_count}/{len(dependencies)}")
    
    if failed_deps:
        print("\n以下依赖需要手动处理:")
        for dep in failed_deps:
            print(f"- {dep}")
    
    print("\n迁移完成！使用 'poetry install' 安装依赖。")


if __name__ == "__main__":
    main() 
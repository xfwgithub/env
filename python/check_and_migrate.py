#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Python项目规范检查与迁移工具
用于检查项目是否符合规范并提供迁移功能
"""

import os
import sys
import subprocess
import shutil
import json
from pathlib import Path
import argparse
import re


class ProjectChecker:
    def __init__(self, project_path='.', verbose=False):
        self.project_path = Path(project_path).resolve()
        self.verbose = verbose
        self.python_version = None
        self.issues = []
        self.has_poetry = False
        self.has_python_version_file = False
        self.has_requirements_txt = False
        self.has_setup_py = False
        self.has_pipfile = False
        self.using_venv = False
        self.venv_path = None
        self.requirements = []

    def log(self, message):
        """打印详细日志"""
        if self.verbose:
            print(f"[INFO] {message}")

    def check_python_version(self):
        """检查项目是否指定了Python版本"""
        python_version_file = self.project_path / '.python-version'
        
        if python_version_file.exists():
            self.has_python_version_file = True
            self.python_version = python_version_file.read_text().strip()
            self.log(f"找到.python-version文件，指定Python版本: {self.python_version}")
            
            # 检查pyenv是否安装了这个版本
            try:
                installed_versions = subprocess.check_output(
                    ['pyenv', 'versions', '--bare'], 
                    universal_newlines=True
                ).splitlines()
                
                if self.python_version not in installed_versions:
                    self.issues.append(f"项目指定的Python版本 {self.python_version} 未通过pyenv安装")
            except subprocess.CalledProcessError:
                self.issues.append("无法执行pyenv命令，请确保已安装pyenv")
        else:
            self.issues.append("缺少.python-version文件指定Python版本")
    
    def check_dependency_management(self):
        """检查依赖管理方式"""
        # 检查Poetry (pyproject.toml)
        pyproject_toml = self.project_path / 'pyproject.toml'
        if pyproject_toml.exists():
            content = pyproject_toml.read_text()
            self.has_poetry = '[tool.poetry]' in content
            
            # 也尝试检查新的PEP 621 格式，这可能是由Poetry或其他工具创建的
            if '[project]' in content and not self.has_poetry:
                self.log("发现PEP 621格式的pyproject.toml")
                self.has_requirements_txt = True  # 将其视为有效的依赖管理方式
            
            if self.has_poetry:
                self.log("项目使用Poetry管理依赖")
            else:
                self.log("发现pyproject.toml但不是Poetry格式")
        
        # 检查requirements.txt
        requirements_txt = self.project_path / 'requirements.txt'
        if requirements_txt.exists():
            self.has_requirements_txt = True
            self.log("项目使用requirements.txt管理依赖")
            # 读取requirements.txt内容
            self.requirements = requirements_txt.read_text().splitlines()
            # 过滤掉注释和空行
            self.requirements = [
                line.strip() for line in self.requirements 
                if line.strip() and not line.strip().startswith('#')
            ]
        
        # 检查setup.py
        setup_py = self.project_path / 'setup.py'
        if setup_py.exists():
            self.has_setup_py = True
            self.log("项目使用setup.py")
        
        # 检查Pipfile
        pipfile = self.project_path / 'Pipfile'
        if pipfile.exists():
            self.has_pipfile = True
            self.log("项目使用Pipenv管理依赖")
        
        # 依赖管理评估
        if not (self.has_poetry or self.has_requirements_txt or self.has_setup_py or self.has_pipfile):
            self.issues.append("没有找到任何依赖管理文件")
        elif not self.has_poetry:
            self.issues.append("未使用Poetry管理依赖（推荐使用Poetry）")
    
    def check_virtual_environment(self):
        """检查虚拟环境设置"""
        # 检查常见的虚拟环境目录
        for venv_dir in ['.venv', 'venv', 'env']:
            venv_path = self.project_path / venv_dir
            if venv_path.exists() and (venv_path / 'bin' / 'python').exists():
                self.using_venv = True
                self.venv_path = venv_path
                self.log(f"找到虚拟环境: {venv_path}")
                break
        
        # 检查Poetry的虚拟环境设置
        if self.has_poetry:
            try:
                result = subprocess.run(
                    ['poetry', 'config', 'virtualenvs.in-project'], 
                    capture_output=True, 
                    text=True,
                    check=False
                )
                if 'true' in result.stdout.lower():
                    self.log("Poetry配置为在项目内创建虚拟环境")
                else:
                    self.issues.append("Poetry未配置为在项目内创建虚拟环境（推荐设置virtualenvs.in-project=true）")
            except Exception as e:
                self.log(f"检查Poetry配置时出错: {e}")
    
    def check_project_structure(self):
        """检查项目结构"""
        # 检查src目录或app目录或其他常见的源代码目录
        src_dirs = [
            self.project_path / 'src',
            self.project_path / 'app',
            self.project_path / 'source',
            self.project_path / 'lib'
        ]
        
        if not any(src_dir.exists() for src_dir in src_dirs) and not list(self.project_path.glob('*.py')):
            self.issues.append("项目中没有找到源代码文件")
        else:
            for src_dir in src_dirs:
                if src_dir.exists():
                    self.log(f"找到源代码目录: {src_dir.relative_to(self.project_path)}")
                    break
        
        # 检查测试目录
        test_dirs = [self.project_path / 'tests', self.project_path / 'test']
        if not any(test_dir.exists() for test_dir in test_dirs):
            self.issues.append("项目中没有找到测试目录")
        else:
            for test_dir in test_dirs:
                if test_dir.exists():
                    self.log(f"找到测试目录: {test_dir.relative_to(self.project_path)}")
                    break
        
        # 检查README文件
        readme_files = list(self.project_path.glob('README*'))
        if not readme_files:
            self.issues.append("项目中没有找到README文件")
        else:
            self.log(f"找到README文件: {readme_files[0].name}")
    
    def run_checks(self):
        """运行所有检查"""
        self.check_python_version()
        self.check_dependency_management()
        self.check_virtual_environment()
        self.check_project_structure()
        
        if self.issues:
            print("\n不符合规范的问题:")
            for i, issue in enumerate(self.issues, 1):
                print(f"{i}. {issue}")
            return False
        else:
            print("恭喜！项目符合规范要求。")
            return True


class ProjectMigrator:
    def __init__(self, checker, verbose=False):
        self.checker = checker
        self.project_path = checker.project_path
        self.verbose = verbose
    
    def log(self, message):
        """打印详细日志"""
        if self.verbose:
            print(f"[MIGRATE] {message}")
    
    def setup_python_version(self):
        """设置Python版本文件"""
        if not self.checker.has_python_version_file:
            # 获取当前Python版本
            current_version = '.'.join(map(str, sys.version_info[:3]))
            
            # 获取可用的pyenv版本
            try:
                available_versions = subprocess.check_output(
                    ['pyenv', 'versions', '--bare'], 
                    universal_newlines=True
                ).splitlines()
                
                # 找到与当前版本最接近的可用版本
                closest_version = None
                major_minor = '.'.join(current_version.split('.')[:2])
                
                for version in available_versions:
                    if version.startswith(major_minor):
                        closest_version = version
                        break
                
                if not closest_version:
                    closest_version = available_versions[-1] if available_versions else current_version
                
                # 创建.python-version文件
                python_version_file = self.project_path / '.python-version'
                python_version_file.write_text(closest_version)
                self.log(f"已创建.python-version文件，指定Python版本: {closest_version}")
                
                # 更新checker状态
                self.checker.has_python_version_file = True
                self.checker.python_version = closest_version
                
            except subprocess.CalledProcessError:
                print("警告: 无法执行pyenv命令，请手动创建.python-version文件")
    
    def migrate_to_poetry(self):
        """迁移到Poetry依赖管理"""
        if not self.checker.has_poetry:
            try:
                # 检查Poetry是否已安装
                subprocess.run(['poetry', '--version'], check=True, capture_output=True)
                
                # 获取现有的pyproject.toml路径
                pyproject_path = self.project_path / 'pyproject.toml'
                
                # 如果存在PEP 621格式的pyproject.toml，先进行转换
                if pyproject_path.exists() and '[project]' in pyproject_path.read_text():
                    self.log("检测到PEP 621格式的pyproject.toml，准备转换为Poetry格式")
                    
                    # 读取原始文件
                    with open(pyproject_path, 'r') as f:
                        content = f.read()
                    
                    # 备份原始文件
                    backup_path = self.project_path / 'pyproject.toml.bak'
                    with open(backup_path, 'w') as f:
                        f.write(content)
                    self.log(f"已备份原始pyproject.toml到{backup_path}")
                    
                    # 尝试解析内容
                    import tomli
                    import tomli_w
                    import re
                    
                    try:
                        # 将内容转换为字典
                        data = tomli.loads(content)
                        
                        # 创建新的Poetry结构
                        poetry_data = {}
                        
                        # 拷贝build-system部分
                        if 'build-system' in data:
                            poetry_data['build-system'] = {
                                'requires': ['poetry-core>=1.0.0'],
                                'build-backend': 'poetry.core.masonry.api'
                            }
                        
                        # 创建tool.poetry部分
                        poetry_data['tool'] = {'poetry': {}}
                        
                        # 复制项目元数据
                        if 'project' in data:
                            project = data['project']
                            poetry = poetry_data['tool']['poetry']
                            
                            # 基础元数据
                            for field in ['name', 'version', 'description', 'readme', 'license']:
                                if field in project:
                                    poetry[field] = project[field]
                            
                            # Python版本
                            if 'requires-python' in project:
                                python_version = project['requires-python']
                                # 移除==、>=等前缀
                                python_version = re.sub(r'[=<>~!]+=?', '', python_version).strip()
                                poetry['python'] = python_version
                            
                            # 作者
                            if 'authors' in project:
                                authors = []
                                for author in project['authors']:
                                    if 'name' in author and 'email' in author:
                                        authors.append(f"{author['name']} <{author['email']}>")
                                    elif 'name' in author:
                                        authors.append(author['name'])
                                poetry['authors'] = authors
                            
                            # 依赖
                            if 'dependencies' in project:
                                deps = {}
                                for dep in project['dependencies']:
                                    if isinstance(dep, str):
                                        # 拆分包名和版本
                                        if '==' in dep:
                                            name, version = dep.split('==', 1)
                                            deps[name] = f"^{version}"
                                        else:
                                            deps[dep] = "*"
                                poetry['dependencies'] = deps
                            
                            # 可选依赖
                            if 'optional-dependencies' in project:
                                opt_deps = {}
                                for group, deps_list in project['optional-dependencies'].items():
                                    group_deps = {}
                                    for dep in deps_list:
                                        if isinstance(dep, str):
                                            if '==' in dep:
                                                name, version = dep.split('==', 1)
                                                group_deps[name] = f"^{version}"
                                            else:
                                                group_deps[dep] = "*"
                                    opt_deps[group] = group_deps
                                poetry['group'] = opt_deps
                        
                        # 复制其他工具配置
                        for tool in data.get('tool', {}):
                            if tool != 'poetry':  # 避免覆盖我们刚刚创建的Poetry配置
                                poetry_data['tool'][tool] = data['tool'][tool]
                        
                        # 将转换后的数据写入pyproject.toml
                        with open(pyproject_path, 'wb') as f:
                            tomli_w.dump(poetry_data, f)
                        self.log("已将pyproject.toml转换为Poetry格式")
                        
                    except (ImportError, Exception) as e:
                        self.log(f"无法解析pyproject.toml: {e}")
                        self.log("将尝试使用Poetry初始化新项目")
                        
                        # 如果解析失败，回退到原始文件
                        if backup_path.exists():
                            import shutil
                            shutil.copy(backup_path, pyproject_path)
                            self.log("已恢复原始pyproject.toml")
                        
                        # 使用poetry init创建新项目
                        os.chdir(self.project_path)
                        subprocess.run(['poetry', 'init', '--no-interaction'], check=False)
                        self.log("已初始化Poetry项目")
                else:
                    # 没有现有的pyproject.toml，直接初始化
                    os.chdir(self.project_path)
                    subprocess.run(['poetry', 'init', '--no-interaction'], check=False)
                    self.log("已初始化Poetry项目")
                
                # 如果有requirements.txt，迁移依赖
                if self.checker.has_requirements_txt:
                    for req in self.checker.requirements:
                        # 移除版本限制符号以简化迁移
                        package = re.split(r'[=<>~!]', req)[0].strip()
                        if package:
                            try:
                                subprocess.run(['poetry', 'add', package], check=False)
                                self.log(f"已添加依赖: {package}")
                            except Exception as e:
                                print(f"警告: 无法添加依赖 {package}: {e}")
                
                # 配置Poetry在项目内创建虚拟环境
                subprocess.run(['poetry', 'config', 'virtualenvs.in-project', 'true'], check=False)
                self.log("已配置Poetry在项目内创建虚拟环境")
                
                # 更新checker状态
                self.checker.has_poetry = True
                
            except subprocess.CalledProcessError:
                print("警告: 未安装Poetry，请先安装Poetry: brew install poetry")
            except Exception as e:
                print(f"警告: 迁移到Poetry时出错: {e}")
    
    def setup_virtual_environment(self):
        """设置虚拟环境"""
        if not self.checker.using_venv and self.checker.has_poetry:
            try:
                os.chdir(self.project_path)
                subprocess.run(['poetry', 'install', '--no-root'], check=False)
                self.log("已通过Poetry创建虚拟环境")
                
                # 更新checker状态
                self.checker.using_venv = True
                self.checker.venv_path = self.project_path / '.venv'
                
            except Exception as e:
                print(f"警告: 无法创建虚拟环境: {e}")
    
    def create_basic_structure(self):
        """创建基本项目结构"""
        # 创建测试目录
        if not (self.project_path / 'tests').exists():
            (self.project_path / 'tests').mkdir(exist_ok=True)
            (self.project_path / 'tests' / '__init__.py').touch()
            self.log("已创建tests目录")
        
        # 创建README.md
        if not list(self.project_path.glob('README*')):
            readme_path = self.project_path / 'README.md'
            project_name = self.project_path.name
            readme_content = f"""# {project_name}

## 项目说明
这是{project_name}项目的README文件。

## 安装
```bash
# 安装依赖
poetry install
```

## 使用方法
```bash
# 运行项目
poetry run python -m {project_name}
```
"""
            readme_path.write_text(readme_content)
            self.log("已创建README.md文件")
    
    def run_migration(self):
        """运行所有迁移步骤"""
        print("\n开始迁移项目到规范...")
        self.setup_python_version()
        self.migrate_to_poetry()
        self.setup_virtual_environment()
        self.create_basic_structure()
        print("项目迁移完成！")


def main():
    parser = argparse.ArgumentParser(description='Python项目规范检查与迁移工具')
    parser.add_argument('path', nargs='?', default='.', help='项目路径（默认为当前目录）')
    parser.add_argument('-v', '--verbose', action='store_true', help='显示详细日志')
    parser.add_argument('-m', '--migrate', action='store_true', help='自动迁移到规范')
    args = parser.parse_args()
    
    # 创建checker并运行检查
    checker = ProjectChecker(args.path, args.verbose)
    is_compliant = checker.run_checks()
    
    # 如果需要迁移且项目不符合规范，运行迁移
    if args.migrate and not is_compliant:
        migrator = ProjectMigrator(checker, args.verbose)
        migrator.run_migration()
        
        # 迁移后重新检查
        print("\n迁移后重新检查项目...")
        checker = ProjectChecker(args.path, args.verbose)
        checker.run_checks()


if __name__ == "__main__":
    main() 
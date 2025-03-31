# Python环境统一管理

本项目旨在解决系统中的Python环境管理问题，提供统一的Python版本和依赖管理方案。

## 环境要求

- Python 3.11+
- pyenv (Python版本管理)
- Poetry (依赖管理)

## 快速开始

### 安装环境

使用自动化脚本安装所需环境:

```bash
# 给脚本执行权限
chmod +x scripts/setup.sh

# 运行安装脚本
./scripts/setup.sh
```

### 项目设置

```bash
# 克隆项目
git clone <项目地址>
cd <项目目录>

# 安装依赖
poetry install

# 激活虚拟环境
poetry shell
```

## 开发指南

### 添加依赖

```bash
# 添加生产依赖
poetry add <package-name>

# 添加开发依赖
poetry add --group dev <package-name>
```

### 运行测试

```bash
poetry run pytest
```

### 代码格式化

```bash
# 使用black格式化代码
poetry run black .

# 使用isort排序导入
poetry run isort .
```

## 环境管理最佳实践

1. 始终使用pyenv管理Python版本
2. 使用Poetry管理项目依赖
3. 定期更新依赖并检查安全漏洞
4. 确保所有开发者使用相同的环境配置

## 常见问题

### 如何处理旧项目兼容性问题?

对于需要使用旧版Python的项目，可以在项目目录中创建`.python-version`文件指定特定版本：

```bash
# 创建.python-version文件
echo "3.8.16" > .python-version
```

### 如何解决Poetry依赖冲突?

如果遇到依赖冲突，可以尝试以下解决方案：

```bash
# 更新所有依赖
poetry update

# 解决特定依赖问题
poetry add <package-name>@<specific-version>
```

## 项目检查与迁移

我们提供了一个检查和迁移工具，可以帮助确认项目是否符合规范，并在需要时进行迁移：

```bash
# 检查当前目录下的项目是否符合规范
./check_and_migrate.py

# 检查指定路径的项目并显示详细信息
./check_and_migrate.py /path/to/project -v

# 检查并自动迁移项目到规范
./check_and_migrate.py -m
```

此工具会检查以下内容：
1. Python版本管理（是否有.python-version文件）
2. 依赖管理（是否使用Poetry）
3. 虚拟环境配置
4. 项目结构（测试目录、README等）

如果项目不符合规范，使用`-m`参数可以自动迁移。 
# Python环境管理详细指南

## 现有环境清理指南

### 1. 识别和清理Python版本

```bash
# 列出所有安装的Python版本
python3 scripts/check_python_env.py

# 使用pyenv设置默认Python版本
pyenv global 3.11.7

# 卸载不需要的Python版本(仅适用于pyenv安装的版本)
pyenv uninstall 3.9.6
```

### 2. 清理依赖管理工具

**Pipenv 迁移**:
```bash
# 从Pipfile转换到Poetry
pip install pipenv-poetry-converter
pipenv-poetry-converter

# 或直接使用requirements.txt
pipenv lock -r > requirements.txt
python scripts/migrate_to_poetry.py requirements.txt
```

**requirements.txt 迁移**:
```bash
# 自动转换
python scripts/migrate_to_poetry.py requirements.txt
```

**Conda 迁移**:
```bash
# 导出Conda环境
conda env export --from-history > environment.yml

# 手动创建pyproject.toml并添加依赖
# 然后使用poetry install安装
```

### 3. 清理虚拟环境

```bash
# 移除旧的venv环境
rm -rf venv env .venv

# 移除Conda环境(如果使用Conda)
conda env remove -n env_name
```

## 环境管理最佳实践详解

### Python版本管理 (pyenv)

pyenv不仅可以在系统级别管理Python版本，还可以为每个项目设置特定版本:

```bash
# 全局Python版本
pyenv global 3.11.7

# 特定目录Python版本
cd /path/to/project
pyenv local 3.10.8

# 临时会话使用特定版本
pyenv shell 3.9.16
```

**优点**:
- 无需root权限
- 在用户空间安装多个Python版本
- 支持特定项目使用特定版本
- 与大多数CI系统兼容

### 依赖管理 (Poetry)

Poetry提供了全面的依赖管理解决方案:

```bash
# 创建新项目
poetry new project_name

# 添加依赖
poetry add requests

# 添加开发依赖
poetry add --group dev pytest

# 安装依赖
poetry install

# 更新依赖
poetry update

# 生成requirements.txt(如果需要)
poetry export -f requirements.txt --output requirements.txt
```

**优点**:
- 精确的依赖锁定(poetry.lock)
- 分组依赖(开发、测试、文档等)
- 内置版本冲突解决
- 虚拟环境管理集成
- 包发布支持

### 整合CI/CD

对于CI/CD系统，可以使用以下方法:

```yaml
# GitHub Actions示例
steps:
  - uses: actions/checkout@v3
  - name: 设置Python
    uses: actions/setup-python@v4
    with:
      python-version: '3.11'
  - name: 安装Poetry
    run: pip install poetry
  - name: 安装依赖
    run: poetry install
  - name: 运行测试
    run: poetry run pytest
```

## 迁移策略建议

1. **增量迁移**：先迁移较小、较新的项目
2. **并行测试**：迁移后与原环境并行测试
3. **文档更新**：更新开发文档以反映新环境
4. **团队培训**：确保所有开发人员熟悉新工具
5. **自动化脚本**：提供自动化脚本简化迁移

## 环境检查清单

- [ ] 移除所有不需要的Python版本
- [ ] 安装并配置pyenv
- [ ] 统一使用Poetry进行依赖管理
- [ ] 更新所有CI/CD配置
- [ ] 添加环境设置文档
- [ ] 确保所有开发人员使用一致工具链 
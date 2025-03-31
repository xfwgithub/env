# 环境检查工具集

这是一个用于检查项目环境配置的工具集，支持Python和Java项目。该工具集可以帮助您快速验证项目是否符合最佳实践标准。

## 项目结构

```
.
├── python/          # Python环境检查工具
└── java/           # Java环境检查工具
```

## Python环境检查工具

Python环境检查工具用于检查Python项目的环境配置，包括：
- Python版本检查（支持3.11+）
- 虚拟环境配置（使用pyenv）
- 依赖管理（使用Poetry）
- 项目结构验证
- 代码风格检查（black, isort）
- 自动化环境设置

### Python工具特性
- 自动安装和配置pyenv
- 自动安装和配置Poetry
- 自动创建和配置虚拟环境
- 自动检查和修复项目结构
- 支持Docker容器化部署

### Python工具使用方法
```bash
# 安装环境
chmod +x python/scripts/setup.sh
./python/scripts/setup.sh

# 检查项目
python python/check_and_migrate.py -p <项目路径> -v

# 自动修复
python python/check_and_migrate.py -p <项目路径> -v -m
```

详细文档请查看 [python/README.md](python/README.md)

## Java环境检查工具

Java环境检查工具用于检查Java项目的环境配置，包括：
- Java版本检查
- Maven配置验证
- 项目结构完整性
- 测试目录检查
- Docker支持

### Java工具特性
- 自动检查Java版本配置
- 验证Maven项目结构
- 自动创建测试目录
- 支持Docker容器化部署

### Java工具使用方法
```bash
# 编译项目
cd java
mvn clean package

# 检查项目
java -jar target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar -p <项目路径> -v

# 自动修复
java -jar target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar -p <项目路径> -v -m
```

详细文档请查看 [java/README.md](java/README.md)

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这些工具。

## 许可证

MIT License 
# 环境检查工具集

这是一个用于检查和修复已有项目环境配置的工具集，支持Python、Java和前端项目。该工具集可以帮助您快速验证项目是否符合最佳实践标准，并自动修复常见问题。同时，它也支持新项目的环境初始化配置。

## 项目结构

```
.
├── python/          # Python环境检查工具
│   ├── scripts/     # 安装和部署脚本
│   └── checks/      # 检查规则模块
├── java/            # Java环境检查工具
│   ├── src/         # 源代码
│   └── scripts/     # 构建和部署脚本
└── frontend/        # 前端环境检查工具
    ├── scripts/     # 脚本和检查工具
    │   ├── setup.sh
    │   └── check.js
    └── config/      # 配置文件
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
java -cp target/java-env-demo-1.0-SNAPSHOT-jar-with-dependencies.jar com.tool.App checkProject <项目路径> verbose

# 自动修复（目前不支持，只能检查）
# 使用构建脚本
chmod +x java/scripts/build.sh
./java/scripts/build.sh
```

详细文档请查看 [java/README.md](java/README.md)

## 前端环境检查工具

前端环境检查工具用于检查和修复其他已有前端项目的环境配置，包括：
- Node.js版本检查（支持18+）
- 包管理工具配置（npm/yarn）
- TypeScript配置验证
- 项目结构完整性检查
- 代码风格检查（ESLint, Prettier）
- 自动化环境设置

### 前端工具特性
- 自动检查和安装目标项目所需的Node.js版本
- 验证和修复目标项目的package.json配置
- 检查和修复TypeScript配置
- 验证前端项目结构是否符合最佳实践
- 自动添加和配置代码质量工具

### 前端工具使用方法
```bash
# 安装前端环境检查工具
chmod +x frontend/scripts/setup.sh
./frontend/scripts/setup.sh

# 检查目标前端项目
node frontend/scripts/check.js -p <前端项目路径> -v

# 自动修复目标前端项目
node frontend/scripts/check.js -p <前端项目路径> -v -m
```

详细文档请查看 [frontend/README.md](frontend/README.md)

## 特殊项目结构说明

### 前后端分离项目（同一仓库）

对于采用前后端分离但位于同一仓库的项目（如前端代码位于`frontend/`子目录），请针对不同部分分别运行检查工具：

#### 检查后端（根目录）
```bash
# Python项目
python python/check_and_migrate.py <项目根路径> -v

# Java项目
java -cp java/target/java-env-demo-1.0-SNAPSHOT-jar-with-dependencies.jar com.tool.App checkProject <项目根路径> verbose
```

#### 检查前端（子目录）
```bash
# 前端位于frontend/子目录
node frontend/scripts/check.js -p <项目根路径>/frontend -v

# 前端位于其他子目录（如client/、web/等）
node frontend/scripts/check.js -p <项目根路径>/<前端目录名> -v
```

这种项目结构（如前端代码位于backend项目的子目录中）是常见且合理的架构选择，特别适合：
- 全栈开发团队
- 需要统一部署的应用
- 前后端需要共享配置的项目
- 避免跨域问题的场景

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这些工具。

## 许可证

MIT License
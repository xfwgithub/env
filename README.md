# Java环境检查工具

这是一个用于检查Java项目环境配置的工具，可以帮助您快速验证项目是否符合最佳实践标准。

## 功能特性

- 检查Java版本配置
- 验证项目结构完整性
- 检查构建系统配置
- 自动修复常见问题
- 支持Docker容器化部署

## 系统要求

- JDK 8 或更高版本
- Maven 3.6 或更高版本
- Docker（可选，用于容器化部署）

## 快速开始

### 1. 编译项目

```bash
mvn clean package
```

### 2. 运行检查

基本检查：
```bash
java -jar target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar -p <项目路径> -v
```

自动修复：
```bash
java -jar target/java-env-checker-1.0-SNAPSHOT-jar-with-dependencies.jar -p <项目路径> -v -m
```

### 3. Docker部署

构建镜像：
```bash
docker build -t java-env-checker .
```

运行容器：
```bash
docker run -v /path/to/project:/app/project java-env-checker -p /app/project -v
```

## 检查项目

工具会检查以下内容：

1. Java版本配置
   - 检查`.java-version`文件
   - 验证Java版本兼容性

2. 项目结构
   - 验证源代码目录结构
   - 检查测试目录结构
   - 确认构建系统配置

3. 文档完整性
   - 检查README文件
   - 验证项目文档

## 自动修复

当使用`-m`参数时，工具会自动修复以下问题：

1. 创建缺失的`.java-version`文件
2. 创建标准的`.gitignore`文件
3. 创建缺失的测试目录结构
4. 添加基本的项目文档

## 日志配置

日志文件位置：`logs/java-env-checker.log`

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个工具。

## 许可证

MIT License 
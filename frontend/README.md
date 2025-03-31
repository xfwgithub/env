# 前端开发环境统一管理

本项目旨在解决前端开发环境管理问题，提供统一的TypeScript和JavaScript开发环境配置。

## 环境要求

- Node.js 18+ (推荐使用LTS版本)
- npm 8+ 或 yarn 1.22+
- nvm (Node版本管理)

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
npm install
# 或使用yarn
yarn install

# 启动开发服务器
npm start
# 或使用yarn
yarn start
```

## 开发指南

### 项目结构

标准前端项目结构:

```
my-project/
├── public/           # 静态资源
│   ├── index.html    # HTML入口文件
│   ├── favicon.ico   # 网站图标
│   └── assets/       # 其他静态资源
├── src/              # 源代码
│   ├── components/   # 组件
│   ├── hooks/        # 自定义Hooks
│   ├── utils/        # 工具函数
│   ├── pages/        # 页面组件
│   ├── App.tsx       # 应用根组件
│   └── index.tsx     # 入口文件
├── package.json      # 依赖管理文件
├── tsconfig.json     # TypeScript配置
└── .eslintrc.js      # ESLint配置
```

### 代码规范

我们使用ESLint和Prettier来保证代码质量和一致性：

```bash
# 检查代码
npm run lint

# 自动修复
npm run lint:fix

# 格式化代码
npm run format
```

### 构建项目

```bash
# 构建生产版本
npm run build

# 检查构建包大小
npm run analyze
```

## 环境管理最佳实践

1. 使用nvm管理Node.js版本
2. 使用package.json锁定依赖版本
3. 定期更新依赖以修复安全漏洞
4. 确保所有开发者使用相同的环境配置
5. 使用.nvmrc文件指定项目Node.js版本

## 常见问题

### 如何切换Node.js版本?

使用nvm可以轻松切换Node.js版本:

```bash
# 列出可用的Node版本
nvm ls

# 安装指定版本
nvm install 18

# 切换版本
nvm use 18

# 为项目指定Node.js版本
echo "18" > .nvmrc
```

### 如何处理npm依赖问题?

```bash
# 清理npm缓存
npm cache clean --force

# 删除node_modules并重新安装
rm -rf node_modules package-lock.json
npm install
```

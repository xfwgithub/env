#!/usr/bin/env node

/**
 * 前端环境检查工具
 * 检查和修复前端项目环境配置
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const chalk = require('chalk');
const { program } = require('commander');

// 默认配置
const DEFAULT_CONFIG = {
  nodeVersion: '18.0.0',
  requiredDirectories: ['src', 'public', 'node_modules'],
  packageChecks: ['name', 'version', 'scripts', 'dependencies'],
  tsConfigRequired: true,
  codeQualityTools: ['eslint', 'prettier']
};

// 解析命令行参数
program
  .name('前端环境检查工具')
  .description('检查和修复前端项目环境配置')
  .version('1.0.0')
  .option('-p, --path <path>', '项目路径')
  .option('-v, --verbose', '显示详细信息')
  .option('-m, --migrate', '自动修复问题')
  .option('--dry-run', '仅显示需要修改的内容，不实际执行')
  .parse(process.argv);

const options = program.opts();

// 验证参数
if (!options.path) {
  console.error(chalk.red('错误: 未指定项目路径，请使用 -p 或 --path 参数'));
  process.exit(1);
}

const projectPath = path.resolve(options.path);
const verbose = options.verbose || false;
const migrate = options.migrate || false;
const dryRun = options.dryRun || false;

// 日志函数
const log = {
  info: (msg) => console.log(chalk.blue(`[信息] ${msg}`)),
  success: (msg) => console.log(chalk.green(`[成功] ${msg}`)),
  warning: (msg) => console.log(chalk.yellow(`[警告] ${msg}`)),
  error: (msg) => console.log(chalk.red(`[错误] ${msg}`)),
  verbose: (msg) => verbose && console.log(chalk.gray(`[详细] ${msg}`))
};

// 主函数
async function main() {
  log.info(`开始检查项目: ${projectPath}`);
  
  if (!fs.existsSync(projectPath)) {
    log.error(`项目路径不存在: ${projectPath}`);
    process.exit(1);
  }
  
  let success = true;
  
  // 检查Node.js版本
  success = checkNodeVersion() && success;
  
  // 检查项目结构
  success = checkProjectStructure() && success;
  
  // 检查package.json
  success = checkPackageJson() && success;
  
  // 检查TypeScript配置
  success = checkTypeScriptConfig() && success;
  
  // 检查代码质量工具
  success = checkCodeQualityTools() && success;
  
  // 结果报告
  if (success) {
    log.success('项目检查完成，所有检查项均通过!');
  } else {
    if (migrate) {
      log.info('项目检查完成，已尝试修复发现的问题');
    } else {
      log.warning('项目检查完成，存在需要修复的问题，请使用 --migrate 参数自动修复');
    }
  }
}

// 检查Node.js版本
function checkNodeVersion() {
  log.info('检查Node.js版本...');
  
  try {
    const currentVersion = process.version.slice(1); // 移除'v'前缀
    log.verbose(`当前Node.js版本: ${currentVersion}`);
    
    // 检查.nvmrc文件
    const nvmrcPath = path.join(projectPath, '.nvmrc');
    let requiredVersion = DEFAULT_CONFIG.nodeVersion;
    
    if (fs.existsSync(nvmrcPath)) {
      requiredVersion = fs.readFileSync(nvmrcPath, 'utf8').trim();
      log.verbose(`项目指定Node.js版本 (.nvmrc): ${requiredVersion}`);
    } else {
      log.warning('未找到.nvmrc文件，建议创建该文件指定Node.js版本');
      
      if (migrate) {
        fs.writeFileSync(nvmrcPath, requiredVersion);
        log.info(`已创建.nvmrc文件，设置Node.js版本为 ${requiredVersion}`);
      }
    }
    
    // 比较版本
    if (!isVersionSatisfied(currentVersion, requiredVersion)) {
      log.error(`Node.js版本不匹配，当前: ${currentVersion}，需要: ${requiredVersion}`);
      
      if (migrate) {
        log.info(`尝试安装所需Node.js版本: ${requiredVersion}`);
        if (!dryRun) {
          try {
            // 检查nvm是否安装
            execSync('command -v nvm', { stdio: 'ignore' });
            execSync(`nvm install ${requiredVersion} && nvm use ${requiredVersion}`, { stdio: 'inherit' });
            log.success(`已切换到Node.js版本: ${requiredVersion}`);
          } catch (e) {
            log.error('安装Node.js版本失败，请手动安装nvm并切换Node.js版本');
            return false;
          }
        }
      }
      
      return false;
    }
    
    log.success(`Node.js版本检查通过: ${currentVersion}`);
    return true;
  } catch (error) {
    log.error(`检查Node.js版本失败: ${error.message}`);
    return false;
  }
}

// 检查项目结构
function checkProjectStructure() {
  log.info('检查项目结构...');
  
  let success = true;
  
  // 检查必要目录
  for (const dir of DEFAULT_CONFIG.requiredDirectories) {
    const dirPath = path.join(projectPath, dir);
    if (!fs.existsSync(dirPath)) {
      log.warning(`缺少目录: ${dir}`);
      success = false;
      
      if (migrate) {
        if (!dryRun) {
          fs.mkdirSync(dirPath, { recursive: true });
        }
        log.info(`已创建目录: ${dir}`);
      }
    } else {
      log.verbose(`目录存在: ${dir}`);
    }
  }
  
  // 检查src目录结构
  const srcPath = path.join(projectPath, 'src');
  if (fs.existsSync(srcPath)) {
    const srcContents = fs.readdirSync(srcPath);
    if (srcContents.length === 0) {
      log.warning('src目录为空，建议添加基本结构');
      
      if (migrate) {
        if (!dryRun) {
          // 创建基本结构
          fs.mkdirSync(path.join(srcPath, 'components'), { recursive: true });
          fs.mkdirSync(path.join(srcPath, 'styles'), { recursive: true });
          fs.mkdirSync(path.join(srcPath, 'utils'), { recursive: true });
          
          // 创建示例文件
          fs.writeFileSync(path.join(srcPath, 'index.js'), '// 项目入口文件\n');
        }
        log.info('已创建基本目录结构');
      }
    }
  }
  
  if (success) {
    log.success('项目结构检查通过');
  }
  
  return success;
}

// 检查package.json
function checkPackageJson() {
  log.info('检查package.json...');
  
  const packagePath = path.join(projectPath, 'package.json');
  if (!fs.existsSync(packagePath)) {
    log.error('未找到package.json文件');
    
    if (migrate) {
      if (!dryRun) {
        // 创建基本package.json
        const basicPackage = {
          name: path.basename(projectPath),
          version: '0.1.0',
          description: '前端项目',
          main: 'src/index.js',
          scripts: {
            start: 'node src/index.js',
            test: 'echo "Error: no test specified" && exit 1'
          },
          keywords: [],
          author: '',
          license: 'MIT',
          dependencies: {},
          devDependencies: {}
        };
        
        fs.writeFileSync(packagePath, JSON.stringify(basicPackage, null, 2));
      }
      log.info('已创建基本package.json文件');
    }
    
    return false;
  }
  
  try {
    const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
    let needsUpdate = false;
    
    // 检查必要字段
    for (const field of DEFAULT_CONFIG.packageChecks) {
      if (!packageJson[field]) {
        log.warning(`package.json缺少${field}字段`);
        needsUpdate = true;
        
        if (migrate && !dryRun) {
          // 添加默认字段
          switch (field) {
            case 'name':
              packageJson.name = path.basename(projectPath);
              break;
            case 'version':
              packageJson.version = '0.1.0';
              break;
            case 'scripts':
              packageJson.scripts = {
                start: 'node src/index.js',
                test: 'echo "Error: no test specified" && exit 1'
              };
              break;
            case 'dependencies':
              packageJson.dependencies = {};
              break;
          }
        }
      }
    }
    
    // 检查scripts中是否包含基础命令
    if (packageJson.scripts) {
      const requiredScripts = ['start', 'test', 'build'];
      for (const script of requiredScripts) {
        if (!packageJson.scripts[script]) {
          log.warning(`package.json缺少scripts.${script}命令`);
          needsUpdate = true;
          
          if (migrate && !dryRun) {
            packageJson.scripts[script] = script === 'test' ? 
              'echo "Error: no test specified" && exit 1' : 
              script === 'build' ? 'echo "No build script configured"' : 
              'node src/index.js';
          }
        }
      }
    }
    
    // 更新package.json
    if (needsUpdate && migrate && !dryRun) {
      fs.writeFileSync(packagePath, JSON.stringify(packageJson, null, 2));
      log.info('已更新package.json文件');
    }
    
    if (!needsUpdate) {
      log.success('package.json检查通过');
      return true;
    }
    
    return !needsUpdate;
  } catch (error) {
    log.error(`解析package.json失败: ${error.message}`);
    return false;
  }
}

// 检查TypeScript配置
function checkTypeScriptConfig() {
  log.info('检查TypeScript配置...');
  
  if (!DEFAULT_CONFIG.tsConfigRequired) {
    log.verbose('TypeScript配置检查已跳过');
    return true;
  }
  
  const tsConfigPath = path.join(projectPath, 'tsconfig.json');
  if (!fs.existsSync(tsConfigPath)) {
    log.warning('未找到tsconfig.json文件');
    
    if (migrate) {
      if (!dryRun) {
        // 创建基本tsconfig.json
        const basicTsConfig = {
          compilerOptions: {
            target: "es6",
            module: "commonjs",
            lib: ["dom", "dom.iterable", "esnext"],
            allowJs: true,
            skipLibCheck: true,
            esModuleInterop: true,
            allowSyntheticDefaultImports: true,
            strict: true,
            forceConsistentCasingInFileNames: true,
            noFallthroughCasesInSwitch: true,
            moduleResolution: "node",
            resolveJsonModule: true,
            isolatedModules: true,
            noEmit: true,
            jsx: "react-jsx"
          },
          include: ["src/**/*"],
          exclude: ["node_modules", "build", "dist"]
        };
        
        fs.writeFileSync(tsConfigPath, JSON.stringify(basicTsConfig, null, 2));
      }
      log.info('已创建基本tsconfig.json文件');
    }
    
    return false;
  }
  
  try {
    const tsConfig = JSON.parse(fs.readFileSync(tsConfigPath, 'utf8'));
    let needsUpdate = false;
    
    // 检查必要字段
    if (!tsConfig.compilerOptions) {
      log.warning('tsconfig.json缺少compilerOptions字段');
      needsUpdate = true;
      
      if (migrate && !dryRun) {
        tsConfig.compilerOptions = {
          target: "es6",
          module: "commonjs",
          esModuleInterop: true,
          strict: true
        };
      }
    }
    
    // 检查include字段
    if (!tsConfig.include) {
      log.warning('tsconfig.json缺少include字段');
      needsUpdate = true;
      
      if (migrate && !dryRun) {
        tsConfig.include = ["src/**/*"];
      }
    }
    
    // 更新tsconfig.json
    if (needsUpdate && migrate && !dryRun) {
      fs.writeFileSync(tsConfigPath, JSON.stringify(tsConfig, null, 2));
      log.info('已更新tsconfig.json文件');
    }
    
    if (!needsUpdate) {
      log.success('TypeScript配置检查通过');
      return true;
    }
    
    return !needsUpdate;
  } catch (error) {
    log.error(`解析tsconfig.json失败: ${error.message}`);
    return false;
  }
}

// 检查代码质量工具
function checkCodeQualityTools() {
  log.info('检查代码质量工具...');
  
  const packagePath = path.join(projectPath, 'package.json');
  if (!fs.existsSync(packagePath)) {
    log.warning('未找到package.json文件，无法检查代码质量工具');
    return false;
  }
  
  try {
    const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
    const devDependencies = packageJson.devDependencies || {};
    let needsUpdate = false;
    
    // 检查ESLint
    if (DEFAULT_CONFIG.codeQualityTools.includes('eslint') && !devDependencies.eslint) {
      log.warning('未配置ESLint');
      needsUpdate = true;
      
      if (migrate) {
        log.info('配置ESLint...');
        
        if (!dryRun) {
          // 添加ESLint相关依赖
          packageJson.devDependencies = packageJson.devDependencies || {};
          packageJson.devDependencies.eslint = "^8.38.0";
          packageJson.devDependencies["eslint-plugin-react"] = "^7.32.2";
          
          // 添加ESLint配置文件
          const eslintConfig = {
            "env": {
              "browser": true,
              "es2021": true,
              "node": true
            },
            "extends": [
              "eslint:recommended",
              "plugin:react/recommended"
            ],
            "parserOptions": {
              "ecmaFeatures": {
                "jsx": true
              },
              "ecmaVersion": "latest",
              "sourceType": "module"
            },
            "plugins": [
              "react"
            ],
            "rules": {
              "indent": ["error", 2],
              "quotes": ["error", "single"],
              "semi": ["error", "always"]
            }
          };
          
          fs.writeFileSync(
            path.join(projectPath, '.eslintrc.json'),
            JSON.stringify(eslintConfig, null, 2)
          );
          
          // 添加ESLint忽略文件
          fs.writeFileSync(
            path.join(projectPath, '.eslintignore'),
            "node_modules\nbuild\ndist\n"
          );
          
          // 添加lint脚本
          packageJson.scripts = packageJson.scripts || {};
          packageJson.scripts.lint = "eslint src/**/*.{js,jsx,ts,tsx}";
          packageJson.scripts["lint:fix"] = "eslint --fix src/**/*.{js,jsx,ts,tsx}";
        }
      }
    }
    
    // 检查Prettier
    if (DEFAULT_CONFIG.codeQualityTools.includes('prettier') && !devDependencies.prettier) {
      log.warning('未配置Prettier');
      needsUpdate = true;
      
      if (migrate) {
        log.info('配置Prettier...');
        
        if (!dryRun) {
          // 添加Prettier相关依赖
          packageJson.devDependencies = packageJson.devDependencies || {};
          packageJson.devDependencies.prettier = "^2.8.7";
          
          // 添加Prettier配置文件
          const prettierConfig = {
            "singleQuote": true,
            "trailingComma": "es5",
            "printWidth": 100,
            "tabWidth": 2,
            "semi": true
          };
          
          fs.writeFileSync(
            path.join(projectPath, '.prettierrc'),
            JSON.stringify(prettierConfig, null, 2)
          );
          
          // 添加Prettier忽略文件
          fs.writeFileSync(
            path.join(projectPath, '.prettierignore'),
            "node_modules\nbuild\ndist\n"
          );
          
          // 添加format脚本
          packageJson.scripts = packageJson.scripts || {};
          packageJson.scripts.format = "prettier --write src/**/*.{js,jsx,ts,tsx,css,scss,json}";
        }
      }
    }
    
    // 更新package.json
    if (needsUpdate && migrate && !dryRun) {
      fs.writeFileSync(packagePath, JSON.stringify(packageJson, null, 2));
      log.info('已更新package.json文件，添加代码质量工具');
      
      log.warning('代码质量工具已配置，请运行 npm install 或 yarn 安装相关依赖');
    }
    
    if (!needsUpdate) {
      log.success('代码质量工具检查通过');
      return true;
    }
    
    return !needsUpdate;
  } catch (error) {
    log.error(`检查代码质量工具失败: ${error.message}`);
    return false;
  }
}

// 工具函数：版本比较
function isVersionSatisfied(current, required) {
  const currentParts = current.split('.').map(Number);
  const requiredParts = required.split('.').map(Number);
  
  for (let i = 0; i < Math.max(currentParts.length, requiredParts.length); i++) {
    const currentPart = currentParts[i] || 0;
    const requiredPart = requiredParts[i] || 0;
    
    if (currentPart > requiredPart) {
      return true;
    }
    
    if (currentPart < requiredPart) {
      return false;
    }
  }
  
  return true;
}

// 执行主函数
main().catch(error => {
  log.error(`执行失败: ${error.message}`);
  process.exit(1);
}); 
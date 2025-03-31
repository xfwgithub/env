package com.tool.checkers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * 测试目录检查器
 * 验证Java项目是否包含适当的测试目录结构，并在需要时自动创建
 */
public class TestDirectoryChecker {
    private static final Logger LOGGER = Logger.getLogger(TestDirectoryChecker.class.getName());
    
    private final String projectPath;
    private final boolean autoFix;
    private final boolean verbose;
    
    /**
     * 构造函数
     * @param projectPath 项目路径
     * @param autoFix 是否自动修复问题
     * @param verbose 是否显示详细日志
     */
    public TestDirectoryChecker(String projectPath, boolean autoFix, boolean verbose) {
        this.projectPath = projectPath;
        this.autoFix = autoFix;
        this.verbose = verbose;
    }
    
    /**
     * 检查测试目录结构
     * @return 检查是否通过
     */
    public boolean check() {
        LOGGER.info("检查测试目录结构...");
        
        boolean result = true;
        
        // 检查主测试目录
        Path testJavaPath = Paths.get(projectPath, "src", "test", "java");
        if (!Files.exists(testJavaPath)) {
            LOGGER.warning("未找到测试源码目录: " + testJavaPath);
            result = false;
            
            if (autoFix) {
                createDirectory(testJavaPath);
            }
        } else if (verbose) {
            LOGGER.info("测试源码目录存在: " + testJavaPath);
        }
        
        // 检查测试资源目录
        Path testResourcesPath = Paths.get(projectPath, "src", "test", "resources");
        if (!Files.exists(testResourcesPath)) {
            LOGGER.warning("未找到测试资源目录: " + testResourcesPath);
            result = false;
            
            if (autoFix) {
                createDirectory(testResourcesPath);
            }
        } else if (verbose) {
            LOGGER.info("测试资源目录存在: " + testResourcesPath);
        }
        
        // 检查是否有测试类
        File testJavaDir = testJavaPath.toFile();
        if (testJavaDir.exists() && testJavaDir.isDirectory()) {
            if (isEmpty(testJavaDir)) {
                LOGGER.warning("测试源码目录为空，建议添加测试类");
                if (autoFix) {
                    createSampleTestClass(testJavaPath);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 创建目录
     * @param path 目录路径
     */
    private void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
            LOGGER.info("已创建目录: " + path);
        } catch (Exception e) {
            LOGGER.severe("创建目录失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查目录是否为空
     * @param directory 目录
     * @return 是否为空
     */
    private boolean isEmpty(File directory) {
        File[] files = directory.listFiles();
        return files == null || files.length == 0;
    }
    
    /**
     * 创建示例测试类
     * @param testDir 测试目录
     */
    private void createSampleTestClass(Path testDir) {
        try {
            // 查找主代码包结构
            Path mainJavaPath = Paths.get(projectPath, "src", "main", "java");
            String packageName = detectMainPackage(mainJavaPath.toFile());
            
            Path testFile = testDir.resolve(packageName.replace('.', File.separatorChar))
                    .resolve("SampleTest.java");
            
            // 确保父目录存在
            Files.createDirectories(testFile.getParent());
            
            // 写入测试类模板
            String testClassContent = 
                    "package " + packageName + ";\n\n" +
                    "import org.junit.jupiter.api.Test;\n" +
                    "import static org.junit.jupiter.api.Assertions.assertTrue;\n\n" +
                    "/**\n" +
                    " * 示例测试类\n" +
                    " */\n" +
                    "public class SampleTest {\n\n" +
                    "    @Test\n" +
                    "    public void sampleTest() {\n" +
                    "        // 示例测试方法\n" +
                    "        assertTrue(true, \"示例测试\");\n" +
                    "    }\n" +
                    "}\n";
            
            Files.writeString(testFile, testClassContent);
            LOGGER.info("已创建示例测试类: " + testFile);
        } catch (Exception e) {
            LOGGER.severe("创建示例测试类失败: " + e.getMessage());
        }
    }
    
    /**
     * 检测主代码包名
     * @param mainDir 主代码目录
     * @return 包名
     */
    private String detectMainPackage(File mainDir) {
        if (!mainDir.exists() || !mainDir.isDirectory()) {
            return "com.example";
        }
        
        StringBuilder packageName = new StringBuilder();
        File current = mainDir;
        
        // 找到第一个Java文件，读取包名
        File javaFile = findFirstJavaFile(current);
        if (javaFile != null) {
            try {
                String content = Files.readString(javaFile.toPath());
                String packageLine = content.lines()
                        .filter(line -> line.startsWith("package "))
                        .findFirst()
                        .orElse("package com.example;");
                
                return packageLine.substring(8, packageLine.indexOf(';')).trim();
            } catch (Exception e) {
                // 忽略错误
            }
        }
        
        // 如果无法检测，返回默认包名
        return "com.example";
    }
    
    /**
     * 查找第一个Java文件
     * @param dir 目录
     * @return Java文件
     */
    private File findFirstJavaFile(File dir) {
        if (!dir.isDirectory()) {
            return null;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".java")) {
                return file;
            } else if (file.isDirectory()) {
                File found = findFirstJavaFile(file);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
} 
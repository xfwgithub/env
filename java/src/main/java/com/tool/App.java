package com.tool;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        if(args.length < 1) {
            showUsage();
            return;
        }
        String command = args[0];
        switch(command) {
            case "codebaseSearch":
                codebaseSearch(args);
                break;
            case "readFile":
                readFile(args);
                break;
            case "runTerminalCmd":
                runTerminalCmd(args);
                break;
            case "listDir":
                listDir(args);
                break;
            case "grepSearch":
                grepSearch(args);
                break;
            case "editFile":
                editFile(args);
                break;
            case "fileSearch":
                fileSearch(args);
                break;
            case "deleteFile":
                deleteFile(args);
                break;
            case "reapply":
                reapply(args);
                break;
            case "checkProject":
                checkProject(args);
                break;
            case "checkFrontend":
                checkFrontend(args);
                break;
            case "checkPython":
                checkPython(args);
                break;
            default:
                System.out.println("未识别的命令: " + command);
                showUsage();
                break;
        }
    }

    private static void showUsage() {
        System.out.println("使用说明:");
        System.out.println(" java -jar YourApp.jar <命令> [参数...]");
        System.out.println("可用的命令:");
        System.out.println(" codebaseSearch <查询字符串> [目标目录]");
        System.out.println(" readFile <文件路径> [起始行] [结束行] (行号为1-indexed)");
        System.out.println(" runTerminalCmd <命令> (多个参数将拼接成一个命令)");
        System.out.println(" listDir <目录路径>");
        System.out.println(" grepSearch <正则表达式> [目标目录]");
        System.out.println(" editFile <文件路径> <目标字符串> <替换字符串>");
        System.out.println(" fileSearch <文件名片段> [目录]");
        System.out.println(" deleteFile <文件路径>");
        System.out.println(" reapply (功能暂未实现)");
        System.out.println(" checkProject <项目路径> [verbose]");
        System.out.println(" checkFrontend <项目路径> [verbose]");
        System.out.println(" checkPython <项目路径> [verbose]");
    }

    private static void codebaseSearch(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供查询字符串。");
            return;
        }
        String query = args[1];
        String targetDir = args.length >= 3 ? args[2] : ".";
        try {
            Files.walk(Paths.get(targetDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (int i = 0; i < lines.size(); i++) {
                            if(lines.get(i).contains(query)) {
                                System.out.println("匹配: " + path.toString() + " 行号: " + (i+1));
                            }
                        }
                    } catch(Exception e) {
                        // 忽略文件读取错误
                    }
                });
        } catch(Exception e) {
            System.out.println("搜索过程中发生错误: " + e.getMessage());
        }
    }

    private static void readFile(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供文件路径。");
            return;
        }
        String filePath = args[1];
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if(args.length >= 4) {
                int start = Integer.parseInt(args[2]) - 1;
                int end = Integer.parseInt(args[3]) - 1;
                for (int i = start; i <= end && i < lines.size(); i++) {
                    System.out.println((i+1) + ": " + lines.get(i));
                }
            } else {
                for (int i = 0; i < lines.size(); i++) {
                    System.out.println((i+1) + ": " + lines.get(i));
                }
            }
        } catch(Exception e) {
            System.out.println("读取文件时发生错误: " + e.getMessage());
        }
    }

    private static void runTerminalCmd(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供要执行的命令。");
            return;
        }
        // 将所有参数拼接为一个命令字符串
        StringBuilder cmdBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            cmdBuilder.append(args[i]).append(" ");
        }
        String command = cmdBuilder.toString().trim();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            // 根据操作系统分割命令
            if(System.getProperty("os.name").toLowerCase().contains("windows")){
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("bash", "-c", command);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("退出码：" + exitCode);
        } catch(Exception e) {
            System.out.println("执行命令时发生错误: " + e.getMessage());
        }
    }

    private static void listDir(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供目录路径。");
            return;
        }
        String dirPath = args[1];
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dirPath))) {
            for(Path entry : stream) {
                System.out.println(entry.getFileName().toString());
            }
        } catch(Exception e) {
            System.out.println("列出目录时发生错误: " + e.getMessage());
        }
    }

    private static void grepSearch(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供正则表达式。");
            return;
        }
        String regex = args[1];
        String targetDir = args.length >= 3 ? args[2] : ".";
        Pattern pattern = Pattern.compile(regex);
        try {
            Files.walk(Paths.get(targetDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (int i = 0; i < lines.size(); i++) {
                            Matcher matcher = pattern.matcher(lines.get(i));
                            if(matcher.find()) {
                                System.out.println("匹配: " + path.toString() + " 行号: " + (i+1));
                            }
                        }
                    } catch(Exception e) {
                        // 忽略读取错误
                    }
                });
        } catch(Exception e) {
            System.out.println("搜索过程中发生错误: " + e.getMessage());
        }
    }

    private static void editFile(String[] args) {
        if(args.length < 4) {
            System.out.println("用法: editFile <文件路径> <目标字符串> <替换字符串>");
            return;
        }
        String filePath = args[1];
        String target = args[2];
        String replacement = args[3];
        try {
            Path path = Paths.get(filePath);
            String content = new String(Files.readAllBytes(path));
            if(content.contains(target)) {
                content = content.replace(target, replacement);
                Files.write(path, content.getBytes());
                System.out.println("文件编辑成功。");
            } else {
                System.out.println("目标字符串未找到。");
            }
        } catch(Exception e) {
            System.out.println("编辑文件时发生错误: " + e.getMessage());
        }
    }

    private static void fileSearch(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供文件名片段。");
            return;
        }
        String fragment = args[1];
        String startDir = args.length >= 3 ? args[2] : ".";
        try {
            Files.walk(Paths.get(startDir))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    if(path.getFileName().toString().contains(fragment)) {
                        System.out.println("匹配文件: " + path.toString());
                    }
                });
        } catch(Exception e) {
            System.out.println("搜索过程中发生错误: " + e.getMessage());
        }
    }

    private static void deleteFile(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供要删除的文件路径。");
            return;
        }
        String filePath = args[1];
        try {
            boolean deleted = Files.deleteIfExists(Paths.get(filePath));
            if(deleted) {
                System.out.println("文件已删除。");
            } else {
                System.out.println("文件不存在或未删除。");
            }
        } catch(Exception e) {
            System.out.println("删除文件时发生错误: " + e.getMessage());
        }
    }

    private static void reapply(String[] args) {
        // 目前未实现 reapply 功能
        System.out.println("reapply 功能暂未实现。");
    }

    private static void checkProject(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供要检查的项目路径。");
            return;
        }
        String projectPath = args[1];
        boolean verbose = false;
        if(args.length >= 3 && args[2].equalsIgnoreCase("verbose")) {
            verbose = true;
        }
        JavaProjectChecker checker = new JavaProjectChecker(projectPath, verbose);
        boolean result = checker.runChecks();
        if(!result) {
            System.out.println("项目检查发现问题，请进行修复。");
        } else {
            System.out.println("项目检查未发现问题。");
        }
    }

    private static void checkFrontend(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供要检查的前端项目路径。");
            return;
        }
        String projectPath = args[1];
        boolean verbose = false;
        if(args.length >= 3 && args[2].equalsIgnoreCase("verbose")) {
            verbose = true;
        }
        System.out.println("检查前端项目: " + projectPath);
        if(Files.exists(Paths.get(projectPath, "package.json"))) {
            System.out.println("[INFO] package.json 文件已找到。");
        } else {
            System.out.println("[WARN] 未找到 package.json 文件。");
        }
        if(Files.isDirectory(Paths.get(projectPath, "node_modules"))) {
            System.out.println("[INFO] node_modules 目录已找到。");
        } else {
            System.out.println("[WARN] 未找到 node_modules 目录。");
        }
        if(Files.exists(Paths.get(projectPath, "index.html"))) {
            System.out.println("[INFO] index.html 文件已找到。");
        } else if(Files.exists(Paths.get(projectPath, "public", "index.html"))) {
            System.out.println("[INFO] public/index.html 文件已找到。");
        } else {
            System.out.println("[WARN] 未找到 index.html 文件。");
        }
        System.out.println("前端项目检查完成。");
    }

    private static void checkPython(String[] args) {
        if(args.length < 2) {
            System.out.println("请提供要检查的 Python 项目路径。");
            return;
        }
        String projectPath = args[1];
        boolean verbose = false;
        if(args.length >= 3 && args[2].equalsIgnoreCase("verbose")) {
            verbose = true;
        }
        System.out.println("检查 Python 项目: " + projectPath);
        if(Files.exists(Paths.get(projectPath, "requirements.txt"))) {
            System.out.println("[INFO] requirements.txt 文件已找到。");
        } else {
            System.out.println("[WARN] 未找到 requirements.txt 文件。");
        }
        if(Files.exists(Paths.get(projectPath, "setup.py"))) {
            System.out.println("[INFO] setup.py 文件已找到。");
        } else {
            System.out.println("[WARN] 未找到 setup.py 文件。");
        }
        try {
            boolean foundPy = Files.walk(Paths.get(projectPath))
                    .filter(Files::isRegularFile)
                    .anyMatch(path -> path.toString().endsWith(".py"));
            if(foundPy) {
                System.out.println("[INFO] 找到了 Python 源代码文件。");
            } else {
                System.out.println("[WARN] 未找到任何 Python 源代码文件。");
            }
        } catch(IOException e) {
            System.out.println("检查 Python 文件时出现错误: " + e.getMessage());
        }
        System.out.println("Python 项目检查完成。");
    }
}

class JavaProjectChecker {
    private String projectPath;
    private boolean verbose;
    private List<String> issues;
    private boolean hasMavenPom;
    private boolean hasGradleBuild;
    private boolean hasJavaVersion;
    private boolean hasSourceDir;
    private boolean hasTestDir;
    private boolean hasDockerfile;
    private String javaVersion;
    
    public JavaProjectChecker(String projectPath, boolean verbose) {
        this.projectPath = projectPath;
        this.verbose = verbose;
        this.issues = new ArrayList<>();
        this.hasMavenPom = false;
        this.hasGradleBuild = false;
        this.hasJavaVersion = false;
        this.hasSourceDir = false;
        this.hasTestDir = false;
        this.hasDockerfile = false;
        this.javaVersion = null;
    }
    
    private void log(String message) {
        if (verbose) {
            System.out.println("[INFO] " + message);
        }
    }
    
    public boolean runChecks() {
        System.out.println("检查Java项目: " + projectPath);
        log("开始检查Java项目: " + projectPath);
        
        checkBuildTools();
        checkJavaVersion();
        checkProjectStructure();
        checkDockerSupport();
        
        if (!issues.isEmpty()) {
            System.out.println("\n不符合规范的问题:");
            for (int i = 0; i < issues.size(); i++) {
                System.out.println((i + 1) + ". " + issues.get(i));
            }
            return false;
        } else {
            System.out.println("恭喜！项目符合规范要求。");
            return true;
        }
    }
    
    private void checkBuildTools() {
        // 检查Maven配置
        File pomFile = new File(projectPath, "pom.xml");
        if (pomFile.exists()) {
            hasMavenPom = true;
            log("找到Maven配置文件: pom.xml");
        }
        
        // 检查Gradle配置
        File gradleFile = new File(projectPath, "build.gradle");
        File gradleKtsFile = new File(projectPath, "build.gradle.kts");
        if (gradleFile.exists() || gradleKtsFile.exists()) {
            hasGradleBuild = true;
            log("找到Gradle配置文件: " + (gradleFile.exists() ? "build.gradle" : "build.gradle.kts"));
        }
        
        if (!hasMavenPom && !hasGradleBuild) {
            issues.add("没有找到Maven或Gradle构建配置文件");
        }
    }
    
    private void checkJavaVersion() {
        // 检查.sdkmanrc文件
        File sdkmanRcFile = new File(projectPath, ".sdkmanrc");
        if (sdkmanRcFile.exists()) {
            try {
                String content = Files.readString(sdkmanRcFile.toPath());
                if (content.contains("JAVA_VERSION")) {
                    hasJavaVersion = true;
                    // 提取Java版本
                    String[] lines = content.split("\\r?\\n");
                    for (String line : lines) {
                        if (line.trim().startsWith("JAVA_VERSION")) {
                            javaVersion = line.split("=")[1].trim();
                            log("找到Java版本配置: " + javaVersion);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log("读取.sdkmanrc文件时出错: " + e.getMessage());
            }
        }
        
        // 检查Maven中的Java版本
        if (hasMavenPom) {
            try {
                Path pomPath = Paths.get(projectPath, "pom.xml");
                String content = Files.readString(pomPath);
                if (content.contains("<maven.compiler.source>") || content.contains("<java.version>")) {
                    hasJavaVersion = true;
                    log("在pom.xml中找到Java版本配置");
                }
            } catch (Exception e) {
                log("读取pom.xml文件时出错: " + e.getMessage());
            }
        }
        
        // 检查Gradle中的Java版本
        if (hasGradleBuild) {
            try {
                Path gradlePath = Paths.get(projectPath, "build.gradle");
                if (!Files.exists(gradlePath)) {
                    gradlePath = Paths.get(projectPath, "build.gradle.kts");
                }
                
                if (Files.exists(gradlePath)) {
                    String content = Files.readString(gradlePath);
                    if (content.contains("sourceCompatibility") || content.contains("targetCompatibility")) {
                        hasJavaVersion = true;
                        log("在Gradle文件中找到Java版本配置");
                    }
                }
            } catch (Exception e) {
                log("读取Gradle文件时出错: " + e.getMessage());
            }
        }
        
        if (!hasJavaVersion) {
            issues.add("未明确指定Java版本");
        }
    }
    
    private void checkProjectStructure() {
        // 检查源代码目录
        String[] sourceDirs = {
            "src/main/java",
            "src/main/kotlin",
            "src/main/scala",
            "src"
        };
        
        for (String dir : sourceDirs) {
            File sourceDir = new File(projectPath, dir);
            if (sourceDir.exists() && sourceDir.isDirectory()) {
                hasSourceDir = true;
                log("找到源代码目录: " + dir);
                break;
            }
        }
        
        if (!hasSourceDir) {
            issues.add("缺少标准的源代码目录");
        }
        
        // 检查测试目录
        String[] testDirs = {
            "src/test/java",
            "src/test/kotlin",
            "src/test/scala",
            "test"
        };
        
        for (String dir : testDirs) {
            File testDir = new File(projectPath, dir);
            if (testDir.exists() && testDir.isDirectory()) {
                hasTestDir = true;
                log("找到测试目录: " + dir);
                break;
            }
        }
        
        if (!hasTestDir) {
            issues.add("缺少测试目录");
        }
        
        // 检查README文件
        File[] readmeFiles = new File(projectPath).listFiles((dir, name) -> 
            name.toLowerCase().startsWith("readme"));
            
        if (readmeFiles == null || readmeFiles.length == 0) {
            issues.add("缺少README文件");
        } else {
            log("找到README文件: " + readmeFiles[0].getName());
        }
    }
    
    private void checkDockerSupport() {
        // 检查Dockerfile
        File dockerfile = new File(projectPath, "Dockerfile");
        if (dockerfile.exists()) {
            hasDockerfile = true;
            log("找到Dockerfile");
        } else {
            log("未找到Dockerfile");
        }
        
        // 检查docker-compose.yml
        File dockerCompose = new File(projectPath, "docker-compose.yml");
        File dockerComposeYaml = new File(projectPath, "docker-compose.yaml");
        
        if (dockerCompose.exists() || dockerComposeYaml.exists()) {
            log("找到docker-compose文件");
        } else {
            log("未找到docker-compose文件");
        }
        
        if (!hasDockerfile) {
            issues.add("未找到Docker支持文件（推荐添加Dockerfile）");
        }
    }
    
    // Getters
    public String getProjectPath() {
        return projectPath;
    }
    
    public List<String> getIssues() {
        return issues;
    }
    
    public boolean hasMavenPom() {
        return hasMavenPom;
    }
    
    public boolean hasGradleBuild() {
        return hasGradleBuild;
    }
    
    public boolean hasJavaVersion() {
        return hasJavaVersion;
    }
    
    public boolean hasSourceDir() {
        return hasSourceDir;
    }
    
    public boolean hasTestDir() {
        return hasTestDir;
    }
    
    public boolean hasDockerfile() {
        return hasDockerfile;
    }
}

class JavaProjectMigrator {
    private JavaProjectChecker checker;
    private boolean verbose;
    private String projectPath;
    
    public JavaProjectMigrator(JavaProjectChecker checker, boolean verbose) {
        this.checker = checker;
        this.verbose = verbose;
        this.projectPath = checker.getProjectPath();
    }
    
    private void log(String message) {
        if (verbose) {
            System.out.println("[MIGRATE] " + message);
        }
    }
    
    public void runMigration() {
        log("开始迁移Java项目: " + projectPath);
        
        if (!checker.hasMavenPom() && !checker.hasGradleBuild()) {
            createMavenPom();
        }
        
        if (!checker.hasJavaVersion()) {
            createJavaVersionConfig();
        }
        
        if (!checker.hasSourceDir()) {
            createSourceDirectory();
        }
        
        if (!checker.hasTestDir()) {
            createTestDirectory();
        }
        
        if (!checker.hasDockerfile()) {
            createDockerfile();
        }
        
        System.out.println("迁移完成！请检查项目结构和配置。");
    }
    
    private void createMavenPom() {
        log("创建pom.xml文件...");
        
        String pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
            "    <modelVersion>4.0.0</modelVersion>\n\n" +
            "    <groupId>com.example</groupId>\n" +
            "    <artifactId>" + new File(projectPath).getName() + "</artifactId>\n" +
            "    <version>1.0-SNAPSHOT</version>\n\n" +
            "    <properties>\n" +
            "        <maven.compiler.source>17</maven.compiler.source>\n" +
            "        <maven.compiler.target>17</maven.compiler.target>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "    </properties>\n\n" +
            "    <dependencies>\n" +
            "        <dependency>\n" +
            "            <groupId>org.junit.jupiter</groupId>\n" +
            "            <artifactId>junit-jupiter-api</artifactId>\n" +
            "            <version>5.9.2</version>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "</project>\n";
            
        try {
            Files.writeString(Paths.get(projectPath, "pom.xml"), pomContent);
            log("已创建pom.xml文件");
        } catch (Exception e) {
            System.err.println("创建pom.xml文件失败: " + e.getMessage());
        }
    }
    
    private void createJavaVersionConfig() {
        log("创建.sdkmanrc文件...");
        
        String sdkmanRcContent = "# 使用SDKMAN进行Java版本管理\n" +
            "JAVA_VERSION=17.0.6-tem\n";
            
        try {
            Files.writeString(Paths.get(projectPath, ".sdkmanrc"), sdkmanRcContent);
            log("已创建.sdkmanrc文件，指定Java版本: 17.0.6-tem");
        } catch (Exception e) {
            System.err.println("创建.sdkmanrc文件失败: " + e.getMessage());
        }
    }
    
    private void createSourceDirectory() {
        log("创建源代码目录结构...");
        
        try {
            // 创建主要源代码目录
            Path srcMainJava = Paths.get(projectPath, "src/main/java");
            Files.createDirectories(srcMainJava);
            
            // 创建示例Java文件
            Path exampleClass = srcMainJava.resolve("Application.java");
            String classContent = "public class Application {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, Java World!\");\n" +
                "    }\n" +
                "}\n";
                
            Files.writeString(exampleClass, classContent);
            log("已创建源代码目录和示例文件");
        } catch (Exception e) {
            System.err.println("创建源代码目录失败: " + e.getMessage());
        }
    }
    
    private void createTestDirectory() {
        log("创建测试目录结构...");
        
        try {
            // 创建测试目录
            Path srcTestJava = Paths.get(projectPath, "src/test/java");
            Files.createDirectories(srcTestJava);
            
            // 创建示例测试文件
            Path testClass = srcTestJava.resolve("ApplicationTest.java");
            String testContent = "import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.*;\n\n" +
                "public class ApplicationTest {\n" +
                "    @Test\n" +
                "    public void testExample() {\n" +
                "        assertTrue(true, \"This test should always pass\");\n" +
                "    }\n" +
                "}\n";
                
            Files.writeString(testClass, testContent);
            log("已创建测试目录和示例测试文件");
        } catch (Exception e) {
            System.err.println("创建测试目录失败: " + e.getMessage());
        }
    }
    
    private void createDockerfile() {
        log("创建Dockerfile...");
        
        String dockerfileContent = "FROM eclipse-temurin:17-jdk-alpine\n" +
            "WORKDIR /app\n\n" +
            "COPY target/*.jar app.jar\n\n" +
            "ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]\n";
            
        try {
            Files.writeString(Paths.get(projectPath, "Dockerfile"), dockerfileContent);
            log("已创建Dockerfile");
            
            // 创建docker-compose.yml
            String dockerComposeContent = "version: '3.8'\n\n" +
                "services:\n" +
                "  app:\n" +
                "    build: .\n" +
                "    ports:\n" +
                "      - \"8080:8080\"\n" +
                "    environment:\n" +
                "      - SPRING_PROFILES_ACTIVE=dev\n";
                
            Files.writeString(Paths.get(projectPath, "docker-compose.yml"), dockerComposeContent);
            log("已创建docker-compose.yml");
        } catch (Exception e) {
            System.err.println("创建Docker配置文件失败: " + e.getMessage());
        }
    }
} 
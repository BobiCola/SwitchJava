# SwitchJava

一个轻量的 JavaFX 桌面 JDK 管理器。集中保存本地 JDK，在 Windows 上按需写入用户或系统 `JAVA_HOME`。

## 下载发行版

从 [最新发行版](https://github.com/BobiCola/SwitchJava/releases/latest) 下载 `SwitchJava-1.1.0-windows.jar`（Windows x64，需要 Java 17+，内含 JavaFX，不含 Java 运行时），运行：

```powershell
java -jar SwitchJava-1.1.0-windows.jar
```

校验值见发行附件 `SHA256SUMS.txt`。源码 ZIP 不是应用发行包。旧版保留，不覆盖旧标签。

## 功能

- **清晰的双栏界面**：版本列表、安装路径、启动时环境变量和操作结果集中展示，可调整窗口大小。
- **本地 JDK 管理**：目录选择、名称/路径去重、`bin/java` 和 `bin/javac` 存在性检查。
- **异步切换**：后台运行 `setx.exe`，检查退出码，15 秒超时；失败不会显示为成功。
- **可选作用域**：默认用户变量，无需管理员；系统变量需以管理员身份启动应用。
- **可靠保存**：独立 UTF-8 配置、临时文件替换、保存成功后才更新列表。
- **旧版迁移**：检测到旧配置后询问是否导入，不修改原文件。
- **安全删除**：二次确认，只删除配置，不删除 JDK 安装目录。

## 环境要求

| 项目 | 要求 |
| --- | --- |
| 构建与运行 | JDK 17 或更高版本，需图形桌面 |
| 构建工具 | Maven 3.8+，或仓库自带 Maven Wrapper |
| 环境变量切换 | Windows |
| macOS / Linux | 可运行界面、管理配置；不支持写入环境变量 |

首次构建需要联网下载 Maven / JavaFX / JUnit 依赖。被管理的 JDK 与运行本工具的 JDK 可以不同。

## 快速开始

```bash
git clone https://github.com/BobiCola/SwitchJava.git
cd SwitchJava
```

Windows PowerShell：

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

macOS / Linux：

```bash
sh mvnw clean verify
sh mvnw javafx:run
```

已安装 Maven 时，也可使用 `mvn clean verify` 和 `mvn javafx:run`。普通 `package` 生成的 JAR 不包含 JavaFX 运行时，不是可双击的独立安装包。

## 使用方式

1. 点击 **添加 JDK**，输入版本名称并选择安装根目录，例如 `C:\Program Files\Java\jdk-21`，不要选择 `bin`。
2. 在左侧选择版本，检查右侧路径。
3. 默认更新当前用户的 `JAVA_HOME`；如需更新系统变量，先以管理员身份启动应用，再勾选系统变量选项。
4. 点击 **切换到此版本**，查看操作结果。
5. 重新打开终端并验证：

```powershell
$env:JAVA_HOME
& "$env:JAVA_HOME\bin\java.exe" -version
java -version
where.exe java
```

### 关于 PATH 和生效范围

工具**只写入 JAVA_HOME，不修改 PATH，也不会清空旧变量后再写入**。请自行检查 PATH 中的 Java 路径及其优先级；可以按自己的环境配置 `%JAVA_HOME%\bin`，但已有 Java shim 或固定路径可能优先。

`setx` 成功仅表示持久化写入命令成功，不表示当前终端已经切换。应用、IDE 和已打开的终端仍保留启动时的环境，需要重新启动；某些环境需要注销后重新登录。右侧“启动时 JAVA_HOME”不会冒充写入后的系统实时值。

用户变量可能覆盖系统变量。切换系统作用域前请检查是否存在用户级 `JAVA_HOME`；切换用户作用域后也请用新终端核实最终结果。

## 配置与迁移

配置位置：`~/.switchjava/profiles.properties`（Windows 通常为 `%USERPROFILE%\.switchjava\profiles.properties`）。名称和路径支持空格、逗号及中文。

旧版本使用 `~/settings.txt`，每行 `名称,路径`。新配置为空且发现旧文件时，会提示导入。导入按第一个逗号分隔名称和路径；旧版名称内含逗号的记录需手动修正。旧文件保持原样，仓库内的示例 `settings.txt` 不会自动写入用户环境。

读取配置失败时，界面会提示错误并禁止添加，以免覆盖原数据。请先备份配置，修复或移走损坏文件后重启。

## 开发与测试

```bash
sh mvnw clean verify
```

JUnit 测试覆盖配置往返、中文/逗号路径、损坏配置、旧格式导入、JDK 目录校验、命令参数边界和非 Windows 平台保护。测试不执行真实的 Windows 环境变量写入。GitHub Actions 在 Windows、Linux、macOS 上运行构建与测试。

核心文件：

```text
src/main/java/smg/switchjava/
  SwitchJavaApplication.java  JavaFX 界面与后台任务
  JdkProfile.java             JDK 配置与目录校验
  ProfileStore.java           配置持久化和旧版导入
  JavaHomeService.java        Windows JAVA_HOME 写入
src/main/resources/styles/app.css
src/test/java/smg/switchjava/ProfileStoreTest.java
```

### 手工回归

- 空配置启动、添加有效/无效目录、重复添加、取消删除、确认删除、重启读取。
- Windows 用户作用域成功写入；系统作用域非管理员失败、管理员写入成功；新终端核验实际 Java 路径。
- 中文及空格路径、旧配置导入确认/取消、损坏配置提示。
- 切换期间界面不冻结，不能同时删除配置或重复切换。

当前不提供自动下载 JDK、自动修改 PATH、跨平台 shell 配置修改或独立 EXE 安装包。

Windows 发行包可通过 `mvn -Pwindows-release clean verify` 重建，产物为 `target/SwitchJava-1.1.0-windows.jar`。发行工作流在 Windows 上执行打包 JAR 的界面启动测试，通过后才发布附件。

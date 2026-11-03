# SwitchJava v1.1.0

## 下载与运行

下载 **SwitchJava-1.1.0-windows.jar**，不要下载 Source code 代替应用包。

适用：**Windows x64 + Java 17 或更高版本**。包内已包含 Windows JavaFX 依赖，但不包含 Java 运行时；不是独立 EXE。

```powershell
java -jar SwitchJava-1.1.0-windows.jar
```

`SHA256SUMS.txt` 提供文件校验值。系统级变量切换需要以管理员身份运行；默认仅写入用户级 `JAVA_HOME`，不改 PATH。切换后重新打开终端核验。

## 更新

- 全新浅色双栏界面、版本详情和操作反馈。
- 目录选择、JDK 校验、重复配置检查与删除确认。
- 后台切换、退出码检查与超时保护，不再把命令失败提示为成功。
- 独立 UTF-8 配置、原子替换保存、确认导入旧版 `settings.txt`。
- 更新 JavaFX 17 补丁版本，重写中文文档。

## 验证范围

发行工作流执行单元测试，并在 Windows 上使用实际发行 JAR 启动 JavaFX 主窗口、加载样式和布局；测试不会修改真实 `JAVA_HOME`。管理员权限及新终端的环境变量生效仍需实际使用时核验。

旧版 v1.0.1 保留用于回退。macOS / Linux 请从源码运行配置管理功能，不要使用 Windows JAR。

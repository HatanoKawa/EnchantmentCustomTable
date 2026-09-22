# macOS 开发客户端 GUI 验证

这个实验为 NeoForge `1.21.1` 的开发客户端提供一个可识别的 macOS 应用身份，
用于让桌面自动化工具定位真实的 Minecraft 窗口、截图及发送键鼠输入。
不修改模组逻辑、常规 `runClient` 配置或发布产物。

## 启动

在本工作目录根部执行：

```sh
./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle \
  :1.21.1:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --launch
```

当前机器在独立 worktree 中使用以下 Gradle Java 路径验证成功：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home \
  ./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle \
  :1.21.1:exportGuiClientLaunch
```

这里的 Gradle JVM 为 Java 25；游戏仍使用项目的 Java 21 toolchain。
还需要 macOS 自带的 `codesign` 和 Python 3。更改源码、依赖、JDK 或移动工作目录后，
重新执行导出命令。重新打包前先退出当前测试客户端；同一时间仅运行一个该实验客户端。

生成的应用位于：

```text
versions/1.21.1/build/gui-validation/ECT GUI Dev.app
```

自动化工具应通过该 `.app` 的绝对路径选择应用。
游戏工作目录、存档和截图仍在本 worktree 的 `versions/1.21.1/run/` 中。
所有生成文件都在已忽略的构建/运行目录内。

## 实现

1. 独立 Gradle init script 从现有 `runClient` 任务导出 Java toolchain、
   JVM/程序参数、ModDev classpath、工作目录和显式环境覆盖；执行该任务的准备依赖。
2. Python 脚本把所选 JDK 的原生 `java` 启动器复制进本地 `.app`，添加 bundle identity，
   并用符号链接连接该 JDK 的 `libjli.dylib`。
3. 对生成的应用进行本地 ad-hoc 签名，先验证 `-version`，再用导出的参数启动游戏。
   不修改或重新签名原始 JDK。

必须让实际 JVM 进程来自 `.app` 内的原生可执行文件。仅用 shell 脚本启动另一个
`java` 进程不能达到本实验的应用身份效果。

这不是可分发的应用包，也不支持直接在 Finder 双击启动；它引用本机 JDK 和 Gradle
缓存，并依赖 Python 启动时传入的参数。导出逻辑针对当前 ModDevGradle 的
`RunGameTask` API；插件升级后需要重新验证。尚未扩展到 Fabric、其他 Minecraft
版本或其他系统。

普通 `codesign --verify` 已通过。由于 `libjli.dylib` 符号链接指向包外的原 JDK，
`codesign --verify --strict` 会拒绝其链接目标；这也是本工具只作为本机开发启动入口、
不能作为独立分发包的一个限制。

## GUI 操作边界

- 本机已开启 Codex Computer Use 的屏幕读取和设备控制权限。实验没有更改这些权限。
- Minecraft 的游戏内按钮/槽位没有完整的系统辅助功能树；实际操作依据截图定位。
- 已验证点击、右键打开方块、Esc、普通文字、Tab 补全和 F2 截图。
- 当前输入工具对部分特殊字符（例如模组 ID 中的 `_`、`:`）存在丢失现象，
  剪贴板粘贴曾超时，组合键也需要逐次确认；不能据此承诺任意输入都可靠。
- 测试命令可用 `Dev` 代替 `@s`，用 Minecraft 的 Tab 补全输入完整注册 ID。
  每一步应读取截图确认结果，避免盲目发送长操作序列。
- 该方案证明可以自主执行离散 GUI 冒烟检查；尚未证明实时战斗/移动控制、
  长时间无人值守或全版本 GUI 回归的可靠性。

## 验证记录

见 [2026-09-22 验证记录](validation-2026-09-22.md)。

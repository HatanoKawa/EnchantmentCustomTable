# macOS 开发客户端 GUI 验证

这个实验为 NeoForge 和 Fabric `1.21.1` 的开发客户端提供可识别的 macOS 应用身份，
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

Fabric 使用独立导出目标和应用身份：

```sh
./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle -PguiLoader=fabric \
  :fabric_1_21_1:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --loader fabric --launch
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
fabric_versions/1.21.1/build/gui-validation/ECT GUI Fabric.app
```

自动化工具应通过该 `.app` 的绝对路径选择应用。
游戏工作目录、存档和截图分别在本 worktree 的 `versions/1.21.1/run/` 和
`fabric_versions/1.21.1/run/` 中。
所有生成文件都在已忽略的构建/运行目录内。

## 实现

1. 独立 Gradle init script 从现有 `runClient` 任务导出 Java toolchain、
   JVM/程序参数、ModDev/Loom classpath、工作目录和显式环境覆盖；执行该任务的准备依赖。
2. Python 脚本把所选 JDK 的原生 `java` 启动器复制进本地 `.app`，添加 bundle identity，
   并用符号链接连接该 JDK 的 `libjli.dylib`。
3. 对生成的应用进行本地 ad-hoc 签名，先验证 `-version`，再用导出的参数启动游戏。
   不修改或重新签名原始 JDK。

必须让实际 JVM 进程来自 `.app` 内的原生可执行文件。仅用 shell 脚本启动另一个
`java` 进程不能达到本实验的应用身份效果。

这不是可分发的应用包，也不支持直接在 Finder 双击启动；它引用本机 JDK 和 Gradle
缓存，并依赖 Python 启动时传入的参数。导出逻辑使用当前 ModDevGradle 的
`RunGameTask` 和 Loom 的 `AbstractRunTask` API；插件升级后需要重新验证。
尚未扩展到其他 Minecraft 版本或其他系统。

普通 `codesign --verify` 已通过。由于 `libjli.dylib` 符号链接指向包外的原 JDK，
`codesign --verify --strict` 会拒绝其链接目标；这也是本工具只作为本机开发启动入口、
不能作为独立分发包的一个限制。

## GUI 操作边界

- 本机已开启 Codex Computer Use 的屏幕读取和设备控制权限。实验没有更改这些权限。
- Minecraft 的游戏内按钮/槽位没有完整的系统辅助功能树；实际操作依据截图定位。
- 已验证点击、右键打开方块、Esc、普通文字、Tab 补全和 F2 截图。
- 当前输入工具对部分特殊字符（例如模组 ID 中的 `_`、`:`）存在丢失现象，
  剪贴板粘贴曾超时，组合键也需要逐次确认；不能据此承诺任意输入都可靠。
- 本轮使用下述纯字母函数别名准备场景，函数内部使用 `@s`，适配不同开发玩家名。
  每一步应读取截图确认结果，避免依赖异步 Tab 补全或盲目发送长操作序列。
- 当前工具没有带修饰键的鼠标点击接口；单独发送 `Shift_L` 被拒绝。
  Shift 取书和 Shift 插书仍需要人工补测或后续驱动支持，不能以普通点击结果代替。
- 该方案证明可以自主执行离散 GUI 冒烟检查；尚未证明实时战斗/移动控制、
  长时间无人值守或全版本 GUI 回归的可靠性。

## 验证记录

- [首轮双加载器用例](cases-1.21.1.md)：28 个用例，每个加载器分别执行。
- [2026-09-22 首轮结果与缺陷](round1-2026-09-22.md)：逐项结果、复现步骤、证据。
- [最初的窗口接入实验](validation-2026-09-22.md)：历史记录，保留当时结论。

## 可重复的场景准备

在专用、允许命令的创造超平坦世界中使用 `fixtures/` 数据包（MC 1.21.1，pack format 48）。
先保存退出世界，把整个 `fixtures/` 目录复制到对应
`run/saves/<测试世界>/datapacks/ectgui/`，再进入世界执行 `/reload`。
不要在日常存档安装：场景函数会清空执行者背包，重置 `0 -60 2` 的台子，并清除附近掉落物。

例如执行 `/function ectguicboundary` 后，通过鼠标向转换台放入材料并领取结果。
数据包只设置前置条件，不调用模组内部方法；业务动作必须通过真实 GUI 完成。
关闭菜单后执行 `/function ectguiinspect`，日志中的 `GUIBLOCK` 和 `GUIPLAYER`
是原版 `tellraw` 读取的服务端物品数据。将它与截图一起留存，区分显示问题和实际物品变化。
存档验证期间不能重新执行场景准备函数。

`/function ectguireloadlater` 会在 5 秒后执行 `/reload`，可立即重新打开菜单以测试重载边界。
所有函数都有纯字母别名；详细准备及预期结果见用例文档。截图可用 F2 保存至对应 `run/screenshots/`。
切换配置用例前退出客户端、备份配置，测试后恢复；保留配置副本与日志。

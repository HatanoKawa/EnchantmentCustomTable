> Current maintenance scope: **26.x**, default **26.3**. Export with an explicit supported `-PguiVersion` / `--minecraft` when following historical examples below. Earlier reports retain their original matrix and evidence.

# macOS 开发客户端 GUI 验证

本分支工具为 NeoForge 和 Fabric `26.x` 的开发客户端提供可识别的 macOS 应用身份，
用于让桌面自动化工具定位真实的 Minecraft 窗口、截图及发送键鼠输入。
启动工具本身不修改模组逻辑、常规 `runClient` 配置或发布产物。

全版本使用[全量/快速分级清单](tiered-matrix.md)。[第三轮结果](round3-2026-09-22.md)
已覆盖全部 30 个目标的可执行清单：548 本轮通过、52 复用通过、36 Shift 输入阻塞、0 待验。
两类旧存档迁移缺陷按用户要求降为低优先级搁置，独立于同版本 GUI 通过记录。
[第四轮记录](round4-2026-09-23.md)新增 26.3 和三个 legacy 分支；26.3 的 SDL 鼠标注入受阻，不能沿用第三轮的 GUI 通过结论。
[2026-09-26 Shift 补测](shift-2026-09-26.md)通过独立原生输入辅助程序完成 1.21.1 双端
E02/E06 及严格配置扩展，解除其中 4 项历史阻塞。
[矩阵补测与 26.3 排查](shift-matrix-2026-09-26.md)又完成其余主线/legacy 的 44 项 Shift
缺项；26.3 双端使用原生鼠标移动/点击后，E02/E06 另有 4 项通过。
[26.3 全量回归](full-26.3-2026-09-27.md)随后补齐每端剩余 25 项，累计双端各 30/30，
另通过严格 Shift 扩展。空候选页码的轻微显示差异单独记录；输入工具旧坐标问题仍使用原生方案处理。

## 启动

在本工作目录根部执行：

```sh
./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle \
  :26.3:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --launch
```

Fabric 使用独立导出目标和应用身份：

```sh
./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle -PguiLoader=fabric \
  :fabric_26_3:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --loader fabric --launch
```

切换版本时同时指定导出目标和启动参数，例如：

```sh
./gradlew --no-configuration-cache -I tools/gui-validation/export-client.init.gradle \
  -PguiLoader=fabric -PguiVersion=26.1 :fabric_26_1:exportGuiClientLaunch
python3 tools/gui-validation/macos_client.py --loader fabric --minecraft 26.1 --launch
```

`-PguiLoader=both -PguiVersion=all` 会在所有版本项目注册导出任务；仍需显式列出需要执行的任务。
每个版本读取对应 `runClient` 的 Java toolchain，26.x 自动使用 Java 25。
用户允许临时保持唤醒时，可在启动命令追加 `--keep-awake`。该选项通过 macOS
`caffeinate` 绑定实际游戏进程，游戏退出即解除，最长持续一小时；不修改系统电源设置。
它不能解锁已锁定的 Mac，也不能阻止用户主动锁屏。
各目标使用含加载器和版本的独立 bundle identifier，并在应用名后追加版本号；历史 1.21.1 的应用身份保持不变。

当前机器在独立 worktree 中使用以下 Gradle Java 路径验证成功：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home \
  ./gradlew --no-configuration-cache \
  -I tools/gui-validation/export-client.init.gradle \
  :26.3:exportGuiClientLaunch
```

这里的 Gradle JVM 与游戏工具链均为 Java 25。
还需要 macOS 自带的 `codesign` 和 Python 3。更改源码、依赖、JDK 或移动工作目录后，
重新执行导出命令。重新打包前先退出当前测试客户端；同一时间仅运行一个该实验客户端。

生成的应用位于：

```text
versions/26.3/build/gui-validation/ECT GUI Dev 26.3.app
fabric_versions/26.3/build/gui-validation/ECT GUI Fabric 26.3.app
```

自动化工具应通过该 `.app` 的绝对路径选择应用。
游戏工作目录、存档和截图分别在本 worktree 的 `versions/26.3/run/` 和
`fabric_versions/26.3/run/` 中。
所有生成文件都在已忽略的构建/运行目录内。

## Legacy 分支

在对应 `maint/1.18.x`、`maint/1.19.x` 或 `maint/1.20.x` 分支中使用 `-PguiLegacy=true -PguiLoader=both -PguiVersion=1.18.2`
导出 `:forge:exportGuiClientLaunch :fabric:exportGuiClientLaunch`，然后使用
`--legacy --loader forge` 或 `--legacy --loader fabric` 启动，版本参数须与分支一致。
这些分支保留 Java 17、Forge/Fabric 结构，不加入主线版本矩阵。
Gradle 8 的 JVM 参数属性与 ForgeGradle 的延迟运行配置须显式导出；只导出加载器环境覆盖，
不保存继承的 shell 环境。旧 Loom 重复追加的 `-XstartOnFirstThread` 在启动器中去重。

Forge 1.18.2 的原始 LWJGL 3.2.1 只有 Intel macOS 原生库。在 Apple Silicon 上，
可显式追加 `--legacy-forge-arm`，复用同分支 Fabric Loom 已准备的七个 LWJGL JAR 和 ARM 原生库。
该选项只适用于 `--legacy --loader forge --minecraft 1.18.2`，另写测试启动清单和 classpath 副本；
不修改普通 `runClient`、缓存或发布包。使用此选项的 GUI 证据须注明运行库差异。

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
导出已覆盖仓库所有版本；实际 GUI 通过范围以分层矩阵的逐目标记录为准，不支持其他系统。

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
- Computer Use 接口没有带修饰键的鼠标点击接口；单独发送 `Shift_L` 被拒绝。
  下述独立原生输入辅助程序已完成既有主线/legacy 矩阵全部 Shift 缺项，
  并完成 26.3 双端 E02/E06；快速组未安排的专项不能据此视为通过。
- 该方案证明可以自主执行离散 GUI 冒烟检查；尚未证明实时战斗/移动控制、
  长时间无人值守或全版本 GUI 回归的可靠性。

## 原生 Shift 与 26.3 鼠标输入

`macos_shift_click.swift` 是用户授权的测试输入辅助程序，只向明确列举的专用客户端发送
macOS CGEvent，不读取或调用 Minecraft/模组内部代码，不进入发布产物。
它复用本机已有辅助功能权限；权限不可用时拒绝执行，不自动请求或修改系统权限。

```sh
mkdir -p build/gui-validation
xcrun swiftc tools/gui-validation/macos_shift_click.swift -o build/gui-validation/shift-click
build/gui-validation/shift-click --check
# PID 必须来自本轮运行的专用客户端；坐标是窗口左上角起算的 macOS points。
build/gui-validation/shift-click <client-pid> <window-x-points> <window-y-points>
# 26.3：系统移动事件先更新游戏鼠标坐标，再发送普通左键/右键；也可只检查悬停。
build/gui-validation/shift-click --plain <client-pid> <window-x-points> <window-y-points>
build/gui-validation/shift-click --right <client-pid> <window-x-points> <window-y-points>
build/gui-validation/shift-click --move <client-pid> <window-x-points> <window-y-points>
```

调用前通过 Computer Use 确认菜单与槽位位置。本次 Retina 窗口为 854×508 points，
Computer Use 截图为 1708×1016 pixels（含标题栏），因此截图坐标除以 2 后传入；
其他缩放/窗口尺寸应重新校准。F2 截图不含标题栏，不能直接使用相同坐标。

程序仅接受源码中明确列举的 ECT 测试客户端 bundle identity，检查目标窗口、坐标、
当前修饰键与前台应用，再发送 Shift 按下、左键按下/抬起、Shift 抬起。
正常返回及抛错清理会释放输入；强制终止和并发人工输入未验证。
`--check` 同时报告系统修饰键状态；发现残留时先确认没有人工按键。只有明确是本工具
留下的左 Shift，才可在对应测试客户端前台执行 `--release-shift <client-pid>` 恢复。
不要用它释放用户正在按住的按键。点击结束后留有短暂间隔以便处理抬键事件。
事件发送到系统前台，执行时保持游戏前台且不要同时操作键鼠。成功回执仅表示发送完成，
必须继续用截图和 `/function ectguiinspect` 判定业务结果。

每端先运行 `/function ectguishiftcontrol`，验证 16 个泥土在原版箱子和背包之间双向
Shift 转移，再普通点击拿起物品，确认 Shift 已释放；通过后再执行 E02/E06。
当前允许 1.21.1、本轮补测矩阵中的主线/legacy 及 26.3 双端客户端。
26.3 使用 Computer Use 点击时观察到游戏仍按旧鼠标位置响应；原生移动加点击已在
双端标题菜单、原版箱子与模组槽位验证可用。该结果定位到输入适配边界，尚未确定
Computer Use/SDL 内部根因，也不代表其他系统或所有输入方式已验证。

`prepare_shift_matrix.py` 从同版本第三/四轮世界复制独立的 R5 世界，并备份配置/options。
导出对应客户端后执行 `--loader fabric --minecraft 26.2` 准备，客户端退出后用
`--config strict` 切换严格组，最终用 `--config restore` 恢复。legacy 另外指定
`--checkout <legacy-checkout> --legacy --loader forge --minecraft 1.20.1`。
脚本拒绝覆盖已有世界/备份；运行记录在 `build/reports/gui-validation/shift-matrix-2026-09-26/`。

## 验证记录

- [全版本全量/快速分层矩阵](tiered-matrix.md)：重点版本依据、30 项全量检查和 8 项快速场景。
- [首轮双加载器用例](cases-1.21.1.md)：28 个用例，每个加载器分别执行。
- [2026-09-22 首轮结果与缺陷](round1-2026-09-22.md)：逐项结果、复现步骤、证据。
- [2026-09-22 第二轮修复与回归](round2-2026-09-22.md)：合入 `dev` 后修复三个缺陷；双端各 26 项通过、2 项 Shift 输入阻塞。
- [2026-09-26 Shift 实测](shift-2026-09-26.md)：1.21.1 双端 E02/E06 及严格规则补测通过，含截图、服务端数据和环境恢复记录。
- [Shift 矩阵补测与 26.3 排查](shift-matrix-2026-09-26.md)：后续执行状态；待执行项不计通过。
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

`prepare_matrix.py` 从显式指定的同版本专用世界复制独立测试世界，按对应游戏 JAR
生成 pack 元数据及附魔组件命令，并备份已有配置和 options。它拒绝覆盖已准备的世界和备份。
这只准备前置条件，不操作游戏业务菜单。先用目标客户端创建允许命令的创造超平坦种子世界并关闭客户端；脚本依赖对应版本的本机 Loom 缓存。
`--seed-world` 只能是该目标 `run/saves` 下的目录名，也可使用 `{loader}` / `{minecraft}` 占位符；不要跨版本复用存档。

```sh
python3 tools/gui-validation/prepare_matrix.py --loader fabric --minecraft 26.1 --seed-world ECT-SEED
# 客户端退出后切换配置组
python3 tools/gui-validation/prepare_matrix.py --loader fabric --minecraft 26.1 --config strict-free
# 客户端退出后恢复原配置和 options（原来不存在的文件会恢复为不存在）
python3 tools/gui-validation/prepare_matrix.py --loader fabric --minecraft 26.1 --config restore
```

操作记录和原文件备份默认放在 `build/reports/gui-validation/round3/<loader>/<version>/`。

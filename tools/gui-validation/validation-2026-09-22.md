# 2026-09-22 GUI 验证记录

## 结论

在本机为真实 JVM 提供原生 macOS `.app` 身份后，Codex Computer Use 可以识别
Minecraft 窗口、读取画面、点击和输入，并独立完成转换台的实际操作。
不需要改动模组业务代码或发布构建。普通 `runClient` 的进程定位问题通过可选的开发
启动入口绕过；不能据此推断任意 Java 程序都不受支持。

本次验证还发现转换台材料消耗在存档重载后回滚的问题。**GUI 自动化通路成立，
但基准版本没有通过库存持久化检查。** 本分支只提供启动工具和证据，保留原逻辑供修复对照。

## 基准与隔离

- 原 `dev` 工作内容先提交为 `46726cb`：`fix: consume books inserted into Fabric generated slots`。
- 从该提交创建分支 `codex/macos-gui-validation`，独立 worktree：
  `/Users/river_quinn/workspace/learn/EnchantmentCustomTable-gui-validation`。
- Minecraft `1.21.1`，NeoForge `21.1.136`，模组 `2.0.2`，Temurin Java `21.0.11`。
- 新建测试世界 `ECT-GUI-1211`：创造模式、允许命令、和平难度、超平坦、种子 `1211`、
  关闭结构生成。未使用原工作目录的存档。
- 创建存档、执行命令、操作菜单、退出游戏均通过实际游戏 GUI。
- 曾临时加入只读键盘事件诊断以排查输入问题；最终复测已移除诊断源目录配置，
  重新编译。确认最终 class 输出不含 `GuiInputTrace.class`，最终日志不含 `GUI-TRACE`。
- 最终客户端由游戏内正常退出，进程退出码 `0`。

## 已执行检查

| 项目 | 结果与范围 |
| --- | --- |
| 应用识别和截图 | 成功，以生成 `.app` 的绝对路径选中真实 Minecraft 窗口 |
| 模组加载 | Mods 页面显示 EnchantmentCustomTable `2.0.2` |
| 世界创建与加载 | 成功创建隔离世界，并多次保存退出/重载 |
| 命令与右键交互 | 成功放置转换台、给予材料、传送定位、右键打开菜单 |
| 搜索 | `sharpness` 显示匹配的附魔书；`zzzzzz` 无匹配，页码显示 `-/-` |
| 翻页 | 从 `1/2` 切到 `2/2`，结果槽随之变化 |
| 拿取结果 | 普通点击领取 Sharpness V 附魔书并放入快捷栏 |
| 当次材料消耗 | 界面和服务端方块数据均从 8 本书/64 绿宝石变为 7 本书/28 绿宝石 |
| 存档持久化 | **失败**：保存退出、重进后材料恢复为 8/64，领取的附魔书仍在玩家背包 |

验证的是 NeoForge 1.21.1 的转换台冒烟流程，不包含 Fabric GUI、自定义附魔台的全部
功能、shift-click/拖拽/自动化管道、多人服务器或所有版本矩阵。输入特殊字符、粘贴和
组合键的可靠性仍有限；本次通过游戏自带补全及简单命令完成操作。

## 库存回滚复现

1. 在测试世界放置 `enchantment_custom_table:enchantment_conversion_table`，
   本次位置为 `0 -60 2`。
2. 在书本槽放 8 本书、支付槽放 64 个绿宝石，搜索 `sharpness`。
3. 普通点击生成槽，领取一册 Sharpness V 附魔书并放入玩家背包。
4. 关闭菜单，执行 `/data get block 0 -60 2`。本次服务端确认书本 7、绿宝石 28。
5. 游戏菜单选择 Save and Quit to Title，然后重新进入同一世界。
6. 再次执行上述命令，材料变为书本 8、绿宝石 64；执行
   `/data get entity Dev Inventory` 确认领取的附魔书仍在。

最终无诊断版本的日志：

```text
16:08:18  block Inventory: book=7, emerald=28
16:09:17  block Inventory after world reload: book=8, emerald=64
16:09:52  player Inventory: two enchanted books, both sharpness=5
```

玩家的两本书分别来自首次探索和最终复测各一次领取。最终复测在同一客户端进程内
完成保存和重载，期间没有替换程序、复制存档或修改方块数据，因此回滚不能归因于
移除诊断后换用旧存档。根因尚未定位，本记录不把它直接归因于某个实现细节。

## 证据与复核命令

本地证据保存在忽略目录
[`build/reports/gui-validation/2026-09-22/`](../../build/reports/gui-validation/2026-09-22/)，
没有将运行存档或机器相关的启动参数提交到 Git：

- `01-search-before.png`：领取前搜索和材料。
- `02-purchase-after.png`：领取后材料与 Sharpness V 提示。
- `03-reload-restored-materials.png`：重载后恢复的材料与两本玩家附魔书。
- `04-page-two.png`、`05-no-search-results.png`：翻页和无匹配结果。
- `06-player-inventory-data.png`：原版命令返回的玩家物品组件。
- `inventory-evidence.log`：库存命令和保存过程的原始日志摘录。
- `gui-client-final-console.log`：最终完整客户端日志。
- `gui-export-final.log`：最终 Gradle 导出/编译记录。

启动步骤见 [README](README.md)。另外执行了 Python 语法检查、帮助入口检查、
生成应用的普通 `codesign --verify`，以及 Git diff 空白检查，均通过。
另外尝试了 `codesign --verify --strict`，由于开发包的 `libjli.dylib` 链接指向外部
JDK 而失败；本工具不提供可分发的独立应用包，详见 README 的限制说明。
保存原工作内容前执行了
`:common:test :fabric_1_21_1:test :fabric_1_21_2:test :fabric_26_2:test`；
任务成功，测试结果为 `UP-TO-DATE` 复用，不能称为本次重新运行全部测试。

## 对 Jev 协作评估的影响

目前已实际证明由 Codex 独立完成上述离散 GUI 验证可行；本实验未接入 Jev，也没有
测量其端到端加速效果。先前资料中的 Jev 1.13 是文本/结构化决策模型，不能直接接收
这些游戏截图，需要先由视觉观察层提取候选动作和状态，再交给它决策。
它不会直接解决这里的窗口身份、特殊字符输入或状态持久化问题。

参考：[Jev 模型说明](https://docs.typesafe.ai/models)、
[API](https://docs.typesafe.ai/api)。后续若评估协作收益，应在相同用例上比较完成率、
错误操作、恢复次数和完整流程耗时，而不只比较单次模型响应速度。

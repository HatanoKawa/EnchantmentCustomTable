# 26.3 双端全量 GUI 回归

2026-09-27，Asia/Shanghai。基准 `dev / a5dd03c`。Fabric、NeoForge 均累计 **30/30 PASS**，
另完成两端严格 Shift 扩展。每端本轮补齐此前待测的 25 项，并重核 C01/V01/V02；
E02/E06 完整默认路径及其持久化复用 R5 的同生产代码证据。V01 世界模型沿用 R4 实机观察，
本轮重新检查菜单、候选、主物品和模板提示。不是宣称 60 项全部在本轮从头重跑。

发现一项轻微页码显示差异，见下文；未发现本清单内的复制、丢物、扣费回滚或等级异常。
没有修改生产源码、依赖、常规 runClient 或发布包。

## 环境与方法

- Minecraft 26.3，Java 25，macOS Apple Silicon；Fabric Loader 0.19.5 / API 0.161.0+26.3，NeoForge 26.3.0.12-beta。
- 重新导出双端开发客户端成功：15 tasks，2 executed、13 up-to-date；不是重新运行 JVM 全矩阵。
- 分别使用同版本 R6 世界的独立副本 `ECT-FULL-R7-fabric-26.3`、`ECT-FULL-R7-neoforge-26.3`。
  世界显示名继承旧值，以目录名和实际加载器窗口区分。未进行跨版本迁移。
- 原生鼠标辅助工具发送移动、普通点击、右键、Shift 点击；Computer Use 负责键盘与逐步观察。
  数据包只准备前置和读取原版服务端物品，未直接调用模组业务方法代替 GUI。
- 窗口模式、GUI Scale 2、renderDistance 4、simulationDistance **5**、pauseOnLostFocus false。
  上轮 simulationDistance 4 无效的问题已在本轮测试配置纠正。
- 默认四开关关闭；严格 Shift 仅开启同级递增和等级上限；K 组四开关全开，费用数值始终 36 绿宝石/4 块。
  配置切换均在客户端正常退出后完成。

[证据索引](evidence/full-26.3-2026-09-27/README.md)包含本轮 112 张 F2 原图、三阶段配置、
过滤日志、原生输入记录、恢复状态和 SHA-256 清单。
[执行摘要](evidence/full-26.3-2026-09-27/execution-summary.json)区分新增、复用和扩展。

## 逐项结果

用例定义沿用 [28 项业务清单](cases-1.21.1.md)和[分层清单的 V 项](tiered-matrix.md)。
下表 PASS 为符合该项约定标准；不代表矩阵之外的玩法全部验证。

| 用例 | 本轮核对结果 | Fabric | NeoForge |
| --- | --- | --- | --- |
| C01 | 空转换界面槽位、标题和布局正确 | PASS | PASS |
| C02 | 泥土不能放入书本、支付、模板槽 | PASS | PASS |
| C03 | 无书、35 绿宝石无结果；补到 36 才生成 | PASS | PASS |
| C04 | 2 书/36 绿宝石领取 Sharpness V 后为 1 书/0 付款 | PASS | PASS |
| C05 | 2 书/4 块领取 Aqua Affinity 后为 1 书/0 付款 | PASS | PASS |
| C06 | 翻页、sharpness、无匹配、清空的候选和页码正确 | PASS | PASS |
| C07 | 连续普通领取两本，每次扣费并补位，筛选保持 | PASS | PASS |
| C08 | 模板 V 预生成、领取并补位，剩 1 书/4 块，模板与下一输出均保留 | PASS | PASS |
| C09 | 多附魔/超上限/工具模板拒绝；移走合法模板恢复普通候选 | PASS | PASS |
| C10 | 普通兑换和复制后关闭重开，材料及产物一致 | PASS | PASS |
| C11 | 普通兑换和复制分别保存重进，服务端读数一致 | PASS | PASS |
| C12 | 菜单打开期间 reload，之后可搜索并正常领取 | PASS | PASS |
| E01 | 四附魔逐一普通取出，前两槽空、后两本仍可取，无重复书 | PASS | PASS |
| E02 | 前两本 Shift、后两本普通取出，保存重进 | REUSED_PASS（R5） | REUSED_PASS（R5） |
| E03 | 普通点击输入槽，增加同名/新附魔并消耗书 | PASS | PASS |
| E04 | 普通点击空生成槽，成功增加 Unbreaking I | PASS | PASS |
| E05 | 普通点击同名生成槽，变为 Unbreaking II，书只消耗一次 | PASS | PASS |
| E06 | 默认四书 Shift 插入、无效物品拒绝、完整客户端重启 | REUSED_PASS（R5） | REUSED_PASS（R5） |
| E07 | 三轮 Looting III→II→III，最终背包空，重开仍 III | PASS | PASS |
| E08 | 一册包含四附魔的书导出；剑和候选清空 | PASS，有页码观察 | PASS，有页码观察 |
| E09 | 普通书/泥土不能进入附魔输入槽，物品保持 | PASS | PASS |
| E10 | 普通合并、整书导出分别重开及保存重进，读数一致 | PASS | PASS |
| K01 | 严格同级 II+II→III，消耗书 | PASS | PASS |
| K02 | 不同级及混合书整体拒绝，无部分加入 | PASS | PASS |
| K03 | III→IV→V，V+V 拒绝，书保留 | PASS | PASS |
| K04 | 新附魔 Looting IV 允许加入 | PASS | PASS |
| K05 | 无材料领取 Sharpness I | PASS | PASS |
| K06 | 拒收书/付款；模板 V 连续复制两次仍为 V，无材料消耗 | PASS | PASS |
| V01 | 两台菜单与物品提示正常；世界模型引用 R4 证据 | PASS（含子项复用） | PASS（含子项复用） |
| V02 | 含 e 的搜索、Backspace、Esc、焦点与按钮正确 | PASS | PASS |

E02/E06 复用来源是 [R5 报告](shift-matrix-2026-09-26.md)，不是仅做输入可行性抽查的 R6。
`821b447..a5dd03c` 的生产目录、构建和依赖文件无差异。历史轮次记录保持原时点，不改写为本轮执行。

## 重点证据

以下五组每端都有保存前、重载后 GUI 和原版服务端读数；过滤掉日志前缀后，
GUIBLOCK/GUIPLAYER 内容逐字相同。具体原值保存在各端 `persistence-checks.json`。

| 检查 | Fabric 前→后 | NeoForge 前→后 | 核对内容 |
| --- | --- | --- | --- |
| C11 普通兑换 | 11:13:03→11:15:29 | 12:31:36→12:39:38 | 台内 1 书、无付款；玩家一本 V |
| C11 复制 | 11:23:22→11:26:55 | 12:52:37→12:59:21 | 台内 1 书/4 块/模板 V/输出 V；玩家领取的一本 V |
| E10 合并 | 11:45:59→11:51:05 | 13:18:27→13:25:26 | 剑 Sharpness II / Unbreaking II / Looting I；普通书/泥土保留 |
| E10 导出 | 11:54:44→11:56:34 | 13:41:19→13:43:06 | 剑无附魔；玩家一册 Sharpness III / Looting III / Unbreaking III / Mending I |
| 严格 Shift 客户端重启 | 12:02:48→12:05:34 | 13:50:15→13:54:35 | 剑 Sharpness V / Looting IV；三本拒绝书完整 |

严格 Shift 扩展本轮新执行：先拒绝 Sharpness I 及 Sharpness I/Unbreaking I 混合书，
再 II→III→IV→V，拒绝 V+V，允许新增 Looting IV。拒绝书经快捷移动回退到主背包，
数量/组件不变。完整重启前后严格两个开关保持开启；重启时额外打开免费/一级开关供 K 组使用，
进入世界后先读取旧结果，未重置场景。

C12 的第一次 Fabric 5 秒调度无法清楚证明菜单在 reload 前打开，不计通过。
改为测试数据包 30 秒调度后重做：Fabric 11:33:56 打开、11:34:06 reload；
NeoForge 13:05:32 打开、13:05:36 reload。均保持菜单打开，随后搜索并领取 Sharpness V，
台内剩 7 书/28 绿宝石。开窗时间来自 `input-events.jsonl`，reload 时间来自日志。

## 遗留问题与边界

**VIS-26.3-01：附魔台空候选页码不一致，低严重度，未修复。** 默认配置整书导出后为 `-/-`，
关闭重开及保存重进后为 `1/1`。两端均复现；候选始终为空，剑无附魔、唯一导出书保持。
这未违反 E08 的物品/候选清空标准，作为显示问题单独登记。后续可统一空列表的页码显示，
验证导出、重开和世界重进三种入口；尚未归因到具体源码。

INPUT-26.3-01 的原生输入方案已足够完成本轮全量 GUI。Computer Use 自带鼠标点击的旧坐标问题
仍按前次复核记录保留，没有证明其内部根因已修复；无需为了本轮验证修改模组业务代码。
本轮没有遇到锁屏或残留修饰键阻塞。

MIG-01/MIG-02 跨版本读取问题继续按用户要求低优先级搁置。多人、漏斗、满背包、
QUICK_CRAFT、数字键交换/丢弃/双击聚合、破坏掉落、中文搜索/不同缩放、竞态和压力测试
仍是清单之外的专项，不能从这 30 项推断通过。26.3 当前清单没有需要用户接管的遗留缺项。
此前 Forge 1.18.2 原始运行库的 Q01～Q03/Q07/Q08 环境确认仍需可运行原始 LWJGL 的机器。

客户端全部正常退出，临时保持唤醒进程随之结束。双端 config/options 四条路径在本轮开始前
均不存在，结束后恢复为不存在并核查；测试副本和证据保留。原生产文件未改动。

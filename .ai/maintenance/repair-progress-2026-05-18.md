# EnchantmentCustomTable 修复评估与进度报告

日期：2026-05-18
归档时最新适配分支：dev_26.1.2
仓库：/Users/river_quinn/workspace/learn/EnchantmentCustomTable

## 总体结论

本轮已经完成从 1.21.9 回归修复到 26.1.2 最新适配分支的链式推进，并补充了第二轮测试反馈中的两个问题。

2026-05-18 追加处理：dev_1.21.9 新增修复“附魔书转换台取出候选书后右侧候选区延迟刷新 / 中文搜索筛选取出时列表闪烁”问题。用户实测后确认搜索闪烁已修复，但普通取出后原槽位延迟补位仍存在；已继续定位并追加小修。随后用户完成全版本 GUI 验证，相关修复已前向合并并推送到 dev_26.1.2。

已完成分支：

- dev_1.21.9：修复拖动/快速转移附魔书输入问题；修复从候选区 shift 取出附魔书时未清理候选区缓存的问题。
- dev_1.21.10：合并 dev_1.21.9 修复并通过验证。
- dev_1.21.11：合并 dev_1.21.10 修复并通过验证。
- dev_26.1：合并 dev_1.21.11，完成 26.1 API 适配；修复 26.1 起疑似默认 GUI 文本回归。
- dev_26.1.1：从 dev_26.1 fork，更新到 Minecraft 26.1.1 / NeoForge 26.1.1.15-beta，并包含上述修复。
- dev_26.1.2：从 dev_26.1.1 fork，更新到 Minecraft 26.1.2 / NeoForge 26.1.2.54-beta，并包含上述修复。

官方版本依据：NeoForge Maven 索引中 26.x 最新条目为 26.1.2.54-beta，26.1.1 最高条目为 26.1.1.15-beta。

## 问题 1：1.21.9 起拖动/快速转移附魔书逻辑未触发

状态：已修复。
起始分支：dev_1.21.9。
前向分支：dev_1.21.10、dev_1.21.11、dev_26.1、dev_26.1.1、dev_26.1.2 均已包含该修复。

根因评估：

1.21.9 的迁移将槽位存储改为 transfer API 相关的 ResourceHandlerSlot / StackCopySlot 后，slot 1 的 setByPlayer 先调用 super.setByPlayer(newStack, oldStack)，会把附魔书写入底层 handler/cache。随后 addEnchantment(newStack, slot) 虽然会清空 slot 1 并重生成右侧候选区，但 StackCopySlot 的缓存/变更路径可能重新保留刚放入的书，导致清空与候选区更新效果在拖动或 shift 快速转移路径中丢失。

修复内容：

- slot 1 的 setByPlayer 不再先持久化 newStack，而是直接调用 addEnchantment(newStack, slot)，避免待消耗附魔书进入 handler/cache 后再被恢复。
- 右侧候选区生成槽位也补充 setByPlayer 处理，保证拖动/快速转移把书放入候选槽时同样进入 addEnchantment 流程。
- 新增 GameTest 覆盖：
  - custom_table_quick_move_book_into_input_updates_tool
  - custom_table_drag_insert_book_into_generated_slot_updates_tool

关键提交：

- dev_1.21.9：501652e fix: handle transfer slot book inserts
- dev_1.21.10：593d268 Merge branch 'dev_1.21.9' into dev_1.21.10
- dev_1.21.11：ef86afb Merge branch 'dev_1.21.10' into dev_1.21.11

验证结果：

- dev_1.21.9：./gradlew compileJava test runGameTestServer build 通过，10/10 GameTests passed。
- dev_1.21.10：./gradlew compileJava test runGameTestServer build 通过，10/10 GameTests passed。
- dev_1.21.11：./gradlew compileJava test runGameTestServer build 通过，10/10 GameTests passed。

## 问题 2：1.21.9 起从候选区 shift 取出附魔书未清理候选区

状态：已修复。
起始分支：dev_1.21.9。
前向分支：dev_1.21.10、dev_1.21.11、dev_26.1、dev_26.1.1、dev_26.1.2 均已包含该修复。

复现与根因评估：

新增回归测试 custom_table_quick_move_generated_book_subtracts_from_tool 后，在修复前可稳定复现：quickMoveStack(player, 2) 会将候选区附魔书转移到玩家背包，也会从工具上移除对应附魔，但候选区槽位仍保留该附魔书。

原因是普通点击候选区时，clicked 路径会在 removeEnchantment 后额外通过 cacheIndexForGeneratedSlot 定位 enchantmentsOnCurrentTool 中的对应缓存位，并置为 ItemStack.EMPTY；而 quickMoveStack 只调用了 removeEnchantment，漏掉了这一步。removeEnchantment 在页数不变时不会重建完整缓存，因此候选区缓存仍含旧书，updateEnchantedBookSlots 会把它重新画回去。

修复内容：

- 在 quickMoveStack 的候选区分支中，使用 cacheIndexForGeneratedSlot 定位被 shift 取出的候选书。
- removeEnchantment 未触发完整缓存重建时，将 enchantmentsOnCurrentTool 对应位置置空，并再次 updateEnchantedBookSlots。
- 新增 GameTest：custom_table_quick_move_generated_book_subtracts_from_tool。

关键提交：

- dev_1.21.9：92db631 fix: clear quick-moved generated books
- dev_1.21.10：e1c4e57 Merge branch 'dev_1.21.9' into dev_1.21.10
- dev_1.21.11：40f4185 Merge branch 'dev_1.21.10' into dev_1.21.11
- dev_26.1：b01a86f Merge branch 'dev_1.21.11' into dev_26.1
- dev_26.1.1：eed4c7d Merge branch 'dev_26.1' into dev_26.1.1
- dev_26.1.2：a4f9079 Merge branch 'dev_26.1.1' into dev_26.1.2

验证结果：

- dev_1.21.9：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_1.21.10：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_1.21.11：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_26.1：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_26.1.1：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_26.1.2：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。

## 问题 2.1：1.21.9 起转换台取出候选书后候选区延迟刷新 / 搜索闪烁

状态：已修复、已前向合并并推送。
起始分支：dev_1.21.9。
前向分支：dev_1.21.10、dev_1.21.11、dev_26.1、dev_26.1.1、dev_26.1.2 均已包含该修复。

现象：

- 搜索栏为空时，书 * 64 与绿宝石块 * 64 会正常生成候选附魔书；取出一本后材料正常消耗，但被取出的槽位保持空白，直到下一次取出时才补上。
- 搜索栏存在中文筛选值，例如“深海”时，取出唯一候选书后右侧列表会短暂闪出全部附魔书，然后变为空。

根因评估：

- 取出候选槽时，原逻辑在客户端和服务端都会执行 pickEnchantedBook / genEnchantedBookSlot。服务端是权威状态，但客户端菜单并没有同步保存搜索框的筛选条件；搜索框此前只把筛选条件发包给服务端。因此中文筛选场景中，客户端取书路径会按“空搜索”先生成全部附魔书，随后再被服务端同步覆盖，形成肉眼可见的跳动。
- pickEnchantedBook 取出后只调用 genEnchantedBookSlot 做增量填充；在 1.21.9 迁移到 ResourceHandlerSlot / StackCopySlot 后，取出路径和缓存回写更敏感，增量填充容易留下已取槽位空洞。此路径更适合在消耗材料后完整重建当前页。
- 用户实测初次修复后，闪烁已消失但普通点击取书仍会留下空槽。继续排查后确认真正的错位点在 AbstractContainerMenu 的普通 PICKUP 路径：slot.tryRemove / onTake 之后，vanilla 还会再次调用 slot.setChanged。ResourceHandlerSlot 继承的 StackCopySlot 会在 setChanged 时把 cachedReturnedStack 写回底层 handler，而该缓存此时仍可能是 tryRemove 后的空栈。因此即使 pickEnchantedBook 已经重建了候选区，最后这一次 setChanged 仍会把刚重建的槽位覆盖为空，直到下一次操作再触发重建。

修复内容：

- 候选槽 onTake 改为仅在服务端执行 pickEnchantedBook，避免客户端使用过期/缺失的搜索状态自行生成候选区。
- pickEnchantedBook 消耗书和付款后改为 rebuildEnchantedBookSlot，完整重建当前页并刷新 totalPage；如果当前页越界则回退到最后一页，不会无条件跳回第一页。
- 搜索框发送 SEARCH 包前，先用客户端语言和本地匹配结果调用 menuContainer.setSearchQuery，让客户端菜单持有与服务端一致的筛选状态，减少后续本地 UI 状态与服务端状态不一致的窗口。
- 追加修复：候选槽服务端 onTake 在重建候选区后立即调用 getItem，刷新 StackCopySlot 的 cachedReturnedStack，使随后 vanilla 额外触发的 setChanged 保留重建后的候选书，而不是把空栈写回去。
- 回归测试调整：转换台两个取出测试从 safeTake 改为 menu.clicked(..., ClickType.PICKUP, ...)，覆盖真实 GUI 普通左键点击路径以及 onTake 后的 setChanged 行为。
- 新增 GameTest：
  - conversion_table_refills_taken_result_slot_after_pick
  - conversion_table_preserves_search_filter_after_pick

关键提交：

- dev_1.21.9：94e0577 fix: refresh conversion results after pickup
- dev_1.21.9：e5a2e93 fix: preserve rebuilt conversion slot cache
- dev_26.1：973dac2 fix: adapt conversion pickup tests for 26.x

前向合并与推送：

- dev_1.21.9 已推送到 origin/dev_1.21.9：92db631..e5a2e93。
- dev_1.21.10 已合并 dev_1.21.9 并推送到 origin/dev_1.21.10：e1c4e57..1bcd2d1。
- dev_1.21.11 已合并 dev_1.21.10 并推送到 origin/dev_1.21.11：40f4185..b56820a。
- dev_26.1 已合并 dev_1.21.11，补充 26.x API 适配，并推送到 origin/dev_26.1：bb9aa69..973dac2。
- dev_26.1.1 已合并 dev_26.1 并推送到 origin/dev_26.1.1：eed4c7d..cf21ab4。
- dev_26.1.2 已合并 dev_26.1.1 并推送到 origin/dev_26.1.2：a4f9079..999f1bc。

验证结果：

- dev_1.21.9：./gradlew compileJava 通过。
- dev_1.21.9：./gradlew runGameTestServer 通过，13/13 GameTests passed。
- dev_1.21.9：./gradlew build 通过。
- 追加小修后再次验证：./gradlew compileJava 通过；./gradlew runGameTestServer 通过，13/13 GameTests passed；./gradlew build 通过。
- dev_26.1：JENV_VERSION=21 ./gradlew compileJava 通过。
- dev_26.1.2：JENV_VERSION=21 ./gradlew runGameTestServer 通过，13/13 GameTests passed。
- dev_26.1.2：JENV_VERSION=21 ./gradlew build 通过。

测试评估：

- 本次已把“服务器端取出候选书后立即补位”和“带搜索筛选取出后仍只显示匹配附魔”的核心逻辑加入 GameTest。
- 搜索闪烁本身涉及客户端菜单状态、渲染帧和服务端同步时序，GameTest 无法完整模拟实际 GUI 一瞬间的视觉表现；当前修复通过同步客户端菜单筛选状态与服务端权威取出流程来约束该问题，仍建议用 runClient 做一次实际交互确认。

## 问题 3：26.1 迁移构建失败

状态：已修复。
分支：dev_26.1。

问题与修复：

- Gradle 9.1 与 Foojay resolver 0.9.0 不兼容，升级 settings.gradle 中 org.gradle.toolchains.foojay-resolver-convention 到 1.0.0。
- ClickType API 迁移为 ContainerInput。
- GameTest 环境定义泛型更新为 TestEnvironmentDefinition<?>。
- AbstractContainerScreen.extractBackground 在 26.1 中需要 public override。
- 附魔台书本渲染 API 迁移：Material / MaterialSet 改为 SpriteId / SpriteGetter，CameraRenderState 包路径迁移，并改用 BookModel.State.forAnimation 与新的 submitModel 参数顺序。

关键提交：

- dev_26.1：a82b23a Merge branch 'dev_1.21.11' into dev_26.1
- dev_26.1：ffa41ce fix: adapt 26.1 api changes

验证结果：

- dev_26.1：./gradlew compileJava 通过。
- dev_26.1：./gradlew test runGameTestServer build 通过，10/10 GameTests passed。

备注：

26.x GameTest 输出中仍会出现 NeoForge/Mixin debug 日志：Class version 69 required is higher than current Mixin supports JAVA_21 supports class version 65。当前运行实际使用 Java 25，且测试与构建均成功，因此本轮未将其作为阻塞项处理。

## 问题 4：26.1 起 GUI 出现意外文字显示

状态：已做代码扫描并提交一个高置信度修复；仍建议后续按实际截图确认。
起始分支：dev_26.1。
前向分支：dev_26.1.1、dev_26.1.2 均已包含该修复。

扫描结论：

26.1 GUI 迁移将旧的 renderLabels 改为 extractLabels 时，在 EnchantingCustomScreen 和 EnchantmentConversionScreen 中新增了 super.extractLabels(graphics, mouseX, mouseY)。旧版自定义 GUI 的 renderLabels 没有调用父类标签绘制，因此不会显示默认标题、Inventory 文本等父类标签。26.1 起调用 super.extractLabels 后，很可能把这些默认文字重新绘制出来，和“意外文字显示”的现象吻合。

修复内容：

- 移除 EnchantingCustomScreen.extractLabels 中的 super.extractLabels 调用。
- 移除 EnchantmentConversionScreen.extractLabels 中的 super.extractLabels 调用。
- 保留自定义页码文本绘制。

关键提交：

- dev_26.1：bb9aa69 fix: suppress default screen labels
- dev_26.1.1：eed4c7d Merge branch 'dev_26.1' into dev_26.1.1
- dev_26.1.2：a4f9079 Merge branch 'dev_26.1.1' into dev_26.1.2

验证结果：

- dev_26.1：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_26.1.1：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。
- dev_26.1.2：./gradlew test runGameTestServer build 通过，11/11 GameTests passed。

限制：

本轮没有启动客户端进行视觉截图验证，因此此项标记为“代码层高置信度修复”。如果实际 GUI 中仍有异常文字，需要后续基于截图对照具体坐标和文本内容继续调整。

## 最新版本适配推进

状态：已推进到当前官方 26.x 最新 NeoForge 条目 26.1.2.54-beta。

dev_26.1.1：

- 从 dev_26.1 fork。
- 更新 gradle.properties：
  - minecraft_version=26.1.1
  - minecraft_version_range=[26.1.1]
  - neo_version=26.1.1.15-beta
  - neo_version_range=[26.1.1.0-beta,)
- 源码无需额外修改。
- 提交：fc4f9db chore: migrate to 26.1.1
- 验证：./gradlew compileJava 通过；./gradlew test runGameTestServer build 通过，10/10 GameTests passed。

dev_26.1.2：

- 从 dev_26.1.1 fork。
- 更新 gradle.properties：
  - minecraft_version=26.1.2
  - minecraft_version_range=[26.1.2]
  - neo_version=26.1.2.54-beta
  - neo_version_range=[26.1.2.0-beta,)
- 源码无需额外修改。
- 提交：babe000 chore: migrate to 26.1.2
- 验证：./gradlew compileJava 通过；./gradlew test runGameTestServer build 通过，10/10 GameTests passed。

## 当前风险与后续建议

- 本次转换台候选区刷新修复已从 dev_1.21.9 前向合并并推送到 dev_26.1.2。
- 用户已在 dev_1.21.9 实测确认搜索闪烁与普通取出延迟补位问题修复；仍建议在另一台设备对已推送分支做统一 GUI 回归确认。
- 本轮无法直接 GUI 复现客户端画面；已修复最可疑的父类默认标签绘制问题，但仍建议后续用实际截图确认。
- 容器层的拖动、shift 输入、普通取出、shift 取出路径均已有 GameTest 覆盖。
- 建议后续在 dev_26.1.2 上手动运行一次 runClient，验证 GUI 拖动、shift 左键、进入世界与新建世界流程。
- 构建中仍有 Gradle 10 兼容性弃用提示，来自 Gradle/插件层面；当前不影响本轮目标。

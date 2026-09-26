# 1.20.1 Shift 缺项补测（2026-09-26～27）

分支 `codex/port-1.20.1`。Fabric / Forge 的 E02/E06 共 **4 项通过**，另完成严格配置扩展。
此前第四轮的 14 项/端结果保留，累计各 16 PASS；没有修改生产代码或重新运行 JVM 全矩阵。

每端先做原版箱子 16 泥土双向 Shift 转移与普通拾取对照。E02 使用四附魔工具，前两本
Shift 取出、后两本普通取出，四本书齐全且工具附魔清空；保存重进后数据一致。
E06 默认配置四本附魔书依次 Shift 插入，普通书/泥土不进入业务槽；锋利 II、抢夺 I、
耐久 II 在完整重启客户端后保持。严格组检查不同等级/混合书原子拒绝、同级递增、V+V
上限拒绝及新增抢夺 IV，最终结果保存重进一致。所有业务动作通过实际 GUI 完成，
fixture 只准备材料与读取服务端物品数据。

两端各三次持久化核对通过；配置/options 四个路径均恢复测试前状态，客户端正常退出。
输入辅助程序与场景准备工具使用 dev 的 `tools/gui-validation/macos_shift_click.swift`
和 `prepare_shift_matrix.py --checkout <本分支路径> --legacy`，显式限定 ECT 客户端。

[逐端证据及执行计数](evidence/shift-matrix-2026-09-26/execution-summary.json)包含截图、
服务端过滤日志、默认/严格配置、结果和 SHA-256 清单。原日志和备份在本 checkout 的
`build/reports/gui-validation/shift-matrix-2026-09-26/`。

E02/E06 已不需要用户手动补测。多人、漏斗/区块卸载、满背包、QUICK_CRAFT 和压力场景
仍未安排；跨版本资源读取继续低优先级搁置。

# 26.3 全量证据索引

2026-09-27 / Asia/Shanghai；生产基准 dev / a5dd03c。112 张 F2 原图未经编辑；每端默认 42、严格 Shift 5、严格与免费 9。

[执行摘要](execution-summary.json) · [原生输入事件](input-events.jsonl) · [导出日志](export-build.log)

服务端日志仅保留场景执行、GUIBLOCK/GUIPLAYER、截图、保存及 reload 行；完整客户端日志保留在本机 build/reports/gui-validation/full-26.3-2026-09-27。

E02/E06 完整步骤复用同生产代码 R5，V01 的世界模型子项复用 R4；不将旧证据算成本轮截图。

## fabric

[结果与恢复](./fabric/result.json) · [保存前后原值比较](./fabric/persistence-checks.json)

复用：[R5 完整默认 Shift 结果](../shift-matrix-2026-09-26/fabric-26.3/result.json)、[转换台世界模型](../round4-2026-09-23/fabric-26.3-conversion-model.png)、[附魔台世界模型](../round4-2026-09-23/fabric-26.3-enchanting-model.png)。

### default：C01～C12，E01/E03～E05/E07～E10，菜单/搜索/提示

[配置](fabric/config-default.json) · [过滤日志](fabric/default-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_11.09.29.png](fabric/screenshots-default/2026-09-27_11.09.29.png) |
| 02 | [2026-09-27_11.10.00.png](fabric/screenshots-default/2026-09-27_11.10.00.png) |
| 03 | [2026-09-27_11.10.44.png](fabric/screenshots-default/2026-09-27_11.10.44.png) |
| 04 | [2026-09-27_11.11.08.png](fabric/screenshots-default/2026-09-27_11.11.08.png) |
| 05 | [2026-09-27_11.11.35.png](fabric/screenshots-default/2026-09-27_11.11.35.png) |
| 06 | [2026-09-27_11.12.59.png](fabric/screenshots-default/2026-09-27_11.12.59.png) |
| 07 | [2026-09-27_11.13.36.png](fabric/screenshots-default/2026-09-27_11.13.36.png) |
| 08 | [2026-09-27_11.15.59.png](fabric/screenshots-default/2026-09-27_11.15.59.png) |
| 09 | [2026-09-27_11.17.15.png](fabric/screenshots-default/2026-09-27_11.17.15.png) |
| 10 | [2026-09-27_11.18.41.png](fabric/screenshots-default/2026-09-27_11.18.41.png) |
| 11 | [2026-09-27_11.19.31.png](fabric/screenshots-default/2026-09-27_11.19.31.png) |
| 12 | [2026-09-27_11.20.28.png](fabric/screenshots-default/2026-09-27_11.20.28.png) |
| 13 | [2026-09-27_11.21.22.png](fabric/screenshots-default/2026-09-27_11.21.22.png) |
| 14 | [2026-09-27_11.23.19.png](fabric/screenshots-default/2026-09-27_11.23.19.png) |
| 15 | [2026-09-27_11.24.00.png](fabric/screenshots-default/2026-09-27_11.24.00.png) |
| 16 | [2026-09-27_11.27.36.png](fabric/screenshots-default/2026-09-27_11.27.36.png) |
| 17 | [2026-09-27_11.28.22.png](fabric/screenshots-default/2026-09-27_11.28.22.png) |
| 18 | [2026-09-27_11.29.18.png](fabric/screenshots-default/2026-09-27_11.29.18.png) |
| 19 | [2026-09-27_11.29.57.png](fabric/screenshots-default/2026-09-27_11.29.57.png) |
| 20 | [2026-09-27_11.30.00.png](fabric/screenshots-default/2026-09-27_11.30.00.png) |
| 21 | [2026-09-27_11.30.38.png](fabric/screenshots-default/2026-09-27_11.30.38.png) |
| 22 | [2026-09-27_11.31.25.png](fabric/screenshots-default/2026-09-27_11.31.25.png) |
| 23 | [2026-09-27_11.34.17.png](fabric/screenshots-default/2026-09-27_11.34.17.png) |
| 24 | [2026-09-27_11.35.57.png](fabric/screenshots-default/2026-09-27_11.35.57.png) |
| 25 | [2026-09-27_11.37.44.png](fabric/screenshots-default/2026-09-27_11.37.44.png) |
| 26 | [2026-09-27_11.38.32.png](fabric/screenshots-default/2026-09-27_11.38.32.png) |
| 27 | [2026-09-27_11.39.14.png](fabric/screenshots-default/2026-09-27_11.39.14.png) |
| 28 | [2026-09-27_11.40.31.png](fabric/screenshots-default/2026-09-27_11.40.31.png) |
| 29 | [2026-09-27_11.41.38.png](fabric/screenshots-default/2026-09-27_11.41.38.png) |
| 30 | [2026-09-27_11.45.55.png](fabric/screenshots-default/2026-09-27_11.45.55.png) |
| 31 | [2026-09-27_11.46.58.png](fabric/screenshots-default/2026-09-27_11.46.58.png) |
| 32 | [2026-09-27_11.51.26.png](fabric/screenshots-default/2026-09-27_11.51.26.png) |
| 33 | [2026-09-27_11.52.05.png](fabric/screenshots-default/2026-09-27_11.52.05.png) |
| 34 | [2026-09-27_11.52.22.png](fabric/screenshots-default/2026-09-27_11.52.22.png) |
| 35 | [2026-09-27_11.52.37.png](fabric/screenshots-default/2026-09-27_11.52.37.png) |
| 36 | [2026-09-27_11.52.53.png](fabric/screenshots-default/2026-09-27_11.52.53.png) |
| 37 | [2026-09-27_11.53.17.png](fabric/screenshots-default/2026-09-27_11.53.17.png) |
| 38 | [2026-09-27_11.53.37.png](fabric/screenshots-default/2026-09-27_11.53.37.png) |
| 39 | [2026-09-27_11.54.07.png](fabric/screenshots-default/2026-09-27_11.54.07.png) |
| 40 | [2026-09-27_11.54.30.png](fabric/screenshots-default/2026-09-27_11.54.30.png) |
| 41 | [2026-09-27_11.55.09.png](fabric/screenshots-default/2026-09-27_11.55.09.png) |
| 42 | [2026-09-27_11.57.03.png](fabric/screenshots-default/2026-09-27_11.57.03.png) |

### strict：严格 Shift 原子拒绝、III、IV、V、新 Looting IV

[配置](fabric/config-strict.json) · [过滤日志](fabric/strict-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_12.00.42.png](fabric/screenshots-strict/2026-09-27_12.00.42.png) |
| 02 | [2026-09-27_12.01.25.png](fabric/screenshots-strict/2026-09-27_12.01.25.png) |
| 03 | [2026-09-27_12.01.50.png](fabric/screenshots-strict/2026-09-27_12.01.50.png) |
| 04 | [2026-09-27_12.02.15.png](fabric/screenshots-strict/2026-09-27_12.02.15.png) |
| 05 | [2026-09-27_12.02.44.png](fabric/screenshots-strict/2026-09-27_12.02.44.png) |

### strict-free：严格 Shift 重启结果；普通 K02、K01、K03、K04；K05/K06

[配置](fabric/config-strict-free.json) · [过滤日志](fabric/strict-free-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_12.06.07.png](fabric/screenshots-strict-free/2026-09-27_12.06.07.png) |
| 02 | [2026-09-27_12.06.56.png](fabric/screenshots-strict-free/2026-09-27_12.06.56.png) |
| 03 | [2026-09-27_12.07.42.png](fabric/screenshots-strict-free/2026-09-27_12.07.42.png) |
| 04 | [2026-09-27_12.08.13.png](fabric/screenshots-strict-free/2026-09-27_12.08.13.png) |
| 05 | [2026-09-27_12.08.55.png](fabric/screenshots-strict-free/2026-09-27_12.08.55.png) |
| 06 | [2026-09-27_12.09.41.png](fabric/screenshots-strict-free/2026-09-27_12.09.41.png) |
| 07 | [2026-09-27_12.11.24.png](fabric/screenshots-strict-free/2026-09-27_12.11.24.png) |
| 08 | [2026-09-27_12.12.18.png](fabric/screenshots-strict-free/2026-09-27_12.12.18.png) |
| 09 | [2026-09-27_12.13.17.png](fabric/screenshots-strict-free/2026-09-27_12.13.17.png) |

## neoforge

[结果与恢复](./neoforge/result.json) · [保存前后原值比较](./neoforge/persistence-checks.json)

复用：[R5 完整默认 Shift 结果](../shift-matrix-2026-09-26/neoforge-26.3/result.json)、[转换台世界模型](../round4-2026-09-23/neoforge-26.3-conversion-model.png)、[附魔台世界模型](../round4-2026-09-23/neoforge-26.3-enchanting-model.png)。

### default：C01～C12，E01/E03～E05/E07～E10，菜单/搜索/提示

[配置](neoforge/config-default.toml) · [过滤日志](neoforge/default-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_12.20.02.png](neoforge/screenshots-default/2026-09-27_12.20.02.png) |
| 02 | [2026-09-27_12.21.10.png](neoforge/screenshots-default/2026-09-27_12.21.10.png) |
| 03 | [2026-09-27_12.22.27.png](neoforge/screenshots-default/2026-09-27_12.22.27.png) |
| 04 | [2026-09-27_12.23.45.png](neoforge/screenshots-default/2026-09-27_12.23.45.png) |
| 05 | [2026-09-27_12.27.28.png](neoforge/screenshots-default/2026-09-27_12.27.28.png) |
| 06 | [2026-09-27_12.31.32.png](neoforge/screenshots-default/2026-09-27_12.31.32.png) |
| 07 | [2026-09-27_12.33.02.png](neoforge/screenshots-default/2026-09-27_12.33.02.png) |
| 08 | [2026-09-27_12.41.12.png](neoforge/screenshots-default/2026-09-27_12.41.12.png) |
| 09 | [2026-09-27_12.42.45.png](neoforge/screenshots-default/2026-09-27_12.42.45.png) |
| 10 | [2026-09-27_12.45.48.png](neoforge/screenshots-default/2026-09-27_12.45.48.png) |
| 11 | [2026-09-27_12.47.13.png](neoforge/screenshots-default/2026-09-27_12.47.13.png) |
| 12 | [2026-09-27_12.48.48.png](neoforge/screenshots-default/2026-09-27_12.48.48.png) |
| 13 | [2026-09-27_12.50.32.png](neoforge/screenshots-default/2026-09-27_12.50.32.png) |
| 14 | [2026-09-27_12.52.33.png](neoforge/screenshots-default/2026-09-27_12.52.33.png) |
| 15 | [2026-09-27_12.53.49.png](neoforge/screenshots-default/2026-09-27_12.53.49.png) |
| 16 | [2026-09-27_13.00.42.png](neoforge/screenshots-default/2026-09-27_13.00.42.png) |
| 17 | [2026-09-27_13.01.54.png](neoforge/screenshots-default/2026-09-27_13.01.54.png) |
| 18 | [2026-09-27_13.03.10.png](neoforge/screenshots-default/2026-09-27_13.03.10.png) |
| 19 | [2026-09-27_13.04.21.png](neoforge/screenshots-default/2026-09-27_13.04.21.png) |
| 20 | [2026-09-27_13.04.24.png](neoforge/screenshots-default/2026-09-27_13.04.24.png) |
| 21 | [2026-09-27_13.05.03.png](neoforge/screenshots-default/2026-09-27_13.05.03.png) |
| 22 | [2026-09-27_13.06.11.png](neoforge/screenshots-default/2026-09-27_13.06.11.png) |
| 23 | [2026-09-27_13.08.42.png](neoforge/screenshots-default/2026-09-27_13.08.42.png) |
| 24 | [2026-09-27_13.09.54.png](neoforge/screenshots-default/2026-09-27_13.09.54.png) |
| 25 | [2026-09-27_13.10.49.png](neoforge/screenshots-default/2026-09-27_13.10.49.png) |
| 26 | [2026-09-27_13.11.38.png](neoforge/screenshots-default/2026-09-27_13.11.38.png) |
| 27 | [2026-09-27_13.13.05.png](neoforge/screenshots-default/2026-09-27_13.13.05.png) |
| 28 | [2026-09-27_13.14.09.png](neoforge/screenshots-default/2026-09-27_13.14.09.png) |
| 29 | [2026-09-27_13.18.23.png](neoforge/screenshots-default/2026-09-27_13.18.23.png) |
| 30 | [2026-09-27_13.19.51.png](neoforge/screenshots-default/2026-09-27_13.19.51.png) |
| 31 | [2026-09-27_13.27.14.png](neoforge/screenshots-default/2026-09-27_13.27.14.png) |
| 32 | [2026-09-27_13.29.22.png](neoforge/screenshots-default/2026-09-27_13.29.22.png) |
| 33 | [2026-09-27_13.31.06.png](neoforge/screenshots-default/2026-09-27_13.31.06.png) |
| 34 | [2026-09-27_13.32.54.png](neoforge/screenshots-default/2026-09-27_13.32.54.png) |
| 35 | [2026-09-27_13.34.21.png](neoforge/screenshots-default/2026-09-27_13.34.21.png) |
| 36 | [2026-09-27_13.36.02.png](neoforge/screenshots-default/2026-09-27_13.36.02.png) |
| 37 | [2026-09-27_13.39.51.png](neoforge/screenshots-default/2026-09-27_13.39.51.png) |
| 38 | [2026-09-27_13.40.11.png](neoforge/screenshots-default/2026-09-27_13.40.11.png) |
| 39 | [2026-09-27_13.40.40.png](neoforge/screenshots-default/2026-09-27_13.40.40.png) |
| 40 | [2026-09-27_13.41.13.png](neoforge/screenshots-default/2026-09-27_13.41.13.png) |
| 41 | [2026-09-27_13.41.40.png](neoforge/screenshots-default/2026-09-27_13.41.40.png) |
| 42 | [2026-09-27_13.43.36.png](neoforge/screenshots-default/2026-09-27_13.43.36.png) |

### strict：严格 Shift 原子拒绝、III、IV、V、新 Looting IV

[配置](neoforge/config-strict.toml) · [过滤日志](neoforge/strict-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_13.47.26.png](neoforge/screenshots-strict/2026-09-27_13.47.26.png) |
| 02 | [2026-09-27_13.48.12.png](neoforge/screenshots-strict/2026-09-27_13.48.12.png) |
| 03 | [2026-09-27_13.48.52.png](neoforge/screenshots-strict/2026-09-27_13.48.52.png) |
| 04 | [2026-09-27_13.49.26.png](neoforge/screenshots-strict/2026-09-27_13.49.26.png) |
| 05 | [2026-09-27_13.50.09.png](neoforge/screenshots-strict/2026-09-27_13.50.09.png) |

### strict-free：严格 Shift 重启结果；普通 K02、K01、K03、K04；K05/K06

[配置](neoforge/config-strict-free.toml) · [过滤日志](neoforge/strict-free-observations.log)

| 序号 | 原始截图 |
| --- | --- |
| 01 | [2026-09-27_13.55.33.png](neoforge/screenshots-strict-free/2026-09-27_13.55.33.png) |
| 02 | [2026-09-27_13.56.39.png](neoforge/screenshots-strict-free/2026-09-27_13.56.39.png) |
| 03 | [2026-09-27_13.57.45.png](neoforge/screenshots-strict-free/2026-09-27_13.57.45.png) |
| 04 | [2026-09-27_13.58.37.png](neoforge/screenshots-strict-free/2026-09-27_13.58.37.png) |
| 05 | [2026-09-27_13.59.29.png](neoforge/screenshots-strict-free/2026-09-27_13.59.29.png) |
| 06 | [2026-09-27_14.00.32.png](neoforge/screenshots-strict-free/2026-09-27_14.00.32.png) |
| 07 | [2026-09-27_14.03.42.png](neoforge/screenshots-strict-free/2026-09-27_14.03.42.png) |
| 08 | [2026-09-27_14.04.45.png](neoforge/screenshots-strict-free/2026-09-27_14.04.45.png) |
| 09 | [2026-09-27_14.05.52.png](neoforge/screenshots-strict-free/2026-09-27_14.05.52.png) |

Fabric default 第 22 张是首次 5 秒 reload 的过程截图，不能单独证明 C12。正式通过依据是第 23～24 张及开窗/重载时间；NeoForge 对应第 22～23 张。

VIS-26.3-01：两端 default 最后三张分别为导出、重开、重进，显示 -/- 与 1/1 差异；服务端物品保持一致。

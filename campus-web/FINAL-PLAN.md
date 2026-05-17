# 最终实施计划 — P7 剩余工作

## 任务分解

| ID | 任务 | 授权范围 | 并行 | 估算工期 |
|----|------|---------|------|---------|
| A1 | 运行 Demo 数据初始化 | `scripts/demo/` 目录 | 独立 | 0.5天 |
| A2 | 新建热榜 Java API | controller/campus + service/campus | ✅ 与A3并行 | 1天 |
| A3 | hotRank.ts 对接真实API | `campus-web/src/services/hotRank.ts` | ✅ 与A2并行 | 0.5天 |
| A4 | 对比分析改真实数据查询 | dao/campus + mapper + service impl | 等A1完成 | 1天 |
| A5 | 传播分析改真实数据查询 | dao/campus + mapper + service impl | 等A1完成 | 1天 |
| A6 | 试运行检查清单 | config/ + docs/ | 最后 | 0.5天 |

## 执行顺序

```
第1轮（并行，互不依赖）:
  A1 → seed 脚本（已有，直接执行）
  A2 → CampusHotRankController + Service（新建）
  A3 → hotRank.ts 改 apiGet 调用（修改1个文件）

第2轮（依赖 A1 完成，有数据才能查）:
  A4 → CampusClueDao 加聚合查询 + CompareServiceImpl 改真实数据
  A5 → CampusEventDao 加传播查询 + SpreadServiceImpl 改真实数据

第3轮:
  A6 → 试运行检查清单
```

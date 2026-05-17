# P4-P7 主控任务书

> 主控: Claude | 遵循 AGENTS.md 规范
> 目标: 完成 P4(对接API) → P5(补齐后端) → P6(Batch推进) → P7(试运行准备)

---

## 总体任务分解

### Phase 4: 对接真实 API（5 个子任务）

| ID | 文件 | 后端 API | 后端已存在？ | 难度 | 并行 |
|----|------|---------|------------|------|------|
| P4.1 | `services/articleDetail.ts` | GET `/campus/clue/detail?id=` | ✅ CampusClueController | 🟢 | ✅ |
| P4.2 | `services/search.ts` | GET `/campus/clue/list?keyword=` | ✅ CampusClueController | 🟢 | ✅ |
| P4.3 | `services/hotRank.ts` | 需新增后端热榜接口 | ❌ 需新增 | 🟡 | ✅ |
| P4.4 | `services/compare.ts` | 需新增后端对比分析 Java API | ❌ 需新增 | 🔴 | ❌ 依赖P5 |
| P4.5 | `services/spread.ts` | 需新增后端传播分析 Java API | ❌ 需新增 | 🔴 | ❌ 依赖P5 |

### Phase 5: 补齐后端 API + TikHub 配置（3 个子任务）

| ID | 内容 | 说明 |
|----|------|------|
| P5.1 | 配置 TikHub API Key | 从 VPS.md 读取 key，配置到后端 TikhubCredentialResolver |
| P5.2 | 新增对比分析 Java API | `CampusCompareController.java` + `CampusCompareService.java` |
| P5.3 | 新增传播分析 Java API | `CampusSpreadController.java` + `CampusSpreadService.java` |

### Phase 6: 剩余 Batch 推进

| ID | 内容 | 参考文档 |
|----|------|---------|
| P6.1 | 按 batch29 做试运行前配置治理 | docs/batch29-prelaunch-governance-plan.md |
| P6.2 | 按 batch30 完善监测任务中心 | docs/batch30-monitor-task-mvp-plan.md |

### Phase 7: 试运行准备

| ID | 内容 | 参考文档 |
|----|------|---------|
| P7.1 | 学校初始化数据 | docs/campus-prelaunch-checklist.md |
| P7.2 | 端到端流程验证 | docs/campus-acceptance-runbook.md |

---

## 执行顺序

```
第 1 轮（并行，低风险）:
  P4.1 + P4.2 + P4.3（3个前端service替换mock）
  
第 2 轮（并行，后端新增）:
  P5.2 + P5.3（新建Java对比分析+传播分析API）
  
第 3 轮（依赖P5完成后）:
  P4.4 + P4.5（前端对比+传播对接真实API）
  P5.1（配置TikHub Key）
  
第 4 轮:
  P6.1 + P6.2（Batch 29 + 30 推进）

第 5 轮:
  P7.1 + P7.2（试运行准备）
```

---

## 质量门禁

每个子任务验收标准：
- [ ] TypeScript/Vue 编译零错误
- [ ] 无死代码/无 console.log
- [ ] 对接真实 API 的 service 标注了 `@// TODO:` 替换说明
- [ ] 新增 Java API 遵循 campus controller 现有风格
- [ ] Java 编译通过（mvn compile）

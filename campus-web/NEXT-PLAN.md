# 下一步工作计划

> 基于: docs/campus-yuqing-implementation-plan.md + batch21-batch32 计划 + 当前完成情况
> 状态：P1-P3（前端改造）已完成，5 个 service 仍使用 mock 数据

---

## 现状回顾

```
已完成
  ├── Java 后端: 49 个 campus service + 17 个 campus controller + TikHub 接入 ✅
  ├── Vue 前端: P1-P3 全部 11 个子任务完成 ✅
  ├── 旧 Thymeleaf: 已迁移至 _legacy ✅
  └── 登录可用: 13900000000 / stonedt ✅

未完成
  └── 5 个前端 service 使用 mock 数据，未对接真实后端 API
```

---

## Phase 4：对接真实 API（第 1 优先）

### 4.1 按依赖顺序逐个替换 mock

| # | Service | 当前 | 后端 API | 后端已存在？ |
|---|---------|------|---------|------------|
| 1 | `articleDetail.ts` | mock | `CampusClueController` → 查文章详情 | ⚠️ 需确认详情接口 |
| 2 | `hotRank.ts` | mock | 需要从微博/抖音/头条获取热榜 | ❌ 后端可能需要新增 |
| 3 | `search.ts` | mock | `CampusClueController` + 搜索参数 | ⚠️ 需确认搜索接口 |
| 4 | `compare.ts` | mock | 需要新建对比分析 API | ❌ 需新建 |
| 5 | `spread.ts` | mock | 需要新建传播分析 API | ❌ 需新建 |

### 4.2 对接方法

每个 mock service 的文件结构都是：
```typescript
export async function fetchXxx(params): Promise<XxxData> {
  // TODO: 对接真实 API
  // return apiGet<XxxData>('/campus/xxx');
  return mockData; // ← 替换这一行
}
```

改成：
```typescript
export async function fetchXxx(params): Promise<XxxData> {
  return apiGet<XxxData>('/campus/xxx', params);
}
```

**预计工期：** 3-5 天

---

## Phase 5：补齐后端缺失 API（第 2 优先）

如果 Phase 4 发现后端缺少某些接口，需要新增：

| 新增接口 | 说明 | 参考 batch |
|---------|------|-----------|
| `/campus/compare` | 对比分析（两个主题的多维对比） | — |
| `/campus/spread/{eventId}` | 传播链分析 | batch30 |
| `/campus/hot-rank` | 热榜数据聚合 | — |
| `/campus/search` | 全文搜索（可能复用 FullSearchController）| — |

**预计工期：** 2-3 天

---

## Phase 6：剩余 Batch 计划执行（第 3 优先）

项目已有完整的 batch 计划（batch21-batch32），以下是对照当前完成情况：

| Batch | 内容 | 当前状态 | 优先级 |
|-------|------|---------|--------|
| 21 | 媒体接入平台 | ✅ 已完成（TikHub 等） | ✅ |
| 22 | TikHub 适配器 | ✅ 已完成 | ✅ |
| 23 | 接入任务调度与运行日志 | ⚠️ 部分完成 | 🟡 |
| 24 | 数据标准化与去重 | ⚠️ 部分完成 | 🟡 |
| 25 | 接入后自动检测联动 | ⚠️ 部分完成 | 🟡 |
| 26 | API 密钥/额度/重试/审计 | ⚠️ 部分完成 | 🟡 |
| 27 | 白名单网页采集器 | ❌ 待开发 | 🟢 |
| 28 | 多平台监测配置前端管理页 | ❌ 待开发 | 🟢 |
| **29** | **试运行前配置治理与学校初始化** | ❌ **待开始** | 🔴 **最高** |
| 30 | 监测任务中心 MVP | ⚠️ MonitorView 前端已有 | 🟡 |
| 31 | 监测调度绑定 | ❌ 待开发 | 🟢 |
| 32 | 监测存储与并发 | ❌ 待开发 | 🟢 |

---

## Phase 7：试运行准备

参考 `docs/campus-prelaunch-checklist.md`，首批学校试运行需要：

1. **初始化学校数据**（部门、用户、角色）
2. **配置监测主题/关键词**
3. **验证 TikHub 数据源接入**
4. **测试推送通知通道**
5. **用户培训材料准备**

---

## 建议路线图

```
第 1 周: Phase 4 — 对接 5 个 mock service 到真实 API
第 2 周: Phase 5 — 补齐后端缺失的 API（compare/spread/search/hotRank）
第 3 周: Phase 6 — 按 batch 29+30 推进
第 4 周: Phase 7 — 试运行准备 + 学校部署
```

## 实际建议

从 **Phase 4 第一个 service（articleDetail.ts）** 开始——这个最简单、影响最直接。
先把"文章详情"从 mock 换成真实数据，用户就能在 ClueView 点击文章看到真实内容了。

# 卓然校园舆情综合研判平台 — 主任务书

> 最后读取: 2026-05-12 09:30
> 当前阶段: P1.1（已启动）
> 总体进度: 1/11 个子任务

---

## 项目架构速览

```
campus-web/ (Vue 3 + Element Plus + ECharts 6 + TypeScript + Vite)
  ├── src/
  │   ├── views/          ← 页面组件（你要改的地方）
  │   ├── components/     ← 通用组件（新建的组件放这里）
  │   ├── services/       ← API 层（不动）
  │   ├── router/         ← 路由配置（新增路由时改）
  │   ├── styles/         ← main.css（全局样式）
  │   ├── config/         ← brand.ts（品牌信息）
  │   └── types/          ← api.ts（类型定义）
  └── PLAN.md             ← 本文件
  └── PROGRESS.md         ← 进度追踪
```

## 任务总清单

### P1: UI 精致度（预计 3 天）

| ID | 任务 | 文件 | 工期 | 依赖 |
|---|---|---|---|---|
| P1.1 | **EmotionBadge 组件** | `components/EmotionBadge.vue` | 0.5天 | 无 |
| P1.2 | **PlatformBadge 组件** | `components/PlatformBadge.vue` | 0.5天 | 无 |
| P1.3 | **全局配色统一** | `styles/main.css` 追加 Element Plus 变量覆盖 | 1天 | 无 |
| P1.4 | **图表三件套** | `components/ChartToolbar.vue` + `composables/useChart.ts` | 1天 | 无 |

### P2: 核心功能补齐（预计 3.5 天）

| ID | 任务 | 文件 | 工期 | 依赖 |
|---|---|---|---|---|
| P2.1 | **文章详情页** | `views/ArticleDetailView.vue` + 路由 | 1.5天 | 无 |
| P2.2 | **三大热榜组件** | `components/HotRankPanel.vue` + WorkbenchView 集成 | 2天 | 无 |

### P3: 差异化功能（预计 6 天）

| ID | 任务 | 文件 | 工期 | 依赖 |
|---|---|---|---|---|
| P3.1 | **全局搜索 + 搜索页** | 顶部栏搜索框 + `views/SearchView.vue` | 2天 | 无 |
| P3.2 | **对比分析** | `views/CompareView.vue` + 路由 | 2天 | P1.3 (配色统一) |
| P3.3 | **传播分析** | EventView 扩展 Tab | 2天 | P1.1 (情感色标) |

---

## 架构决策记录

1. **组件放在 `components/`**：所有可复用 UI 组件放这里，页面专属逻辑放 views/
2. **Composables 放 `composables/`**：如果用到可复用的逻辑（如 useChart.ts），放这里
3. **全局样式追加到 `main.css`**：不要新建 CSS 文件，统一在 main.css 追加
4. **新增路由在 `router/index.ts` 追加**：不要改已有的路由配置
5. **颜色变量使用 `--color-*` 体系**：不要硬编码颜色值

---

## 质量标准

每个子任务完成时检查：
- [ ] Vue 组件编译无报错
- [ ] TypeScript 类型正确
- [ ] Element Plus 组件使用正确（icon 大小、按钮类型等）
- [ ] 样式与现有设计一致（间距、颜色、圆角）
- [ ] 响应式布局考虑（移动端至少不崩溃）
- [ ] 路由配置正确（如果有新页面）

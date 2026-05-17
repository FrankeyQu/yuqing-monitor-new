# 卓然校园舆情综合研判平台 — 改造方案

> 对比基准: mymonitor.xyz (kwm 网络舆情监测系统)
> 当前状态: Vue 3 + Element Plus + ECharts 6 + TypeScript
> 端口: campus-web (5175) ←→ Java API (8084)

---

## Phase 0：清理无用代码（立即做）

| 文件 | 问题 | 操作 |
|---|---|---|
| `src/views/PlaceholderView.vue` | 在 router/index.ts 第6行 import 了但**没有任何路由使用它** | ✅ 删除文件和import行 |
| `src/router/index.ts` 第6行 | `import PlaceholderView from '../views/PlaceholderView.vue'` | ✅ 删除这行 |

**无需删除：**
- `SettingsNav.vue` → 被4个settings页面引用 ✅ 正常
- `http.ts` → 被9个service引用（service层的基础封装）✅ 正常
- `campus-mark.svg` → 登录页和侧边栏用了 ✅ 正常

---

## Phase 1：补齐 UI 精致度（对标 mymonitor）

### 1.1 情感色标系统（1天）

**现状**：线索库/事件页用 `el-tag` 泛型标签，没有统一的情感色标。

**改造**：加全局情感色标组件 + CSS 变量

```typescript
// src/components/EmotionBadge.vue（新建）
// 正面 → #67C23A 绿色底
// 中性 → #E6A23C 黄色底  
// 负面 → #F56C6C 红色底
// 未知 → #909399 灰色底
```

在 `main.css` 或新建 `emotion.css` 中定义，在 `ClueView`、`EventView`、`AlertView` 中使用。

### 1.2 来源平台标识（0.5天）

**现状**：线索库只有文字显示"平台"字段。

**改造**：加 `PlatformBadge.vue` 组件，给每个平台一个带颜色的标识：

```
微博→红 / 微信→绿 / 抖音→黑 / 贴吧→蓝 / 知乎→蓝 / 小红书→红
```

数据来源从 `sourcePlatform` 字段映射。

### 1.3 全局配色统一（1天）

**现状**：主题色用 `--color-primary: #0f766e`（翠绿），Element Plus 组件默认蓝色。

**决定**：保持一致——要么全用翠绿（校园特色），要么全用 Element 蓝。

建议保留 **翠绿 `#0f766e`** 作为品牌色，并覆盖 Element Plus 的 CSS 变量：

```css
:root {
  --el-color-primary: #0f766e;
  --el-color-primary-light-3: #2d9a8f;
  --el-color-primary-light-5: #5cbdb2;
  --el-color-primary-light-7: #8ed6ce;
  --el-color-primary-light-8: #b2e4de;
  --el-color-primary-light-9: #d6f1ed;
  --el-color-primary-dark-2: #0b5e57;
}
```

### 1.4 图表三件套（1天）

**现状**：SituationView 有大量 ECharts 图表但没有统一操作栏。

**改造**：在每个图表卡片右上角加 `chart-toolbar`：

```
┌──────────────────────────────┐
│  标题              [📋][⛶][🔄] │
├──────────────────────────────┤
│                              │
│         (图表区域)             │
│                              │
└──────────────────────────────┘
```

三个按钮：复制数据/全屏/刷新。用 Element Plus 的 `el-dropdown` 或 `el-button-group`。

---

## Phase 2：补齐功能短板（对标 mymonitor）

### 2.1 工作台三大热榜（2天）

**现状**：WorkbenchView 只有统计卡片+告警表格，没有实时热点。

**改造**：在 workbench 下方加一排热榜卡片：

```
┌─────────────┬─────────────┬─────────────┐
│  微博热搜     │  抖音热榜    │  头条热榜    │
│  1. OPPO...  │  1. 世乒赛  │  1. 特朗普  │
│  2. 国乒...  │  2. 天舟十号│  2. 台湾... │
│  3. ...      │  3. ...     │  3. ...     │
│  Top 10      │  Top 10     │  Top 10     │
└─────────────┴─────────────┴─────────────┘
```

**后端接口**：Java 后端已有 `/api/v1/global-hot-event?Source_Type=SW|DY|TT`（mymonitor 方式），需要 campus-web 实现相似接口或直接从 mymonitor 的 API 代理。

**备选方案**：先写死 mock 数据展示 UI，等后端接口就绪再对接。

### 2.2 文章详情页（1.5天）

**现状**：ClueView 有文章列表，但点击列表项后没有专门的文章详情路由。

**改造**：新建 `ArticleDetailView.vue`，路由 `/clues/:id`

```
详情页包含：
  ├─ 标题 + 来源 + 时间（顶栏）
  ├─ 情感色标 + 风险等级
  ├─ 正文内容（富文本/纯文本展示）
  ├─ 相似文章推荐（底部）
  └─ 操作区（标记已处理/加入事件/导出）
```

### 2.3 全库搜索 + 全局搜索框（2天）

**现状**：没有搜索入口，顶部栏没有搜索框。

**改造**：
1. 顶部栏右侧加全局搜索框（`el-autocomplete`）
2. 新建 `SearchView.vue` 搜索结果页

### 2.4 对比分析（本品 vs 竞品）（2天）

**现状**：无

**改造**：新建 `CompareView.vue`
- 选两个主题
- 对比维度：声量、情感分布、媒体分布、时间趋势
- 用 ECharts 多系列柱状图 + 雷达图展示

### 2.5 传播分析（2天）

**现状**：EventView 有事件处置，但没有传播链分析。

**改造**：在 EventView 中加"传播分析"Tab
- 传播源头
- 传播时间线
- 关键节点媒体
- 转发关系图（用 ECharts 关系图或力导向图）

---

## Phase 3：文章校对 + 外部工具聚合（锦上添花）

这两个 mymonitor 有，但优先级较低，可以先放一放。

---

## 路线图

```
Phase 0：立即      清理 PlaceholderView（10分钟）
─────────────────────────────────────────
Phase 1：第1周     UI精致度
  Day 1:  情感色标 EmotionBadge.vue
  Day 2:  来源平台标识 PlatformBadge.vue
  Day 3:  全局配色统一（Element Plus 变量覆盖）
  Day 4:  图表三件套
─────────────────────────────────────────
Phase 2：第2-3周   功能补齐
  Week 2:
    - 工作台三大热榜
    - 文章详情页
  Week 3:
    - 全库搜索 + 全局搜索框
    - 对比分析
    - 传播分析
─────────────────────────────────────────
Phase 3：第4周     锦上添花
    - 文章校对
    - 外部工具聚合
```

## 优先级建议

| 优先级 | 项目 | 工作量 | 效果 |
|---|---|---|---|
| P0 | 清理 PlaceholderView | 10分钟 | 代码整洁 |
| P1 | 情感色标 Badge | 0.5天 | 视觉提升明显 |
| P1 | 来源平台标识 | 0.5天 | 专业感↑ |
| P1 | 全局配色统一 | 1天 | 品牌一致性 |
| P1 | 图表三件套 | 1天 | 交互一致性 |
| P2 | 文章详情页 | 1.5天 | 核心功能 |
| P2 | 工作台三大热榜 | 2天 | Dashboard活性↑ |
| P3 | 全库搜索 | 2天 | 搜索入口 |
| P3 | 对比分析 | 2天 | 差异化功能 |
| P4 | 传播分析 | 2天 | 深度分析 |
| P4 | 文章校对 | 待定 | 锦上添花 |
| P4 | 外部工具 | 待定 | 锦上添花 |

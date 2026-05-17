# 路由重构方案 — 前台/后台分离

## 任务

重构 `src/router/index.ts`，将页面分为两组：

### 前台路由（MainLayout — 5项菜单）
```
/               → 主页（工作台）
/situation      → 态势大屏（主页子页）
/monitor        → 分类信息
/clues          → 线索库
/clues/:id      → 文章详情
/analysis       → 分析
/reports        → 汇报
/auto-reports   → 自动报告
/search         → 搜索
/compare        → 对比分析
```

### 后台路由（AdminLayout — 后台管理）
```
/admin/accounts       → 重点账号
/admin/events         → 事件处置
/admin/alerts         → 预警中心
/admin/detection      → 检测任务
/admin/ingest         → 数据接入
/admin/settings/departments → 部门管理
/admin/settings/dicts       → 数据字典
/admin/settings/audit       → 审计日志
/admin/settings/permissions → 权限管理
```

### 子线程授权范围
- **S1**: 修改 `campus-web/src/router/index.ts`（仅此1个文件）
- **S2**: 在 campus-web 目录执行 `npx vue-tsc --noEmit` 验证编译

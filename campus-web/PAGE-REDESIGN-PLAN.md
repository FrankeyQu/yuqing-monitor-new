# 页面重构方案：主页大屏 + 分类信息页

## 目标

1. **主页** → Dashboard 大屏（类似 mymonitor）：词云 + 学校舆情概览
2. **分类信息页（原监测任务）** → 信息列表 + 主题分析 + 主题预警，高密度筛选布局
3. 快速创建入口缩小为小图标

---

## 任务拆分

### T1：主页 Dashboard 大屏（前端为主）

| 属性 | 值 |
|------|-----|
| **授权范围** | `campus-web/src/views/DashboardView.vue`(新建), `campus-web/src/components/WordCloud.vue`(新建), `campus-web/src/components/StatCardGroup.vue`(新建), `campus-web/src/services/dashboard.ts`(修改), `campus-web/src/types/api.ts`(修改,追加类型), `campus-web/src/router/index.ts`(修改路由) |
| **禁止触碰** | MainLayout.vue, 后端 Java 代码, 其他 views |

**具体内容：**
- 新建 `DashboardView.vue`：全宽大屏布局
  - 顶部：4-6 个核心指标卡片（今日线索、待处理预警、处置中事件、高风险事件、活跃监测任务、在线用户）
  - 左中：词云图区域（echarts-wordcloud 或自定义 Canvas）
  - 右中：舆情趋势折线图（7天/30天）
  - 下方左：最新舆情列表（实时滚动）
  - 下方右：情感分布饼图 + 媒体来源分布
- 新建 `WordCloud.vue`：基于 ECharts wordCloud 扩展的词云组件
- 修改 `router/index.ts`：`/` 路由从 `WorkbenchView` 改为 `DashboardView`
- 修改 `services/dashboard.ts`：新增词云数据获取、趋势数据获取
- 保留 WorkbenchView.vue 不删除（作为备份）

### T2：分类信息页（MonitorView 重写，前端为主）

| 属性 | 值 |
|------|-----|
| **授权范围** | `campus-web/src/views/MonitorView.vue`(重写), `campus-web/src/components/EmotionBadge.vue`(已存在,可能微调), `campus-web/src/components/PlatformBadge.vue`(已存在,可能微调), `campus-web/src/services/monitor.ts`(修改), `campus-web/src/services/search.ts`(修改), `campus-web/src/types/api.ts`(追加类型) |
| **禁止触碰** | 后端 Java 代码, router/index.ts, MainLayout.vue, 其他 views |

**具体内容：**
- 重写 `MonitorView.vue`：
  - **面包屑**：`[蓝色竖线 #3D5AFE] 机构/品牌 / 新疆大学` 右侧 tab 切换 `信息列表 | 主题分析 | 主题预警`
  - **快速创建**：缩小为右上角一个小 `+` 图标按钮，点击弹出 Dialog
  - **筛选面板**（可折叠）：
    - 采集时间（快捷：今天/本周/本月/自定义日期范围）
    - 发布时间（同采集时间）
    - 情感类型（7个checkbox：全部、疑似正面、确认正面、疑似中性、确认中性、疑似负面、确认负面）
    - 文章状态（5个radio：全部/未读/已读/已选/未选）
    - 相似信息去重（开关）
    - 词语匹配对象（仅标题/仅内容/标题+内容）
    - 排序（价值度/情感等级/发布时间/网站等级/相关度）
    - 关键词搜索（输入框）
  - **内容分类 tabs**：全部(N) | 新闻(N) | 论坛(N) | 自媒体(N) | 视频(N) | 其它(N)
  - **子 tabs**（论坛下）：全部(N) | 贴吧(N) | 知乎(N) | 论坛(N) | 问答(N)
  - **工具栏**：负面/中性/正面/无 快捷标签、刷新、标记本页已读、添加文章、导出、批量操作
  - **数据表格**：Checkbox | # | 情感(EmotionBadge) | 标题-摘要(关键词高亮) | 发布时间 | 来源(PlatformBadge) | 操作
  - **样式**：蓝色主题(#3D5AFE)、浅灰背景(#f5f6fa)、白色卡片、高信息密度
- 修改 `services/monitor.ts`：新增高级筛选接口调用
- 保留原有任务管理功能到独立 Dialog（通过小图标进入）

### T3：后端 API 增强

| 属性 | 值 |
|------|-----|
| **授权范围** | `src/main/java/com/stonedt/intelligence/controller/campus/CampusClueController.java`, `src/main/java/com/stonedt/intelligence/service/campus/CampusClueService*.java`, `src/main/java/com/stonedt/intelligence/dao/campus/CampusClueDao.java`, `src/main/resources/mapper/CampusClueMapper.xml`, `src/main/java/com/stonedt/intelligence/controller/campus/CampusDashboardController.java`(可能新建) |
| **禁止触碰** | 非 campus 包的 Controller/Service/DAO, pom.xml, application.yml, config/ |

**具体内容：**
- `CampusClueController.java`：
  - 增强 `GET /campus/clue/list`：支持 `sentiment`, `articleStatus`, `publishTimeStart/End`, `collectTimeStart/End`, `matchScope`, `similarDedup`, `sortBy`, `sourcePlatform`, `sourceSubPlatform`, `keyword` 参数
  - 新增 `GET /campus/clue/count-by-media-type`：返回各类媒体数量（全部/新闻/论坛/自媒体/视频/其它）
  - 新增 `GET /campus/clue/count-by-sub-platform`：返回子平台数量
- `CampusClueDao.java` + `CampusClueMapper.xml`：增强 SQL 支持多条件筛选
- `CampusDashboardController.java`（如需要）：
  - 新增 `GET /campus/dashboard/word-cloud`：返回词频数据
  - 增强 `GET /campus/dashboard/statistics`：补充趋势数据

---

## 执行顺序

```
第1轮（并行）:
  T1: 主页 Dashboard 大屏（前端）
  T2: 分类信息页重写（前端）
  T3: 后端 API 增强

第2轮（汇总）:
  主控审核 → 编译验证 → 用户确认
```

T1 和 T2 都只改前端文件，互不冲突，可完全并行。
T3 改后端，与 T1/T2 无文件冲突，也可并行。但 T1/T2 依赖 T3 提供的 API 契约 —— 因此 T1/T2 先按约定 API 格式写，T3 按同样契约实现。

---

## API 契约（T1/T2 前端假定，T3 按此实现）

### GET /campus/dashboard/word-cloud
```json
{
  "code": 1,
  "data": [
    { "name": "食品安全", "value": 128 },
    { "name": "招生", "value": 96 }
  ]
}
```

### GET /campus/clue/list（增强参数）
```
?keyword=&sentiment=positive_confirmed&articleStatus=unread
&publishTimeStart=2026-05-01&publishTimeEnd=2026-05-12
&collectTimeStart=&collectTimeEnd=
&matchScope=title_content&similarDedup=true
&sortBy=value_score&sourcePlatform=新闻&sourceSubPlatform=
&pageNum=1&pageSize=20
```

### GET /campus/clue/count-by-media-type
```json
{
  "code": 1,
  "data": [
    { "name": "全部", "value": 613 },
    { "name": "新闻", "value": 9 },
    { "name": "论坛", "value": 69 },
    { "name": "自媒体", "value": 326 },
    { "name": "视频", "value": 208 },
    { "name": "其它", "value": 1 }
  ]
}
```

### GET /campus/clue/count-by-sub-platform
```json
{
  "code": 1,
  "data": [
    { "name": "全部", "value": 69 },
    { "name": "贴吧", "value": 67 },
    { "name": "知乎", "value": 0 },
    { "name": "论坛", "value": 1 },
    { "name": "问答", "value": 1 }
  ]
}
```

### GET /campus/dashboard/statistics（增强）
现有接口，新增字段：`sentimentDistribution`, `mediaDistribution`, `trendData`

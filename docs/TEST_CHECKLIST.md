# 测试检查清单 — 卓然舆情

> 当前项目测试覆盖率仍然偏低，但已不再是“只有 1 个 stub 测试”。截至 2026-05-17，`src/test/java/` 下已有 16 个 Java 测试类；旧的 Spring 上下文测试已降级为轻量占位测试，`mvn test -DskipTests=false` 已可稳定结束。
> `pom.xml` 仍配置了 `<skipTests>true</skipTests>`，日常最小门禁以编译和前端构建为准。

## 后端测试命令

```powershell
cd D:\PRJ\yuqing
$jdk = Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME=$jdk
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests compile       # 最小后端门禁
.\mvnw.cmd -DskipTests package       # 后端打包门禁
.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test    # 启用后端测试，当前 55 个用例通过
```

⚠️ **当前状态**：
- `skipTests=true` 在 pom.xml 中硬编码
- 测试目录 `src/test/java/` 当前有 16 个 Java 测试类
- 接入模块和轻量占位测试共 55 个用例
- `StonedtPortalApplicationTests` 已移除完整 Spring 上下文加载，避免测试门禁依赖数据库/Redis/外部配置
- 最小可行验证命令：`.\mvnw.cmd -DskipTests compile`

## 前端测试命令

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build  # 校园 Vue 3 前端构建与类型检查

cd large_screen
npm run build  # 大屏 Vue 2 构建，仅修改 large_screen 时需要
```

⚠️ **当前状态**：`campus-web` 和 `large_screen` 均无前端单元测试/E2E 测试脚本，主要依赖构建与人工浏览器验收。

## 2026-05-17 监测新版页面统一口径验收

- [ ] `/monitor` 默认请求 `hitScope=all`，普通关键词命中和风险命中都可见；`hitScope=risk` 只返回 `riskMarked=true`。
- [ ] 主体词/别名命中但关键词未命中时不生成新监测结果；关键词命中且负面词未命中时生成普通监测结果；关键词与负面词同时命中时生成风险标记。
- [ ] 发布时间为空时显示“发布时间未知”，并按采集时间在未知发布时间记录内部倒序；`collectTimeStart/End` 只过滤采集时间。
- [ ] 情感筛选只展示全部、正面、中性、负面、未知；接口参数使用 `positive/neutral/negative/none` 并兼容历史中文值。
- [ ] 排序只展示发布时间、采集时间、相关度、情感；旧 `value/siteLevel` 请求不报错并落到默认排序。
- [ ] “合并相似信息”开启后，列表分页总数、媒体类型统计与实际列表一致。
- [ ] “新增人工线索”保存后进入线索库，不新增普通监测命中；“添加文章”入口不可见。
- [ ] 批量操作对监测命中支持转线索、转预警、忽略；对已转线索支持研判、加入事件；混选时展示成功、失败、跳过数量。
- [ ] 客户后台看不到“数据接入”，直接访问 `/admin/ingest` 重定向到 `/admin/monitor-tasks`；无后台菜单权限账号看不到“后台管理”。
- [ ] session 过期或 `/campus/system/current-user` 校验失败时刷新受保护页面跳转登录页。

## Lint / TypeCheck / Build 命令

```powershell
# 后端编译
.\mvnw.cmd -DskipTests compile

# 校园前端构建
cd D:\PRJ\yuqing\campus-web
npm run build

# 大屏前端构建
cd D:\PRJ\yuqing\large_screen
npm run build

# 代码检查：当前无配置（无 checkstyle / ESLint / Prettier）
```

## 最近验证记录

### 2026-05-17 校园事件单用户台账模式验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 后端编译：使用 `.codex-tools/jdk8` 设置 `JAVA_HOME` 后执行 `.\mvnw.cmd -DskipTests compile` 通过，编译 492 个 Java source files，仅有旧代码内部 API、deprecated、unchecked 警告。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 浏览器冒烟：Vite 本地服务启动于 `http://127.0.0.1:5173/`；未启动后端会按现有鉴权守卫跳转 `/login`，页面级登录后验收仍需连接本地或线上后端。
- 验收口径：`/monitor` 的“加入事件”改为调用事件归集接口；`/events` 主流程改为事件台账、线下处置记录和直接归档；相似线索支持加入当前事件。

### 2026-05-17 Batch1-Batch6 报告/统计/接入/状态/搜索口径验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 后端编译：使用 `.codex-tools/jdk8` 设置 `JAVA_HOME` 后执行 `.\mvnw.cmd -DskipTests compile` 通过，编译 491 个 Java source files，仅有旧代码内部 API、deprecated、unchecked 警告。
- 后端测试：`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- XML 检查：`CampusClueMapper.xml`、`CampusMonitorResultMapper.xml` 可被 XML parser 正常解析。
- 验收口径：报告 AI 生成会持久化内容；报告统计按事件/关键词/周期同一 scope 聚合；首页监测指标改用统一监测信息风险口径；接入运行支持 `partial_success` 并继承记录风险；线索/事件写链路加入事务和状态保护；搜索页明确为线索搜索，辅助研判明确为规则辅助。

### 2026-05-17 监测命中预览与筛选交互修正验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 验收口径：`/monitor` 切换 `hitScope` 时清空平台、情感、状态、关键词和自定义时间等窄筛选，并显示当前列表总数；`/monitor?hitScope=all` 可直接进入全部真实命中；`/situation` 监测命中卡片仅作为风险命中预览，显示总量并跳转到完整列表。

### 2026-05-17 监测信息默认完整列表修正验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 验收口径：菜单或直接访问 `/monitor` 默认请求 `hitScope=all`，显示全部真实监测命中；`/monitor?hitScope=risk` 和态势页预览仍保留风险命中口径。

### 2026-05-17 监测信息风险/全部命中双口径验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 后端编译/测试：使用 `.codex-tools/jdk8` 设置 `JAVA_HOME` 后执行 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd test -DskipTests=false` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped；`CampusMonitorResultMapper.xml` 可被 XML parser 正常解析。
- 验收口径：`/campus/monitor/information/**` 使用 `hitScope=risk/all`；默认 `all` 展示全部关键词命中，`risk` 只展示风险标记命中；主体词-only、普通线索和搜索过程内容均不进入监测信息。
- 线上冒烟：备份 `/home/ubuntu/yuqing-backups/deploy-20260517-032925-hit-scope`；`yuqing/nginx` active，`https://yuqing.zhuoran.cc/monitor` 返回 200，未登录 `/campus/monitor/information/list?...hitScope=all` 返回 302；正式库口径核验 `hitScope=all` 为 296 条、`hitScope=risk` 为 12 条。

### 2026-05-17 前后台入口与监测信息口径纠偏验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 后端编译/测试：使用 `.codex-tools/jdk8` 设置 `JAVA_HOME` 后执行 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd test -DskipTests=false` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped。
- XML 检查：`CampusMonitorResultMapper.xml` 可被 XML parser 正常解析。
- 验收口径：前台侧边栏不再展示 `/admin/**` 后台菜单；`/admin` 默认进入 `/admin/monitor-tasks`；`/monitor` 不再提供快速创建监测任务；`/campus/monitor/information/**` 只读取监测命中结果，不再混入普通线索或搜索过程沉淀内容。

### 2026-05-17 Batch41-B47 蓝图 MVP 闭环验证
- 代码检查：`git diff --check` 通过；仅有 Git 提示的 LF 到 CRLF 工作区换行警告，无 whitespace error。
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 后端编译：使用 `.codex-tools/jdk8` 设置 `JAVA_HOME` 后执行 `.\mvnw.cmd -DskipTests compile` 通过，编译 491 个 Java source files，仅有旧代码 deprecated/unchecked/internal API 警告。
- 后端测试：`.\mvnw.cmd test -DskipTests=false` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped。
- 验收口径：学校相关性、主题分类、结构化预警依据、事件状态校验、风险 SLA、相似线索推荐、报告治理复盘和仪表盘治理指标已进入本地 MVP 闭环。

### 2026-05-16 全量服务器同步验证（Batch39 + 搜索时间口径）
- 同步目录：服务器 `/home/ubuntu/yuqing-fullsync-20260516-224509`；备份目录 `/home/ubuntu/yuqing-backups/deploy-20260516-224509-fullsync`。
- 后端测试：服务器 `./mvnw -DskipTests=false test` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped。
- 后端打包：服务器 `./mvnw -DskipTests package` 通过，已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar`。
- 前端构建：服务器 `campus-web npm install && npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告，已覆盖 `/opt/yuqing/web`。
- Flyway：启动初次被旧报告模板 `${...}` 文本触发 placeholder 校验阻断；设置 `SPRING_FLYWAY_PLACEHOLDER_REPLACEMENT=false` 后启动成功，并迁移到 `1.39 CampusAiCapabilityManagement`。
- 数据库验收：`campus_ai_call_log`、`campus_ai_feature_binding`、`campus_ai_model`、`campus_ai_prompt_template`、`campus_ai_provider` 均存在；供应商种子数据 7 条。
- 线上冒烟：`https://yuqing.zhuoran.cc/`、`/login`、`/monitor`、`/search?q=新疆大学`、`/admin/monitor-tasks`、`/admin/settings/ai` 返回 200；未登录访问 `/campus/monitor/information/list`、`/campus/ai/overview`、`/campus/clue/list?...sortBy=publish_time` 返回 302。

### 2026-05-16 AI 能力管理 P0-P2 验证
- 前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 后端编译/测试：本地 Windows 环境未配置 `JAVA_HOME`，本地 Maven 被阻断；已在服务器全量快照补跑 `./mvnw -DskipTests=false test` 通过 55 个用例，`./mvnw -DskipTests package` 通过。
- 验收口径：后台 `/admin/settings/ai` 可配置供应商、模型、功能绑定、提示词并查看调用日志；DeepSeek 报告/研判/词云从 `campus_ai` 读取配置，TikHub/百度/Jina 优先读取供应商接入点和密钥引用，旧写作宝/OCR/分词登记为停用历史能力。

### 2026-05-16 Batch38 监测信息内容质量与 Jina 修复验证
- 本地后端完整测试：`.\mvnw.cmd "-DskipTests=false" test` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped。
- 本地前端构建：`campus-web npm run build` 通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 本地后端打包：`.\mvnw.cmd -DskipTests package` 通过，Jar 包含 `V1.37__MonitorInformationContentQualityCleanup.sql`、`V1.38__CleanLegacyInvalidWeiboMonitorRows.sql`、`JinaReaderClient` 和 `CampusIngestTextSanitizer`。
- 线上发布：前后端备份 `/home/ubuntu/yuqing-backups/deploy-20260516-203436-content-quality-jina`；二次后端备份 `/home/ubuntu/yuqing-backups/deploy-20260516-203927-invalid-weibo-clean`；`yuqing/nginx/mariadb/redis-server` 均为 active。
- Flyway：正式库 `1.37 MonitorInformationContentQualityCleanup`、`1.38 CleanLegacyInvalidWeiboMonitorRows` 均 `success=1`。
- 数据验收：活跃接入记录、监测结果、线索中的 `<em class="keyword">` 均为 0；小红书“新大，您真的把我养得很差！”可见记录为 155 字正文，60 字短记录与对应监测结果已隐藏；活跃非 `http/https` 原文链接为 0；非法微博接入和监测结果均为 0。
- Jina 验证：生产环境 `CONTENT_EXTRACTION_ENABLED=true`、`JINA_READER_API_URL=http://43.160.254.21/jina-reader-proxy`；服务器用 `Java/1.8.0_482` UA 访问代理复现 403/1010，用 `Mozilla/5.0` UA 访问同一代理返回 200，验证本轮客户端 UA 修复命中根因。
- 线上冒烟：`https://yuqing.zhuoran.cc/`、`/login`、`/monitor`、`/admin/monitor-tasks` 返回 200；未登录访问 `/campus/monitor/information/list` 返回 302。

### 2026-05-14 主线校准验证（P0 修复前）
- `.\mvnw.cmd -DskipTests compile`：通过，编译 452 个 Java source files；仅有旧代码内部 API、deprecated、unchecked 警告。
- `campus-web npm run build`：通过；仅有 Rollup PURE 注释警告和 chunk 体积警告。
- `.\mvnw.cmd test -DskipTests=false`：3 分钟超时；接入模块 9 个测试类报告通过，残留 Java 进程已手动停止。

### 2026-05-14 P0 测试门禁修复验证
- `.\mvnw.cmd test -DskipTests=false`：通过，10 个测试类、33 个用例，0 failures / 0 errors / 0 skipped。
- `campus-web npm run build`：通过；仍有 Rollup PURE 注释警告和 chunk 体积警告。

### 2026-05-14 监测 P0-P2 功能验证
- `.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test`：通过，10 个测试类、33 个用例，0 failures / 0 errors / 0 skipped；编译 461 个 Java source files，仅旧 `sun.misc.BASE64*`、deprecated、unchecked 警告。
- `campus-web npm run build`：通过；仍有 Rollup PURE 注释警告和 chunk 体积警告。
- 备注：本轮未新增自动化业务用例，仍需后续补 `CampusMonitorService`、`CampusEducationService` 和 API 集成测试。

### 2026-05-15 Batch33 监测任务后台设置验证
- `.\mvnw.cmd -DskipTests compile`：通过，编译 462 个 Java source files；仅旧 `sun.misc.BASE64*`、deprecated、unchecked 警告。
- `campus-web npm run build`：通过；仍有 Rollup PURE 注释警告和 chunk 体积警告。
- 本地 dev server：`http://127.0.0.1:5174/admin/monitor-tasks` 未登录访问被前端守卫重定向到 `/login`，符合现有登录态约束；登录后页面交互仍需人工账号验收。
- 备注：本轮新增后台监测任务设置页和预警 `create-from-clue` 兼容接口，尚未补充自动化 API/组件测试。

### 2026-05-15 Batch33 P2 后台收口验证
- `campus-web npm run build`：通过；仍有 Rollup PURE 注释警告和 chunk 体积警告。
- `.\mvnw.cmd -DskipTests compile`：通过，编译 467 个 Java source files；仅旧 `sun.misc.BASE64*`、deprecated、unchecked 警告。
- 验收口径：后台导航显示“监测任务管理”而非“检测任务”；`/admin/monitor-tasks` 仅展示任务配置，不展示具体监测内容、命中结果或运行日志；数据接入页不展示运行日志 Tab、API 调用日志 Tab 和历史外部接口供应商调用配置。
- 服务器同步验证：旧测试目录 `/home/ubuntu/yuqing-test-batch33-p2-20260515-092114` 中 `./mvnw -DskipTests compile`、`campus-web npm install && npm run build`、`./mvnw -DskipTests package` 均通过；后续正式发布使用 `/home/ubuntu/yuqing-test-20260515-092413` 快照。

### 2026-05-15 Jina Reader 正文增强实施验证
- 本地后端测试命令：`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" "-Dtest=PublicWebFetchConfigTest,PublicWebWhitelistValidatorTest,PublicWebIngestAdapterTest,JinaReaderClientTest,BaiduIngestFetchConfigTest,BaiduIngestResponseMapperTest,CampusIngestGovernanceServiceTest" test`
- 服务器补跑结果：`/home/ubuntu/yuqing-test-20260515-092413` 中接入/Jina 相关 7 个测试类共 25 个用例通过，0 failures / 0 errors / 0 skipped。
- 服务器门禁：完整后端 `mvn test -DskipTests=false` 通过，14 个测试类共 42 个用例；`campus-web npm install && npm run build` 通过；`mvn -DskipTests package` 通过并产出 `target/stonedt-portal-0.5.3-SNAPSHOT.jar`。
- 线上补验：已发布到 `/opt/yuqing`，`yuqing/nginx/mariadb/redis-server` 均为 active；`https://yuqing.zhuoran.cc/`、`/login`、`/ingest`、`/admin/monitor-tasks` 返回前端 200。服务器访问托管 `r.jina.ai` 的 80/443 连接超时，Reader 真实调用需自部署或放通出口后复验。

### 2026-05-16 Batch34 监测任务驱动接入与展示治理验证
- `campus-web npm run build`：通过；仍有既有 Rollup PURE 注释和 chunk 体积警告。
- `.\mvnw.cmd -DskipTests compile`：本地 Windows 环境未安装 Java，失败原因 `JAVA_HOME not found`；后端编译需在服务器或配置 Java 后补跑。
- 服务器后端门禁：`/home/ubuntu/yuqing-test-batch34-20260516-035757` 中 `mvn -DskipTests compile`、`mvn -DskipTests package` 通过。
- 服务器单测：新增 `TikhubClientTest`，`mvn -DskipTests=false -Dtest=TikhubClientTest test` 通过，1 个用例，0 failures / 0 errors / 0 skipped。
- Flyway 正式库补验：`flyway_schema_history` 已存在 `1.28` 成功记录；`campus_monitor_task` 已存在 `display_enabled`、`auto_ingest_enabled`、`ingest_capability_status`。
- 自动接入补验：手动运行 `新疆大学` 监测任务后，百度新闻/网页与 TikHub 抖音、小红书、B站、微博接入任务均自动创建/复用并成功运行；监测运行返回 `scanned=107`、`match=107`。
- 前台展示补验：隐藏 `新疆大学` 任务后，任务级监测信息总数从 167 变为 0，全局信息总数从 560 变为 393，平台统计同步扣减；恢复展示后数据恢复。
- 删除补验：临时删除验证任务运行产生 10 条命中，软删除后该任务监测信息列表和平台统计均为 0，任务管理列表不再返回该任务。

### 2026-05-16 小红书两段式详情增强验证
- 服务器单测：`/home/ubuntu/yuqing-test-batch34-20260516-035757` 执行 `mvn -DskipTests=false -Dtest=TikhubResponseMapperTest,TikhubClientTest test` 通过，8 个用例，0 failures / 0 errors / 0 skipped。
- 后端门禁：同目录完整 `mvn -DskipTests=false test` 通过，45 个用例；`mvn -DskipTests package` 通过并产出可部署 Jar。
- 线上部署：已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 并重启 `yuqing`，`yuqing/nginx` 均为 active，`https://yuqing.zhuoran.cc/` 返回 200。
- 业务验收：手动运行 `新疆大学` 监测任务成功，返回 `scanned=9/match=9/alert=7`；自动小红书接入任务 `fetched=20/success=3/duplicate=17/fail=0`。
- 去重验收：小红书 `source_id + external_id` 重复组为 0；重复 `note_id` 只增强原 `campus_ingest_record`，不新增第二条详情内容。
- 字段验收：已增强 20 条小红书记录，监测信息按发布时间排序可返回原文 URL、发布时间、点赞、转发、收藏和更完整正文；`V1.29__BackfillXiaohongshuOriginalUrl.sql` 已应用成功，137 条小红书接入记录 `missing_url=0`，任务内 137 条小红书监测行均可回填原文链接。
- 展示隔离验收：短暂关闭任务前台展示后，任务级平台统计从 316 变为 0，恢复后回到 316；临时置为逻辑删除后也为 0，恢复后任务状态正常。

### 2026-05-16 监测 P0-P4 平台正文链路验证
- 本地单测：`.\mvnw.cmd '-Dtest=TikhubClientTest,TikhubResponseMapperTest' -DskipTests=false test` 通过，13 个用例，0 failures / 0 errors / 0 skipped。
- 本地完整测试：`.\mvnw.cmd test -DskipTests=false` 通过，15 个测试类共 50 个用例，0 failures / 0 errors / 0 skipped。
- 前端构建：`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 本地打包：`.\mvnw.cmd -DskipTests package` 通过，产出 `target/stonedt-portal-0.5.3-SNAPSHOT.jar`。
- 编译复验：最终 `.\mvnw.cmd -DskipTests compile` 通过。
- 线上部署：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-170425-p0p4`，覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 与 `/opt/yuqing/web` 后重启 `yuqing`、reload `nginx`。
- 线上冒烟：`yuqing/nginx/mariadb/redis-server` 均为 active；`https://yuqing.zhuoran.cc/`、`/login`、`/admin/monitor-tasks` 返回 200；未登录访问 `/campus/monitor/information/list` 返回 302。

### 2026-05-16 Jina Reader 官方 API 单点测试
- 本地官方 API：`https://r.jina.ai/http://example.com` 返回 200，能输出 `Title`、`URL Source`、`Markdown Content`。
- 本地中文页面：`https://r.jina.ai/https://www.moe.gov.cn/` 返回 200，能提取教育部官网中文 Markdown 内容。
- 密钥验证：Jina Reader API Key 已写入服务器环境文件并脱敏校验为 `<configured>`；本地带 `Authorization: Bearer <key>` 调用官方 API 返回 200。
- 服务器官方 API：当前不可用。服务器 DNS 将 `r.jina.ai` 解析到异常 IP；强制解析到本地确认的 Cloudflare IP 后仍 `Connection reset by peer`；服务器带密钥调用仍连接超时。
- 当前测试结论：代码与官方 API 格式匹配，但线上真实调用需要先修复服务器到 `r.jina.ai` 的外联；后续再评估 `s.jina.ai` SERP 搜索能力。

### 2026-05-16 Jina Reader 代理链路验证
- 代理节点本机：`http://127.0.0.1/jina-reader-proxy/http://example.com` 返回 200，输出 Reader Markdown。
- 卓然舆情服务器到代理：`http://43.160.254.21/jina-reader-proxy/http://example.com` 返回 200，输出 Reader Markdown。
- 生产配置：`JINA_READER_API_URL=http://43.160.254.21/jina-reader-proxy`；`JINA_READER_API_KEY` 在卓然舆情服务器为空，密钥由代理节点注入。
- 服务状态：`yuqing/nginx/mariadb/redis-server` 均为 active，`https://yuqing.zhuoran.cc/` 返回 200。
- 待业务验收：在数据接入后台创建或运行一个 `public_web_pull` 的 `jina_reader` 任务，或开启百度任务 `readerEnabled=true`，确认 `campus_ingest_record.content` 写入正文且 `campus_ingest_api_call_log(provider=jina_reader)` 有成功记录。

### 2026-05-16 首页线索库入口收敛验证
- `campus-web npm run build`：通过；仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- `.\mvnw.cmd -DskipTests compile`：通过；当前 Java classes 已是最新，Maven 返回 `BUILD SUCCESS`。
- 线上同步：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-181109-hide-clues` 并覆盖前后端产物；`yuqing/nginx/mariadb/redis-server` 均为 active。
- Flyway/数据库：`flyway_schema_history` 存在 `1.31 HideLegacyClueMenu success=1`；`campus_permission_menu` 中 `menu_code=clues` 为 `visible=0,status=0,deleted=0`。
- 线上冒烟：`https://yuqing.zhuoran.cc/`、`/login`、`/admin/monitor-tasks` 返回 200；未登录访问 `/campus/monitor/information/list` 返回 302。
- 验收口径：首页“最新舆情线索”按钮进入 `/monitor`；搜索结果详情进入 `/monitor/article/{id}`；历史 `/clues` 仅作为兼容重定向，不再作为独立前台菜单入口。

## Project 级测试清单

### 编译检查
- [x] 后端 `.\mvnw.cmd -DskipTests compile` 通过（2026-05-14）
- [x] 后端 `.\mvnw.cmd test -DskipTests=false` 通过（2026-05-14，33 tests）
- [x] 校园前端 `campus-web npm run build` 通过（2026-05-14）
- [ ] 大屏前端 `large_screen npm run build` 通过（修改 large_screen 时）

### API 测试
- [ ] 登录接口（手机号+密码+图形验证码）
- [ ] Token 获取接口
- [ ] 文章列表查询（含分页、筛选、排序）
- [ ] 文章详情
- [ ] 方案 CRUD
- [ ] 报告列表/详情
- [ ] 预警配置 CRUD
- [ ] 预警中心从线索生成预警：`POST /campus/alert/create-from-clue` 返回 `ResultVO<List<CampusAlert>>`，不再出现方法错配
- [ ] 全文搜索各分类数据查询
- [ ] 微信扫码登录流程
- [ ] 监测任务多语言关键词：`keywordsI18n/negativeWordsI18n/excludeWordsI18n` 与旧字段兼容
- [ ] 监测信息统一列表：默认本年范围只返回已命中监测任务的 `monitor_result` 数据，平台标签固定展示全部/抖音/小红书/知乎/新闻/网页/微博/微信公众号/B站/快手，操作列无“查看/线索”重复入口
- [ ] 监测信息平台状态：无数据平台能区分未接入、未启用和已接入 0 条；公开论坛不再作为固定独立平台入口，历史论坛/贴吧/豆瓣数据按新闻/网页口径兼容
- [ ] 监测信息字段完整性：监测结果行可从接入记录回填发布时间、作者、原文链接和互动数；`contentCaptureStatus/contentCaptureLabel` 能区分完整正文、摘要/标题、未采集；平台未返回时展示“未采集”
- [ ] 监测结果列表：按 `monitorTaskId/platform/language/converted/resultStatus/riskLevel/keyword` 筛选
- [ ] 监测结果转线索：正文、原文链接、作者、语言、命中词、风险等级进入线索库；接入记录已绑定线索时复用已有线索
- [ ] 风险等级统一：线索、监测结果、预警、事件、账号动态、检测命中均使用 `normal/concern/major/urgent`；前端展示为普通关注/一般预警/重大预警/特别重大，旧中文值和 `higher` 写入后能归一
- [ ] 学校相关性与主题分类：监测命中和线索均返回 `schoolRelevanceScore/schoolRelevanceReason/matchedSchoolTerms/topicCategory/topicReason`，监测命中转线索后字段保持一致
- [ ] 监测预警依据：`alert_mode=all_hits` 仅风险命中自动预警，普通主题词命中保持待处理；已预警行需能解释依据
- [ ] 结构化预警依据：监测、检测、线索/账号规则预警均写入 `evidenceJson`，预警列表可展示依据摘要
- [ ] 事件处置状态：未定级事件不能分派，未反馈任务不能复核，未复核事件不能归档，已归档事件不能继续编辑或流转
- [ ] 事件 SLA：分派不传 `dueTime` 时按风险等级生成默认截止时间；前端可展示截止时间
- [ ] 事件相似线索：`GET /campus/event/clue/suggest` 返回同主题/同风险线索并排除已关联当前事件线索
- [ ] 报告治理复盘：日报/事件复盘模板可渲染 `${governanceTable}`，仪表盘统计返回 `topicRiskDistribution/governanceMetrics`
- [ ] 任务内重点账号/链接：保存、编辑、启停、一键加入、扫描过滤、删除
- [ ] 后台监测任务管理：`/admin/monitor-tasks` 支持任务新增、编辑、启停、前台展示开关、删除、手动运行和重点目标维护；普通表单不再展示接入任务选择，高级诊断只读展示自动绑定接入任务、接入状态和最近错误
- [ ] 监测任务自动接入：保存任务后应为所选平台创建/复用 `campus_ingest_source` 与 `campus_ingest_task`；运行任务时先运行自动绑定接入任务，再扫描对应 `campus_ingest_record` 生成 `campus_monitor_result`
- [ ] 监测命中精准度：自动监测接入任务使用 `target_type=monitor_scan`，不得在规则命中前自动转线索；主体词/别名不得单独作为有效关键词生成监测命中，历史主体词-only命中、普通线索和搜索过程沉淀内容不得出现在监测信息统一列表
- [ ] 监测信息过滤：`displayEnabled=0` 或已删除监测任务产生的命中不得出现在 `/campus/monitor/information/list`、`count-by-platform`、`count-by-sub-platform` 和监测概览统计中
- [ ] 监测任务暂停过滤：`taskStatus=paused/disabled` 的监测任务历史命中不得出现在 `/campus/monitor/information/list`、平台统计和监测概览统计中
- [ ] 监测信息站内详情：点击统一列表“详情”应先展示站内同步的标题、正文、平台、作者、发布时间、命中词和互动数；只有点击“查看原链接”才打开外部原文
- [ ] 微博接入精准度：`weibo_search_all` 不得把搜索页、超话/话题统计卡、账号资料卡或无正文对象入库；开启详情增强时必须按微博帖子 ID 调 `weibo_post_detail_v2` 并回填正文/原文链接/互动数
- [ ] 数据接入后台：`/ingest` 不展示运行日志、API 调用日志和历史外部接口供应商调用配置
- [ ] AI 能力管理后台：`/admin/settings/ai` 可查询和保存供应商、模型、功能绑定、提示词，并能查看 `campus_ai_call_log`；接口失败时页面不应空白，应展示可读错误提示
- [ ] AI 能力配置：DeepSeek 报告/少数民族研判/词云读取 `campus_ai_feature_binding`；配置停用或密钥缺失时按失败策略回退或报错
- [ ] 词云提取：DeepSeek 可用时返回结构化热词；不可用时回退现有关键词统计，首页不报错
- [ ] 历史能力：写作宝、OCR、旧 NLP 分词、DashScope 默认停用，不自动进入当前校园主流程
- [ ] 数据接入后台：百度任务可配置 `readerEnabled/maxReaderCalls/fallbackToSnippet/readerTimeoutMs`；公开网页任务可在 `metadata_only` 与 `jina_reader` 间切换
- [ ] 百度正文增强：Reader 成功时 `campus_ingest_record.content` 为正文；Reader 失败且 `fallbackToSnippet=true` 时保留百度摘要；调用写入 `campus_ingest_api_call_log(provider=jina_reader)`
- [ ] TikHub 多平台接入：抖音、小红书、微博、B站、知乎、微信公众号、快手 endpointKey 均在 allowlist；小红书/微博/B站开启 `detailEnabled=true` 时只增强同一条接入记录，不新增重复详情记录
- [ ] 白名单公开网页：`mode=jina_reader` 只允许白名单内单 URL，成功后产生 1 条 `platform=public_web` 接入记录；非白名单 URL 必须阻断
- [ ] 教育专题：专题列表、学校声量正负面排名、百度接入来源选择、百度任务创建/立即运行
- [ ] 学校主体：模板下载、CSV 导入、按 `schoolId/schoolName` 去重更新

### 权限测试
- [ ] 未登录访问需鉴权接口 → 重定向到登录或返回403
- [ ] Token 过期后访问 → 正常处理
- [ ] 用户数据隔离（A 用户看不到 B 用户的方案/报告）
- [ ] 无效 Token 访问 → 正确拒绝
- [ ] URL 参数 token 方式正常
- [ ] Cookie token 方式正常
- [ ] 请求头 token 方式正常
- [ ] `campus_operator` 可访问教育专题读写接口，`campus_viewer` 只能访问 GET
- [ ] 无 `campus:monitor:operate` 权限时不能转线索、加重点账号/链接
- [ ] 无 `campus:monitor:operate` 权限时不能在后台监测任务管理页新增、编辑、启停、删除、手动运行或维护重点目标
- [ ] 无 `campus:education:operate` 权限时不能新增/编辑/删除/导入学校，也不能创建/运行百度任务
- [ ] 后台管理入口按当前用户菜单/权限过滤，不展示未授权模块入口

### 状态流转测试
- [ ] 用户状态：正常↔禁用↔注销
- [ ] 方案软删除：正常→删除
- [ ] 预警开关：开启↔关闭
- [ ] 文章已读/未读标记
- [ ] 文章失效/有效标记
- [ ] 报告状态流转（任务→编制中→成功/失败）
- [ ] 舆情研判任务状态流转
- [ ] 监测结果：`pending/alerted → converted`，重复转线索返回已有线索
- [ ] 监测结果：普通主题词命中不从 `pending` 自动转为 `alerted`
- [ ] 事件：`pending_judge → rated → assigned/processing → feedback → reviewed → archived` 前置校验生效
- [ ] 任务内重点目标：`active ↔ paused → deleted=1`
- [ ] 学校主体：`1 ↔ 0 → deleted=1`

### 数据一致性测试
- [ ] 方案创建后偏好设置和预警配置同时创建
- [ ] 方案删除后关联数据（文章、预警、收藏）正确处理
- [ ] 方案修改后 project_task 同步更新
- [ ] Kafka 热词发送正常

### 回归测试
- [ ] 核心功能：数据监测列表+详情
- [ ] 核心功能：监测分析图表
- [ ] 核心功能：方案管理（增删改查）
- [ ] 核心功能：报告管理
- [ ] 核心功能：全文搜索
- [ ] 核心功能：用户登录/登出

### 冒烟测试
- [ ] 应用启动正常（Spring Boot 启动无异常）
- [ ] 首页/登录页正常渲染
- [ ] 监测页面正常加载文章列表
- [ ] 分析页面正常显示图表数据
- [ ] 数据库连接正常
- [ ] Redis 连接正常

## AI 子线程完成后的最低测试要求

1. **编译检查**：如果修改了后端代码 → `mvn compile` 通过
2. **构建检查**：如果修改了前端代码 → `npm run build` 通过
3. **API 检查**：如果新增/修改 API → 在 API_CONTRACT.md 中记录
4. **权限检查**：如果涉及鉴权 → 在 PERMISSION_RULES.md 中评估
5. **状态检查**：如果涉及状态变更 → 在 STATE_MACHINE.md 中更新
6. **约定检查**：代码风格符合 CONVENTIONS.md
7. **模块检查**：如果新增业务 → 在 `docs/modules/*/manifest.md` 中确认模块归属和影响范围

## 当前测试缺口

1. **单元测试覆盖不足** — 已有接入模块单测，但核心业务 Service 覆盖仍很少
2. **完整测试门禁覆盖面有限** — `mvn test -DskipTests=false` 已稳定，但多数核心业务仍缺少断言覆盖
3. **无前端测试** — 无组件测试/E2E 测试
4. **无 API 测试** — 无 Postman/API Fox 集合或自动化 API 测试
5. **无 CI 流水线** — 无自动构建/测试/部署流程
6. **Lint 未配置** — 无法自动检查代码质量

## 最低可执行测试方案（逐步补充）

1. **P0**：将 `.\mvnw.cmd -DskipTests compile` 和 `campus-web npm run build` 固化为当前主线最低门禁
2. **P0**：已拆分旧 `StonedtPortalApplicationTests` 中的完整上下文加载，让 `mvn test -DskipTests=false` 可稳定结束
3. **P1**：为核心 Service（UserService、ProjectService、CampusMonitorService、CampusReportService）添加基础单元测试
4. **P1**：创建 Postman/API Fox 测试集合用于手工 API 测试
5. **P2**：为新增功能添加单元测试，逐步提高覆盖率
6. **P3**：引入 CI（GitHub Actions），自动执行测试

## 最近验证记录

- 2026-05-17：Batch41-B47 生产发布验证；`.\mvnw.cmd clean -DskipTests package` 通过，确认 jar 内仅包含 `V1.40__CampusReportTargetedScope.sql`、`V1.41__CampusBlueprintCompletion.sql`、`V1.42__CampusRiskTopicTaxonomy.sql`；服务器备份 `/home/ubuntu/yuqing-backups/deploy-20260517-021145-governance-full`，Flyway `1.41/1.42` 成功，`yuqing/nginx/mariadb/redis-server` active，后端监听 8084；`https://yuqing.zhuoran.cc/`、`/monitor`、`/admin/monitor-tasks`、`/settings/ai-management` 返回 200，未登录 `/campus/dashboard/overview`、`/campus/ai/overview`、`/campus/monitor/information/list` 返回 302。
- 2026-05-17：Batch41-B47 蓝图 MVP 闭环本地验证；`git diff --check` 通过，`campus-web npm run build` 通过，`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过（16 个测试类，55 tests，0 failures / 0 errors / 0 skipped）。
- 2026-05-17：报告功能恢复与 AI 生成优化本地验证；`.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过（18 个测试类，61 tests，0 failures / 0 errors / 0 skipped），`.\mvnw.cmd -DskipTests package` 通过，`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- 2026-05-16：旧食品安全数据清理与微信公众号排查发布；服务器 `/home/ubuntu/yuqing-test-batch37-20260516-191931` 中 `ThirdPartyApiIngestAdapterTest,TikhubClientTest,TikhubResponseMapperTest` 通过（16 tests），完整后端 `./mvnw -DskipTests=false test` 通过（53 tests），`campus-web npm run build` 通过，`./mvnw clean -DskipTests package` 通过；线上备份 `/home/ubuntu/yuqing-backups/deploy-20260516-192245-food-clean-wechat`，Flyway `1.36` 成功，旧食品安全线索/接入记录/监测结果/预警剩余均为 0；微信公众号自动任务已改为 `query=新疆大学/sortType=_0`，最新运行成功但 TikHub 返回 0 条；`yuqing/nginx/mariadb/redis-server` active，`/login`、`/monitor` 返回 200，未登录 `/campus/monitor/information/list` 返回 302。
- 2026-05-16：Batch35 监测命中精准度热修完成；本地 `.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过（15 个测试类，50 tests），`.\mvnw.cmd -DskipTests package` 通过；服务器已发布到 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar`，备份路径 `/home/ubuntu/yuqing-backups/deploy-20260516-175458-precision`；Flyway `1.30` 应用成功，8 个“自动监测-*”任务修正为 `target_type=monitor_scan`，监测信息统一列表新口径为 `monitor_result=119`、`clue=191`；`yuqing/nginx/mariadb/redis-server` active，`/`、`/login`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/information/list` 返回 302。
- 2026-05-16：暂停任务过滤、微博精准接入和站内详情发布；`TikhubResponseMapperTest` 通过 10 tests，`campus-web npm run build` 通过，`.\mvnw.cmd test -DskipTests=false` 通过（15 个测试类，51 tests），`.\mvnw.cmd -DskipTests package` 通过；服务器备份 `/home/ubuntu/yuqing-backups/deploy-20260516-183244-detail-precision`，已同步 jar 与前端 dist；线上 `校园食品安全监测=disabled/display_enabled=1` 但不再进入监测信息，active 监测命中为 `新疆大学=13`，统一列表新口径为 `monitor_result=13`、`clue=191`；`/`、`/login`、`/monitor`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/information/list` 返回 302，`yuqing/nginx/mariadb/redis-server` active。
- 2026-05-16：微博旧链接与旧详情二次清理；`V1.32` 修复可识别帖子 ID 的旧链接，`V1.33` 隐藏/置空非真实微博帖子的旧 profile/search/external 链接；`TikhubResponseMapperTest` 通过 11 tests，完整后端 `.\mvnw.cmd test -DskipTests=false` 通过（52 tests），`.\mvnw.cmd -DskipTests package` 通过；服务器备份 `/home/ubuntu/yuqing-backups/deploy-20260516-185347-weibo-post-only`，Flyway `1.32/1.33` 成功，非真实帖子格式的微博接入记录和线索链接均为 0，微博可见信息收敛到 56 条真实帖子链接；`/`、`/monitor` 返回 200，未登录接口 302，四个服务 active。
- 2026-05-14：监测信息工作台前端 `campus-web npm run build` 通过；服务器 `/home/ubuntu/yuqing-test-87286a5` 中 `bash mvnw -q -DskipTests compile`、`campus-web npm install && npm run build`、`bash mvnw -q -DskipTests package` 均通过；正式库统一列表默认本年口径命中 114 条。
- 2026-05-15：Batch33 P2 当前工作区快照已同步到服务器测试目录 `/home/ubuntu/yuqing-test-batch33-p2-20260515-092114`；服务器 `./mvnw -DskipTests compile`、接入/Jina 7 个测试类 25 个用例、`campus-web npm install && npm run build`、`./mvnw -DskipTests package` 均通过；未覆盖生产服务。
- 2026-05-14：`campus-web npm run build` 通过。
- 2026-05-14：已同步到服务器测试目录 `/home/ubuntu/yuqing-test-66adf24`；服务器 `mvn "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过（33 tests），服务器 `campus-web npm run build` 通过。

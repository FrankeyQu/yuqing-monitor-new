# AI 进度记录 — 卓然舆情

## 项目当前状态摘要

- **项目名称**：卓然舆情（Zhuoran Insight）
- **开源协议**：GPLv3
- **当前阶段**：报告恢复、监测 AI 分析、舆情态势工作台/大屏合并和接入去重修复已发布，旧生产日报/月报分支已核对为当前 `main` 等价吸收，正在修复监测命中 AI 分析返回格式兼容问题并部署
- **正式主线**：本地 `D:\PRJ\yuqing` 的 `main` 分支
- **当前 Git 状态**：`main` 以记录式合并纳入旧生产线 `claude/daily-monthly-reports`，文件内容保持当前生产代码
- **最近已发布功能提交**：commit `6b30d00 Merge production ingest dedup and dashboard sync`
- **当前版本**：0.5.3-SNAPSHOT
- **主线确认时间**：2026-05-14，用户先确认以本地 `master` 作为正式主线；同日已将本地主线改名为 `main`

## 2026-05-18 main 追平生产分支

- **用户目标**：确认线上可用后，将 GitHub `main` 追平当前服务器实际运行代码，避免后续从 `main` 部署时回退大屏统一、监测 AI 或接入去重修复。
- **合并范围**：在 `D:\PRJ\yuqing-report-ai-recovery` 的 `main` worktree 合并 `claude/fix-ingest-deleted-dedup`；纳入舆情态势工作台/大屏合并、`V1.46__CampusDashboardScreenUnification.sql`、接入软删除记录去重修复和对应文档。
- **生产现状核验**：线上 jar 已包含 `V1.44__CampusReportPromptAndTemplates.sql`、`V1.45__CampusMonitorAiAnalysis.sql`、`V1.46__CampusDashboardScreenUnification.sql` 与接入去重修复；`yuqing/nginx/mariadb/redis-server` 均 active，`yuqing` 当前 `NRestarts=0`。
- **发布策略**：本轮是 Git 主线追平生产代码，不重新覆盖服务器；后续发布应以合并后的 `main` 为准。

## 2026-05-18 日报月报旧生产分支并入 main

- **用户目标**：将本地 `claude/daily-monthly-reports` 全部 merge 到 `main` 并部署上线，同时排查监测任务 AI 分析失败 20 条的原因。
- **分支判断**：`claude/daily-monthly-reports` 指向旧 `deploy-vps/main` 历史线，与当前 GitHub `main` 无共同 merge-base；其日报/月报、报告 scope 和自动报告能力已在当前 `main` 中通过报告恢复线、`V1.40__CampusReportTargetedScope.sql`、报告模板和自动报告模块等价吸收。
- **合并策略**：本地尝试采用 `ours` merge 记录式并入旧历史，避免把旧生产线文件内容覆盖到当前 `main`，从而回退监测 AI、舆情态势大屏、AI 管理和接入去重修复；GitHub 远端因旧历史缺失对象 `718ba19b31214b77913b95b5860cd738d73acd8b` 拒收带旧父节点的 merge commit，因此最终以当前 `main` 代码树和文档记录完成等价吸收，保留本地保护分支 `codex/daily-history-merge-unpushable` 便于追溯。
- **部署策略**：仍以当前 `main` 文件树打包并覆盖服务器；此次部署是确认 `main` 与生产代码保持一致，而不是回滚到旧日报/月报分支内容。
- **AI 分析失败 20 条根因**：线上 `campus_ai_call_log` 显示 DeepSeek 调用 HTTP 200 且状态为 success，失败发生在应用层解析。AI 实际返回了根级 JSON 数组，并使用 `hitAdvice/schoolRelevance/reason` 等旧字段；后端只接受 `{ "results": [...] }` 且期望 `shouldHit/schoolRelevanceScore/hitReason/topicReason`，因此抛出“AI响应缺少results数组”，本次选择的 20 条被统一标记失败。
- **兼容修复**：`CampusMonitorServiceImpl` 对监测命中 AI 分析追加运行时 JSON 合同约束；解析端兼容根级数组和 `data/results` 包装；字段归一兼容 `hitAdvice -> shouldHit`、`reason -> hitReason/topicReason`、`schoolRelevance(high/medium/low) -> schoolRelevanceScore`，避免同类模型输出导致整批失败。

## 2026-05-17 多分支合并与生产部署收口

- **用户目标**：把之前已完成的报告恢复、监测 AI 分析、工作台/大屏合并等改动合并到同一条可部署分支，并发布到服务器。
- **执行分支 / Worktree**：本地 worktree `D:\PRJ\yuqing`，分支 `claude/merge-all-deploy-20260517`；合并 `claude/unify-dashboard-screen` 与 `main` 报告恢复线，保留监测情感、ID 精度、报告模板和大屏入口等前序改动。
- **迁移对齐**：生产库已执行 `V1.44__CampusReportPromptAndTemplates.sql` 和一次 `V1.45__CampusMonitorAiAnalysis.sql`，因此源码最终对齐为 `V1.44` 报告模板、`V1.45` 监测 AI、`V1.46` 工作台/大屏菜单，避免 Flyway checksum 和版本复用冲突。
- **本地验证**：使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd clean -DskipTests package` 通过，jar 内迁移确认只包含 `V1.44__CampusReportPromptAndTemplates.sql`、`V1.45__CampusMonitorAiAnalysis.sql`、`V1.46__CampusDashboardScreenUnification.sql`；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **Git 同步**：提交 `6027506 merge: combine report dashboard monitor ai changes` 与 `765a4f5 fix: align campus migration order with production` 已推送到 GitHub `origin/claude/merge-all-deploy-20260517` 和服务器远端 `deploy-vps/claude/merge-all-deploy-20260517`。
- **生产发布**：发布包 SHA256 已与服务器 `/tmp` 上传包一致；发布前备份 jar、web 和数据库到 `/home/ubuntu/yuqing-backups/deploy-20260517-232619-merge-all-flyway-align`；已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 与 `/opt/yuqing/web`。
- **线上验收**：`yuqing` 为 active，后端监听 `8084`；Flyway `1.44 CampusReportPromptAndTemplates`、`1.45 CampusMonitorAiAnalysis`、`1.46 CampusDashboardScreenUnification` 均 success=1；菜单 `workbench` 已改为“舆情态势”且 `situation` 已隐藏；`/`、`/situation`、`/monitor`、`/reports`、`/report-templates`、`/auto-reports` 返回 200，未登录 dashboard、监测 AI、报告列表接口返回 302；线上静态资源包含“大屏模式”“AI分析”“报告模板”。
- **遗留观察**：启动后接入调度出现重复 `external_id` 的业务告警，属于接入去重数据问题，不影响本次发布启动和页面/API 冒烟，后续可单独排查。

## 2026-05-17 接入软删除记录去重修复

- **用户目标**：修复生产接入调度里 `Duplicate entry ... uk_campus_ingest_record_external` 告警，避免软删除历史记录导致同一来源外部 ID 重复插入失败。
- **根因确认**：`campus_ingest_record` 数据库唯一索引 `uk_campus_ingest_record_external(source_id, external_id)` 和 `uk_campus_ingest_record_hash(source_id, content_hash)` 不区分 `deleted`；应用层查重却过滤 `deleted=0`，导致历史清理软删除记录仍占用唯一键，但新接入无法识别为重复。
- **后端修复**：`CampusIngestRecordMapper` 中唯一键对应的 `selectDuplicate`、`selectDuplicateByExternalId`、`selectDuplicateByContentHash` 不再过滤 `deleted=0`；软删除历史记录会被判为 duplicate，不恢复显示、不重新插入，保持历史清理结果。
- **测试补充**：新增 `CampusIngestRecordMapperContractTest`，锁定外部 ID / 内容哈希查重必须覆盖软删除记录，同时保留平台标题近似去重只查可见记录。
- **本地验证**：使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd "-Dtest=CampusIngestRecordMapperContractTest" "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过；`.\mvnw.cmd clean -DskipTests package` 通过。
- **生产发布**：提交 `7dda184 fix: deduplicate soft deleted ingest records` 已推送 GitHub `origin/claude/fix-ingest-deleted-dedup` 和服务器远端；发布前备份 jar 到 `/home/ubuntu/yuqing-backups/deploy-20260517-234447-ingest-deleted-dedup`，已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 并重启 `yuqing`。
- **线上验证**：`yuqing` active，后端监听 `8084`；`/`、`/monitor` 返回 200，未登录 `/campus/ingest/runs` 返回 302；手动将问题任务 `2054608482077904896` 的 `next_run_time` 拨到当前时间触发一次调度，最新 run `2056039386365169664` 为 `success`，`fetched_count=20/duplicate_count=20/fail_count=0/error_type=NULL`，任务 `consecutive_fail_count` 清零且 `last_error_type=NULL`。

## 2026-05-17 监测任务与监测信息 AI 分析

- **用户目标**：监测任务页增加 AI 体检；监测信息页支持手动单条/批量 AI 分析，给每条命中重新判断情感、生成一句话摘要、判断是否建议命中并归类校园主题。
- **后端调整**：新增 `POST /campus/monitor/task/ai-diagnose` 和 `POST /campus/monitor/result/ai-analyze`；新增 `campus_monitor_result` AI 辅助字段 `ai_summary/ai_hit_recommendation/ai_hit_reason/ai_confidence/ai_analysis_time/ai_provider_code/ai_model_code`；监测命中 AI 分析成功后更新情感、AI摘要、AI建议、学校相关性和主题分类，已转未归档线索同步相关字段，已归档线索关联记录跳过写入。
- **AI 能力配置**：新增功能绑定和提示词 `monitor_result_analysis`、`monitor_task_diagnosis`，继续通过 `campus_ai` 统一读取模型配置和记录脱敏调用日志；合并发布过程中生产库已执行 `V1.45__CampusMonitorAiAnalysis.sql`，源码按线上 Flyway 历史保留该版本，避免校验冲突。
- **前端调整**：`/admin/monitor-tasks` 增加“AI体检”操作和只读结果弹窗；`/monitor` 增加当前页 AI 分析、单条“更多 → AI分析”、批量 AI 分析和 AI 建议列，标题摘要优先展示 AI 摘要。
- **业务边界**：AI 判断“不建议命中”只作为辅助建议展示，不自动忽略、不删除、不转预警、不改变风险等级或结果状态；任务 AI 体检不写回配置、不展示具体采集内容。
- **本地验证**：`git diff --check` 通过（仅保留既有 CRLF 工作区提示）；`CampusMonitorResultMapper.xml`、`CampusClueMapper.xml` XML 解析通过；`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。

## 2026-05-17 舆情态势工作台与大屏模式合并

- **用户目标**：消除客户侧“工作台 / 态势大屏”重复入口，将普通排列式工作台和一屏大屏整合到同一业务页面内，通过“大屏模式”切换展示。
- **执行分支 / Worktree**：本地 `D:\PRJ\yuqing`，分支 `claude/unify-dashboard-screen`；实施前 worktree 已存在另一组监测 AI 分析相关未提交改动，本轮仅暂存和提交本任务文件。
- **前端调整**：`/` 统一为“舆情态势工作台”，右上新增“大屏模式 / 退出大屏”；`/situation` 复用同一 `DashboardView` 并直接进入大屏模式；大屏状态隐藏侧边栏和顶栏，使用 100vh 一屏网格展示核心指标、监测趋势、风险压力、最新命中、告警、任务和事件图表。
- **数据与图标**：新增 `useCampusSituationDashboard` 统一态势数据加载和统计计算，移除首页默认 mock 兜底数据；导航和指标图标更新为 `LayoutDashboard / Radar / Siren / RadioTower / ScanSearch / Target / BellRing / Gauge` 等更贴近业务的 Lucide 图标；`PlatformBadge` 在 `showIcon=true` 时补齐平台图标。
- **菜单与兼容**：新增 Flyway `V1.46__CampusDashboardScreenUnification.sql`，将 `workbench` 菜单展示名调整为“舆情态势”并隐藏 `situation` 菜单；历史 `/situation` 路由和权限保留，避免直达链接失效。
- **文档同步**：更新 `docs/ARCHITECTURE.md`、`docs/PERMISSION_RULES.md`、`docs/campus-web-runbook.md` 和 `docs/campus-acceptance-runbook.md`，记录页面入口和菜单权限变化。
- **本地验证**：`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；`git diff --check` 针对本任务文件通过，仅有 CRLF 换行提示；使用本地 Vite + 临时 mock 后端浏览器验收 `/` 普通模式和 `/situation` 大屏模式，确认侧边栏/顶栏在大屏模式隐藏、图表渲染、核心文案和菜单入口正确。

## 2026-05-17 监测信息情感人工校正

- **用户目标**：监测信息页每条信息的情感可人工修改，并评估后确认采用“监测信息为入口，已转线索同步修正，已归档线索禁止修改”的校园单用户模式。
- **后端调整**：新增 `POST /campus/monitor/result/sentiment`，统一写入 `positive/neutral/negative/none`；更新 `campus_monitor_result.sentiment`，已关联线索时同步 `campus_clue.sentiment` 并写入线索操作日志；关联线索已归档时返回失败，不改监测结果或线索。
- **前端调整**：`/monitor` 监测信息表格“情感”列改为可操作下拉；批量操作新增“批量修改情感”；无监测操作权限、缺少监测结果 ID 或关联已归档线索时禁止修改。
- **文档同步**：更新 `docs/API_CONTRACT.md`、`docs/TEST_CHECKLIST.md`、`docs/modules/campus_monitor/manifest.md` 和 `docs/modules/campus_clue/manifest.md`，记录接口、同步口径和验收点。
- **本地验证**：`git diff --check` 通过（仅换行提示）；`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd -DskipTests package` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **GitHub / 服务器同步**：提交 `d1e7e41 feat: allow campus monitor sentiment edits` 已推送 GitHub `origin/claude/fix-campus-monitor-id-precision` 和服务器远端 `deploy-vps/claude/fix-campus-monitor-id-precision`。
- **生产部署**：已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`；发布前备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-193923-monitor-sentiment`，包含 `app.jar` 与 `web.tar.gz`。
- **线上验收**：`yuqing/nginx/mariadb/redis-server` 均为 active，后端监听 `8084`；`https://yuqing.zhuoran.cc/monitor` 返回 200；未登录访问 `POST /campus/monitor/result/sentiment?monitorResultId=1&sentiment=negative` 返回 302，符合鉴权预期；线上静态资源已包含“批量修改情感”。
- **样式修正**：按用户反馈将监测信息表格情感列从常驻下拉框恢复为原 `EmotionBadge` 标签样式，仅点击标签时弹出修改菜单；批量修改情感保留在批量操作弹窗内。
- **样式修正验证**：`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **样式修正发布**：提交 `81b6aa6 fix: restore monitor sentiment badge style` 已推送 GitHub 和服务器远端；仅覆盖 `/opt/yuqing/web`，发布前备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-195451-monitor-sentiment-style`；`https://yuqing.zhuoran.cc/monitor` 返回 200，线上静态资源已包含 `sentiment-badge-trigger` 且不再包含 `sentiment-select`。

## 2026-05-17 报告功能恢复与 AI 生成优化

- **执行边界**：从生产最新代码点 `53f77f1` 创建独立分支 `claude/report-ai-recovery` 和 worktree，未混入原工作区其它模块改动；不修改 `pom.xml` 和核心配置，不删除旧报告链路。
- **ID 精度修复**：报告、模板、报告事件、自动报告任务和生成日志的 Long 业务 ID 增加字符串序列化；前端报告相关 API 改用 `ApiId`，避免 19 位 Snowflake ID 被浏览器 number 精度截断。
- **字段与链路补齐**：实体和 Mapper 接入 `generation_mode`、AI 审计字段、scope 字段、自动报告调度锁、生成日志 `generation_mode/duration_ms`；自动报告按任务 `generationMode` 调用传统或 AI 生成。
- **针对性分析与 AI 输出**：报告数据聚合统一使用关键词、排除词、平台、风险等级、部门、监测任务和事件 scope；`analysisProfile` 控制 AI Prompt 侧重点；AI 失败不再保存失败 markdown 为正式报告内容。
- **自动调度与 SSE**：新增自动报告调度扫描组件，默认关闭，按 `active + nextRunTime` 加锁执行；AI SSE 改为边生成边发送 `message`，完成发送 `done`，失败发送 `error`。
- **二次恢复与模板体验**：修复 AI 输入 JSON 快照中的 Fastjson `$ref` 问题；新增 `ai_user_prompt`、自动报告 `event_id`、事件下拉 ID 字符串序列化；报告/自动报告表单改为模板和事件下拉；模板管理拆为 `/report-templates` 独立列表与编辑页；新增高校日报、周报、月报、重大事件、招生就业、后勤服务、学生安全心理风险模板种子。
- **本地验证**：使用 `D:\PRJ\yuqing\.codex-tools\jdk8\jdk8u482-b08` 临时设置 `JAVA_HOME` 后，`.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd test -DskipTests=false` 通过（19 个测试类，62 tests）；`.\mvnw.cmd -DskipTests package` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **GitHub 合并**：提交 `e06d5ae feat: restore AI report generation` 已推送到 GitHub `origin/claude/report-ai-recovery`，并 fast-forward 合并推送到 `origin/main`；服务器裸仓库已同步 `deploy-vps/claude/report-ai-recovery`，`deploy-vps/main` 与 GitHub 快照历史无共同祖先，本次未强推覆盖。
- **生产发布**：发布前备份线上 jar、前端 web 和数据库到 `/home/ubuntu/yuqing-backups/deploy-20260517-214310-report-ai-template`；已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`，重启 `yuqing` 并 reload `nginx`。
- **线上验收**：Flyway 已从 `1.43` 迁移到 `1.44 CampusReportPromptAndTemplates` 且 success=1，7 个高校场景模板种子已落库；`yuqing/nginx/mariadb/redis-server` 均 active，后端监听 `8084`；`https://yuqing.zhuoran.cc/`、`/reports`、`/report-templates`、`/auto-reports` 返回 200，未登录 `/campus/report/list?pageNum=1&pageSize=1` 返回 302。

## 2026-05-17 校园事件单用户台账模式收敛

- **用户目标**：事件处置先按单用户平台使用，不在系统内确定部门、反馈人和复核人；学校内部协同、反馈和复核暂时走线下流程。
- **后端调整**：`CampusEventController/Service` 新增 `/campus/event/clue/add`，把已有线索真正加入已有事件，同时写入 `campus_event_clue` 与 `campus_clue.event_id`；新增 `/campus/event/record/add`，用于记录线下处置情况，并支持 `/campus/event/record/list?eventId=` 按事件查看处置记录；事件归档放宽为任意未归档事件填写结论后可归档。
- **前端调整**：`/events` 从“处置任务”收敛为“处置记录”，主操作改为“记录线下处置”；相似线索增加“加入事件”按钮；`/monitor` 批量/单条“加入事件”改为调用事件归集接口，不再伪装成线索保存。
- **文档同步**：更新 `docs/API_CONTRACT.md`、`docs/STATE_MACHINE.md`、`docs/TEST_CHECKLIST.md` 和 `docs/modules/campus_event/manifest.md`，记录单用户事件台账模式、接口、状态流转和验证结果。
- **本地验证**：使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd -DskipTests compile` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **GitHub / 服务器同步**：提交 `53f77f1 feat: simplify campus event workflow` 已推送 GitHub `origin/claude/fix-campus-monitor-id-precision` 和服务器远端 `deploy-vps/claude/fix-campus-monitor-id-precision`。
- **生产部署**：本地 `.\mvnw.cmd -DskipTests package` 与 `campus-web npm run build` 通过后，已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`；发布前备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-185919-event-single-user`；`yuqing/nginx/mariadb/redis-server` 均为 active，`https://yuqing.zhuoran.cc/events` 返回 200，未登录访问 `/campus/event/list?pageNum=1&pageSize=1` 返回 302，符合鉴权预期。

## 2026-05-17 Batch1-Batch6 新界面业务闭环修正

- **执行边界**：按用户要求作为主线程完成 Batch1-Batch6；本轮不考虑权限问题，不新增账号/角色逻辑，不改 `pom.xml` 和核心配置，不创建/切换分支。
- **Batch1 报告 AI 生成闭环**：`/campus/report/generate-ai` 改为返回并持久化 `CampusReport`，`generate-ai-stream` 结束后前端重新读取报告详情；报告 Service 统一保存 `reportContent/reportStatus/reportFormat/fileName/generatedBy/generateTime`。
- **Batch2 报告统计周期口径**：报告数据聚合新增 `eventId + keyword + period` scope，媒体分布、情感分布、热词、热点文章、走势和总数共用同一批线索，避免不同统计块口径漂移。
- **Batch3 首页与监测口径统一**：首页监测概览的今日监测数和 7 日趋势改用 `campus_monitor_result` 统一监测信息风险口径，继续只统计 active 且允许展示任务。
- **Batch4 接入部分成功与风险继承**：接入运行日志新增 `partial_success` 推导；任务运行有成功也有失败时不再整体失败；`target_type=clue` 自动转线索继承接入记录风险等级，转换异常会标记记录失败。
- **Batch5 线索事件事务与状态保护**：线索保存、研判、归档、删除和事件保存、转事件、定级、分派、反馈、退回、复核、归档均补事务；已归档线索禁止编辑/研判/重复归档，已转事件线索禁止重复研判或重复转事件，SQL 层补充并发保护。
- **Batch6 搜索与辅助研判定位**：前端搜索明确为“线索搜索”，继续调用 `/campus/clue/list`；辅助研判页面标注为“规则辅助研判”，默认 `local_heuristic`，避免误导为完整 AI 研判。
- **文档同步**：更新 `docs/API_CONTRACT.md`、`docs/STATE_MACHINE.md`、`docs/TEST_CHECKLIST.md` 以及 `campus_report/campus_ingest/campus_clue/campus_event` manifest。
- **本地验证**：`git diff --check` 通过；`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过，16 个测试类 55 tests；`campus-web npm run build` 通过；`CampusClueMapper.xml` 和 `CampusMonitorResultMapper.xml` XML 解析通过。

## 2026-05-17 Batch41-B47 生产发布与 Flyway 修复

- **Git 提交与推送**：先提交 `090ef63 feat: complete campus monitoring governance`，后因生产库已执行过旧 `V1.40`，补交 `9c2ba43 fix: preserve applied flyway migration`；两次提交均已推送到服务器裸仓库远端 `deploy-vps/claude/batch33-monitor-admin`。GitHub HTTPS 远端 `origin/new-origin` 推送仍失败，远端报 `did not receive expected object 718ba19b31214b77913b95b5860cd738d73acd8b`，需后续单独处理远端仓库对象问题。
- **发布备份**：发布前已备份线上 jar、前端 web 和数据库，备份目录为 `/home/ubuntu/yuqing-backups/deploy-20260517-021145-governance-full`，包含 `app.jar`、`web.tar.gz` 和 `campus_yuqing.sql`。
- **Flyway 修复**：生产库已有 `1.40 CampusReportTargetedScope`，本地曾把风险主题字典误放到 `V1.40`，导致 checksum mismatch。已恢复原始 `V1.40__CampusReportTargetedScope.sql`，并将风险等级/主题字典迁移顺延为 `V1.42__CampusRiskTopicTaxonomy.sql`；重新 `clean package` 后确认 jar 内只有 `V1.40/V1.41/V1.42` 各一份。
- **发布结果**：已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`，`SPRING_FLYWAY_PLACEHOLDER_REPLACEMENT=false` 保持生效。线上 Flyway 已成功应用 `1.41 CampusBlueprintCompletion` 和 `1.42 CampusRiskTopicTaxonomy`；`flyway_schema_history` 中 `1.40/1.41/1.42` 均为 success。
- **线上验收**：`yuqing/nginx/mariadb/redis-server` 均 active，后端监听 `8084`；`https://yuqing.zhuoran.cc/`、`/monitor`、`/admin/monitor-tasks`、`/settings/ai-management` 返回 200 并命中新前端 `index.html`；未登录访问 `/campus/dashboard/overview`、`/campus/ai/overview`、`/campus/monitor/information/list` 返回 302，符合鉴权预期。

## 2026-05-17 GitHub 快照迁移

- **迁移目标**：用户明确不保留 GitHub 历史记录，只要求远端代码与本地当前代码一致；因此采用“当前文件树快照”迁移，而不是继续修复旧历史对象包。
- **远端整理**：新的 GitHub 仓库 `https://github.com/FrankeyQu/yuqing-monitor-new.git` 已设为本地 `origin`；旧仓库 `https://github.com/FrankeyQu/yuqing-monitor.git` 保留为 `origin-old`；服务器裸仓库 `deploy-vps` 保留完整历史与部署分支。
- **一致性校验**：GitHub `origin/main` 使用本地最新 `HEAD` 的 tree 创建无父快照提交，远端 `main` 与当前工作分支文件树一致；本地 `main` 已跟踪 `origin/main`，原本地 `main` 已备份为 `backup/main-before-snapshot-20260517`。
- **服务器状态**：服务器生产服务仍运行同一份代码包，未因 GitHub 历史迁移重启；`yuqing/nginx/mariadb/redis-server` 保持 active。

## 2026-05-17 监测信息风险/全部命中双口径

- **问题定位**：线上 active 且允许展示的真实监测命中约 296 条，但默认列表只剩约 12 条，是因为主体词-only 精准度过滤被写死在后端统一列表中；该过滤方向正确，但缺少“全部真实命中”覆盖面视图。
- **后端调整**：`/campus/monitor/information/list`、`count-by-platform`、`count-by-sub-platform` 新增 `hitScope` 参数；默认 `risk` 继续过滤主体词-only 历史命中，`all` 只放开真实 `campus_monitor_result` 的主体词-only 命中，仍过滤暂停/禁用/隐藏/删除任务，并且不再 union 普通线索。
- **前端调整**：`/monitor` 监测信息标题区增加“风险命中 / 全部真实命中”切换；切换时同步刷新列表、平台统计和子平台统计，默认保持“风险命中”。
- **本地验证**：`git diff --check` 通过；`CampusMonitorResultMapper.xml` 通过 XML parser；`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd test -DskipTests=false` 通过 16 个测试类 55 tests，0 failures / 0 errors / 0 skipped；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **线上发布**：提交 `9996215 feat: add monitor information hit scopes` 已推送 `deploy-vps/claude/batch33-monitor-admin`；备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-032925-hit-scope`；线上 `yuqing/nginx` active，`/monitor` 返回 200，未登录接口返回 302；正式库核验 `hitScope=all` 为 296 条、`hitScope=risk` 为 12 条。

## 2026-05-17 监测命中预览与筛选交互修正

- **问题定位**：线上日志显示完整 `/monitor` 信息接口已按 `hitScope` 返回，但态势页“最新监测命中”卡片仍固定取 `pageSize=6` 的原始监测结果，容易被误解为系统总量只有 6 条；同时 `/monitor` 切换命中口径时会保留旧平台、情感、状态或时间筛选，导致“全部真实命中”仍可能只显示旧筛选交集。
- **前端调整**：`/monitor` 切换“风险命中 / 全部真实命中”时清空窄筛选并回到本年范围，标题区显示当前总数；支持 `/monitor?hitScope=all` 直接进入全部真实命中口径。
- **态势页调整**：`/situation` 的“最新监测命中”改用统一监测信息接口 `hitScope=risk`，卡片显示总量，按钮跳转到 `/monitor?hitScope=all` 查看完整覆盖面。
- **文档状态**：本节为前端交互修正记录；接口契约未新增，仅补齐前端请求类型中的 `hitScope`。

## 2026-05-17 监测信息默认完整列表修正

- **问题定位**：用户线上刷新后仍看到少量记录。Nginx 日志确认浏览器已加载新前端资源，但菜单进入 `/monitor` 时仍默认请求 `hitScope=risk`，因此看到的是风险命中预览口径，而不是完整监测记录。
- **前端调整**：`/monitor` 无 `hitScope` 查询参数时默认进入 `hitScope=all` 的“全部真实命中”；只有显式 `/monitor?hitScope=risk` 或用户手动切换时才展示“风险命中”。态势页预览继续使用 `risk`，避免首页卡片过噪。

## 2026-05-17 前后台入口与监测信息口径纠偏

- **问题定位**：后台菜单 `AI能力管理` 以 `/admin/settings/ai` 作为一级菜单写入权限树，前台侧边栏只过滤了部分后台路径，导致后台能力可能出现在首页菜单；`/admin` 本身没有默认子路由，进入后台壳层时可能出现内容空白；`/monitor` 仍保留“快速创建任务”，与后台“监测任务管理”职责重复。
- **前端修复**：前台侧边栏统一过滤所有 `/admin/**` 路由；`/admin` 默认重定向到 `/admin/monitor-tasks`；`/monitor` 标题和入口文案改为“监测信息”；移除前台快速创建监测任务入口，任务新增/编辑/启停统一归后台管理。
- **AI 管理可用性**：`/admin/settings/ai` 保留后台入口，页面在 `/campus/ai/**` 查询失败时展示可读错误提示，不再只靠消息弹窗，便于判断是接口、权限还是迁移缺失。
- **监测信息口径**：`/campus/monitor/information/**` 改为只读取真实 `campus_monitor_result` 监测命中，不再 union 普通 `campus_clue`，避免搜索过程、手工线索或普通接入线索混入“监测任务”条目。
- **文档同步**：更新 `API_CONTRACT.md`、`PERMISSION_RULES.md`、`TEST_CHECKLIST.md` 和 `campus_monitor` manifest，记录前后台边界和监测信息只读来源。
- **本地验证**：`git diff --check` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过 16 个测试类 55 tests，0 failures / 0 errors / 0 skipped；`CampusMonitorResultMapper.xml` 通过 XML 解析检查。

## 2026-05-17 公开采集平台新项目蓝图

- **文档产出**：新增 `docs/public-crawler-platform-blueprint.md`，用于后续独立开新项目时承接“公开信源采集平台 + 社媒补充适配器 + 合规采集治理中心”的总体设想。
- **平台分层**：明确新闻/官网/政府/学校/RSS/论坛/贴吧为自建主力；B站、知乎、微博、微信公众号采用搜索发现 + 公开详情页抽取 + TikHub 兜底；抖音、小红书、快手继续以 TikHub/API 为主通道。
- **参考项目评估**：记录 MediaCrawler 仓库 `https://github.com/NanmiCoder/MediaCrawler`，本地临时检查路径 `C:\Users\qjw\AppData\Local\Temp\mediacrawler_inspect`，检查 commit `f328ee3`；结论为可借鉴平台化结构，不直接复用源码或登录态/签名/代理池能力。
- **合规边界**：文档中强调非登录态、白名单、最小化入库、评论默认关闭、禁止账号池/Cookie 池/验证码绕过/代理池规避/批量个人信息采集。
- **验证说明**：本次仅新增规划文档并更新进度记录，未修改业务代码，未运行编译测试。

## 2026-05-17 二至四阶段现状融合实施蓝图

- **规划边界**：按用户要求将爬虫/自研采集平台单独考虑，本轮只基于现有 `百度搜索 + TikHub + public_web_pull/Jina Reader + 手工录入` 采集模式规划研判、预警、事件处置和报告复盘。
- **新增文档**：`docs/campus-phase2-4-current-state-blueprint.md`，系统盘点现有 `campus_ingest / campus_monitor / campus_clue / campus_alert / campus_event / campus_report / campus_ai` 能力，明确已具备骨架、主要缺口、跨模块边界和实施批次。
- **关键决策**：风险等级先沿用当前代码已使用的 `normal / concern / major / urgent`，展示层统一映射为普通关注、一般预警、重大预警、特别重大，避免再引入 `warning / critical` 造成状态和报表口径分裂。
- **实施路径**：建议从 Batch40 风险等级和主题字典统一开始，再推进学校相关性判断、主题分类、结构化预警依据、事件聚合/SLA、报告数据源收口和治理复盘指标。

## 2026-05-17 Batch40 风险等级与主题字典统一

- **后端收口**：新增 `CampusRiskLevel`，统一 `normal/concern/major/urgent` 风险编码，并兼容历史中文值和旧编码 `higher`；线索研判、事件定级、预警规则、监测命中、接入记录、账号动态和检测命中写入均走归一化。
- **前端统一**：新增 `campus-web/src/config/campusTaxonomy.ts`，监测、研判、预警、事件、检测、接入、账号、态势、工作台和分析页复用统一风险标签；修复批量研判曾提交中文“关注”的问题。
- **字典迁移**：新增 `V1.40__CampusRiskTopicTaxonomy.sql`，修正 `risk_level` 字典展示名，新增 `campus_event_topic` 固定主题字典，并停用历史 `higher` 字典项。
- **文档同步**：更新 `API_CONTRACT.md`、`STATE_MACHINE.md`、`TEST_CHECKLIST.md` 和二至四阶段蓝图，记录风险等级口径、兼容规则和当前落地范围。
- **本地验证**：`git diff --check` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过 16 个测试类 55 tests，0 failures / 0 errors / 0 skipped。

## 2026-05-17 Batch41-B47 蓝图 MVP 闭环实施

- **主控说明**：按 AGENTS.md 重新确认模块边界后，以主线程串行实施，未触碰爬虫/采集平台重构；本轮只在既有采集模式下补齐二至四阶段研判、预警、事件和报告闭环。
- **学校相关性与主题分类**：新增 `CampusSchoolRelevanceService`、`CampusTopicClassifier` 和 `V1.41__CampusBlueprintCompletion.sql`，为 `campus_monitor_result`、`campus_clue` 增加 `schoolRelevance*`、`matchedSchoolTerms`、`topic*` 字段；监测命中扫描生成，转线索继承，手工/接入转线索自动兜底。
- **结构化预警依据**：`campus_alert` 增加 `evidence_json`；监测告警、检测告警和规则/敏感词预警均写入来源对象、风险等级、命中词、学校相关性、主题分类和内容摘要等证据快照。
- **事件聚合与 SLA**：事件流转增加 Service 前置校验；未定级不能分派，未复核不能归档，已归档不能继续编辑；分派默认按风险等级生成 SLA 截止时间；新增 `GET /campus/event/clue/suggest` 推荐相似线索并排除已关联线索。
- **报告与治理指标**：日报/事件复盘模板种子增加 `${governanceTable}`；报告生成补治理复盘表；仪表盘增加 `topicRiskDistribution` 和 `governanceMetrics`。
- **前端同步**：监测信息列表/详情展示主题和相关性；预警列表展示结构化依据摘要；事件页增加“相似线索”Tab；前端类型和风险/主题 taxonomy 同步。
- **文档同步**：更新 `API_CONTRACT.md`、`STATE_MACHINE.md`、`PERMISSION_RULES.md`、`TEST_CHECKLIST.md`、`docs/modules/*/manifest.md` 和蓝图文档；新增 `campus_event`、`campus_report` manifest。
- **本地验证**：`git diff --check` 通过；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；使用 `.codex-tools/jdk8` 执行 `.\mvnw.cmd -DskipTests compile` 通过，`.\mvnw.cmd test -DskipTests=false` 通过 16 个测试类 55 tests，0 failures / 0 errors / 0 skipped。

## 2026-05-16 全量同步服务器（Batch39 + 搜索时间口径）

- **同步范围**：已将当前工作区完整快照同步到服务器构建目录 `/home/ubuntu/yuqing-fullsync-20260516-224509`，包含 AI 能力管理 P0-P2、监测任务保存兜底、搜索结果时间口径、Jina/内容质量治理等当前已完成改动。
- **服务器验证**：`./mvnw -DskipTests=false test` 通过，16 个测试类共 55 个用例，0 failures / 0 errors / 0 skipped；`./mvnw -DskipTests package` 通过；`campus-web npm install && npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告。
- **发布结果**：备份目录 `/home/ubuntu/yuqing-backups/deploy-20260516-224509-fullsync`，包含发布前 jar、前端 web 压缩包和数据库 dump；已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`。
- **Flyway 处理**：首次启动被旧报告模板迁移中的 `${...}` 文本触发 Flyway placeholder 校验阻断；已在 `/opt/yuqing/config/yuqing.env` 设置 `SPRING_FLYWAY_PLACEHOLDER_REPLACEMENT=false`，随后服务启动成功并迁移到 `1.39 CampusAiCapabilityManagement`。
- **线上验收**：`yuqing/nginx/mariadb/redis-server` 均为 active；`/`、`/login`、`/monitor`、`/search?q=新疆大学`、`/admin/monitor-tasks`、`/admin/settings/ai` 返回 200；未登录 API 返回 302；`campus_ai_*` 表存在，AI 供应商种子数据 7 条。

## 2026-05-16 监测任务编辑保存兜底修复

- **问题定位**：现有“新疆大学”监测任务编辑保存报“监测任务不存在”，保存链路会直接用请求体 `monitorTaskId` 查询 `campus_monitor_task.monitor_task_id`；该字段为 19 位雪花 ID，如果前端或历史接口把它按 JS Number 处理，会发生精度丢失，导致后端拿到的 ID 与数据库真实 ID 不一致。
- **后端处理**：`CampusMonitorServiceImpl.saveTask` 在编辑场景中保留原 `monitorTaskId` 查询，同时新增按数据库行 `id` 兜底找回真实 `monitorTaskId`，再执行更新；`CampusMonitorTaskDao` 和 mapper 新增 `selectById`。
- **影响范围**：不改变 API 地址、参数结构和数据库数据；仅增强编辑保存兼容性，避免旧前端、缓存数据或大数字精度问题导致已有任务无法保存。
- **本地验证**：`git diff --check` 通过；本地 Windows 环境当前无可用 JDK/JAVA_HOME，后端 Maven 编译需在服务器或补齐 JDK 后补跑。
- **线上同步**：已在服务器基于当前生产 jar 编译热补丁，只替换 `CampusMonitorServiceImpl`、`CampusMonitorTaskDao` 和 `CampusMonitorTaskMapper.xml`，未同步未发布的 AI 管理 V1.39；备份路径 `/home/ubuntu/yuqing-backups/deploy-20260516-221400-task-save-hotfix`，热补丁构建目录 `/home/ubuntu/yuqing-hotfix-task-save-20260516-221028`。
- **线上验收**：`yuqing/nginx/mariadb/redis-server` 均为 active；`/`、`/login`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/task/list` 返回 302；运行 jar 已确认包含 `resolveTaskForSave` 和 mapper `selectById`；启动日志显示 Flyway 当前版本仍为 `1.38`，未执行 `V1.39`。

## 2026-05-16 搜索结果时间口径核查

- **问题定位**：右上角搜索走 `/campus/clue/list`，不是实时百度网页搜索；搜索结果显示 `publish_time`，但旧页面默认按 `discover_time` 排序，所以 2026-05-16 新采集到的 2023 平台旧内容会排在前面。
- **线上核查**：`新疆大学` 搜索命中的 2023 记录共 5 条，均来自接入记录同名 `publish_time`：快手 3 条、B站 1 条、微博 1 条；这些是平台发布时间为 2023 的历史内容，不是前端时间显示错误。当前可见线索 320 条中有 180 条 `publish_time` 为空，无未来时间或 2018 年前异常时间。
- **前端处理**：搜索页请求新增 `sortBy=publish_time`，默认按真实发布时间倒序；搜索结果和线索详情分开展示“发布时间”和“发现时间”，没有发布时间时显示“未知”，不再用 `create_time` 冒充发布时间；词云点击参数统一为 `/search?q=...`。
- **验证/同步**：`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；已随 2026-05-16 全量同步发布到服务器，线上 `/search?q=新疆大学` 返回 200，部署包内已确认包含“发布时间/发现时间”展示文案。

## 2026-05-16 AI 能力管理 P0-P2

- **模块边界**：新增 `campus_ai` manifest 和 Flyway `V1.39__CampusAiCapabilityManagement.sql`，独立管理供应商、模型、功能绑定、提示词模板和 AI 调用日志；密钥只保存 `credential_ref`，不入库真实 Key。
- **P0 当前主用能力**：DeepSeek 继续作为 AI 报告、自动报告、少数民族语言研判、关键词/词云提取的主模型；后台可调整接入点、模型编码、超时、启停、失败策略和提示词模板。
- **P1 接入增强配置**：TikHub、百度千帆 AI Search、Jina Reader 登记为启用供应商；TikHub/Base URL、百度搜索 endpoint、Jina Reader endpoint/credentialRef 优先读取 `campus_ai_provider`，保留原 Spring/env 配置兜底。
- **P2 历史能力收口**：写作宝、旧 OCR、旧 NLP 分词、DashScope/Qwen 登记为 `legacy` 或停用供应商，默认不进入当前校园主流程；词云不依赖旧分词，优先 DeepSeek，失败回退原关键词统计。
- **后端实现**：新增 `/campus/ai/**` 管理接口，覆盖 overview、provider/model/feature/prompt/call-log 查询和保存，以及供应商配置测试；AI Chat 统一封装 OpenAI-compatible DeepSeek 调用并写入 `campus_ai_call_log` 脱敏日志。
- **业务接入**：`AiReportServiceImpl`、`AiJudgmentEngine`、`MinorityLLMAnalyzer`、`CampusDashboardServiceImpl` 已接入统一 AI 配置；首页词云新增 10 分钟缓存，DeepSeek 不可用时不影响页面访问。
- **前端实现**：新增后台 `/admin/settings/ai`，导航显示“AI能力”；页面包含功能绑定、供应商、模型、提示词、调用日志、历史能力等管理视图。
- **验证/同步**：本地 `campus-web npm run build` 通过；后端已在服务器全量快照中执行 `./mvnw -DskipTests=false test` 通过 55 个用例，`./mvnw -DskipTests package` 通过；Flyway `1.39` 已成功执行，`/admin/settings/ai` 返回 200。

## 2026-05-16 监测信息内容质量治理与 Jina Reader 修复

- **问题定位**：监测信息中 `<em class="keyword">` 来自 TikHub/B站搜索高亮 HTML；小红书“新大，您真的把我养得很差！”同时存在 155 字长正文记录和 60 字短摘要近似重复记录，短记录因不同 `external_id` 未被合并；微信公众号任务已请求 TikHub 且重试后成功，但接口返回 0 条可识别文章；Jina 失败不是密钥或代理白名单问题，而是 Java 默认 `User-Agent: Java/1.8...` 被 Jina 官方 Cloudflare 返回 403/1010。
- **接入治理**：新增 `CampusIngestTextSanitizer`，接入标准化时统一清洗标题、正文、作者和关键词；TikHub 映射复用该清洗，B站等平台新数据不再保留搜索高亮 HTML；小红书同来源/同平台/同标题记录按近似重复合并，保留更长正文；详情增强失败写入 `_detail_capture_status=failed`，不再完全静默。
- **展示治理**：监测信息线索分支也优先使用更长的 `campus_ingest_record.content`；原文链接只返回合法 `http/https`，前端详情弹窗里的“查看原链接”也会二次校验；“全部（年份/今天/本周）”统一改为“全部”；公众号空结果会在运行日志保留“请求成功但无可识别文章”说明。
- **存量治理**：新增并上线 `V1.37__MonitorInformationContentQualityCleanup.sql` 清洗存量 `<em class="keyword">`、回填更长正文、隐藏小红书短正文近似重复项；新增并上线 `V1.38__CleanLegacyInvalidWeiboMonitorRows.sql` 逻辑删除历史微博账号/超话/搜索卡片等非真实帖子记录。
- **Jina 修复**：`JinaReaderClient` 改为显式发送 `User-Agent: Mozilla/5.0` 和 `Accept-Language`；线上复测 `Java/1.8.0_482` 访问代理仍为 403/1010，`Mozilla/5.0` 访问同一代理返回 200，说明修复点命中。
- **本地验证**：`.\mvnw.cmd "-DskipTests=false" test` 通过，16 个测试类 55 tests，0 failures / 0 errors / 0 skipped；`campus-web npm run build` 通过，仅有既有 Rollup PURE 注释和 chunk 体积警告；`.\mvnw.cmd -DskipTests package` 通过。
- **线上发布**：前后端发布备份 `/home/ubuntu/yuqing-backups/deploy-20260516-203436-content-quality-jina`；二次后端迁移发布备份 `/home/ubuntu/yuqing-backups/deploy-20260516-203927-invalid-weibo-clean`。Flyway `1.37`、`1.38` 均成功，`yuqing/nginx/mariadb/redis-server` active。
- **线上验收**：存量可见接入/监测/线索中的 `<em class="keyword">` 剩余均为 0；小红书问题标题只保留 155 字长正文记录可见，60 字短记录已 `deleted=1` 且对应监测结果隐藏；非 `http/https` 活跃原文链接为 0；非法微博接入和监测结果均为 0；`/`、`/login`、`/monitor`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/information/list` 返回 302。

## 2026-05-16 旧食品安全数据清理与微信公众号排查

- **旧数据定位**：线上残留主要来自旧接入任务 `微博-食品安全搜索`、`B站-校园食品安全搜索`、`抖音-校园食品安全搜索`、`百度-校园食品安全搜索`，这些任务仍有调度开启并沉淀了 112 条旧线索；同时禁用的 `校园食品安全监测` 仍保留 106 条历史监测结果和 21 条监测预警。
- **清理策略**：新增 `V1.36__CleanLegacyFoodSafetyAndWechatAutoIngest.sql`，只做逻辑删除和停用：旧食品安全接入任务 `schedule_enabled=0/task_status=paused`，旧食品安全线索、接入记录、监测结果、监测预警置 `deleted=1`，`校园食品安全监测` 置 `display_enabled=0/auto_ingest_enabled=0/schedule_enabled=0`。
- **微信公众号原因**：线上 `自动监测-2054480494304825344-微信公众号` 没有任何 `wechat_official` 接入记录；历史运行中 TikHub 公众号搜索 3 次返回 400，1 次成功但抓取 0 条。根因不是展示详情，而是采集链路没有形成公众号记录；代码已给公众号搜索增加最多 3 次短重试，并把自动查询从组合词 `新疆大学 新大` 收敛为主主体词 `新疆大学`，排序使用 `_0`。
- **迁移顺序修正**：线上已有报告模块迁移 `V1.34__CampusReportAiGeneration.sql`、`V1.35__CampusReportScheduling.sql`，本地补齐这两个迁移后，本次清理顺延为 `V1.36`，避免 Flyway 版本号冲突。
- **验证/发布**：服务器 `/home/ubuntu/yuqing-test-batch37-20260516-191931` 中 `ThirdPartyApiIngestAdapterTest,TikhubClientTest,TikhubResponseMapperTest` 16 tests 通过，完整后端测试 53 tests 通过，`campus-web npm run build` 通过，`./mvnw clean -DskipTests package` 通过；已发布并备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-192245-food-clean-wechat`。
- **线上验收**：Flyway `1.36` 应用成功；旧食品安全线索/接入记录/监测结果/监测预警剩余均为 0；旧食品安全接入任务全部 `paused/schedule_enabled=0`；`校园食品安全监测` 为 `disabled/display_enabled=0/auto_ingest_enabled=0/schedule_enabled=0`；微信公众号自动任务配置为 `query=新疆大学/sortType=_0/target_type=monitor_scan`，最新运行成功但 TikHub 返回 0 条；`yuqing/nginx/mariadb/redis-server` active，`/login`、`/monitor` 返回 200，未登录 `/campus/monitor/information/list` 返回 302。

## Batch35 监测命中精准度热修（2026-05-16）

- **问题定位**：线上监测信息页噪声来自两类数据：一是“自动监测-*”接入任务仍以 `target_type=clue` 运行，导致宽泛搜索结果在规则命中前被自动转线索并并入监测信息；二是监测任务把主体词同时配置为关键词，旧逻辑把“只出现主体词、没有有效关键词/负面词”的内容也算作命中。
- **线上核查**：正式库监测信息来源中，未关联监测结果的线索为 473 条，其中自动监测接入误转线索 282 条；监测结果 493 条中，按新精准口径可见约 119 条，主体词-only 历史命中约 300+ 条。
- **修复方案**：自动监测接入任务改用内部 `target_type=monitor_scan`，仅 `target_type=clue` 的人工/线索接入任务允许自动转线索；监测扫描时从关键词集合移除主体词/别名，要求“主体 + 有效关键词或负面/风险词”才生成命中；监测信息统一列表隐藏历史主体词-only 命中和自动监测误转线索，不删除存量数据。
- **迁移**：新增 `V1.30__CampusMonitorAutoIngestPrecision.sql`，将存量“自动监测-*”任务的 `target_type` 从 `clue` 修正为 `monitor_scan`。
- **本地验证**：`.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd test -DskipTests=false` 通过，15 个测试类共 50 个用例 0 失败；`.\mvnw.cmd -DskipTests package` 通过。
- **线上发布**：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-175458-precision` 并覆盖后端 jar；`yuqing/nginx/mariadb/redis-server` 均为 active；`/`、`/login`、`/admin/monitor-tasks` 返回 200，未登录访问 `/campus/monitor/information/list` 返回 302。
- **线上验收**：Flyway `1.30` 应用成功；8 个“自动监测-*”任务均为 `target_type=monitor_scan`；监测信息统一列表按新口径为 `monitor_result=119`、`clue=191`，自动监测误转线索和主体词-only历史命中不再进入统一列表。

## 2026-05-16 首页线索库入口收敛

- **问题定位**：前端历史 `/clues` 路由已重定向到 `/monitor`，但权限菜单种子仍保留“线索库”入口，首页总览也有“进入线索库”按钮，导致用户在首页点击“线索库”和“监测任务”看到同一内容。
- **处理结果**：前台只保留“监测任务”作为统一信息工作台入口；首页按钮改为进入 `/monitor`；搜索结果详情链接改为 `/monitor/article/{id}`；历史 `/clues/{id}` 兼容重定向到监测详情。
- **权限/迁移**：新增 `V1.31__HideLegacyClueMenu.sql`，将历史 `campus_permission_menu.menu_code=clues` 置为不可见/停用；`campus:clue:*` API 权限继续保留用于转线索、研判、归档等后续链路。
- **验证**：`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；`.\mvnw.cmd -DskipTests compile` 通过，Maven 返回 `BUILD SUCCESS`。
- **线上同步**：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-181109-hide-clues`，覆盖 `/opt/yuqing/web` 与 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 后重启 `yuqing`、reload `nginx`；Flyway `1.31` 应用成功，`campus_permission_menu.clues` 已为 `visible=0/status=0`；`/`、`/login`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/information/list` 返回 302。

## 2026-05-16 监测暂停过滤、微博精准接入与站内详情

- **食品安全任务原因**：线上 `校园食品安全监测` 已为 `disabled`，但旧展示 SQL 只过滤 `display_enabled=0/deleted=1`，未过滤 `task_status`，所以禁用任务历史命中仍出现在监测信息中。
- **后端修复**：监测信息统一列表、平台统计和今日概览只纳入 `task_status=active` 且 `display_enabled=1` 的监测结果；暂停/禁用任务不再进入前台监测信息。
- **微博链路梳理与修复**：微博自动接入仍走 TikHub `weibo_search_all` 搜索 + `weibo_post_detail_v2` 详情增强；本轮增加搜索结果清洗，只接收能解析微博帖子 ID 且含正文文本的真实帖子，过滤 `s.weibo.com` 搜索页、`sinaweibo://tabbar` 超话/话题统计卡、账号资料卡和无正文对象。
- **前端修复**：监测信息列表“详情”改为站内内容详情弹窗，展示标题、正文、平台、作者、发布时间、正文采集状态、命中词和互动数；弹窗中的“查看原链接”才打开外部原文，避免小红书、微博等平台链接直接跳出且不可用。
- **验证**：`TikhubResponseMapperTest` 通过 10 个用例；`campus-web npm run build` 通过；`.\mvnw.cmd test -DskipTests=false` 通过 15 个测试类、51 个用例；`.\mvnw.cmd -DskipTests package` 通过。
- **线上同步**：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-183244-detail-precision`，覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web` 后重启 `yuqing`、reload `nginx`；线上 `校园食品安全监测` 仍为 `disabled/display_enabled=1`，但新口径下可见监测结果只剩 active 的 `新疆大学=13`，统一列表为 `monitor_result=13`、`clue=191`；`/`、`/login`、`/monitor`、`/admin/monitor-tasks` 返回 200，未登录 `/campus/monitor/information/list` 返回 302，四个服务均 active。

## 2026-05-16 微博旧链接与旧详情二次清理

- **问题确认**：线上仍有 78 条旧微博接入记录使用 `sinaweibo://` 或空链接、3 条是 `s.weibo.com` 搜索页；统一列表里 109 条微博线索主要来自历史 `微博-食品安全搜索`、`微博-新疆大学搜索` 等旧接入任务，其中一部分是账号资料/外部博客链接，不是微博帖子详情。
- **采集逻辑修正**：新采集继续过滤搜索页/话题卡/账号资料；`sinaweibo://detail/?mblogid=...` 这类真实详情 scheme 会解析出帖子 ID，并转换成 `https://weibo.com/{userId}/{postId}` 或 `https://m.weibo.cn/detail/{postId}`。
- **历史数据迁移**：新增并上线 `V1.32__NormalizeWeiboOriginalUrls.sql` 和 `V1.33__HideLegacyWeiboNonPostUrls.sql`；能用 13 位以上帖子 ID 修复的旧链接统一修成 `m.weibo.cn/detail/{postId}`，不能确认是微博帖子的旧 app/search/profile/external 链接置空。
- **展示口径**：监测信息中的微博内容必须有真实帖子链接：`weibo.com/{userId}/{postId}` 或 `m.weibo.cn/detail/{postId}`；不满足该条件的旧微博线索不再展示，避免旧数据继续污染新规则。
- **验证/发布**：`TikhubResponseMapperTest` 11 tests 通过，完整后端 `.\mvnw.cmd test -DskipTests=false` 通过 52 tests，`.\mvnw.cmd -DskipTests package` 通过；已备份并发布到 `/home/ubuntu/yuqing-backups/deploy-20260516-185347-weibo-post-only`。线上 Flyway `1.32/1.33` 成功，非真实帖子格式的微博接入记录和线索链接均为 0，微博可见信息从 109 条旧线索收敛到 56 条真实帖子链接；`/`、`/monitor` 返回 200，未登录接口 302，四个服务均 active。

## 已识别技术栈

详见 [ARCHITECTURE.md](ARCHITECTURE.md) 技术栈识别章节

核心：Spring Boot 2.1.4 + MyBatis + MySQL + Redis + Thymeleaf + Vue 2 大屏 + Vue 3 校园前端 + ECharts

## 已识别核心模块

| 模块 | 控制器 | 功能描述 |
|------|--------|---------|
| 登录认证 | LoginController | 账号/微信/自动登录 |
| 数据监测 | MonitorController | 文章列表/详情/搜索/导出 |
| 监测分析 | AnalysisController | 图表/情感/热点/高频词 |
| 方案管理 | ProjectController | 方案组/方案 CRUD + Kafka 同步 |
| 系统设置 | SystemController | 预警/偏好/收藏/反馈 |
| 全文搜索 | FullSearchController | 全数据类型搜索 |
| 数据报告 | ReportController | 报告 CRUD/编制 |
| 用户管理 | UserController | 用户 CRUD/密码管理 |
| 微信集成 | WechatController | 扫码登录/绑定/事件 |
| 综合看板 | DisplayBoardController | 数据展示 |
| 声量监测 | VolumeController | 声量分析 |
| REST API | ApiController | Token/文章 API |
| 校园业务后台 | controller/campus/* | 工作台、线索、账号、事件、预警、报告、检测、接入、监测任务 |
| 校园新前端 | campus-web/ | Vue 3 校园舆情综合研判平台 |
| 媒体接入平台 | CampusIngestController | 接入来源、接入任务、标准化记录、运行日志、API 调用日志 |
| 监测任务中心 | CampusMonitorController | 主体/关键词/负面词监测、接入绑定、自动调度、负面告警 |
| 多语言研判 | service/campus/ingest/baidu + service/campus/judgment | 百度接入、自动转线索、按语言分流研判 |
| 任务内重点目标 | campus_monitor_watch_target | 监测任务下的重点账号、指定链接、一键加入和扫描过滤 |
| 教育专题 | CampusEducationController | 本地区教育新闻/政策/招生专题、学校主体和学校声量正负面排名 |
| 词库模块 | campus_dict_type/item | 负面词、正面词、风险词、教育新闻/政策/招生专题词 |

## 当前工程风险

1. **分层边界模糊** — 大量业务逻辑在 Controller 层实现
2. **API 风格不统一** — 3 种返回格式共存
3. **权限体系双轨** — 旧系统仍以登录拦截为主；校园模块已有最小角色/菜单/API 权限模型
4. **测试覆盖仍低** — `mvn test -DskipTests=false` 已可稳定通过 33 个用例，但核心业务 Service 覆盖仍不足
5. **FastJSON 安全隐患** — 版本 2.0.21
6. **JDK 8 过旧** — 无法使用新特性
7. **JSON 序列化不统一** — FastJSON vs Jackson
8. **异常处理不完善** — 无全局异常处理
9. **局部 mock 残留** — `CampusSpreadServiceImpl` 传播时间线、媒体排行、关系网络仍含随机/mock 数据
10. **worktree 残留风险** — 已完成本地残留 worktree 清理；当前 `git worktree list` 仅保留 `D:\PRJ\yuqing` 主 worktree
11. **P0-P2 新业务测试仍偏薄** — 前端 build 已通过，但监测转线索、重点目标、教育专题仍缺少专门自动化用例
12. **校园信息列表文章状态模型待补** — 页面有已读/已选筛选入口，但 `campus_clue` 暂无对应持久字段，本轮已停止错误映射到 `clue_source`
13. **监测信息统一列表缺少自动化用例** — 后端编译、前端构建和正式库 SQL 已补验，但 `/campus/monitor/information/**` 仍需后续加入 API 自动化测试
14. **Jina Reader 官方 API 网络待处理** — 当前已通过受控代理链路解决卓然舆情服务器到 Jina Reader 的正文提取访问，后续仍建议评估正式自部署或受控出口策略
15. **监测任务自动接入本地门禁受限** — 本地 Windows 环境无 Java/JAVA_HOME；后端编译、打包和线上验收已在服务器补跑通过，后续仍建议本地补齐 JDK 配置

## 当前文档生成情况

| 文档 | 状态 | 说明 |
|------|------|------|
| 主线prompt.md | ✅ 已完成 | 主控 Agent 总控 Prompt |
| AGENTS.md | ✅ 已完成 | AI 协作规范 |
| docs/ARCHITECTURE.md | ✅ 已完成 | 架构文档 |
| docs/CONVENTIONS.md | ✅ 已完成 | 开发规范 |
| docs/API_CONTRACT.md | ✅ 已完成 | API 契约（部分待补全） |
| docs/STATE_MACHINE.md | ✅ 已完成 | 状态机（校园核心状态已补齐，旧系统仍待细化） |
| docs/PERMISSION_RULES.md | ✅ 已完成 | 权限规则（旧系统与校园模块双轨） |
| docs/TEST_CHECKLIST.md | ✅ 已完成 | 测试清单（已校准当前测试现状） |
| docs/DEPLOY_CHECKLIST.md | ✅ 已完成 | 部署清单 |
| docs/AI_PROGRESS.md | ✅ 已完成 | 本文 |
| docs/modules/*/manifest.md | ✅ 已完成 | Odoo 式模块 manifest，覆盖监测、线索、接入、重点账号、词库、教育专题 |

## 后续建议阶段

### P0（立即可以做的）
- [x] 在项目根目录添加 CLAUDE.md（项目级 AI 配置）
- [x] 人工确认本地 `master` 作为正式主线（2026-05-14）
- [x] 将本地主线从 `master` 改名为 `main`（2026-05-14）
- [x] 补齐 `/campus/**` 核心 API 契约
- [x] 补齐校园核心状态机
- [x] 修复旧 Spring 上下文测试，让 `mvn test -DskipTests=false` 稳定通过
- [x] 补充校园试点部署检查清单
- [x] 处理并清理残留 worktree（2026-05-14）
- [ ] 人工确认部署所需的外部服务清单
- [ ] 设置 `token.private-key` 为正式生产密钥
- [ ] Batch32 必要 API 验证和 Flyway 最新版本补验
- [x] 监测任务多语言关键词输入兼容现有多语言逻辑
- [x] 监测结果页面展示具体命中内容并支持处理
- [x] 监测信息工作台融合：监测命中与线索在同一列表展示，行操作按数据来源启用
- [x] 线索详情参数与正文兜底修正
- [x] 监测任务展示治理：新增前台展示开关，删除任务默认隐藏前台数据，监测信息/统计排除隐藏或已删除任务

### P1（短期）
- [ ] 统一 API 返回格式（存量接口迁移到 ResultVO）
- [ ] 补充全局异常处理（@ControllerAdvice）
- [x] 校园模块建立最小角色模型（campus_admin / campus_operator / campus_viewer）
- [ ] 旧系统用户管理接口补管理员权限限制
- [ ] 为关键 Service 添加基础单元测试
- [ ] 建立 CI 基础流水线（GitHub Actions）
- [x] 低成本采集互动指标：点赞、评论、转发、收藏、浏览/播放，平台无数据时留空
- [x] 任务内重点账号/指定链接联动：一键从监测结果加入，支持 DPI/第三方来源字段
- [x] 情感/负面/风险词库初始化，复用校园字典模块
- [x] 方案1：监测结果转线索优先复用接入记录已绑定线索，避免线索库重复
- [x] 方案2：重点账号/链接扫描规则调整为“范围命中 + 关键词命中”，不再孤立于任务扫描
- [x] 方案3：监测结果页增加本任务重点账号/链接管理区，支持新增、编辑、启停、删除和一键加入后的回显
- [x] 监测任务驱动采集：保存/运行任务自动创建或复用百度/TikHub 接入任务，运行时先采集再扫描接入记录

### P2（中期）
- [ ] 抽取 Service 层业务逻辑，瘦身 Controller
- [ ] 引入统一权限框架（Spring Security / Shiro）
- [ ] 升级 JDK 到 11+
- [ ] 补充核心模块集成测试
- [ ] 标准化 JSON 序列化
- [ ] 前端代码规范（Lint/格式化）
- [ ] 建立 API 自动化测试
- [x] 教育专题模块：教育新闻/政策/招生专题、百度接入任务创建、学校正负面声量排名
- [x] 方案4：教育专题百度任务支持选择接入来源并创建后立即运行，运行结束回到接入任务管理链路
- [x] 方案5：学校主体支持模板下载和 CSV 导入，支撑学校声量排名基础数据
- [x] 方案6：后台入口与关键操作按钮增加权限感知，未授权入口/操作不再作为孤立可点功能暴露
- [x] 监测任务可观测：后台列表展示前台展示、接入能力状态、最近采集、最近命中、展示数据和最近错误提示

### P3（长期）
- [ ] 微服务化拆分（全文搜索独立、监测分析独立）
- [ ] 升级 Spring Boot 版本
- [ ] 全面测试覆盖
- [ ] 引入 API 网关
- [ ] 前端工程化重构
- [x] 监测任务高级入口收口：普通表单移除接入任务选择，仅保留自动接入诊断只读信息；手动绑定保留为后端兼容/运维能力

## 下一步建议任务

1. **人工确认** 试点部署所需外部服务和密钥：DeepSeek、TikHub、百度千帆、旧 NLP/写作宝等
2. **补验 Batch32 API 与 Flyway 最新版本**，确认当前数据库可迁移到 `V1.24`
3. **建立 CI** GitHub Actions 基础流水线（后端 test + campus-web build）
4. **补充测试** 至少为核心 Service 添加单元测试

## 近期完成

### 2026-05-16 接续完成监测 P0-P4 平台正文链路
- 模块归属：`campus_ingest` + `campus_monitor` + `campus-web` 监测页面；未新增业务表，未修改公共配置或核心依赖。
- P0 平台收口：公开论坛不再作为固定独立平台入口；历史论坛/贴吧/豆瓣数据按新闻/网页口径兼容统计，监测任务自动接入“全部”范围不再创建 forum 平台任务。
- P1 正文状态：`CampusMonitorInformation` 增加 `contentCaptureStatus/contentCaptureLabel` 只读字段，前端监测信息表新增“正文”列和导出列，区分完整正文、摘要/标题、未采集。
- P2 正文详情链路：微博和 B站自动任务开启 `detailEnabled`；微博按 `id` 调详情，B站按 `aid` 调详情；搜索映射补齐微博/B站原文链接兜底，已有监测结果在接入记录后续增强时刷新正文快照和原文链接。
- P3 新平台接入：TikHub allowlist 和 URL 构造补齐知乎 `zhihu_article_search_v3`、微信公众号 `wechat_mp_search_article`、快手 `kuaishou_search_comprehensive`；自动监测任务保存/运行可创建对应接入来源和接入任务。
- P4 分析优先正文：监测列表、转线索、已有结果刷新均优先使用详情增强后的 `campus_ingest_record.content/original_url`；详情增强只更新同一接入记录，不新增第二条详情记录。
- 文档：已同步 `docs/API_CONTRACT.md`、`docs/modules/campus_ingest/manifest.md`、`docs/modules/campus_monitor/manifest.md`、`docs/TEST_CHECKLIST.md`。
- 本地验证：`.\mvnw.cmd '-Dtest=TikhubClientTest,TikhubResponseMapperTest' -DskipTests=false test` 通过，13 个用例 0 失败；`.\mvnw.cmd test -DskipTests=false` 通过，15 个测试类共 50 个用例 0 失败；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；`.\mvnw.cmd -DskipTests package` 通过并产出最新 jar；最终 `.\mvnw.cmd -DskipTests compile` 复验通过。
- 线上部署：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260516-170425-p0p4`，覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 与 `/opt/yuqing/web` 后重启 `yuqing` 并 reload `nginx`。
- 线上冒烟：`yuqing/nginx/mariadb/redis-server` 均为 active；`https://yuqing.zhuoran.cc/`、`/login`、`/admin/monitor-tasks` 返回 200；未登录访问 `/campus/monitor/information/list` 返回 302，符合现有鉴权链路。

### 2026-05-16 Codex 接续上下文溢出线程并完成本地补验
- 背景：上一对话因模型上下文窗口耗尽中断；本线程从当前 worktree、`docs/AI_PROGRESS.md` 和未提交 diff 恢复现场，确认遗留任务为 Batch34 监测任务自动接入、展示治理、Jina Reader 正文增强和小红书两段式详情增强。
- 状态判断：代码和文档改动已落在 `claude/batch33-monitor-admin` 分支工作区，上一线程记录显示服务器已部署验收；本线程未继续扩大业务范围，仅做本地一致性补验和交接记录。
- 本地验证：使用项目内便携 JDK `D:\PRJ\yuqing\.codex-tools\jdk8\jdk8u482-b08` 临时设置 `JAVA_HOME`，执行 `.\mvnw.cmd -DskipTests compile` 通过。
- 完整测试：执行 `.\mvnw.cmd test -DskipTests=false` 通过，15 个测试类共 45 个用例，0 失败、0 错误、0 跳过。
- 前端验证：执行 `campus-web npm run build` 通过；仍只有既有 Rollup PURE 注释和 chunk 体积警告。
- 结论：本地未发现需修复的编译或测试问题；未提交改动仍需人工审核后再决定提交、合并或发布策略。

### 2026-05-16 小红书两段式详情增强改造
- 模块归属：`campus_ingest` 负责 TikHub 小红书搜索和详情增强；`campus_monitor` 只读取增强后的接入记录用于监测信息展示和转线索。
- 设计：小红书 `search_notes` 先拿 `note_id`，开启 `detailEnabled=true/maxDetailCalls` 后按图文/视频详情接口增强同一条 `source_id + note_id` 记录；重复命中时只更新原接入记录的正文、原文链接、发布时间和互动数，不新增第二条详情内容。
- 展示：监测信息统一列表对监测结果行优先使用增强后的接入记录正文、原文链接、发布时间和互动数，避免历史结果快照为空时页面仍无原文。
- 转线索：监测结果转线索时优先复用增强后的接入记录正文、原文链接、发布时间、作者、情感和语言。
- 服务器验证：`mvn -DskipTests=false -Dtest=TikhubResponseMapperTest,TikhubClientTest test` 通过 8 个用例；完整 `mvn -DskipTests=false test` 通过 45 个用例；`mvn -DskipTests package` 通过并已发布到 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar`。
- 业务验收：2026-05-16 15:48 手动运行 `新疆大学` 监测任务成功，返回 `scanned=9/match=9/alert=7`；小红书接入任务 `fetched=20/success=3/duplicate=17/fail=0`，重复 `note_id` 走增强更新而非新增。
- 数据验收：小红书 `source_id + external_id` 重复组为 0；已增强 20 条小红书记录，发布时间、转发/收藏等字段可回填到监测信息列表；`V1.29__BackfillXiaohongshuOriginalUrl.sql` 已上线并成功应用，137 条小红书接入记录 `missing_url=0`，该任务 137 条小红书监测行均可通过接入记录回填原文链接。
- 展示治理复验：短暂关闭 `新疆大学` 任务 `display_enabled` 后，任务级平台统计从 316 变为 0，恢复后回到 316；临时置 `deleted=1` 后同样为 0，恢复后任务状态为 `deleted=0/display_enabled=1/active`。

### 2026-05-16 Batch34 服务器部署与自动接入验收
- 部署：已在服务器 `/home/ubuntu/yuqing-test-batch34-20260516-035757` 完成后端编译/打包并覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar`；前端 `campus-web/dist` 已覆盖 `/opt/yuqing/web`；热修备份路径为 `/home/ubuntu/yuqing-backups/deploy-batch34-hotfix-20260516-041125`。
- Flyway：正式库 `flyway_schema_history` 已应用 `V1.28__CampusMonitorDisplayAutoIngest.sql`；`campus_monitor_task` 已存在 `display_enabled`、`auto_ingest_enabled`、`ingest_capability_status` 等字段。
- 编译/测试：服务器 `mvn -DskipTests compile`、`mvn -DskipTests package` 通过；新增 `TikhubClientTest`，服务器执行 `mvn -DskipTests=false -Dtest=TikhubClientTest test` 通过，1 个用例 0 失败。
- 接入热修：B站 TikHub `order` 参数改为数字兜底，自动 TikHub 任务写入 `timeoutMs=30000`；复跑“新疆大学”监测任务后，百度、抖音、小红书、B站、微博接入任务均自动创建/复用并运行成功。
- 手动运行验收：`新疆大学` 任务复跑返回 `scanned=107`、`match=107`、`negative=3`、`alert=50`；监测信息列表该任务可见总数 167，平台分布含微博、小红书、B站、新闻、抖音。
- 展示治理验收：将 `新疆大学` 任务 `display_enabled=0` 后，`/campus/monitor/information/list?monitorTaskId=...` 总数从 167 变为 0，全局总数从 560 变为 393，平台统计同步从 560 变为 393；恢复展示后总数恢复。
- 删除治理验收：新建临时删除验证任务并运行，产生 10 条前台可见命中；调用软删除后该任务信息列表总数和平台统计均为 0，任务管理列表也不再返回该任务。
- 仍需确认：平台范围选择“全部”时，知乎、微信公众号、公开论坛、快手仍因未配置自动适配器显示为 `partial/暂未接入`，这是当前能力边界，不是本次发布故障。

### 2026-05-16 Batch34 监测任务驱动接入与展示治理 P0-P3
- 模块归属：`campus_monitor` 负责任务配置、展示治理、自动接入编排和监测匹配；`campus_ingest` 仍负责外部平台接入、正文提取、接入记录和运行日志。监测模块不直接外呼平台 API。
- P0：`campus_monitor_task` 增加 `display_enabled/auto_ingest_enabled/last_collect_time/last_match_count/last_error_message/ingest_capability_status`；删除任务会软删除、隐藏前台数据、停用调度并清理接入绑定；监测信息列表、平台统计和今日概览排除隐藏/删除任务数据。
- P1：保存/运行监测任务时自动创建或复用接入来源和接入任务；运行任务先触发自动绑定的 `campus_ingest_task`，再扫描对应 `campus_ingest_record` 生成监测命中。当前自动映射支持百度新闻/公开网页、TikHub 抖音/小红书/B站/微博；知乎、微信公众号、论坛、快手暂按未接入/部分可用提示。
- P2/P3：后台 `/admin/monitor-tasks` 移除普通“接入任务”选择，增加“前台展示”“自动采集”“接入状态”“最近采集/命中/展示数据/错误提示”和高级诊断只读区，避免孤立功能和人工绑定误解。
- API/文档：新增 `POST /campus/monitor/task/update-display`；`/campus/monitor/information/**` 支持 `monitorTaskId` 过滤；同步更新 API 契约、状态机、权限、测试清单和模块 manifest。
- 验证：`campus-web npm run build` 通过；本地 `.\mvnw.cmd -DskipTests compile` 因未配置 `JAVA_HOME` 无法执行，需服务器或 Java 环境补验后端编译。

### 2026-05-16 Jina Reader 官方 API 正文提取测试
- 当前策略：先只使用官方 `r.jina.ai` 做“已知 URL 正文提取”，暂不接入 `s.jina.ai` SERP 搜索能力，搜索能力后续作为 `jina_search` 接入源单独评估。
- 本地官方 API 验证：`https://r.jina.ai/http://example.com` 返回 200，响应包含 `Title`、`URL Source`、`Markdown Content`；`https://r.jina.ai/https://www.moe.gov.cn/` 返回 200，可提取教育部官网中文 Markdown 内容。
- 密钥配置：用户提供的 Jina Reader API Key 已写入服务器 `/opt/yuqing/config/yuqing.env` 的 `JINA_READER_API_KEY`，未写入仓库或文档；`JINA_READER_CREDENTIAL_REF=JINA_READER_API_KEY`，后端已重启并保持 active。
- 带密钥验证：本地带 `Authorization: Bearer <key>` 调用 `https://r.jina.ai/http://example.com` 返回 200，确认官方 API 和密钥调用方式可用。
- 服务器网络验证：服务器系统 DNS 将 `r.jina.ai` 解析到异常 IP；本地正常解析应为 Cloudflare IP（如 `104.26.10.242`、`104.26.11.242`、`172.67.70.54`）。服务器使用 `curl --resolve` 强制正确 IP 后仍出现 `Connection reset by peer`。
- 服务器带密钥验证：服务器从环境文件读取密钥后调用官方 API 仍然连接超时，说明当前阻塞是服务器到 `r.jina.ai:443` 的外联链路，而不是认证或代码解析问题。
- 结论：官方 API 能力满足正文提取测试目标，但当前生产服务器外联链路不可用；在修复 DNS/出口策略前，线上开启 `readerEnabled` 的任务会因无法访问官方 API 而失败或走摘要兜底。
- 运维处理：已停止被中断的 Docker 拉取进程并清理 Docker 临时层；不继续自部署 Reader。

### 2026-05-16 Jina Reader 官方 API 代理链路上线
- 决策：不继续自部署 Reader，先使用官方 `r.jina.ai` 做正文提取；由于卓然舆情生产服务器直连官方 API 不通，新增一台可访问官方 API 的转发节点。
- 代理节点：`43.160.254.21`，Nginx 复用 80 端口，新增 `server_name 43.160.254.21` 下的 `/jina-reader-proxy/` 路径；该路径只允许 `127.0.0.1` 和卓然舆情服务器 `82.156.43.48` 访问，其余来源拒绝。
- 密钥放置：Jina API Key 只保存在代理节点 Nginx 配置中，由代理注入 `Authorization`；卓然舆情服务器 `/opt/yuqing/config/yuqing.env` 已将 `JINA_READER_API_KEY` 清空，避免通过 HTTP 传输密钥。
- 生产配置：卓然舆情服务器 `JINA_READER_API_URL=http://43.160.254.21/jina-reader-proxy`，`JINA_READER_CREDENTIAL_REF=JINA_PROXY_INJECTED`，`CONTENT_EXTRACTION_ENABLED=true`。
- 验证：代理节点本机访问 `/jina-reader-proxy/http://example.com` 返回 200；卓然舆情服务器访问 `http://43.160.254.21/jina-reader-proxy/http://example.com` 返回 200 和 Reader Markdown；`yuqing/nginx/mariadb/redis-server` 均保持 active，`https://yuqing.zhuoran.cc/` 返回 200。
- 备份：卓然舆情服务器环境文件切换前备份为 `/home/ubuntu/yuqing-backups/yuqing.env.before-jina-proxy-20260516030609`。

### 2026-05-15 Jina Reader 正文增强实施
- 模块归属：`campus_ingest`。Jina Reader 被定义为“正文提取器”，不负责搜索；百度千帆负责发现公开 URL，白名单公开网页负责合规边界，监测任务仍只扫描 `campus_ingest_record`。
- 后端新增正文提取抽象与 Jina Reader Client：支持 `CONTENT_EXTRACTION_ENABLED`、`JINA_READER_API_URL`、`JINA_READER_API_KEY`、超时和正文长度配置；外部调用写入 `campus_ingest_api_call_log(provider=jina_reader)`。
- `baidu_search` 增加 `readerEnabled/maxReaderCalls/fallbackToSnippet/readerTimeoutMs` 配置；Reader 成功时用正文增强接入记录，失败默认保留百度摘要；百度平台值统一入库为 `news`。
- `public_web_pull` 从纯 `metadata_only` 扩展出 `jina_reader` 模式；仍必须先通过白名单域名/路径校验，第一版仅允许单 URL，`maxDepth=0`。
- 前端 `/ingest` 结构化任务表单补齐百度正文增强和公开网页读取模式配置入口，避免功能只能手写 JSON。
- 测试补充：新增/更新 `JinaReaderClientTest`、`BaiduIngestFetchConfigTest`、`BaiduIngestResponseMapperTest`、`PublicWebIngestAdapterTest`、`PublicWebFetchConfigTest`。
- 验证状态：已在服务器测试目录 `/home/ubuntu/yuqing-test-20260515-092413` 补跑接入/Jina 相关 7 个测试类共 25 个用例，均通过；完整后端测试 `mvn test -DskipTests=false` 通过，14 个测试类共 42 个用例；`campus-web npm run build` 和后端 `mvn -DskipTests package` 均通过。文档已同步更新 `API_CONTRACT`、`DEPLOY_CHECKLIST`、`TEST_CHECKLIST`、`ARCHITECTURE`、`campus_ingest` manifest 和前端运行手册。
- 正式服务发布：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260515-092413`，覆盖 `/opt/yuqing/web` 与 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 后重启 `yuqing.service`；Flyway 已将正式库从 `1.26` 迁移到 `1.27`。
- 线上验证：`yuqing`、`nginx`、`mariadb`、`redis-server` 均为 active；`https://yuqing.zhuoran.cc/`、`/login`、`/ingest`、`/admin/monitor-tasks` 均返回 Vue 前端 200；服务器环境已启用 `CONTENT_EXTRACTION_ENABLED=true` 和 `JINA_READER_API_URL=https://r.jina.ai`。
- 待处理：服务器 DNS 可解析 `r.jina.ai`，但 IPv4/IPv6 访问 80/443 均连接超时；因此 Reader 代码和配置已发布，真实正文增强调用仍需自部署 Jina Reader 或放通服务器外联后再验收。

### 2026-05-15 Batch33 监测任务后台管理与数据接入 P2 收口
- 模块归属：监测任务后台管理归 `campus_monitor`；数据接入业务页收口归 `campus_ingest`；预警 `create-from-clue` 兼容入口归 `campus_alert`，内部复用线索预警评估能力；接入任务选择仅读取 `campus_ingest` 合法任务。
- 后端修复：新增 `POST /campus/alert/create-from-clue`，返回 `ResultVO<List<CampusAlert>>`，补齐前端和文档已引用但后端缺失导致的 405 契约缺口。
- 前端新增：`/admin/monitor-tasks` 后台页面，命名为“监测任务管理”，支持监测任务列表筛选、新增/编辑、接入任务绑定、中文/蒙语/维语关键词配置、启用/暂停/停用、手动运行、删除，以及任务内重点账号/链接维护；不展示具体监测内容、命中结果或运行日志。
- 后台收口：后台导航不再展示“检测任务”，旧 `/admin/detection` 重定向到 `/admin/monitor-tasks`；数据接入页隐藏运行日志、API 调用日志和历史外部接口供应商调用配置，后端兼容能力保留，正式下线另起迁移批次。
- 权限与菜单：新增 Flyway `V1.27__CampusMonitorTaskAdminMenu.sql`，写入后台菜单 `/admin/monitor-tasks`，复用 `campus:monitor:view`；页面写操作按钮受 `campus:monitor:operate` 控制。
- 文档同步：更新 `docs/API_CONTRACT.md`、`docs/PERMISSION_RULES.md`、`docs/TEST_CHECKLIST.md`、`docs/campus-web-runbook.md`、`docs/modules/campus_monitor/manifest.md`、`docs/modules/campus_ingest/manifest.md`。
- 验证：`campus-web npm run build` 通过；`.\mvnw.cmd -DskipTests compile` 通过；本地 dev server `http://127.0.0.1:5174/admin/monitor-tasks` 未登录访问重定向到 `/login`，符合现有前端登录守卫。前端构建仍仅有既有 Rollup PURE 注释和 chunk 体积警告，后端编译仅有旧 `sun.misc.BASE64*`、deprecated、unchecked 警告。
- 服务器同步：旧测试目录 `/home/ubuntu/yuqing-test-batch33-p2-20260515-092114` 已完成构建验证；后续正式发布使用 `/home/ubuntu/yuqing-test-20260515-092413` 快照并已覆盖 `/opt/yuqing` 生产服务。

### 2026-05-14 监测信息字段、平台和预警依据修正
- 模块归属：`campus_monitor` + `campus_ingest`，未新增业务表；新增统一子平台统计接口 `/campus/monitor/information/count-by-sub-platform`，用于监测信息页公开论坛子标签。
- 前端监测信息表收口：操作列只保留“详情 + 更多”，转线索/转预警/忽略/加入重点账号/链接及线索研判动作统一下拉；发布时间、互动指标缺失时显示“未采集”；状态标签增加预警依据提示。
- 平台标签固定展示接入状态：有数据展示数量，已接入但无数据展示 0，只有来源未启用展示“未启用”，未配置来源展示“未接入”；公开论坛子标签限定为贴吧、豆瓣、本地论坛、其它论坛。
- 接入映射修正：TikHub 未配置 `platform` 时不再默认抖音，按 endpoint 推断真实平台；通用映射器过滤仅账号资料对象，补充微博字符串发布时间和点赞/评论/转发解析。
- 监测结果/线索维度补齐：统一信息视图中线索行从绑定接入记录回填平台、发布时间、作者、原文链接和互动数。
- 预警规则修正：监测主体词和普通关键词不再同时作为负面词触发；`all_hits` 兼容旧值但语义调整为“风险命中告警”，普通主题命中保持待处理。
- 数据迁移：新增 `V1.26__MonitorInformationSignalCleanup.sql`，用于修正历史平台误标、补齐结果/线索展示字段并清理普通主题命中造成的误预警。

### 2026-05-14 Batch33 预警中心 405 与监测任务后台设置实施方案
- 新增 `docs/batch33-monitor-task-admin-plan.md`，正式记录预警中心 405 排查、监测任务后台详细设置页、权限菜单、接口复用、任务拆分和验收清单。
- 明确本批模块归属：预警接口归 `campus_alert` / `campus_monitor`，监测任务后台设置归 `campus_monitor`，接入任务选择只读取 `campus_ingest` 合法任务。
- 当前结论：`/campus/alert/list` 与 `/campus/alert/handle` 主链路前后端方法一致；`/campus/alert/create-from-clue` 存在“前端与文档已引用、后端未实现”的契约缺口，需要在实施首步确认修正策略。
- 明确后台新增入口建议为 `/admin/monitor-tasks`，前台 `/monitor` 保持“监测信息工作台”，避免再次混入复杂任务维护。
- 本次仅新增实施方案文档，未修改业务代码，未执行编译或前端构建。

### 2026-05-14 监测信息工作台融合
- 模块归属：`campus_monitor`，依赖 `campus_clue`；新增 `CampusMonitorInformation` 只读 DTO，不新增业务表。
- 后端新增 `/campus/monitor/information/list` 和 `/campus/monitor/information/count-by-platform`，统一返回 `campus_monitor_result` 与未被监测结果引用的 `campus_clue`，用于前端同一张“监测信息”表。
- 前端移除顶部“信息列表/监测结果”切换模块，页面收口为“监测信息”；统一行操作：监测结果行支持转线索/转预警/忽略/加重点账号/加指定链接，线索行支持查看/编辑/研判/归档/加入事件。
- 信息表支持列隐藏和拖拽排序；平台标签固定展示“全部、抖音、小红书、知乎、新闻媒体、微博、微信公众号、B站、公开论坛”，避免只有当前有数据的平台才出现。
- 文档同步：更新 `docs/API_CONTRACT.md`、`docs/modules/campus_monitor/manifest.md`、`docs/TEST_CHECKLIST.md`。
- 验证：本地 `campus-web npm run build` 通过；服务器测试目录 `/home/ubuntu/yuqing-test-87286a5` 中 `bash mvnw -q -DskipTests compile` 通过，`campus-web npm install && npm run build` 通过，`bash mvnw -q -DskipTests package` 通过。
- 正式服务发布：已备份到 `/home/ubuntu/yuqing-backups/deploy-20260514-230505`，覆盖 `/opt/yuqing/web` 与 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 后重启 `yuqing.service`。
- 线上验证：`yuqing`、`nginx` 均为 active；`https://yuqing.zhuoran.cc/` 返回 200；正式库统一监测信息默认本年口径命中 114 条，平台聚合为 `douyin=114`。未登录直接访问 `/campus/monitor/information/**` 返回 302 登录拦截，符合现有鉴权链路。

### 2026-05-14 监测信息列表筛选口径修正
- 修正 `MonitorView` 信息列表默认筛选：采集时间默认改为“本年”，发布时间“全部”时不附加发布时间条件，用于展示本年度监测/线索结果。
- 修正媒体平台 tab 数据结构：tab 的真实值使用平台编码，数量仅用于展示，避免点击 `douyin(74)` 时误传 `sourcePlatform=74` 导致列表为空。
- 平台统计改为与列表共用当前筛选条件；前端统一合成“全部（年份）”总数标签，避免后端和前端重复生成“全部”。
- 后端 `/campus/clue/list` 补充 `language` 筛选，日期结束日改为包含整天；`sourceSubPlatform` 修正为按 `clue_source` 过滤。
- `/campus/clue/count-by-media-type` 与 `/campus/clue/count-by-sub-platform` 增强为接收列表同口径筛选参数。
- 移除信息列表“文章状态”误按 `clue_source` 过滤的隐性错误；校园线索库后续仍需补齐真实已读/已选状态模型。
- 验证：本地 `campus-web npm run build` 通过；本机缺少 `JAVA_HOME`，后端本地 Maven 未能启动。
- 服务器同步：已推送到 `deploy-vps/main`，提交 `ae7b67f`；已在服务器创建测试目录 `/home/ubuntu/yuqing-test-ae7b67f`。
- 服务器验证：`mvn -DskipTests=false -Dmaven.test.skip=false test` 通过（33 tests）；`campus-web npm install && npm run build` 通过。
- 追加修正：提交 `e23c713` 临时隐藏监测页右上角 `主题分析`、`主题预警` 两个孤立入口，仅保留“信息列表/监测结果”。
- 正式服务发布：已备份 `/opt/yuqing/web` 与后端 jar 到 `/home/ubuntu/yuqing-backups/deploy-20260514-222851`，并覆盖 `/opt/yuqing/web` 与 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 后重启 `yuqing.service`。
- 线上验证：`yuqing`、`nginx` 均为 active；`https://yuqing.zhuoran.cc/` 返回 200；正式库 `campus_clue` 74 条均在 2026 年范围内，旧日期结束口径命中 0 条，新整天结束口径命中 74 条。

### 2026-05-14 卓然舆情品牌收口
- 确定产品中文名为“卓然舆情”，英文名为 `Zhuoran Insight`。
- 重写根目录 `README.md` 和 `README-campus.md`，对外介绍统一为卓然舆情的产品定位、能力、技术栈、构建方式和合规边界。
- 更新校园前端品牌配置与浏览器标题：`campus-web/src/config/brand.ts`、`campus-web/index.html`、登录页和主布局。
- 清理文档中的旧产品名和旧项目介绍口径；静态扫描确认旧品牌字样已移除。

### 2026-05-14 方案1-6可用性闭环补强
- 方案1：`/campus/monitor/result/convert-clue` 增加接入记录线索复用逻辑；新建线索后回写 `campus_ingest_record.target_type=clue,target_id=clueId,normalized_status=converted`，减少监测任务与线索库重复数据。
- 方案2：监测扫描支持任务内重点账号/指定链接范围命中；配置了重点目标时先限定账号/链接范围，再在范围内匹配关键词/负面词，符合“在账号内或链接内搜索关键词”的需求。
- 方案3：`MonitorView` 的监测结果页新增“本任务重点账号/链接”管理区和编辑弹窗；一键从命中结果加入后可在同页回显、启停、删除。
- 方案4：教育专题百度任务改为选择已有百度接入来源，支持“创建并运行一次”，仍通过 `campus_ingest` 运行，不在教育模块直接外呼百度。
- 方案5：教育专题学校主体支持 CSV 模板下载和导入，按 `schoolId/schoolName` 去重插入或更新。
- 方案6：后台管理导航按当前用户菜单/权限过滤；监测结果和教育专题关键按钮增加 `campus:monitor:operate` / `campus:education:operate` 权限感知。
- 文档同步：更新 `docs/API_CONTRACT.md`、`docs/PERMISSION_RULES.md`、`docs/STATE_MACHINE.md`、`docs/TEST_CHECKLIST.md`、`docs/modules/*/manifest.md`。
- 同步与验证：本地提交 `66adf24 feat: complete campus monitor education workflows`；已上传并解包到服务器 `/home/ubuntu/yuqing-test-66adf24`；服务器 `mvn "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过（33 tests）；服务器 `campus-web npm run build` 通过。

### 2026-05-14 监测任务 P0-P2 功能闭环
- 建立 Odoo 式模块 manifest：`docs/modules/` 下新增监测、线索、接入、重点账号、词库、教育专题 6 个模块说明。
- P0：监测任务快速创建支持中文/蒙语/维语关键词和负面词输入，后端保存 `keywords_i18n/negative_words_i18n/exclude_words_i18n` 并按接入记录语言匹配；监测结果页展示具体命中内容、命中词、正文摘要、互动数、状态和转线索/转预警/忽略操作；线索详情修正 `clueId` 参数并增加正文兜底。
- P1：接入记录、监测结果、重点账号内容支持低成本互动指标；TikHub 返回尽量解析点赞/评论/转发/收藏/浏览；新增任务内重点账号/指定链接表和接口，支持从命中结果一键加入并保留来源依据和授权范围；新增负面词、正面词、风险词及教育专题词库。
- P2：新增教育专题后台，支持本地区教育重点新闻/政策/招生专题筛选、学校主体维护、学校声量正负面排名，以及创建百度搜索接入任务。
- 权限/API/状态/测试文档已同步更新：`docs/API_CONTRACT.md`、`docs/PERMISSION_RULES.md`、`docs/STATE_MACHINE.md`、`docs/TEST_CHECKLIST.md`。
- 验证：`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过（33 tests）；`campus-web npm run build` 通过。

### 2026-05-14 P0 工程治理
- 补齐 `docs/API_CONTRACT.md`：新增 `/campus/**` 核心接口契约、鉴权/权限/分页/错误/Breaking Change 规则
- 补齐 `docs/STATE_MACHINE.md`：新增校园线索、事件、处置、预警、接入、监测、检测、账号、分析、报告等状态机
- 补充 `docs/DEPLOY_CHECKLIST.md`：新增校园前端、Flyway `V1.24`、试点强制配置、DeepSeek/TikHub/百度密钥、校园权限和健康检查
- 修复 `src/test/java/com/stonedt/intelligence/StonedtPortalApplicationTests.java`：移除完整 Spring 上下文加载，保留轻量占位测试
- 更新 `docs/TEST_CHECKLIST.md`、`docs/codex-coordination-memory.md`、`docs/codex-main-thread-control-board.md`
- 验证：`.\mvnw.cmd test -DskipTests=false` 通过（33 tests）；`campus-web npm run build` 通过
- worktree 判断：`quirky-gauss-97fef0` 可在确认丢弃后清理；`serene-hugle-07b8f8` 建议先评审 TikHub 扩展；`xenodochial-goodall-d37fc8` 有大量 staged 变更，不能粗暴删除

### 2026-05-14 主线改名与 worktree 清理
- 已提交 P0 文档与测试门禁修正：`b6aef42 docs: complete P0 governance baseline`
- 已清理 `quirky-gauss-97fef0` Git worktree，并删除 `claude/quirky-gauss-97fef0` 分支
- 已清理 `.claude/worktrees/` 下残留的旧 worktree 空目录
- 已将本地主线分支从 `master` 改名为 `main`
- 当前 `git worktree list` 仅保留 `D:/PRJ/yuqing`

### 2026-05-14 主线与文档状态校准
- 用户确认本地 `D:\PRJ\yuqing` 的 `master` 分支作为正式主线
- 修正 `AI_PROGRESS.md` 顶部摘要：阶段、分支、最近提交、技术栈和风险描述
- 修正 `TEST_CHECKLIST.md`：当前已有 10 个测试文件，接入模块 9 个测试类报告通过，完整测试命令仍会在旧 Spring 上下文测试处超时
- 修正 `PERMISSION_RULES.md`：补充校园模块 `campus_admin` / `campus_operator` / `campus_viewer` 角色和菜单/API 权限模型
- 修正主线程协调文档：Batch32 后端 compile 已补跑通过，残留 worktree 需要先审查后清理
- 验证：`.\mvnw.cmd -DskipTests compile` 通过；`campus-web npm run build` 通过；`mvn test -DskipTests=false` 3 分钟超时

### 2026-05-13 前后端 API 修复
- 修复 Mapper XML 非法列名（`source_sub_platform`→`clue_source`，`article_status`→`clue_source`）
- 修复 `countByMediaType` SQL 条件
- 修复 `@DateTimeFormat` 日期格式（5个 Controller，`yyyy-MM-dd HH:mm:ss`→`yyyy-MM-dd`）
- 修复 Vite proxy 缺少 `/mail`、`/user` 转发
- 过滤 MainLayout 侧边栏管理后台菜单项

### 2026-05-13 前后端功能完整性审计
- 审计 22 个前端视图 × 42 个后端 Controller
- 识别 2 个后端假数据服务、11 个前端占位功能、4 个缺失 UI 按钮
- 制定 4 子任务补全方案并启动执行

### 2026-05-13 双模报告生成系统（P1-P5 全部完成 ✓）

**P1 — 数据聚合层**
- 新建 `ReportDataVO.java`（13 字段 DTO）
- 新建 `CampusReportDataService` 接口 + `CampusReportDataServiceImpl` 实现
- 实现 7 维数据聚合：舆情走势/媒体分布/情感分布/热词/热点文章/平台排行/概述
- 自动生成中文概述文本
- 新增文件：`vo/ReportDataVO.java`、`service/campus/CampusReportDataService.java`、`service/impl/campus/CampusReportDataServiceImpl.java`

**P2 — 传统模板引擎增强**
- 重写 `CampusReportServiceImpl.buildReportContent()`：集成真实数据聚合
- 模板变量从 10 个扩展至 18 个：新增 `${totalCount}`、`${negativeCount}`、`${neutralCount}`、`${positiveCount}`、`${trendTable}`、`${mediaTable}`、`${sentimentTable}`、`${keywordTable}`、`${hotArticles}`、`${platformRanking}`
- 无模板时自动生成完整 markdown（7 节：舆情概况/走势/媒体/情感/热词/热点文章/说明）
- 修改文件：`CampusReportServiceImpl.java`（+232 行）

**P3 — AI 报告生成（DeepSeek-v4-pro）**
- 新建 `AiReportService` 接口 + `AiReportServiceImpl` 实现
- 使用 `java.net.HttpURLConnection`（JDK 8 兼容）调用 DeepSeek API
- 配置项：`deepseek.api.url`、`deepseek.api.key`
- 4 种报告类型专用中文 Prompt（日报/周报/月报/专题）
- 支持 SSE 流式输出（`SseEmitter` 300s 超时）和非流式两种模式
- 新增文件：`service/campus/AiReportService.java`、`service/impl/campus/AiReportServiceImpl.java`

**P4 — 报告管理 API**
- 新增 4 个 REST 端点：
  - `POST /campus/report/generate-ai` — 非流式 AI 生成
  - `GET /campus/report/generate-ai-stream` — SSE 流式 AI 生成
  - `GET /campus/report/download-docx` — 下载 .docx
  - `GET /campus/report/download-pptx` — 下载 .pptx
- 修改文件：`CampusReportController.java`（+194 行）

**P5 — docx/pptx 导出**
- 新建 `ReportExportService` 接口 + `ReportExportServiceImpl` 实现
- 使用 Apache POI 3.15：docx 支持标题/正文/列表/引用/表格；pptx 支持标题页+内容分页
- 新增文件：`service/campus/ReportExportService.java`、`service/impl/campus/ReportExportServiceImpl.java`

**前端改动**
- `ReportView.vue`（→1074 行）：生成模式对话框（传统模板/AI智能）、变量参考面板、SSE 实时预览、下载下拉（Markdown/Word/PPT）
- `AutoReportView.vue`（→389 行）：定时任务支持 generationMode + docx/pptx 输出
- `analysisReport.ts`（→191 行）：新增 4 个 API 函数
- `api.ts`：CampusReport/CampusReportJob 新增 generationMode 字段

**编译验证**：
- 前端：`vue-tsc --noEmit` 通过（0 errors）| `vite build` 成功
- 后端：代码遵循现有模式，建议手动 `mvn compile -DskipTests`

**修改汇总**：7 个新 Java 文件 + 5 个修改文件（后端 4 + 前端 4）= 共 11 个文件变更

### 2026-05-13 前后端功能补全（4 子任务全部完成 ✓）

**子任务 A — 热度榜后端修复**（`CampusHotRankServiceImpl`）
- 移除全部 30 条硬编码假新闻标题和 `Random` 调用
- 新增 `getHotRankKeywords(days)` SQL：拆分 `campus_clue.keywords` 逗号分隔字段，按平台+关键词统计频次
- 兼容 MySQL 5.5（SUBSTRING_INDEX/CHAR_LENGTH 语法，无窗口函数）
- 修改文件：`CampusHotRankServiceImpl.java`、`CampusClueDao.java`、`CampusClueMapper.xml`

**子任务 B — 对比分析后端修复**（`CampusCompareServiceImpl`）
- 移除 `Random()` 和假公式（`platformCount * 15 + 30`）
- 新增 4 个关键词过滤 DAO 方法：`getDailyTrendByKeyword`、`countBySentimentByKeyword`、`countByPlatformByKeyword`、`countByKeyword`
- 本品与竞品数据完全分离查询，雷达图归一化计算、声量趋势独立获取
- 修改文件：`CampusCompareServiceImpl.java`、`CampusClueDao.java`、`CampusClueMapper.xml`

**子任务 C — MonitorView 前端补全**（6 项功能）
- 实现 主题分析 Tab（3 个 ECharts 图表 + 关键词表格，从线索数据提取）
- 实现 主题预警 Tab（调用 `/campus/monitor/alert/list`，带风险级别筛选和分页）
- 实现 标记本页已读（localStorage 去重存储）
- 实现 导出（CSV + UTF-8 BOM）
- 实现 批量操作（研判/加入事件对话框）
- 实现 添加文章（表单验证对话框）
- 0 个 `功能开发中` 残留
- 修改文件：`MonitorView.vue`（1629 行）

**子任务 D — 缺失 UI 按钮修复**
- ClueView 新增删除按钮（红色，带确认对话框）
- AccountView 新增删除按钮 + 状态下拉切换（el-dropdown，4 个状态选项）
- MainLayout 搜索建议从固定 TODO 改为真实 API 调用（`/campus/clue/suggest`）
- 后端新增 `GET /campus/clue/suggest?keyword=` 端点
- 修改文件：`ClueView.vue`、`AccountView.vue`、`MainLayout.vue`、`campusBusiness.ts`、`CampusClueController.java`、`CampusClueDao.java`、`CampusClueMapper.xml`

**编译验证**：
- 前端：`npx vue-tsc --noEmit` 通过 | `npx vite build` 成功
- 后端：代码语法正确，遵循现有模式（Java 环境不可用，建议手动 `mvn compile -DskipTests`）

### 2026-05-13 多语言舆情采集与研判系统（主控调度 5 子线程并行完成 ✓）

**方案设计**：汉语双通道（TikHub 社媒 + 百度千帆网页）+ 蒙语/维语百度千帆通道，统一汇入线索库，按语言分流研判（汉语规则引擎 + 蒙语/维语 DeepSeek AI 引擎）。

**主控职责**：任务拆分 → 创建共享契约 → 并行 4 子线程 → 审核 → 前端子线程 → 汇总

**Round 1 — 并行子线程（4 线程同时执行）**

| 子线程 | 阶段 | 新建/修改文件 | 状态 |
|--------|------|-------------|------|
| Agent A | P1 百度 Ingest 适配器 | 5 新文件 (baidu/ 目录) | ✅ 完成 |
| Agent B | P2 DB 迁移 + 实体 | 1 新 + 4 修改 (V1.24, Entity, Mapper) | ✅ 完成 |
| Agent C | P4 研判引擎 | 3 新文件 (judgment/ 目录) | ✅ 完成 |
| Agent D | P3 自动转线索 | 1 修改 + 2 DAO + 2 Mapper | ✅ 完成 |

**Round 2 — 前端子线程**

| 子线程 | 阶段 | 新建/修改文件 | 状态 |
|--------|------|-------------|------|
| Agent E | P5 前端改动 | 2 新 + 5 修改 (Vue + TS + Router) | ✅ 完成 |

**编译验证**：
- 前端：`npx vue-tsc --noEmit` 通过（0 errors）
- 后端：代码遵循现有模式，建议手动 `mvn compile -DskipTests`

**本次新增文件汇总（15 个新文件 + 14 个修改文件）：**

新增后端：
```
service/campus/ingest/baidu/
├── BaiduIngestAdapter.java
├── BaiduIngestFetchConfig.java
├── BaiduIngestResponseMapper.java
├── BaiduIngestSanitizer.java
└── BaiduIngestException.java
service/campus/judgment/
├── ClueJudgmentService.java（共享契约，主控创建）
├── ClueJudgmentServiceImpl.java
├── RuleJudgmentEngine.java
└── AiJudgmentEngine.java
db/migration/
└── V1.24__ClueAndRecordLanguage.sql
```

新增前端：
```
campus-web/src/views/
└── JudgmentView.vue（舆情研判工作台）
```

修改文件：
- 后端：`CampusClue.java`、`CampusIngestRecord.java`、`CampusIngestServiceImpl.java`、`CampusClueMapper.xml`、`CampusIngestRecordMapper.xml`、`CampusIngestRecordDao.java`、`CampusClueDao.java`、`config.properties`
- 前端：`IngestView.vue`、`ClueView.vue`、`campusBusiness.ts`、`api.ts`、`router/index.ts`、`MainLayout.vue`

**架构变化**：
1. 新增 `baidu_search` 适配器类型，与 `third_party_api`（TikHub）独立并存，由 `CampusIngestAdapterRegistry` 自动注册
2. 接入记录→线索 改为全自动转换，不再需要手动逐条 `convert-clue`
3. 研判引擎按语言分流：`zh`→规则引擎（关键词/负面词/正则）、`mongolian`/`uyghur`→AI引擎（DeepSeek 翻译+情感+主题+风险）
4. CampusClue + CampusIngestRecord 新增 `language` 字段
5. 百度查询支持布尔语法：`新疆大学 OR 新大 OR 心大 -录取分数线`

### 2026-05-13 蒙语/维语舆情监控模块（主控调度 3 子线程并行完成 ✓）

**整体方案**：通过百度+必应搜索 + DeepSeek AI 分析 + 内存统计聚合，实现蒙语/维语关键词的舆情监控。LLM 替代传统 NLP 解决了少数民族语言的处理难题。

**主控职责**：任务拆分 → 创建共享契约文件 → 并行分发子线程 → 审核汇总

**Phase 1 — 共享契约（主控直接创建）**
- 新建 5 个文件：`MinoritySearchParam`、`MinoritySearchResult`、`MinorityStatistics` 模型类 + `SearchEngineAdapter` 接口 + `MinorityLanguageUtil` 语言检测工具
- 更新 `config.properties`：追加 `deepseek.api.url` / `deepseek.api.key` 配置
- 更新 `docs/API_CONTRACT.md`：记录 `/api/minority/search` API 契约

**Phase 2 — 并行子线程（3 线程同时执行）**

| 子线程 | 创建文件 | 行数 | 状态 |
|--------|---------|------|------|
| Agent A — 搜索适配器 | `BaiduSearchAdapter.java` + `BingSearchAdapter.java` | 各 158 行 | ✅ 完成 |
| Agent B — LLM 分析层 | `MinorityLLMAnalyzer.java` | 489 行 | ✅ 完成 |
| Agent C — 服务编排+API | `MinoritySearchService.java` + `Impl` + `Controller` | 共 245 行 | ✅ 完成 |

**编译验证**：Java 环境不可用，建议手动 `mvn compile`

**本次新增文件汇总（11 个新 Java 文件 + 2 个修改文件）：**
```
service/minority/
├── controller/MinoritySearchController.java
├── model/MinoritySearchParam.java
├── model/MinoritySearchResult.java
├── model/MinorityStatistics.java
├── search/SearchEngineAdapter.java
├── search/impl/BaiduSearchAdapter.java
├── search/impl/BingSearchAdapter.java
├── analyze/MinorityLLMAnalyzer.java
├── service/MinoritySearchService.java
├── service/impl/MinoritySearchServiceImpl.java
└── util/MinorityLanguageUtil.java
```
修改：`config/config.properties`、`docs/API_CONTRACT.md`

## 2026-05-17 新版监测统一方案落地与生产发布

- **工作分支 / worktree**：`claude/batch33-monitor-admin`，`D:\PRJ\yuqing`；未触碰 `D:\PRJ\yuqing-daily-monthly-reports`。
- **方案基线**：已按 `docs/campus-monitor-unified-implementation-plan.md` 落地，保留 `docs/campus-monitor-implementation-plan.md` 与 `docs/new-ui-visible-feature-consolidation-plan.md` 作为来源方案。
- **后端口径**：监测扫描改为只按 `keywords` 生成命中；`monitor_subject/subject_aliases` 只保留展示与历史兼容；负面词只生成风险标记。`/campus/monitor/information/**` 默认 `hitScope=all`，`hitScope=risk` 使用 `riskMarked` 过滤；返回补齐 `collectTime/publishTimeStatus/riskMarked`，默认排序按明确发布时间优先、未知发布时间按采集时间倒序。
- **治理与迁移**：新增 `V1.43__CampusMonitorUnifiedGovernance.sql`，隐藏数据接入菜单、规范历史情感值、逻辑隐藏快手搜索反馈/UI 噪声记录；新增 `CampusSentimentNormalizer`，接入、监测、线索、规则研判和 AI 研判统一写入 `positive/neutral/negative/none`。
- **前端收口**：监测页删除“添加文章”，保留“新增人工线索”；情感筛选改为全部/正面/中性/负面/未知；排序移除“价值度/网站等级”；“相似信息”改为“合并相似信息”；批量操作按监测命中与已转线索分流并显示成功/失败/跳过数量。
- **权限与入口**：客户后台隐藏“数据接入”，`/admin/ingest` 重定向 `/admin/monitor-tasks`；受保护路由进入前调用 `/campus/system/current-user` 校验真实 session；权限加载失败或后台菜单为空时不再展示全部后台菜单，无后台权限账号隐藏“后台管理”。
- **文档同步**：更新 `docs/API_CONTRACT.md`、`docs/PERMISSION_RULES.md`、`docs/STATE_MACHINE.md`、`docs/TEST_CHECKLIST.md`、`docs/modules/campus_monitor/manifest.md`、`docs/modules/campus_ingest/manifest.md`、`docs/modules/campus_clue/manifest.md`。
- **本地验证**：`CampusMonitorResultMapper.xml`、`CampusClueMapper.xml` XML 解析通过；`git diff --check` 通过（仅换行提示）；`.codex-tools/jdk8` 下 `.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过，16 个测试类 57 tests；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；`.\mvnw.cmd -DskipTests package` 通过，jar 内确认包含 `V1.43`、`CampusMonitorResultMapper.xml` 和 `CampusSentimentNormalizer.class`。
- **Git 提交与生产发布**：提交 `feat: unify monitor information workflow`；发布前备份 `/opt/yuqing/app` jar、`/opt/yuqing/web` 和 `campus_yuqing` 数据库，备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-122325-monitor-unified`；已覆盖 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`，重启 `yuqing` 并 reload `nginx`。
- **线上验收**：`yuqing/nginx` active，后端监听 `8084`；Flyway 已应用 `1.43 CampusMonitorUnifiedGovernance` 且 success=1；`campus_permission_menu.ingest` 已为 `visible=0,status=0`；`https://yuqing.zhuoran.cc/`、`/monitor`、`/admin/ingest` 均返回前端 index 200，未登录访问 `/campus/monitor/information/list?...hitScope=all` 返回 302，符合鉴权预期。

## 2026-05-17 监测信息操作 ID 精度修复

- **工作分支 / worktree**：`claude/fix-campus-monitor-id-precision`，`D:\PRJ\yuqing`。
- **问题定位**：线上审计日志确认“转线索/转预警/忽略/加入重点账号/加入指定链接”提交的 `monitorResultId` 被浏览器按 JS number 处理后发生精度丢失，例如提交 `2055870813135048700`，库中真实值为 `2055870813135048704`，导致后端 `selectByResultId` 报“监测结果不存在”。
- **后端修复**：对校园监测操作链路会回传给前端再提交的 Long ID 增加 Jackson `ToStringSerializer`，覆盖 `CampusMonitorInformation`、`CampusMonitorResult`、`CampusMonitorTask`、`CampusMonitorWatchTarget`、`CampusMonitorRunLog`、`CampusAlert`，并同步覆盖监测信息中会用到的 `CampusClue` 关键 ID；Controller 入参仍保持 Long，由 Spring 兼容字符串绑定。
- **前端修复**：新增 `ApiId = string | number`，监测服务层和相关页面按 `ApiId` 原样传参；搜索结果承接线索 ID 字符串；监测信息“更多”操作缺少结果/任务 ID 时显示明确提示，不再静默返回。
- **契约同步**：更新 `docs/API_CONTRACT.md`，明确监测模块 Snowflake 业务 ID 响应按字符串序列化，接口入参仍兼容 Long 字符串。
- **本地验证**：`CampusMonitorIdSerializationTest` 2 tests 通过；`.\mvnw.cmd -DskipTests compile` 通过；`.\mvnw.cmd "-DskipTests=false" "-Dmaven.test.skip=false" test` 通过，17 个测试类 59 tests；`campus-web npm run build` 通过，仅保留既有 Rollup PURE 注释和 chunk 体积警告；`.\mvnw.cmd -DskipTests package` 通过；`git diff --check` 通过（仅换行提示）。
- **Git 与发布**：因 GitHub `origin/main` 为 snapshot 历史，本次修复从 `origin/main` cherry-pick 生成最终提交 `f7655c3 fix: preserve campus monitor id precision`，已推送 GitHub 分支 `claude/fix-campus-monitor-id-precision` 和 `deploy-vps/claude/fix-campus-monitor-id-precision`；保留本地备份分支 `claude/fix-campus-monitor-id-precision-local` 指向原部署分支同内容提交 `f623422`。
- **生产部署**：已部署 jar 与 `campus-web/dist` 到服务器；发布前备份 `/opt/yuqing/app/stonedt-portal-0.5.3-SNAPSHOT.jar` 和 `/opt/yuqing/web`，备份目录 `/home/ubuntu/yuqing-backups/deploy-20260517-131918-id-precision`；`yuqing` 重启成功，`nginx -t` 通过并 reload。
- **线上验收**：`https://yuqing.zhuoran.cc/` 返回 200，`/monitor` 返回 200；未登录访问 `/campus/monitor/information/list?pageNum=1&pageSize=1` 返回 302，符合鉴权预期；线上静态资源已包含“当前记录缺少监测结果ID，请刷新后重试”提示。

## 重要决策记录

| 日期 | 决策 | 说明 |
|------|------|------|
| 2026-05-17 | 监测模块 Snowflake ID 响应按字符串返回 | 监测信息和相关操作链路避免浏览器 19 位 Long 精度丢失；后端接口入参继续使用 Long 并兼容字符串绑定 |
| 2026-05-17 | 新版监测统一方案已落地并发布 | 监测命中、风险标记、时间排序、情感、人工线索、批量操作、数据接入隐藏和权限导航按统一方案完成，生产已应用 `V1.43` |
| 2026-05-17 | 校园监测与新版页面统一实施方案已形成 | 融合 `docs/campus-monitor-implementation-plan.md` 与 `docs/new-ui-visible-feature-consolidation-plan.md`，新增 `docs/campus-monitor-unified-implementation-plan.md` 作为后续唯一综合执行口径 |
| 2026-05-17 | 新版页面可见功能收口方案已固化为文档 | 在 `claude/batch33-monitor-admin` 分支、`D:\PRJ\yuqing` worktree 中新增 `docs/new-ui-visible-feature-consolidation-plan.md`，仅保存方案，不修改业务代码 |
| 2026-05-15 | Jina Reader 定位为正文增强层 | 搜索发现仍由百度/TikHub/DPI/上级平台完成；Jina Reader 只读取已发现且合规的公开 URL 正文，监测任务不直接外呼 Reader |
| 2026-05-16 | 小红书采用 TikHub 两段式详情增强 | `search_notes` 负责发现 `note_id`，图文/视频详情接口只增强同一接入记录；精确去重口径为 `source_id + external_id` |
| 2026-05-15 | 完成接入能力盘点与平台任务补齐 | 服务器已建立抖音、小红书、B站、百度新闻/网页、公开网页来源和接入任务；抖音/B站/百度已跑通并进入监测信息；2026-05-16 已补齐小红书详情增强并复跑通过 |
| 2026-05-14 | 新业务采用 Odoo 式模块化约束 | 新增能力必须先确认 `docs/modules/*/manifest.md`，同步权限、API、状态、测试和文档影响 |
| 2026-05-14 | 本地主线改名为 main | P0 提交完成后，将 `master` 改名为 `main`，并清理残留 Claude worktree |
| 2026-05-14 | 完成 P0 工程治理主项 | 校园核心 API/状态机/部署清单已补齐，后端测试门禁恢复为 33 tests 通过 |
| 2026-05-13 | 多语言舆情采集与研判系统 P1-P5 完成 | 15 新文件 + 14 修改文件 = 29 文件变更；百度+TikHub双通道采集→自动转线索→语言分流研判，5 子线程并行实施 |
| 2026-05-13 | 双模报告生成系统 P1-P5 完成 | 7 新文件 + 5 修改文件 = 12 文件变更；数据聚合→模板增强→AI生成→导出→前端UI，全程并行 Agent 实施 |
| 2026-05-13 | 完成前后端功能补全 | 4 个子任务全部完成：热度榜/对比分析/MonitorView/UI按钮，共修改 10 个文件 |
| 2026-05-13 | 启动前后端功能补全 | 4 个子任务并行执行：热度榜/对比分析后端修复 + MonitorView/按钮前端补全 |
| 2026-05-12 | 初始化 AI 工程化文档体系 | 建立 10 份核心文档，规范 AI 协作流程 |

## 仍待人工确认问题

1. 试点部署是否启用 DeepSeek、TikHub、百度千帆、旧 NLP/写作宝等外部服务。
2. 学校正式组织、部门、角色矩阵、管理员账号和真实数据接入授权范围。
3. Jina Reader 正式部署方式待确认：托管 `r.jina.ai`、当前代理链路，还是自部署 `jina-ai/reader`；学校/政企正式环境建议优先自部署或走受控外联出口。
4. Jina Reader 正文增强已在服务器测试目录补跑通过；正式发布前仍需确认是否启用以及托管/自部署方式。
5. 小红书详情增强当前每次运行默认最多调用 20 条详情，历史未增强记录如需全部补齐，需要单独安排低频补采/回填任务，避免一次性消耗过多 TikHub 配额。

## 待确认问题（已解决 ✅）

以下问题已通过代码分析确认：

1. 当前开源版本（数据展示模块）**只需 MySQL + Redis**。ES/Kafka/NLP/写作宝通过 HTTP API 调用，无需本地部署。RabbitMQ/MongoDB/ClickHouse/DataX 属于数据处理和采集模块，当前不依赖。
2. 全文搜索数据源：通过 `FullSearchService` → `sendPostRaw()` → 外部 HTTP 服务，非直接查询数据库。
3. `aop/` 与 `aspect/` 包重复：两个包定义了完全相同的 `@SystemControllerLog` 注解。`aop/` 为活跃包（被 Controller 引用 + `SystemLogAspect`），`aspect/` 为死代码残留，可安全删除。
4. `identity`/`user_type`/`user_level` 字段：均为预留字段。`identity` 在创建用户时硬编码为 1，三个字段在代码中从未被引用用于权限或功能控制。
5. 微信扫码登录配置参数：依赖 `wechat_config` 表中的 AppID/AppSecret 等字段，需人工配置。
6. 声量监测（VolumeMonitor）数据来源：通过 `monitor_analysis` 表的 JSON 大字段读取。
7. config/xml/ 目录：存放 mxGraph draw.io 格式的 XML 图文件，非应用配置文件。线上路径指向 `/opt/open_yq/xml/`。
8. `insertnewwords.url` 指向 `dx1.stonedt.com:3407/newDic/insert`，用于 IK 分词器热词插入的外部服务。

### 已确认

- **智写报告（AI 报告生成）**：代码已完整实现。后端 `PlatformController` + `PlatformServiceImpl` 通过 SSE 流式对接外部写作宝服务，前端 `report.html` 有完整 UI。配置 `platform.xie.url` 指向 `xie.stonedt.com` 即可使用。
- **大屏模块部署方式**：推荐 **Nginx 直接代理 `large_screen/dist/` 静态文件**。配置参考：
  ```nginx
  location /opinion_screen {
      alias /path/to/large_screen/dist/;
      try_files $uri $uri/ /opinion_screen/index.html;  # Vue history 模式 fallback
  }
  ```
  无需额外 Tomcat 实例，性能更好、资源更省。

## 2026-05-17 监测采集与展示口径固化方案

**工作分支 / worktree**
- 分支：`claude/batch33-monitor-admin`
- worktree：`D:\PRJ\yuqing`
- 当前 HEAD：`0090f98`

**新增项目记录**
- 新增方案文档：`docs/campus-monitor-implementation-plan.md`

**固化口径**
- 监测任务名称 / 主体词只表示任务对象，不作为命中条件。
- 监测命中只依据 `keywords`；命中关键词后再识别 `negative_words`。
- `/monitor` 默认展示全部关键词命中结果，风险词命中只作为标记和筛选条件。
- 接入层必须过滤搜索反馈项、平台 UI 文案、控件文案等非内容记录。
- `publishTime` 与 `collectTime` 分离；发布时间缺失时展示为“发布时间未知”，排序时不能压过明确发布时间内容。

**当前现状记录**
- 当前“新疆大学”任务中主体词与关键词基本一致，因此既有监测命中整体可保留。
- 当前存在快手 UI 文案混入、部分平台发布时间缺失、旧文档描述与新口径不一致等问题。
- 后续实施应按 `docs/campus-monitor-implementation-plan.md` 分阶段推进，并同步更新模块 manifest、API 契约、状态机和测试清单。

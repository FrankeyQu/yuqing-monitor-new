# 校园舆情系统试运行验收手册

## 1. 验收基线

- 后端：`http://127.0.0.1:8084/`
- 前端：`http://127.0.0.1:5175/`
- Flyway：应迁移到当前最新迁移版本（截至 2026-05-14 为 `V1.24`）
- 范围：本地代码、配置、数据库迁移和试运行交付文档

试运行账号由学校管理员初始化发放。交付版登录页和手册不再写入默认账号密码。

## 2. 启动与构建

后端构建：

```powershell
$jdk = Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME=$jdk
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

前端构建：

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build
```

本地服务：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev/start-local.ps1 -AppPort 8084
cd D:\PRJ\yuqing\campus-web
npm run dev -- --host 127.0.0.1 --port 5175
```

## 3. 验收路径

1. 登录后进入 `/situation`，确认态势大屏可渲染风险、预警、检测命中和处置中事件。
2. 进入 `/monitor`，确认可创建监测任务，设置主体、别名、关键词、负面词、平台、频率、自动扫描和绑定接入任务。
3. 在 `/monitor` 点击立即运行，确认监测结果生成，负面命中进入负面告警。
4. 临时开启监测调度开关后，确认启用任务会自动写入下一次运行时间并生成调度日志。
5. 进入 `/ingest`，确认媒体接入中心可查看来源、任务、公开网页白名单、运行日志和 API 调用日志。
6. 在 `/ingest` 新建或查看任务时，确认 TikHub 只使用 `credentialRef`，公开网页仍为 `metadata_only` 占位。
7. 进入 `/detection`，确认检测主题、规则、任务、命中和运行日志可查看。
8. 进入 `/alerts`、`/clues`、`/events`，确认预警处理、线索研判、事件分派反馈链路可用。
9. 进入 `/analysis`，确认辅助研判结果带人工复核，不作为自动结论。
10. 进入 `/reports` 和 `/auto-reports`，确认报告列表、生成、下载、归档和任务日志可用。
11. 进入 `/settings/permissions`，确认管理员能查看角色、菜单权限和接口权限。
12. 进入 `/settings/audit`，确认关键操作有审计记录。

## 4. Batch29 配置检查

- 登录页不预填账号、密码和验证码。
- 旧定时任务默认关闭，只有学校确认后才通过环境变量开启。
- 历史外部服务地址默认为空，不指向旧外部地址。
- API 文档默认关闭；如需开放，需单独确认访问范围和账号。
- `prelaunch.strict=1` 时，会拒绝演示 token、root/123456、旧任务开启和未确认旧外部地址。
- 监测自动调度默认关闭，需显式设置 `SCHEDULE_CAMPUS_MONITOR_OPEN=1` 才会自动运行。
- `campus_operator` 不再依赖 `/campus/**` 通配权限。
- `campus_viewer` 可读取工作台、态势和报告所需只读接口。

## 5. 真实接入前确认

启用真实数据源前必须完成：

- 数据源名称、平台、接入方式。
- 授权依据、授权范围、责任部门、负责人。
- 采集或调用频率、额度、保留期限。
- API Key 或凭证存放方式，不写入代码和数据库明文字段。
- 停用和回滚方式。
- 是否涉及公开网页白名单、robots、栏目路径和访问频率。

不得接入私信、通讯录、密码、Cookie、非公开资料，也不得设计绕过平台限制的能力。

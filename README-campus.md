# 卓然舆情 Zhuoran Insight

卓然舆情是一套面向校园和教育场景的舆情数据接入、监测研判、事件处置与报告归档平台。

## 当前能力

- 校园工作台与态势大屏。
- 监测任务中心：配置监测主体、别名、关键词、负面词、平台、频率和接入任务绑定，支持手动运行和默认关闭的自动扫描，负面命中自动告警。
- 线索库、预警中心、事件处置和报告归档。
- 重点关注账号的授权登记、审核和公开动态管理。
- 检测任务、检测命中和自动预警。
- 媒体接入中心：接入来源、接入任务、标准化记录、运行日志、API 调用日志。
- TikHub 安全适配占位：只保存 `credentialRef`，真实密钥来自环境变量。
- 公开网页白名单预留：当前仅校验白名单和 `metadata_only`，不执行真实网页抓取。
- 校园角色、菜单、接口权限和审计日志。

## 试运行边界

- 只处理公开、授权、上级移交或学校业务中依法获得的数据。
- 真实数据源启用前必须确认授权依据、范围、频率、额度、保留期限和责任部门。
- 不保存真实 API Key、Cookie、生产密码到代码或数据库明文字段。
- 不接入私信、通讯录、密码、非公开个人资料。
- 检测命中和辅助研判只作为线索，最终结论必须人工确认。

## 本地构建

后端：

```powershell
$jdk = Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME=$jdk
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

前端：

```powershell
cd D:\PRJ\yuqing\campus-web
npm run build
```

## 关键文档

- `docs/campus-acceptance-runbook.md`
- `docs/campus-web-runbook.md`
- `docs/campus-prelaunch-checklist.md`
- `docs/campus-residual-risks-and-next-steps.md`
- `docs/codex-coordination-memory.md`

试运行交付优先阅读本文件、根目录 `README.md` 和 `docs/` 下的工程文档。

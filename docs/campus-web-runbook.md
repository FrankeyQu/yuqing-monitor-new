# 校园前端本地运行手册

本手册仅用于本地开发和试运行前自测。交付给学校的试运行账号应由管理员初始化发放，不在页面或文档中写入默认密码。

## 1. 运行前提

- 后端本地服务：`http://127.0.0.1:8084/`
- 前端目录：`D:\PRJ\yuqing\campus-web`
- Node.js 与 npm 已可用。

## 2. 常用命令

```powershell
cd D:\PRJ\yuqing\campus-web
npm install
npm run dev -- --host 127.0.0.1 --port 5175
```

生产构建：

```powershell
npm run build
```

## 3. 登录说明

- 登录接口：`POST /login`
- Cookie：后端写入 HttpOnly `token`
- 账号来源：由学校管理员或试运行初始化脚本发放。

本地开发环境如需使用演示账号，请查阅本地协调文档，不写入交付手册。

## 4. 页面清单

- `/login`：校园版登录页。
- `/`：舆情态势工作台，右上“大屏模式”可切换为一屏展示。
- `/situation`：历史兼容路由，直接进入同一舆情态势页的大屏模式，不作为独立客户菜单入口；当前大屏为“校园舆情智能驾驶舱”，按 BI 大屏方式展示全量/风险命中趋势、来源排行、情感分布、负面告警、处置中事件和监测任务运行状态。
- `/monitor`：监测信息工作台，统一展示监测命中和线索。
- `/admin/monitor-tasks`：后台监测任务管理，只维护任务配置、接入任务绑定、重点目标、启停和手动运行，不展示具体监测内容。
- `/clues`：历史兼容路由，不作为独立前台菜单入口，访问时重定向到 `/monitor`。
- `/accounts`：重点账号与账号公开动态。
- `/alerts`：预警中心。
- `/events`：事件处置。
- `/ingest`：媒体接入中心，包含来源、任务、接入记录和公开网页白名单；运行日志和外部调用日志不在后台业务页展示。
- `/analysis`：辅助研判。
- `/reports`：报告归档。
- `/auto-reports`：自动报告。
- `/settings/departments`：部门管理。
- `/settings/dicts`：数据字典。
- `/settings/audit`：审计日志。
- `/settings/permissions`：权限管理。

## 5. 试运行注意

- 登录页不预填账号和密码。
- 前端不提供真实 API Key、Cookie、账号池、代理池或签名参数输入。
- 外部接口适配保留后端兼容，后台数据接入页不展示供应商调用配置。
- 公开网页接入默认仍可使用 `metadata_only` 仅校验白名单；如后端启用 `CONTENT_EXTRACTION_ENABLED=true`，可在白名单任务中选择 `jina_reader` 读取单个公开 URL 正文。
- 监测任务自动扫描默认关闭，只有在后端显式开启 `SCHEDULE_CAMPUS_MONITOR_OPEN=1` 时才会自动运行。
- 处置员或查看员如访问未授权操作，后端会返回 `403`，由管理员在权限页调整。

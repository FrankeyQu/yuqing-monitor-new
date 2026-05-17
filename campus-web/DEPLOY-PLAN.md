# 生产部署 & 首校落地计划

## 任务分解

| ID | 任务 | 授权范围 | 并行 | 说明 |
|----|------|---------|------|------|
| D1 | 生产构建脚本 | `deploy.sh` + `campus-web/Dockerfile` | ✅ 与D2并行 | 一键打包后端jar+前端dist |
| D2 | 前端生产构建配置 | `campus-web/vite.config.ts` | ✅ 与D1并行 | 生产环境API代理配置 |
| D3 | 数据接入配置 | `config/` 配置 + TikHub调度验证 | 等D1完成 | TikHub定时任务 + 公开网页白名单 |
| D4 | 学校部署手册 | `docs/school-deployment-guide.md` | 等D1+D2完成 | 从0到1部署一整套学校的操作手册 |

## 执行顺序

```
第1轮（并行）:
  D1: deploy.sh 构建脚本
  D2: 前端 vite.config.ts 生产配置

第2轮:
  D3: 数据接入 + TikHub 调度

第3轮:
  D4: 学校部署手册（含部门/主题/账号配置指南）
```

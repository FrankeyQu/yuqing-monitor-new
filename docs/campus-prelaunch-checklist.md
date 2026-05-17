# 校园舆情系统试运行前检查清单

## 1. 默认配置

- [ ] 设置 `TOKEN_PRIVATE_KEY`，长度不少于 32 位，不使用演示值。
- [ ] 设置试运行数据库账号和密码，不使用 `root/123456`。
- [ ] 保持 `SPRINGDOC_API_DOCS_ENABLED=false`、`SPRINGDOC_SWAGGER_UI_ENABLED=false`、`KNIFE4J_ENABLED=false`。
- [ ] 设置 `PRELAUNCH_STRICT=1` 进行启动前校验。
- [ ] 确认 `system.url`、CORS 白名单和正式前端地址。

## 2. 旧任务与旧外部服务

- [ ] 所有 `LEGACY_SCHEDULE_*` 默认保持 `0`。
- [ ] `LEGACY_ES_HOT_OPEN` 默认保持 `0`。
- [ ] `LEGACY_SPIDER_OPEN` 默认保持 `0`。
- [ ] 如需启用历史外部服务，先形成学校确认记录，再设置对应环境变量。
- [ ] 不在仓库中保存真实 API Key、Cookie、公众号密钥或生产密码。

## 3. 学校初始化

- [ ] 将 `V1.19` 中的“试运行学校”替换或维护为真实学校名称。
- [ ] 初始化网信办、宣传部、学工部、保卫处、后勤管理处、学院单位等组织。
- [ ] 录入试运行管理员账号，并完成首次登录改密。
- [ ] 给普通业务用户分配 `campus_operator` 或更细角色，不默认分配管理员。
- [ ] 给领导查看类账号分配 `campus_viewer`，确认是否允许下载报告。

## 4. 真实数据接入

- [ ] 确认数据源、平台、接口或白名单页面。
- [ ] 确认授权依据、授权范围、责任部门、负责人和任务编号。
- [ ] 确认调用频率、额度、保留期限、停用和回滚方式。
- [ ] TikHub 真实密钥只放在环境变量 `TIKHUB_API_KEY`。
- [ ] 公开网页采集需确认白名单、栏目路径、robots 和访问频率。
- [ ] 不接入私信、通讯录、密码、非公开个人资料。

## 5. 验收命令

```powershell
$jdk = Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME=$jdk
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package

cd D:\PRJ\yuqing\campus-web
npm run build
```

## 6. 验收结果记录

- 后端构建结果：
- 前端构建结果：
- Flyway 版本：
- 严格模式校验结果：
- 登录页默认值检查：
- 权限页检查：
- 接入中心检查：
- 审计日志检查：

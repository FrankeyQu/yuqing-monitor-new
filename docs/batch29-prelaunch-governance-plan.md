# Batch 29 试运行前配置治理与学校初始化实施方案 V0.2

状态：Done。

## 1. 背景

Batch21-28 已完成媒体接入平台主线。系统当前处于本地开发可用状态，下一步要转入学校试运行准备。Batch29 不做攻击测试、不做外部扫描、不接真实外部数据源，只处理本地代码、配置和交付文档中的上线前准备项。

## 2. 本批目标

- 清理前端默认登录值，避免交付界面出现演示账号密码。
- 将旧系统定时任务改为默认关闭，只允许试运行确认后显式开启。
- 将旧外部服务地址改为环境变量配置，默认不指向历史外部地址。
- 关闭接口文档默认暴露，需显式配置后才开启。
- 新增试运行严格模式配置校验，帮助部署前发现不合规默认配置。
- 收紧校园权限初始化，避免业务角色继续依赖粗粒度 `/campus/**`。
- 增加学校组织和字典初始化模板。
- 更新交付文档，移除默认账号密码直写，增加真实数据接入前确认表。

## 3. 本批明确不做

- 不修改真实生产账号和真实密码。
- 不写入真实 API Key、Cookie、公众号密钥、数据库生产密码。
- 不启用 TikHub 真实调用。
- 不实现公开网页真实抓取。
- 不对外部地址做扫描或连通性测试。
- 不清空数据库，不删除用户数据。
- 不替学校决定最终角色矩阵、管理员账号、学院/部门清单。

## 4. 主线程合并摸底结论

### 4.1 旧任务与配置

- 旧 Quartz 调度全局启用，多个旧任务默认开启。
- 配置中保留历史外部服务地址。
- 旧全文检索链路会读取 `config/xml` 模板并发往外部服务。
- 微信相关常量中存在不适合交付的固定配置。
- API 文档默认启用且 basic 账号为演示值。

主线程决策：

- 本批只做默认关闭和环境变量化，不删除旧功能。
- 对需要学校确认的外部服务，先放入确认清单。
- 增加 `prelaunch.strict` 严格模式，试运行部署前可强制检查。

### 4.2 权限与初始化

- 初始迁移曾把所有启用用户授予 `campus_admin`。
- `campus_operator` 仍有粗粒度接口权限。
- `campus:system:self` 范围过宽。
- 查看员菜单和只读接口权限不完全闭环。
- 学校组织、平台字典、账号类型字典和关键词模板不足。

主线程决策：

- 新增修正迁移，不回改历史迁移。
- 管理员保留全量接口；业务处置员改为业务模块权限；查看员补只读接口。
- 只保留默认演示管理员为管理员，其他用户默认转业务处置员。
- 新增学校组织模板和常用平台/账号类型字典项。

### 4.3 前端与文档

- 登录页预填演示账号密码。
- 运行手册和验收手册仍写默认账号密码。
- README 已切换为卓然舆情项目说明，后续仍需保持与试运行交付口径一致。
- 真实数据接入前确认项分散在多个文档。

主线程决策：

- 本批移除登录页默认值。
- 新增/更新交付文档，把演示账号改为“由学校管理员初始化发放”。
- 新增真实接入启用前确认表。

## 5. 实施范围

后端配置：

- `config/application.properties`
- `config/application.yml`
- `WechatConstant`
- 新增 `PrelaunchReadinessValidator`

数据库迁移：

- 新增 `V1.19__CampusPrelaunchGovernance.sql`

前端：

- `campus-web/src/views/LoginView.vue`
- 必要时调整布局环境文案。

文档：

- `docs/campus-acceptance-runbook.md`
- `docs/campus-web-runbook.md`
- `docs/campus-residual-risks-and-next-steps.md`
- 新增 `docs/campus-prelaunch-checklist.md`

## 6. 验收步骤

```powershell
$jdk = Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName
$env:JAVA_HOME=$jdk
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package

cd D:\PRJ\yuqing\campus-web
npm run build
```

联调检查：

- 前端登录页不再预填演示账号密码。
- 本地服务启动后 Flyway 迁移到 `V1.19`。
- 默认旧调度配置均为关闭。
- 接口文档默认关闭。
- `prelaunch.strict=1` 时，若仍使用演示 token、root/123456、旧外部地址或开启旧任务，应阻断启动并提示配置项。
- 校园权限 API 可以加载，管理员仍可使用系统。

## 7. 待用户后续确认

- 学校正式名称。
- 试运行管理员账号发放方式。
- 学校部门、学院、负责人账号清单。
- 角色矩阵：管理员、值班员、研判员、处置员、领导查看员、审计员。
- 真实数据源、授权依据、责任部门、采集频率、额度、保留期限。
- 是否保留上游 README 作为归档说明。

## 8. 实施结果

- 登录页已移除默认账号、密码和验证码预填。
- 旧定时任务、旧 NLP/LLM 配置和旧实时检索桥接默认关闭或环境变量化，外部地址默认空值。
- API 文档默认关闭，Knife4j basic 账号密码不再写默认值。
- 新增 `PrelaunchReadinessValidator`，`prelaunch.strict=1` 会阻断演示 token、本地数据库默认值、旧任务开启和未确认旧外部地址。
- 新增 `V1.19__CampusPrelaunchGovernance.sql`，停用业务角色旧 `/campus/**` 通配接口权限，补充处置员和查看员最小接口权限。
- 新增学校 root、常用部门和平台/账号/敏感词/组织类型字典模板。
- 更新试运行验收手册、前端运行手册、残余风险文档，新增试运行前检查清单和校园版 README。

## 9. 验证结果

- 后端 `.\mvnw.cmd -DskipTests package` 通过。
- 前端 `campus-web npm run build` 通过，仅保留既有 Rollup 注释和 chunk size 警告。
- 本地服务启动成功，Flyway 从 `V1.18` 迁移到 `V1.19`。
- 数据库抽查确认旧业务通配 API `180503` 已停用，处置员和查看员使用拆分后的接口权限。
- 严格模式负向检查确认会拒绝演示 token、`root` 数据库账号和本地默认数据库密码。

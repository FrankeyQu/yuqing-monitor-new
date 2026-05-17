# 本地开发运行手册

本项目当前云电脑不使用 Docker。Codex 已在 `.codex-tools/` 下准备免安装运行环境：

- JDK 8：Temurin 1.8.0_482
- MariaDB 10.11：MySQL 协议兼容，端口 `3306`
- Redis for Windows 5.0：端口 `6379`

`.codex-tools/` 已加入 `.gitignore`，不要提交工具包和本地数据库文件。

## 启动

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev/start-local.ps1
```

访问：

```text
http://127.0.0.1:8084/
```

启动日志：

```text
.codex-tools/app.log
.codex-tools/app.err.log
```

## 停止

```powershell
powershell -ExecutionPolicy Bypass -File scripts/dev/stop-local.ps1
```

## 数据库

默认连接与 `config/application.yml` 保持一致：

```text
jdbc:mysql://localhost:3306/stonedt_portal
root / 123456
```

首次启动时 Flyway 会自动执行 `V1.0` 到 `V1.12` 迁移。

## 演示数据

```powershell
powershell -ExecutionPolicy Bypass -File scripts/demo/seed-campus-demo-data.ps1
```

演示数据说明见：

```text
docs/campus-demo-data.md
docs/campus-acceptance-runbook.md
```

## 构建

```powershell
$env:JAVA_HOME=(Get-ChildItem .codex-tools\jdk8 -Directory | Select-Object -First 1 -ExpandProperty FullName)
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -DskipTests package
```

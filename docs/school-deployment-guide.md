# 卓然舆情 — 学校部署手册

> 适用场景：学校从零开始部署卓然舆情（单机部署）。

---

## 1. 环境要求

| 组件 | 版本要求 | 用途 |
| --- | --- | --- |
| JDK | 8（1.8） | 运行后端 Java 应用 |
| MySQL | 5.7+（兼容 MariaDB 10.11） | 业务数据存储 |
| Redis | 4.0+ | 会话存储和缓存 |
| Node.js | 18+ | 仅构建前端时使用，生产环境不常驻 |
| Nginx | 任意稳定版本 | 生产环境反向代理和静态文件服务 |

### 1.1 端口规划

| 用途 | 默认端口 | 说明 |
| --- | --- | --- |
| 后端 API | 8084 | Spring Boot 服务 |
| Redis | 6379 | 会话存储 |
| MySQL | 3306 | 业务数据库 |
| Nginx | 80 / 443 | 对外 HTTP/HTTPS 服务 |

---

## 2. 部署步骤

### 2.1 数据库初始化

#### 2.1.1 创建数据库

登录 MySQL 并创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS stonedt_portal
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

> **说明**：数据库名称 `stonedt_portal` 为默认值，如需修改请同步更新 `config/application.yml` 中的 `spring.datasource.druid.url`。

#### 2.1.2 Flyway 自动建表

后端首次启动时 Flyway 会自动执行 `classpath:db/migration` 路径下的所有迁移脚本（V1.0 到 V1.22+），无需手动执行建表 SQL。

#### 2.1.3 导入演示数据（可选）

如需要演示数据进行功能验收和培训，执行以下命令：

```bash
# Linux
mysql -u root -p stonedt_portal < scripts/demo/seed-campus-demo.sql

# Windows (PowerShell)
powershell -ExecutionPolicy Bypass -File scripts/demo/seed-campus-demo-data.ps1
```

> 演示数据说明详见 `docs/campus-demo-data.md`，核心链路业务 ID 见该文档第 3 节。
>
> 演示数据可重复执行，脚本使用 `ON DUPLICATE KEY UPDATE` 进行幂等更新。

---

### 2.2 后端部署

#### 2.2.1 构建后端

```bash
# 设置 JDK 8 环境
export JAVA_HOME=/path/to/jdk8
export PATH=$JAVA_HOME/bin:$PATH

# 构建（跳过测试）
./mvnw -DskipTests clean package
```

构建产物路径：`target/stonedt-portal-0.5.3-SNAPSHOT.jar`

> **Windows**: 使用 `.\mvnw.cmd` 代替 `./mvnw`。
>
> **Maven 镜像**: 如果 Maven 依赖下载慢，在项目根目录创建 `.mvn/jvm.config` 或配置 `~/.m2/settings.xml` 使用国内镜像（如阿里云镜像）。

#### 2.2.2 准备配置文件

将 `config/application.yml` 复制到部署目录并修改：

```bash
mkdir -p /opt/stonedt-portal/config
cp config/application.yml /opt/stonedt-portal/config/
```

修改 `config/application.yml` 中的以下配置：

| 配置项 | 说明 | 默认值 | 生产建议 |
| --- | --- | --- | --- |
| `server.port` | 后端端口 | `8084` | 保持默认或按需修改 |
| `spring.datasource.druid.url` | MySQL 连接地址 | `jdbc:mysql://localhost:3306/stonedt_portal?...` | 修改为实际 MySQL 地址 |
| `spring.datasource.druid.username` | MySQL 用户名 | `root` | **必须修改**，使用专用账号 |
| `spring.datasource.druid.password` | MySQL 密码 | `123456` | **必须修改** |
| `spring.redis.host` | Redis 主机 | `localhost` | 修改为实际 Redis 地址 |
| `spring.redis.port` | Redis 端口 | `6379` | 保持默认或按需修改 |

推荐使用环境变量覆盖数据库配置（避免在 YAML 中明文写入密码）：

| 环境变量 | 对应配置项 | 说明 |
| --- | --- | --- |
| `DB_URL` | `spring.datasource.druid.url` | 完整 JDBC URL |
| `DB_USERNAME` | `spring.datasource.druid.username` | 数据库用户名 |
| `DB_PASSWORD` | `spring.datasource.druid.password` | 数据库密码 |

#### 2.2.3 启动后端

```bash
# 前台启动（调试用）
java -jar target/stonedt-portal-0.5.3-SNAPSHOT.jar \
  --spring.config.location=config/application.yml

# 后台启动（生产用）
nohup java -jar target/stonedt-portal-0.5.3-SNAPSHOT.jar \
  --spring.config.location=config/application.yml \
  > app.log 2>&1 &

# 使用 systemd 管理（推荐）
# 见下方 systemd 服务单元示例
```

**验证后端启动**：

```bash
curl http://127.0.0.1:8084/
# 应返回页面内容（登录页 HTML），无报错即表示启动成功
```

**systemd 服务单元**（`/etc/systemd/system/stonedt-portal.service`）：

```ini
[Unit]
Description=Stonedt Campus Portal
After=network.target mysql.service redis.service

[Service]
Type=simple
User=deploy
WorkingDirectory=/opt/stonedt-portal
Environment="DB_URL=jdbc:mysql://localhost:3306/stonedt_portal?useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&serverTimezone=Asia/Shanghai&useSSL=false"
Environment="DB_USERNAME=stonedt_user"
Environment="DB_PASSWORD=your_secure_password"
Environment="TOKEN_PRIVATE_KEY=your-32-char-random-string-here!"
Environment="TIKHUB_API_KEY=your_tikhub_api_key"
Environment="PRELAUNCH_STRICT=1"
ExecStart=/usr/bin/java -jar /opt/stonedt-portal/stonedt-portal-0.5.3-SNAPSHOT.jar --spring.config.location=/opt/stonedt-portal/config/application.yml
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用并启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable stonedt-portal
sudo systemctl start stonedt-portal
sudo systemctl status stonedt-portal
```

---

### 2.3 前端部署

#### 2.3.1 构建前端

```bash
cd campus-web

# 安装依赖（首次或依赖更新时）
npm install

# 生产构建
npm run build
```

构建产物输出到 `campus-web/dist/` 目录。

> **Node.js 版本**：建议使用 Node.js 18 LTS 或 20 LTS。如果版本不匹配，可使用 `nvm`（Node Version Manager）切换版本。
>
> **npm 镜像**：如果 npm install 慢，可临时使用淘宝镜像：`npm install --registry=https://registry.npmmirror.com`。

#### 2.3.2 部署到 Nginx

```bash
# 创建前端目录
sudo mkdir -p /var/www/campus-web

# 复制构建产物
sudo cp -r campus-web/dist/* /var/www/campus-web/
```

---

### 2.4 Nginx 配置

#### 2.4.1 安装 Nginx

```bash
# CentOS / RHEL
sudo yum install -y nginx

# Ubuntu / Debian
sudo apt update && sudo apt install -y nginx

# 验证安装
nginx -v
```

#### 2.4.2 添加站点配置

```nginx
server {
    listen 80;
    server_name school.yuqing.com;

    # 前端静态文件
    location / {
        root /var/www/campus-web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /campus/ {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Cookie $http_cookie;
        proxy_pass_header Set-Cookie;
        proxy_http_version 1.1;
    }

    # 登录接口（GET 返回页面，POST 处理登录）
    location /login {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Cookie $http_cookie;
        proxy_pass_header Set-Cookie;
    }

    # 登出接口
    location /logout {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Cookie $http_cookie;
        proxy_pass_header Set-Cookie;
    }

    # 静态资源
    location /img/ {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

> **路径说明**：
> - `/campus/` 代理到后端 API（所有校园业务接口）
> - `/login` 和 `/logout` 代理到后端认证接口
> - `/img/` 代理静态图片资源

#### 2.4.3 启用站点

```bash
# 将配置写入 /etc/nginx/conf.d/campus-school.conf

# 测试配置
sudo nginx -t

# 重载配置
sudo nginx -s reload
```

#### 2.4.4 HTTPS 配置（推荐）

```nginx
server {
    listen 443 ssl http2;
    server_name school.yuqing.com;

    ssl_certificate     /etc/nginx/ssl/school.yuqing.com.pem;
    ssl_certificate_key /etc/nginx/ssl/school.yuqing.com.key;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;

    # 其余 location 配置同上
    location / {
        root /var/www/campus-web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /campus/ {
        proxy_pass http://127.0.0.1:8084;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Cookie $http_cookie;
        proxy_pass_header Set-Cookie;
        proxy_http_version 1.1;
    }

    location /login { proxy_pass http://127.0.0.1:8084; }
    location /logout { proxy_pass http://127.0.0.1:8084; }
    location /img/ { proxy_pass http://127.0.0.1:8084; }
}

# HTTP 自动跳转 HTTPS
server {
    listen 80;
    server_name school.yuqing.com;
    return 301 https://$host$request_uri;
}
```

---

## 3. 学校初始化配置

### 3.1 必填环境变量

| 环境变量 | 用途 | 要求 | 示例 |
| --- | --- | --- | --- |
| `TOKEN_PRIVATE_KEY` | JWT Token 签名密钥 | 32 位随机字符串，**不能使用演示值** | `export TOKEN_PRIVATE_KEY="xK9mP2qR5vB8nJ4hF7wL1cT3yA6sD0eG"` |
| `TIKHUB_API_KEY` | TikHub 数据接入 API 密钥 | 由 TikHub 平台分配 | `export TIKHUB_API_KEY="sk-xxxxxxxxxxxxxxxx"` |

```bash
# 在启动后端之前设置
export TOKEN_PRIVATE_KEY="your-32-char-random-string-here!"
export TIKHUB_API_KEY="your_tikhub_api_key_here"
```

> 生成随机密钥可使用：`openssl rand -base64 24`（输出前 32 位字符）。

### 3.2 推荐环境变量（安全加固）

| 环境变量 | 值 | 说明 |
| --- | --- | --- |
| `PRELAUNCH_STRICT` | `1` | 启用启动前严格校验：拒绝演示 token、拒绝 root/123456、检查旧服务状态 |
| `SPRINGDOC_API_DOCS_ENABLED` | `false` | 关闭 Swagger API 文档（生产环境建议关闭） |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | `false` | 关闭 Swagger UI |
| `KNIFE4J_ENABLED` | `false` | 关闭 Knife4j 增强文档 |

### 3.3 试运行前检查

请参见 `docs/campus-prelaunch-checklist.md` 完整检查清单，核心要点：

1. [ ] `TOKEN_PRIVATE_KEY` 已设置为非演示值（32 位以上随机字符串）
2. [ ] 数据库账号不使用 root/123456
3. [ ] `PRELAUNCH_STRICT=1` 已开启
4. [ ] API 文档接口已关闭
5. [ ] 旧定时任务（`LEGACY_SCHEDULE_*`）保持关闭
6. [ ] 旧 ES 和爬虫服务保持关闭
7. [ ] CORS 白名单已配置为实际前端地址

---

## 4. 学校运营配置

### 4.1 登录系统

访问 http://school.yuqing.com （或实际部署地址），使用管理员账号登录。

> 首次登录后请立即修改默认密码。默认演示账号 `13900000000` 仅用于演示环境，生产环境不应使用。

### 4.2 后台管理配置

路径：登录后进入 `/admin` 或 `/settings/` 相关页面。

#### 4.2.1 配置部门结构

根据学校实际组织架构配置部门（详见 `docs/campus-demo-data.md` 部门示例）：

| 部门 | 说明 |
| --- | --- |
| 网信办 | 舆情工作统筹 |
| 宣传部 | 对外宣传与舆情回应 |
| 学工部 | 学生相关舆情处置 |
| 保卫处 | 校园安全类舆情 |
| 后勤管理处 | 后勤服务类舆情 |
| 学院单位 | 各院系（按需配置） |

#### 4.2.2 创建用户并分配角色

| 角色 | 权限范围 | 适用对象 |
| --- | --- | --- |
| `campus_admin` | 全部权限，含系统管理 | 运维管理员 |
| `campus_operator` | 日常业务操作（监测、处置、报告） | 网信办/宣传部业务人员 |
| `campus_viewer` | 只读查看（工作台、态势、报告） | 校领导 |

#### 4.2.3 配置监测主题和关键词

在 `/monitor` 页面创建监测任务：

1. 填写任务名称和主体名称
2. 设置关键词（如学校名称、简称、相关事件词）
3. 配置负面词库
4. 选择监测平台范围
5. 设置扫描频率
6. 绑定接入数据源任务

#### 4.2.4 配置数据接入源

在 `/ingest` 页面配置数据接入：

| 接入方式 | 配置项 | 说明 |
| --- | --- | --- |
| TikHub API | `credentialRef` | 引用环境变量 `TIKHUB_API_KEY`，不明文存储密钥 |
| 公开网页采集 | 白名单 URL + 栏目路径 | 设置采集频率、robots 策略 |

> 真实数据接入前请完成 `docs/campus-acceptance-runbook.md` 第 5 节的确认事项。

### 4.3 启用自动调度（可选）

监测任务默认**不自动运行**。如需开启自动调度：

```bash
export SCHEDULE_CAMPUS_MONITOR_OPEN=1
```

> 在试运行期间，建议手动触发监测任务（在 `/monitor` 页面点击"立即运行"），待验证稳定后再启用自动调度。

### 4.4 验收确认

部署完成后，按照 `docs/campus-acceptance-runbook.md` 第 3 节的验收路径逐项确认：

1. [ ] `/situation` — 态势大屏可渲染
2. [ ] `/monitor` — 可创建和运行监测任务
3. [ ] `/ingest` — 媒体接入中心可查看
4. [ ] `/detection` — 检测主题和任务可查看
5. [ ] `/alerts` — 预警处理流程可用
6. [ ] `/clues` — 线索库可查看
7. [ ] `/events` — 事件处置流程可用
8. [ ] `/analysis` — 辅助研判可查看
9. [ ] `/reports` — 报告可生成和下载
10. [ ] `/settings/permissions` — 权限管理可访问

---

## 5. 常见问题

### 5.1 端口冲突

**现象**：后端启动报错 `Address already in use`。

**排查**：
```bash
# 查看端口占用
netstat -tlnp | grep 8084

# 找到占用进程并终止
lsof -i :8084
kill -9 <PID>
```

**解决**：修改 `config/application.yml` 中的 `server.port` 为其他端口（如 8085），并同步更新 Nginx 的 `proxy_pass` 地址。

### 5.2 数据库连接失败

**现象**：后端启动报错 `Cannot create PoolConnection` 或 `CommunicationsException`。

**排查**：
```bash
# 确认 MySQL 服务运行中
systemctl status mysql
# 或
service mysqld status

# 确认连接信息正确
mysql -u stonedt_user -p -h 127.0.0.1 stonedt_portal -e "SELECT 1"
```

**常见原因**：
- MySQL 服务未启动
- 数据库用户名或密码错误
- `DB_URL` 中的主机地址不是后端可访问的地址
- MySQL 未开启远程连接（后端与数据库同机部署通常为 `127.0.0.1`）
- 时区设置问题 — 检查 `serverTimezone=Asia/Shanghai`

### 5.3 Redis 连接失败

**现象**：后端报错 `Cannot connect to Redis` 或 `RedisConnectionException`。

**排查**：
```bash
# 确认 Redis 运行中
systemctl status redis
# 或
redis-cli ping  # 应返回 PONG
```

### 5.4 登录后白屏

**现象**：输入账号密码登录成功（URL 变化），但页面空白。

**排查步骤**：

1. 检查浏览器开发者工具 Network 标签，确认 `/campus/` 接口请求是否有 404 或 502
2. 检查 Nginx 是否正确代理了 `/campus/` 路径到后端
3. 确认前端构建产物是否正确部署到 Nginx 的 `root` 目录
4. 清除浏览器缓存和 Cookie 后重试
5. 检查 Nginx 错误日志：`tail -f /var/log/nginx/error.log`

**常见原因**：
- Nginx 配置中 `try_files` 没有 `$uri/ /index.html`，导致 Vue Router 历史模式刷新时 404
- 前端 API 代理路径与后端实际路径不匹配
- Cookie 未正确传递（检查 `proxy_set_header Cookie` 配置）

### 5.5 数据不显示

**现象**：页面加载成功，但列表、图表为空。

**排查**：
- 检查演示数据是否已成功导入：`SELECT COUNT(*) FROM campus_department;`
- 检查浏览器开发者工具 Network 标签，确认 API 请求是否返回 403
- 检查用户是否有所需角色的权限
- 确认 `TIKHUB_API_KEY` 是否配置正确
- 确认监测任务已经手动运行过（如未配置自动调度）

### 5.6 Flyway 迁移失败

**现象**：后端启动报 Flyway 相关错误。

**解决**：
```sql
-- 查看 Flyway 迁移记录
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;

-- 如果某条迁移标记为失败（success=0），排查后手动修复
-- 或删除失败的迁移记录后重启
DELETE FROM flyway_schema_history WHERE success = 0;
```

### 5.7 部署命令速查表

| 操作 | 命令 |
| --- | --- |
| 构建后端 | `./mvnw -DskipTests clean package` |
| 后端重启 | `sudo systemctl restart stonedt-portal` |
| 查看后端日志 | `journalctl -u stonedt-portal -f` 或 `tail -f app.log` |
| 构建前端 | `cd campus-web && npm install && npm run build` |
| Nginx 重载 | `sudo nginx -s reload` |
| 查看 Nginx 日志 | `tail -f /var/log/nginx/access.log /var/log/nginx/error.log` |

---

## 附录

### A. 目录结构参考

```
/opt/stonedt-portal/
├── stonedt-portal-0.5.3-SNAPSHOT.jar   # 后端可执行 JAR
├── config/
│   └── application.yml                  # 后端配置文件
└── app.log                              # 运行日志

/var/www/campus-web/
├── index.html                           # 前端入口
├── assets/                              # 静态资源
└── ...                                  # 其他构建产物

/etc/nginx/conf.d/
└── campus-school.conf                   # Nginx 站点配置
```

### B. 相关文档索引

| 文档 | 路径 | 说明 |
| --- | --- | --- |
| 前端运行手册 | `docs/campus-web-runbook.md` | 前端本地运行、页面清单 |
| 试运行检查清单 | `docs/campus-prelaunch-checklist.md` | 安全配置检查清单 |
| Demo 数据说明 | `docs/campus-demo-data.md` | 演示数据业务 ID 和边界说明 |
| 验收手册 | `docs/campus-acceptance-runbook.md` | 完整验收路径和配置检查 |
| 本地开发手册 | `docs/local-dev-runbook.md` | 本地启动、构建说明 |
| Demo 数据脚本 | `scripts/demo/seed-campus-demo-data.ps1` | 演示数据导入脚本（Windows） |
| Demo 数据 SQL | `scripts/demo/seed-campus-demo.sql` | 演示数据 SQL 文件 |

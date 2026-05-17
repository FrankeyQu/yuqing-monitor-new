# 校园舆情系统架构拓扑

## 1. 总体架构拓扑

```mermaid
flowchart TB
    subgraph U["用户侧"]
        U1["校领导"]
        U2["网信联络员"]
        U3["宣传部研判员"]
        U4["学工处/保卫处"]
        U5["学院处置员"]
        U6["审计员"]
    end

    subgraph A["访问入口"]
        A1["PC 管理端"]
        A2["校园舆情大屏"]
        A3["移动端/内网入口 可选"]
        A4["统一身份认证 可选"]
    end

    subgraph G["网关与安全层"]
        G1["Nginx/反向代理"]
        G2["HTTPS/TLS"]
        G3["登录认证"]
        G4["RBAC 权限控制"]
        G5["数据范围控制"]
    end

    subgraph S["校园舆情主系统 Spring Boot"]
        S1["首页工作台"]
        S2["线索库"]
        S3["重点关注账号库"]
        S4["舆情事件库"]
        S5["处置流转"]
        S6["预警中心"]
        S7["统计分析"]
        S8["报告归档"]
        S9["系统管理"]
        S10["审计日志"]
    end

    subgraph P["辅助服务层"]
        P1["数据接入服务 campus-ingest-service"]
        P2["智能分析服务 campus-analysis-service"]
        P3["报告生成服务 campus-report-service"]
        P4["定时任务服务"]
    end

    subgraph D["数据存储层"]
        D1["MySQL 业务库"]
        D2["Redis 缓存/会话"]
        D3["Elasticsearch 检索库 可选"]
        D4["MinIO/文件存储 可选"]
        D5["日志库/审计库"]
    end

    subgraph X["外部/上游数据源"]
        X1["人工录入"]
        X2["Excel 导入"]
        X3["上级移交数据"]
        X4["公开网页/公开平台"]
        X5["校内业务系统 可选"]
    end

    U1 --> A1
    U2 --> A1
    U3 --> A1
    U4 --> A1
    U5 --> A1
    U6 --> A1
    U1 --> A2

    A1 --> G1
    A2 --> G1
    A3 --> G1
    A4 --> G3
    G1 --> G2
    G2 --> G3
    G3 --> G4
    G4 --> G5
    G5 --> S

    X1 --> S2
    X2 --> S2
    X3 --> P1
    X4 --> P1
    X5 --> P1

    P1 --> S2
    P1 --> S3
    S --> P2
    P2 --> S6
    S --> P3
    P3 --> S8
    P4 --> S6

    S --> D1
    S --> D2
    S --> D3
    S --> D4
    S10 --> D5
    P1 --> D1
    P2 --> D1
    P3 --> D4
```

## 2. 部署拓扑

```mermaid
flowchart LR
    subgraph NET1["校园内网用户区"]
        C1["办公电脑浏览器"]
        C2["大屏展示终端"]
    end

    subgraph NET2["应用服务区"]
        N1["Nginx 网关"]
        APP1["Java 主系统 Zhuoran Insight"]
        APP2["大屏前端 large_screen"]
        APP3["数据接入服务 Python"]
        APP4["智能分析服务 Python"]
        APP5["报告生成服务 Python"]
    end

    subgraph NET3["数据服务区"]
        DB1["MySQL 主库"]
        DB2["Redis"]
        DB3["Elasticsearch 可选"]
        DB4["文件存储 MinIO/NAS"]
        DB5["日志/审计存储"]
    end

    subgraph NET4["外联/隔离区"]
        EXT1["上级数据接口"]
        EXT2["公开互联网采集出口 可选"]
        EXT3["模型 API/本地模型服务 可选"]
    end

    C1 -->|"HTTPS"| N1
    C2 -->|"HTTPS"| N1
    N1 --> APP1
    N1 --> APP2

    APP1 --> DB1
    APP1 --> DB2
    APP1 --> DB3
    APP1 --> DB4
    APP1 --> DB5

    APP1 --> APP3
    APP1 --> APP4
    APP1 --> APP5

    APP3 --> DB1
    APP4 --> DB1
    APP5 --> DB4

    APP3 --> EXT1
    APP3 --> EXT2
    APP4 --> EXT3
    APP5 --> EXT3
```

## 3. 业务模块拓扑

```mermaid
flowchart TD
    M0["工作台"] --> M1["线索库"]
    M0 --> M2["重点关注账号库"]
    M0 --> M3["舆情事件库"]
    M0 --> M4["预警中心"]
    M0 --> M5["统计分析"]

    M1 --> M6["线索研判"]
    M6 --> M7["线索归档"]
    M6 --> M3

    M2 --> M8["账号审核"]
    M8 --> M9["账号关注任务"]
    M9 --> M10["账号动态"]
    M10 --> M1
    M10 --> M4

    M3 --> M11["风险定级"]
    M11 --> M12["部门分派"]
    M12 --> M13["处置反馈"]
    M13 --> M14["复核确认"]
    M14 --> M15["报告归档"]

    M4 --> M11
    M5 --> M15

    M16["组织机构"] --> M12
    M17["用户权限"] --> M0
    M18["数据字典"] --> M1
    M18 --> M2
    M18 --> M3
    M18 --> M4
    M19["审计日志"] --> M0
    M19 --> M1
    M19 --> M2
    M19 --> M3
    M19 --> M15
```

## 4. 数据流拓扑

```mermaid
sequenceDiagram
    participant Source as 数据来源
    participant Ingest as 数据接入服务
    participant Main as Java 主系统
    participant DB as MySQL 业务库
    participant Analysis as 智能分析服务
    participant User as 研判人员
    participant Dispose as 处置部门
    participant Report as 报告服务
    participant Audit as 审计日志

    Source->>Main: 人工录入/Excel导入线索
    Source->>Ingest: 上级接口/公开数据接入
    Ingest->>Main: 标准化线索/账号动态
    Main->>DB: 写入线索库/账号库
    Main->>Audit: 记录导入和新增操作

    Main->>Analysis: 请求关键词命中/风险辅助判断
    Analysis->>Main: 返回情感倾向/风险建议/摘要
    Main->>DB: 保存辅助分析结果

    User->>Main: 人工研判
    Main->>DB: 创建或更新舆情事件
    Main->>Audit: 记录研判和定级

    Main->>Dispose: 分派处置任务
    Dispose->>Main: 提交处置反馈
    Main->>Audit: 记录分派和反馈

    User->>Main: 复核归档
    Main->>Report: 生成日报/周报/专报
    Report->>Main: 返回 HTML/PDF/Markdown
    Main->>DB: 保存报告元数据
    Main->>Audit: 记录报告生成和下载
```

## 5. 安全与审计拓扑

```mermaid
flowchart TB
    R1["用户登录"] --> R2["身份认证"]
    R2 --> R3["角色权限校验"]
    R3 --> R4["数据范围校验"]
    R4 --> R5["业务操作"]

    R5 --> O1["查看线索"]
    R5 --> O2["导入数据"]
    R5 --> O3["账号入库"]
    R5 --> O4["事件定级"]
    R5 --> O5["任务分派"]
    R5 --> O6["处置反馈"]
    R5 --> O7["报告导出"]
    R5 --> O8["权限变更"]

    O1 --> L["审计日志"]
    O2 --> L
    O3 --> L
    O4 --> L
    O5 --> L
    O6 --> L
    O7 --> L
    O8 --> L

    L --> Q1["操作人"]
    L --> Q2["操作时间"]
    L --> Q3["操作对象"]
    L --> Q4["操作类型"]
    L --> Q5["请求 IP"]
    L --> Q6["前后变更"]
    L --> Q7["任务编号/来源依据"]
```

## 6. 第一阶段 MVP 拓扑

第一阶段建议只建设如下闭环：

```mermaid
flowchart LR
    A["人工录入/Excel导入/上级移交"] --> B["线索库"]
    B --> C["人工研判"]
    C --> D["舆情事件库"]
    C --> E["重点关注账号库"]
    D --> F["部门分派"]
    F --> G["处置反馈"]
    G --> H["复核归档"]
    H --> I["报告导出"]

    J["组织机构"] --> F
    K["角色权限"] --> B
    K --> D
    K --> E
    L["审计日志"] --> B
    L --> C
    L --> D
    L --> E
    L --> I
```

## 7. 第二阶段增强拓扑

第二阶段再接入 BettaFish 思路：

```mermaid
flowchart TD
    A["校园舆情主系统"] --> B["campus-ingest-service"]
    A --> C["campus-analysis-service"]
    A --> D["campus-report-service"]

    B --> B1["参考 MindSpider"]
    B1 --> B2["话题任务"]
    B1 --> B3["平台适配"]
    B1 --> B4["采集状态"]

    C --> C1["参考 InsightEngine"]
    C --> C2["参考 SentimentAnalysisModel"]
    C1 --> C3["库内检索"]
    C1 --> C4["摘要生成"]
    C2 --> C5["情感倾向"]
    C2 --> C6["风险分类"]

    D --> D1["参考 ReportEngine"]
    D1 --> D2["报告模板"]
    D1 --> D3["HTML/PDF 渲染"]
    D1 --> D4["日报/周报/专报"]
```

## 8. 拓扑落地建议

- 主业务系统优先部署在校园内网应用区。
- 数据库、Redis、审计库放在数据服务区，禁止公网直接访问。
- 数据接入服务如需访问外网，建议放在外联隔离区或通过受控代理出口。
- 智能分析和报告服务可以先内网部署，后期再决定是否接入外部模型 API。
- 所有敏感操作必须走 Java 主系统统一鉴权和审计。
- 大屏只读展示，不承载新增、删除、导出等高风险操作。

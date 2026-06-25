# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

国内 WMS（仓库管理系统），从 0 到 1 构建。前后端分离，后端采用 DDD（领域驱动设计）六边形架构。

| 项目 | 技术栈 | 说明 |
|------|--------|------|
| `wms-server` | Java 21 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis | DDD 多模块后端 |
| `wms-web` | Vue 3 + Vite + Element Plus + Pinia + TypeScript | PC 管理后台 |
| `wms-pda` | UniApp (Vue 3) + uView Plus | 手持终端 APK |

## Repository Structure

```
ml-wms/
├── wms-server/                # 后端父工程 (Maven 多模块)
│   ├── wms-common/            # 公共模块：基础实体、异常、工具类、DTO
│   ├── wms-masterdata/        # 基础数据域：仓库/库区/库位/SKU/货主
│   ├── wms-inbound/           # 入库域：ASN→收货→验收→上架
│   ├── wms-outbound/          # 出库域：订单→波次→拣货→复核→发货
│   ├── wms-inventory/         # 库存域：查询/移动/盘点/冻结/批次
│   ├── wms-strategy/          # 策略域：上架/分配/波次/拣货策略
│   ├── wms-task/              # 任务域：生成/分配/执行/取消
│   ├── wms-print/             # 打印域：模板/打印记录
│   └── wms-web/               # Web 入口：Spring Boot 启动、全局配置、聚合
├── wms-web/                   # PC 前端 (Vue 3)
└── wms-pda/                   # PDA 前端 (UniApp)
```

## DDD Architecture (per bounded context module)

Each business module follows this four-layer architecture:

```
<module>/src/main/java/com/wms/<domain>/
├── interfaces/          # 接口适配层（入站）
│   ├── rest/            # REST Controller (对外 API)
│   ├── event/           # 事件监听器 (MQ/Spring Event)
│   └── cmd/             # CLI / 定时任务
├── application/         # 应用服务层（用例编排）
│   ├── service/         # AppService：编排领域服务，事务边界
│   ├── dto/             # DTO：外部入参对象（与领域对象隔离）
│   └── assembler/       # DTO ⇄ Domain 转换器
├── domain/              # 领域层（核心业务逻辑，零外部依赖）
│   ├── entity/          # 聚合根、实体、值对象
│   ├── service/         # DomainService：跨聚合根的领域逻辑
│   └── repository/      # 仓储接口（定义，不实现）
└── infrastructure/      # 基础设施层（出站）
    ├── repository/      # 仓储实现（MyBatis Mapper 实现）
    └── mapper/          # MyBatis XML Mapper
```

**Key rules:**
- 领域层不依赖任何框架/基础设施代码（纯 Java，无 Spring 注解）
- Repository 接口定义在 `domain/repository`，实现在 `infrastructure/repository`
- Controller 只做参数校验和响应组装，不写业务逻辑
- AppService 编排领域服务，标注 `@Transactional`，一个用例一个方法
- 模块间通过事件异步通信（Spring Event 或 MQ），避免直接调用

## WMS Domain Model (Key Aggregates)

```
基础数据域:  Warehouse → Area → Location
             Owner → SKU → SKUPackage → LotAttribute

入库域:     AsnHeader → AsnLine
             ReceiveHeader → ReceiveLine
             PutawayHeader → PutawayLine

出库域:     OrderHeader → OrderLine
             WaveHeader → WaveLine
             PickHeader → PickLine
             CheckHeader → CheckLine

库存域:     Inventory (quantity, location, lot, sn)
             StockSnapshot → StockTransaction (审计日志)

策略域:     StrategyConfig → StrategyRule (condition → action)
             上架策略 · 分配策略 · 波次策略 · 拣货策略

任务域:     TaskHeader → TaskLine
             AssignRule → ExecStatus (pending→executing→done→cancelled)
```

## Build Commands

### Backend (wms-server)

```bash
# 编译所有模块
cd wms-server && mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -pl wms-masterdata -Dtest=WarehouseServiceTest

# 打包（跳过测试）
mvn clean package -DskipTests

# 启动（开发环境，从 wms-web 模块）
mvn spring-boot:run -pl wms-web

# 启动时指定环境
mvn spring-boot:run -pl wms-web -Dspring-boot.run.profiles=dev

# 代码生成（MyBatis-Plus Generator，仅首次/表结构变更）
mvn compile -pl wms-common exec:java -Dexec.mainClass="com.wms.common.generator.CodeGenerator"
```

### Web Frontend (wms-web)

```bash
cd wms-web
npm install                      # 安装依赖
npm run dev                      # 启动开发服务器 (localhost:5173)
npm run build                    # 生产构建
npm run test                     # 运行测试
npm run lint                     # ESLint 检查
```

### PDA Frontend (wms-pda)

```bash
cd wms-pda
npm install
npm run dev:mp-weixin            # 微信小程序调试
npm run dev:android              # Android 标准基座调试
npm run build:android            # Android 打包
```

## 修复任务验证流程

使用 `/验证`（或 `/verify`）skill。Claude 会自动检测改动范围，按分层策略验证（基础设施 → 增量编译 → API 检查 → 浏览器 UI），只通过后才邀请用户检查。详见 `.claude/skills/verify/SKILL.md`。

## API Conventions

- REST 路径格式：`/api/v1/{domain}/{resource}`
  - 例：`GET /api/v1/inbound/asn/{id}`、`POST /api/v1/outbound/wave/batch-create`
- 统一响应体：`com.wms.common.base.ApiResponse<T>`
  ```json
  { "code": 200, "message": "success", "data": {}, "traceId": "uuid" }
  ```
- 分页请求：`PageRequest`（pageNum, pageSize, sortField, sortOrder）
- 分页响应：`PageResponse<T>`（records, total, pageNum, pageSize）
- 异常统一由 `@RestControllerAdvice` 拦截，返回 `ApiResponse` 格式
- 请求入参校验使用 JSR-303 Bean Validation（`@Valid` + 注解）
- 登录态使用 JWT token，`Authorization: Bearer <token>`

## Database Conventions

- 表名：`wms_<domain>_<entity>`（例：`wms_masterdata_warehouse`、`wms_inbound_asn_header`）
- 主键：统一使用雪花 ID（`Long id`），MyBatis-Plus `IdType.ASSIGN_ID`
- 每表必有字段：`created_at`、`updated_at`、`created_by`、`updated_by`、`version`（乐观锁）、`is_deleted`（逻辑删除）
- 租户隔离：`tenant_id`（多租户物理字段），所有查询必须包含
- 逻辑删除：MyBatis-Plus `@TableLogic`，字段 `is_deleted`
- 枚举：数据库存 `varchar`，Java 使用 `enum` + MyBatis `TypeHandler` 转换
- 金额用 `BigDecimal`，精度到分（`DECIMAL(18,4)`）
- 数量用 `BigDecimal`（`DECIMAL(18,4)`），整数用 `Integer`
- **改表必须同步 init.sql**：任何 `ALTER TABLE` 或表结构变更，必须同步更新 `wms-server/wms-web/src/main/resources/db/init.sql`。init.sql 是唯一的数据源建表脚本，新增环境或重建数据库都依赖它。

## Key Development Rules

1. **跨模块通信**：Domain/Application 层不允许直接注入其他模块的 Service 或 Repository。跨模块调用通过以下方式：
   - **Port/Adapter 模式**：在自己的 domain 层定义 Gateway 接口（端口），infrastructure 层实现 Adapter 调用其他模块的 ApplicationService（出站适配器）。这是同步调用的首选方式。
   - **Spring Events / MQ**：适用于异步解耦场景（如"上架完成后通知任务域创建任务"），不适用于需要同步返回值的查询或命令。
   - **禁止**：任何模块的 domain 或 application 层直接 import 其他模块的 domain/repository/*。只有 infrastructure 层的 Gateway Adapter 可以调用其他模块的 ApplicationService。
2. **事务边界**：事务标注在 AppService 方法上，不允许在 Controller 或 DomainService 上开事务
3. **领域对象隔离**：Controller 接收的 DTO 必须在应用层通过 Assembler 转换为领域对象，领域对象不暴露到接口层
4. **PDA 接口**：PDA 专用 API 路径加 `/pda/` 前缀（如 `/api/v1/pda/pick/task-list`），因为 PDA 接口的数据结构和分页方式与 PC 不同
5. **扫码处理**：PDA 扫码结果统一为 `ScanResult{BarcodeType, BarcodeValue, ParsedData}`，由业务组件处理
6. **库存变更**：所有库存变更必须写 `stock_transaction` 审计日志表
7. **并发控制**：库存扣减等并发关键操作使用乐观锁（version 字段），失败重试或使用 Redis 分布式锁

## Getting Started for New Developers

1. Clone 仓库
2. 创建 MySQL 数据库 `ml_wms`，执行 `wms-server/wms-web/src/main/resources/db/init.sql`
3. 配置 `application-dev.yml` 中的数据源连接信息
4. 启动后端：`cd wms-server && mvn spring-boot:run -pl wms-web`
5. 启动前端：`cd wms-web && npm install && npm run dev`
6. 访问 `http://localhost:5173`，默认管理员 admin/admin123

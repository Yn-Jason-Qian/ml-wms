# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

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

### DDD 分层约束（强制性）

参照 `wms-masterdata` 模块，每层严格遵守以下注入和职责边界：

#### 1. interfaces/rest/ — Controller

```
只注入：AppService
禁止注入：Mapper、Repository、DomainService、Assembler、Entity
```

- 只做参数校验（`@Valid`）和响应组装（`ApiResponse.ok()`）
- 调用 AppService 方法获取已转换好的 DTO，**绝不**调用 `assembler.toDTO()`
- 分页响应从 `IPage<XxxDTO>` 提取 `records/total/current/size` 组装 `PageResponse`
- 不写任何业务逻辑，不直接访问数据库

#### 2. application/service/ — AppService

```
注入：Repository（domain 接口） + Mapper（仅分页） + DomainService + Assembler
```

- **读操作（无事务）**：通过 `repository.findById()` 查询 → `assembler.toDTO(entity)` 返回 DTO
- **分页查询**：直接使用 `xxxMapper.selectPage(page, wrapper)` → `result.convert(assembler::toDTO)` 返回 `IPage<XxxDTO>`。Mapper 仅在分页场景直接使用，因为 MyBatis-Plus 的 `LambdaQueryWrapper` 动态条件难以抽象到 Repository 接口
- **写操作（@Transactional）**：创建实体 → 注入上下文（tenantId/userId）→ domainService 校验 → repository 持久化 → assembler 转换返回
- **子实体加载**：Controller 需要 lines 时，AppService 负责加载并组装（先查 header → 再查 lines → 转换 → setLines）
- AppService 方法返回 DTO，**绝不**返回 Entity 或 `Map<String, Object>`

#### 3. application/assembler/ — Assembler

```
@Component 零外部依赖，纯 Java 映射
禁止注入：Mapper、Repository、AppService、DomainService
```

- `toDTO(Entity)` / `toLineDTO(LineEntity)` — 实体 → DTO（一对一字段拷贝）
- `toDTO(Header, List<Line>)` — 带行的 DTO 转换（纯数据组装，不查数据库）
- `toEntity(CreateCmd)` — Cmd → 新建实体
- `mergeToEntity(UpdateCmd, Entity)` — 合并更新字段到已有实体
- 只做字段拷贝和默认值设置，不包含业务规则、不访问数据库

#### 4. domain/repository/ — Repository 接口

```
纯 Java 接口，无 Spring 注解
定义方法：findById, findByIds, save, update, deleteById, existsBy*, findBy*
返回值：Optional<Entity>（单条）、List<Entity>（多条）
```

- 定义领域需要的持久化契约，不依赖任何 ORM 框架
- **不定义分页方法**（分页在 AppService 通过 Mapper 直接处理）

#### 5. domain/service/ — DomainService

```
注入：仅 Repository 接口（同域的）
禁止注入：Mapper、Assembler、其他模块的 Service
```

- `validateCreate(entity)` / `validateUpdate(entity)` — 业务规则校验（唯一性、状态机等）
- 跨聚合根的领域逻辑编排
- **不标注 @Transactional**（事务由 AppService 管理）

#### 6. infrastructure/repository/ — Repository 实现

```
@Repository，implements domain Repository 接口
注入：仅对应的 Mapper
```

- 每个方法委托到 MyBatis-Plus Mapper（`mapper.selectById`, `mapper.insert`, `mapper.updateById` 等）

#### 7. infrastructure/mapper/ — MyBatis Mapper

```
@Mapper，extends BaseMapper<Entity>
```

- MyBatis-Plus 数据访问接口，继承 `selectPage`, `selectList`, `insert`, `updateById` 等方法

#### 分层依赖图（自顶向下）

```
Controller  →  AppService  →  Repository(接口) + Mapper(仅分页) + DomainService + Assembler
                                   ↑
DomainService  →  Repository(接口)     Assembler (零依赖，纯映射)
                       ↑
              RepositoryImpl  →  Mapper  →  DB
```

#### 禁止事项清单

| 层 | ❌ 禁止 |
|---|---|
| Controller | 注入 Mapper、Repository、Assembler；调用 `assembler.toDTO()`；写业务逻辑 |
| AppService | 返回 `Map<String, Object>` 或 `Object`；返回 Entity |
| Assembler | 注入 Mapper 查数据库；包含业务逻辑 |
| DomainService | 注入 Mapper、Assembler；标注 @Transactional |
| Repository(接口) | 依赖 Spring 注解；定义分页方法 |

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

使用 `/验证`（或 `/verify`）skill。Codex 会自动检测改动范围，按分层策略验证（基础设施 → 增量编译 → API 检查 → 浏览器 UI），只通过后才邀请用户检查。详见 `.Codex/skills/verify/SKILL.md`。

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

DDD 分层约束详见 [DDD 分层约束（强制性）](#ddd-分层约束强制性)。以下为跨域、PDA 及数据层面的补充规则：

1. **跨模块通信**：Domain/Application 层不允许直接注入其他模块的 Service 或 Repository。跨模块调用通过以下方式：
   - **Port/Adapter 模式**：在自己的 domain 层定义 Gateway 接口（端口），infrastructure 层实现 Adapter 调用其他模块的 ApplicationService（出站适配器）。这是同步调用的首选方式。
   - **Spring Events / MQ**：适用于异步解耦场景（如"上架完成后通知任务域创建任务"），不适用于需要同步返回值的查询或命令。
   - **禁止**：任何模块的 domain 或 application 层直接 import 其他模块的 domain/repository/*。只有 infrastructure 层的 Gateway Adapter 可以调用其他模块的 ApplicationService。
2. **PDA 接口**：PDA 专用 API 路径加 `/pda/` 前缀（如 `/api/v1/pda/pick/task-list`），因为 PDA 接口的数据结构和分页方式与 PC 不同
3. **扫码处理**：PDA 扫码结果统一为 `ScanResult{BarcodeType, BarcodeValue, ParsedData}`，由业务组件处理
4. **库存变更**：所有库存变更必须写 `stock_transaction` 审计日志表
5. **并发控制**：库存扣减等并发关键操作使用乐观锁（version 字段），失败重试或使用 Redis 分布式锁

## Getting Started for New Developers

1. Clone 仓库
2. 创建 MySQL 数据库 `ml_wms`，执行 `wms-server/wms-web/src/main/resources/db/init.sql`
3. 配置 `application-dev.yml` 中的数据源连接信息
4. 启动后端：`cd wms-server && mvn spring-boot:run -pl wms-web`
5. 启动前端：`cd wms-web && npm install && npm run dev`
6. 访问 `http://localhost:5173`，默认管理员 admin/admin123

<p align="center">
  <h1 align="center">ML-WMS</h1>
  <p align="center">国内仓库管理系统（WMS），从 0 到 1 构建</p>
</p>

---

## 项目简介

面向仓储作业全流程的管理平台，覆盖基础数据、入库、出库、库存、策略、任务六大核心业务域。

| 端 | 技术栈 | 说明 |
|---|---|---|
| 🖥️ PC 管理后台 (`wms-web`) | Vue 3 + Vite + Element Plus + Pinia + TypeScript | 管理员日常操作入口 |
| ⚙️ 后端服务 (`wms-server`) | Java 21 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis | DDD 六边形架构 |
| 📱 PDA 手持终端 (`wms-pda`) | UniApp (Vue 3) + uView Plus | 仓库作业扫码操作 |

## 业务模块

| 域 | 核心流程 | 说明 |
|---|---|---|
| 基础数据 | 仓库 → 库区 → 库位 / 货主 → SKU → 包装 | 系统运转的元数据基础 |
| 入库 | ASN → 收货 → 验收 → 上架 | 预发货通知到货物入库全链路 |
| 出库 | 订单 → 波次 → 拣货 → 复核 → 发货 | 出库单到发货完成全链路 |
| 库存 | 查询 / 移动 / 盘点 / 冻结 / 批次 | 库存变动与审计追溯 |
| 策略 | 上架策略 · 分配策略 · 波次策略 · 拣货策略 | 可配置业务规则引擎 |
| 任务 | 生成 → 分配 → 执行 → 取消 | 仓库作业任务生命周期管理 |
| 打印 | 模板管理 / 打印记录 | 标签与单据打印 |

## 后端架构（DDD 六边形架构）

每个业务模块遵循四层架构，依赖方向由外向内：**interfaces → application → domain**，domain 为核心零外部依赖。

```
<module>/src/main/java/com/wms/<domain>/
├── interfaces/          # 接口适配层 — REST Controller、事件监听、定时任务
├── application/         # 应用服务层 — 用例编排、DTO、Assembler 转换
├── domain/              # 领域层 — 聚合根、实体、值对象、领域服务、仓储接口
└── infrastructure/      # 基础设施层 — 仓储实现、MyBatis Mapper
```

**架构原则：**
- 领域层零外部依赖（纯 Java，无 Spring 注解）
- Controller 只做参数校验和响应组装，不写业务逻辑
- AppService 编排领域服务，`@Transactional` 事务边界
- 模块间通过 Spring Event / MQ 异步解耦，禁止直接注入 Service

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- Docker Compose（用于 MySQL 和 Redis）

### 1. 启动基础设施

```bash
docker-compose up -d
```

这会启动 MySQL 8.0（端口 3306）和 Redis 7（端口 6379），并自动执行数据库初始化脚本。

### 2. 启动后端

```bash
cd wms-server
# 编译（可选）
mvn clean compile

# 启动开发服务
mvn spring-boot:run -pl wms-web -Dspring-boot.run.profiles=dev
```

默认配置下后端运行在 `http://localhost:8080`，API 路径前缀 `/api/v1/`。

### 3. 启动 PC 前端

```bash
cd wms-web
npm install
npm run dev
```

开发服务器运行在 `http://localhost:5173`，默认管理员账号 `admin` / `admin123`。

### 4. 启动 PDA 前端（可选）

```bash
cd wms-pda
npm install
npm run dev:android    # Android 标准基座调试
npm run dev:mp-weixin  # 微信小程序调试
```

## API 约定

| 约定 | 说明 |
|---|---|
| 路径格式 | `{GET\|POST\|PUT\|DELETE} /api/v1/{domain}/{resource}` |
| 统一响应体 | `ApiResponse<T>`：`{ code: 200, message: "success", data: {}, traceId: "uuid" }` |
| 分页请求 | `PageRequest { pageNum, pageSize, sortField, sortOrder }` |
| 分页响应 | `PageResponse<T> { records, total, pageNum, pageSize }` |
| 参数校验 | JSR-303 Bean Validation（`@Valid`） |
| 异常处理 | `@RestControllerAdvice` 全局拦截，统一 `ApiResponse` 格式 |
| 认证方式 | JWT token — `Authorization: Bearer <token>` |
| PDA 接口 | 路径加 `/pda/` 前缀（如 `/api/v1/pda/pick/task-list`） |

## 数据库约定

- **表名**：`wms_<domain>_<entity>`（如 `wms_masterdata_warehouse`、`wms_inbound_asn_header`）
- **主键**：雪花 ID（`Long id`），MyBatis-Plus `IdType.ASSIGN_ID`
- **必备字段**：`created_at`、`updated_at`、`created_by`、`updated_by`、`version`（乐观锁）
- **多租户**：`tenant_id` 物理隔离字段
- **逻辑删除**：`is_deleted`（MyBatis-Plus `@TableLogic`）
- **枚举**：数据库存 `varchar`，Java 用 `enum` + MyBatis `TypeHandler` 转换
- **金额/数量**：`BigDecimal`，精度 `DECIMAL(18,4)`

## 项目结构

```
ml-wms/
├── wms-server/                      # 后端父工程 (Maven 多模块)
│   ├── wms-common/                  # 公共模块：基础实体、异常、工具类、DTO
│   ├── wms-masterdata/              # 基础数据域：仓库/库区/库位/SKU/货主
│   ├── wms-inbound/                 # 入库域：ASN→收货→验收→上架
│   ├── wms-outbound/                # 出库域：订单→波次→拣货→复核→发货
│   ├── wms-inventory/               # 库存域：查询/移动/盘点/冻结/批次
│   ├── wms-strategy/                # 策略域：上架/分配/波次/拣货策略
│   ├── wms-task/                    # 任务域：生成/分配/执行/取消
│   ├── wms-print/                   # 打印域：模板/打印记录
│   └── wms-web/                     # Web 入口：Spring Boot 启动、全局配置、聚合
├── wms-web/                         # PC 前端 (Vue 3)
├── wms-pda/                         # PDA 前端 (UniApp)
├── docker-compose.yml               # MySQL + Redis 基础设施
└── CLAUDE.md                        # AI 辅助开发指南
```

## 常用命令

### 后端

```bash
cd wms-server

# 编译
mvn clean compile

# 运行测试
mvn test

# 运行单个测试类
mvn test -pl wms-masterdata -Dtest=WarehouseServiceTest

# 打包（跳过测试）
mvn clean package -DskipTests

# 代码生成（MyBatis-Plus Generator）
mvn compile -pl wms-common exec:java -Dexec.mainClass="com.wms.common.generator.CodeGenerator"
```

### PC 前端

```bash
cd wms-web

npm install         # 安装依赖
npm run dev         # 启动开发服务器
npm run build       # 生产构建
npm run test        # 运行测试
npm run lint        # ESLint 检查
```

### PDA

```bash
cd wms-pda

npm install
npm run dev:android       # Android 调试
npm run build:android     # Android 打包
```

## 开发规范

1. **跨模块通信**：模块间不允许直接注入 Service，使用 Spring Events 或 MQ 解耦
2. **事务边界**：事务标注在 AppService 方法上，不在 Controller 或 DomainService 开事务
3. **领域对象隔离**：DTO 通过 Assembler 转换为领域对象，领域对象不暴露到接口层
4. **库存变更**：所有库存变更必须写 `stock_transaction` 审计日志表
5. **并发控制**：库存扣减使用乐观锁（`version` 字段），失败重试或使用 Redis 分布式锁
6. **扫码处理**：PDA 扫码结果统一为 `ScanResult{BarcodeType, BarcodeValue, ParsedData}`

## License

[MIT](LICENSE)

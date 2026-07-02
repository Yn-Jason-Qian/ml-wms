# CLAUDE.md

## Project Overview

国内 WMS（仓库管理系统），前后端分离，后端 DDD 六边形架构。

| 项目 | 技术栈 |
|------|--------|
| `wms-server` | Java 21 + Spring Boot 3 + MyBatis-Plus + MySQL 8 + Redis |
| `wms-web` | Vue 3 + Vite + Element Plus + Pinia + TypeScript |
| `wms-pda` | UniApp (Vue 3) + uView Plus |

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
│   └── wms-web/               # Web 入口：Spring Boot 启动、全局配置
├── wms-web/                   # PC 前端 (Vue 3)
└── wms-pda/                   # PDA 前端 (UniApp)
```

## DDD 分层架构

```
<module>/src/main/java/com/wms/<domain>/
├── interfaces/          # 接口适配层
│   ├── rest/            # REST Controller
│   ├── event/           # 事件监听器 (MQ/Spring Event)
│   └── cmd/             # CLI / 定时任务
├── application/         # 应用服务层
│   ├── service/         # AppService：编排领域服务，事务边界
│   ├── dto/             # DTO：外部入参对象
│   └── assembler/       # DTO ⇄ Domain 转换器
├── domain/              # 领域层（核心业务，零外部依赖）
│   ├── entity/          # 聚合根、实体、值对象
│   ├── service/         # DomainService：跨聚合根的领域逻辑
│   └── repository/      # 仓储接口（定义，不实现）
└── infrastructure/      # 基础设施层
    ├── repository/      # 仓储实现
    └── mapper/          # MyBatis Mapper
```

### 分层约束（强制性，参照 `wms-masterdata` 模块）

**依赖关系：**

```
Controller  →  AppService  →  Repository(接口) + Mapper(仅分页) + DomainService + Assembler
DomainService  →  Repository(接口)     Assembler (零依赖，纯映射)
              RepositoryImpl  →  Mapper  →  DB
```

**职责与禁止：**

| 层 | 注入 | ❌ 禁止 | 关键职责 |
|---|---|---|---|
| Controller | AppService | Mapper, Repository, DomainService, Assembler | 参数校验 + 响应组装；调用 AppService 获取已转好的 DTO |
| AppService | Repository, Mapper(分页), DomainService, Assembler | 返回 Entity / Map | 读: repository+assembler；分页: Mapper 直接查；写: @Transactional 编排；始终返回 DTO |
| Assembler | 无（纯 Java 映射） | Mapper, Repository, AppService | 只做字段拷贝和默认值，不查数据库、不含业务逻辑 |
| DomainService | Repository(同域) | Mapper, Assembler, 跨模块 Service | 业务规则校验；不标注 @Transactional |
| Repository(接口) | 无（纯接口，无 Spring 注解） | 定义分页方法 | findById/save/update/deleteById/existsBy*/findBy*；返回 Optional 或 List |
| RepositoryImpl | 对应 Mapper | — | 委托到 MyBatis-Plus Mapper |
| Mapper | — | — | `@Mapper extends BaseMapper<Entity>` |

## 跨模块通信

- **Port/Adapter（同步首选）**：domain 定义 Gateway 接口 → infrastructure 实现 Adapter 调用其他模块 AppService
- **Spring Events / MQ（异步）**：如"上架完成通知任务域创建任务"
- **禁止**：domain/application 层直接 import 其他模块的 `domain/repository/*`

## WMS Domain Model

```
基础数据:  Warehouse → Area → Location,  Owner → SKU → SKUPackage → LotAttribute
入库:      AsnHeader→AsnLine,  ReceiveHeader→ReceiveLine,  PutawayHeader→PutawayLine
出库:      OrderHeader→OrderLine,  WaveHeader→WaveLine,  PickHeader→PickLine,  CheckHeader→CheckLine
库存:      Inventory (quantity,location,lot,sn),  StockSnapshot → StockTransaction (审计日志)
策略:      StrategyConfig → StrategyRule (condition→action): 上架·分配·波次·拣货
任务:      TaskHeader→TaskLine,  AssignRule → ExecStatus (pending→executing→done→cancelled)
```

## Build Commands

```bash
# 后端
cd wms-server && mvn clean compile                           # 编译
mvn test -pl wms-masterdata -Dtest=XxxTest                   # 单测
mvn clean package -DskipTests                                # 打包
mvn spring-boot:run -pl wms-web                              # 启动
mvn spring-boot:run -pl wms-web -Dspring-boot.run.profiles=dev  # 指定环境

# 前端
cd wms-web && npm run dev                                    # PC (localhost:5173)
cd wms-pda && npm run dev:android                            # PDA
```

## API Conventions

- 路径: `/api/v1/{domain}/{resource}`，PDA 加 `/pda/` 前缀
- 响应: `ApiResponse<T>` → `{ code, message, data, traceId }`
- 分页: `PageRequest`(pageNum, pageSize, sortField, sortOrder) → `PageResponse<T>`(records, total, pageNum, pageSize)
- 校验: `@Valid` + JSR-303，异常由 `@RestControllerAdvice` 拦截
- 认证: JWT `Authorization: Bearer <token>`

## Database Conventions

- 表名: `wms_<domain>_<entity>`；主键: 雪花 ID `Long id` (`IdType.ASSIGN_ID`)
- 每表必有: `created_at`, `updated_at`, `created_by`, `updated_by`, `version`(乐观锁), `is_deleted`(逻辑删除 `@TableLogic`), `tenant_id`(租户隔离)
- 枚举: DB `varchar` ↔ Java `enum` (TypeHandler)
- 金额/数量: `BigDecimal(DECIMAL(18,4))`，整数: `Integer`
- **改表必须同步 `wms-server/wms-web/src/main/resources/db/init.sql`**（唯一建表脚本）

## 关键规则

1. **库存变更**必须写 `stock_transaction` 审计日志
2. **并发控制**：库存扣减用乐观锁 (version)，失败重试或 Redis 分布式锁
3. **PDA 扫码**：统一为 `ScanResult{BarcodeType, BarcodeValue, ParsedData}`
4. **代码生成**：MyBatis-Plus Generator 仅首次/表结构变更使用，日常开发手写

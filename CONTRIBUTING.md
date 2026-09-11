# 贡献指南

感谢你有兴趣参与 WMS 项目！本文档说明如何搭建环境、提交改动，以及需要遵守的约定。

## 开始之前

- 先读 [README.md](README.md)，了解项目定位、技术栈与快速启动方式
- 后端是 DDD 六边形架构，**分层约束是强制性的**，动手前请读 [AGENTS.md](AGENTS.md) 的「DDD 分层约束」
- 部署相关内容见 [deploy/README.md](deploy/README.md)

## 环境要求

| 组件 | 版本 |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker | 用于本地起 MySQL / Redis |

## 本地开发

```bash
# 1. 起基础设施（只需 MySQL + Redis）
docker compose up -d mysql redis

# 2. 后端
cd wms-server
mvn spring-boot:run -pl wms-web -Dspring-boot.run.profiles=dev

# 3. PC 前端
cd wms-web && npm install && npm run dev     # http://localhost:5173
```

默认管理员账号 `admin` / `admin123`。数据库首次启动会自动执行
`wms-server/wms-web/src/main/resources/db/init.sql`。

## 提交前必须通过

```bash
# 后端: 编译 + 单元测试 + 代码格式校验（与 CI 完全一致）
cd wms-server && mvn install

# PC 前端: 类型检查 + 构建
cd wms-web && npm run build
```

CI（GitHub Actions）会在 PR 上运行同样的检查，本地先跑一遍能省一次往返。

涉及页面的改动，建议按 [Web 全页面功能检查清单](docs/web-verification-checklist.md) 手工过一遍再提交。

## 代码规范

### 后端

- 严格遵守四层分层：`interfaces` → `application` → `domain` → `infrastructure`
  - Controller 只注入 AppService；AppService 返回 DTO，绝不返回 Entity 或 `Map<String, Object>`
  - Assembler 零依赖，只做字段拷贝；DomainService 不注入 Mapper、不标 `@Transactional`
- 跨模块调用只能通过 `domain/gateway` 端口 + `infrastructure` 适配器，或 Spring Event
- 代码格式由 `fmt-maven-plugin`（google-java-format）强制校验，提交前执行：

  ```bash
  cd wms-server && mvn com.spotify.fmt:fmt-maven-plugin:format
  ```

- 新增业务逻辑请补单元测试（`src/test/java`，命名 `XxxTest`）

### 前端

- Vue 3 `<script setup>` + TypeScript，组件放 `src/views/<domain>/`，接口封装放 `src/api/<domain>/`
- 提交前执行 `npm run lint`（wms-web）

## 数据库变更

**任何表结构变更都必须同步 `wms-server/wms-web/src/main/resources/db/init.sql`** ——
它是唯一的建表脚本，新环境与重建数据库都依赖它。已经存在的库需要手工执行对应的 `ALTER TABLE`。

## 提交与 PR

- 分支命名：`feat/xxx`、`fix/xxx`、`docs/xxx`
- Commit message 格式：`<type>(<scope>): <描述>`
  - type：`feat` / `fix` / `refactor` / `chore` / `docs` / `test` / `style`
  - 例：`fix(ci): 部署改为单个 tar 包传输`
- 一个 PR 只做一件事，描述里写清楚：
  1. 改了什么 2. 为什么改 3. 怎么验证的
- 涉及 API 变更时，请同步更新 README 或相关文档

## 报告问题

提交 issue 时请附上：复现步骤、期望行为、实际行为、环境信息（JDK / Node 版本、是否用 Docker）。
能附上日志或截图更好。

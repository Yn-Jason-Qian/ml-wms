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
├── wms-server/    # 后端 (Java 21 + Spring Boot 3)，约定见 wms-server/SERVER-CONVENTIONS.md
├── wms-web/       # PC 前端 (Vue 3)，约定见 wms-web/WEB-CONVENTIONS.md
└── wms-pda/       # PDA 前端 (UniApp)
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

## 领域约定文件（按需加载）

| 文件 | 何时 Read |
|------|-----------|
| `wms-server/SERVER-CONVENTIONS.md` | 修改后端代码（模块结构、DDD 分层、跨模块通信、Domain Model、DB 规范、关键规则、构建命令） |
| `wms-web/WEB-CONVENTIONS.md` | 修改前端代码（目录结构、API 模块、命名约定、导入规范） |

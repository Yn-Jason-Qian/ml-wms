---
name: verify
description: Verify code changes in this WMS project. Triggered by user saying "验证", "verify", "测试", "检查", "检查一下", or after completing a fix/feature. Automatically detects change type (frontend/backend/both) and runs the fastest appropriate verification — infrastructure health → incremental compile → API check → browser UI check.
---

# 验证技能

对 WMS 项目改动进行分层验证。优先用最快的方式，避免每次都等 Docker 全量构建。

## 检测改动范围

先判断改动类型：

```
前端改动: wms-web/src/** 有变更
后端改动: wms-server/** 有变更
基础设施: docker-compose.yml 或 init.sql 有变更
```

用 `git diff --name-only HEAD` 或 `git status --short` 判断。

## 验证流程

> **Docker 只用于运行 MySQL 和 Redis 等基础组件。后端和前端始终用 Maven/npm 直接启动，不过 Docker。**

### 第一层：基础设施（仅首次或变更时）

```bash
# 只启动 MySQL + Redis，不启动后端/前端容器
docker compose up -d mysql redis
docker compose ps mysql redis  # 确认 both healthy
```

如果 infra 无变更且已 `healthy`，跳过。
如果改了 init.sql，需手动执行新 SQL：
```bash
docker exec wms-mysql mysql -uroot -proot ml_wms < <sql_file>
```

### 第二层：后端改动验证

```bash
cd wms-server
# 增量编译，只编译有改动的模块及依赖（秒级）
mvn compile -pl <changed-module> -am -q
# 安装到本地 Maven 仓库（必须！否则 spring-boot:run 会加载 ~/.m2 里的旧 JAR）
mvn install -DskipTests -q
# 直接启动，连 Docker 里的 MySQL/Redis（不使用 Docker 运行后端）
mvn spring-boot:run -pl wms-web -Dspring-boot.run.profiles=dev
```

确认日志 `Started WmsApplication`，然后用 curl 快速检查接口：

```bash
# 先登录拿 token
TOKEN=$(curl -s 'http://localhost:8080/api/v1/auth/login' -X POST \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

# 验证分页接口
curl -s 'http://localhost:8080/api/v1/masterdata/skus/page' \
  -X POST -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"pageNum":1,"pageSize":10}'
# 确认返回 code:200
```

### 第三层：前端改动验证

```bash
cd wms-web && npm run dev   # http://localhost:5173, HMR 秒级生效
```

然后浏览器检查：
- 页面正常渲染、数据加载
- 控制台无报错
- 涉及的表单/弹窗功能正常

## 必做检查清单

无论改动大小，验证通过标准：

- [ ] 编译通过：前端 `vue-tsc && vite build` 无报错，后端 `mvn compile` 通过
- [ ] 接口正确：涉及 API 返回 200，数据结构正确
- [ ] 页面正确：涉及页面正常渲染，控制台无报错
- [ ] 容器健康：`docker compose ps mysql redis` 均为 healthy
- [ ] 后端启动：`Started WmsApplication` 无异常日志

以上全部通过后才邀请用户检查。

## Chrome DevTools 注意事项

使用 Chrome DevTools MCP 做浏览器验证时：
- `fill` 工具对 Element Plus `el-select` 组件不可靠，优先用键盘导航（`press_key` ArrowDown/Enter）
- 避免 `evaluate_script` 读取 localStorage/sessionStorage（会触发安全拦截）
- 复杂表单优先用 `fill_form`，单字段用 `fill`

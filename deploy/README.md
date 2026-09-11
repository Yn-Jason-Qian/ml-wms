# 部署说明（tencent-test）

## 架构

```
tencent-test (124.222.189.203)
├── mysql8      ← 已有容器（另一份 compose 管理），本部署只复用，不接管
├── redis7      ← 已有容器（另一份 compose 管理），本部署只复用，不接管
├── wms-server  ← 本部署管理（Spring Boot，端口 8080）
└── wms-web     ← 本部署管理（Nginx，端口 80；反代 /api 与 /ws-stomp 到 wms-server）
```

- 后端连接串指向 `mysql8:3306/ml_wms`，Redis 指向 `redis7:6379`
- 四个容器在同一个 docker 网络内（`mysql8` / `redis7` 所在的那个 compose 网络），可直接用容器名互访
- 前端同样以容器方式部署：服务器上没有宿主机 Nginx，容器方案可以直接复用仓库里已有的
  `nginx.conf`（API/WebSocket 反代 + SPA fallback），并与后端作为单一发布单元一起 `up -d`

## 网络说明

`mysql8` / `redis7` 由服务器上那份 compose 管理，网络名由 compose 自动生成，通常是
**`<compose项目名>_backend`**（例如在 `/opt/mysql` 目录下执行就是 `mysql_backend`），并不是简单的 `backend`。

`deploy.sh` 会自动探测：

```sh
docker inspect -f '{{range $k,$v := .NetworkSettings.Networks}}{{println $k}}{{end}}' mysql8
```

探测不到时才会退化为创建/使用 `backend` 网络。想手动确认，可执行上面的命令。

## 首次部署

正常情况下由 Jenkins 的 `Deploy to tencent-test` 阶段自动完成。等价的手工步骤：

```bash
ssh root@124.222.189.203
mkdir -p /opt/wms/server /opt/wms/web
# 确认基础设施容器在线
docker ps --format '{{.Names}}\t{{.Status}}' | grep -E 'mysql8|redis7'
# 提前拉基础镜像，减少首次构建时间
docker pull eclipse-temurin:21-jre-alpine
docker pull nginx:1.27-alpine
```

然后把 Jenkins 构建产物与 `deploy/` 目录传到 `/opt/wms`，执行：

```bash
sh /opt/wms/deploy.sh
```

## 数据库

`deploy.sh` 会检查 `ml_wms` 库是否存在，**不存在时才**导入 `init.sql`（`init.sql` 自带
`CREATE DATABASE IF NOT EXISTS`），已存在则跳过，避免误改线上表结构。

> 项目约定：任何表结构变更都必须同步 `wms-server/wms-web/src/main/resources/db/init.sql`。
> 已存在的库需要手工执行对应的 `ALTER TABLE`。

## 数据库口令

默认使用 `root/root`。要覆盖的话，在 `/opt/wms/.env` 中写：

```
MYSQL_USER=root
MYSQL_PASSWORD=你的密码
```

`deploy.sh` 与 `docker compose` 都会自动读取该文件。

## 排查

```bash
docker logs wms-server --tail 100
docker logs wms-web --tail 50
curl -I http://localhost/doc.html     # 后端接口文档
curl -I http://localhost/             # 前端
```

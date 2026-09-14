# 部署说明

本目录是一份**自托管部署的参考实现**：把后端与前端构建成两个容器，复用服务器上已有的
MySQL / Redis，由 CI 构建产物后推送到目标服务器。

> 文中出现的具体值（容器名 `mysql8` / `redis7`、库名 `ml_wms`、目录 `/opt/wms`）
> **都只是默认值**，可按下方的「可配置项」替换成你自己的环境。

## 架构

```
<你的服务器>
├── <mysql 容器>    ← 已存在，本部署只复用，不接管
├── <redis 容器>    ← 已存在，本部署只复用，不接管
├── wms-server     ← 本部署管理（Spring Boot，宿主端口 8080）
└── wms-web        ← 本部署管理（Nginx，宿主端口 80；反代 /api 与 /ws-stomp 到 wms-server）
```

- 后端按**容器名**连 MySQL / Redis（同一 docker 网络内可直接解析容器名）
- 四个容器处在同一个 docker 网络内（即已有 mysql/redis 所在的那张网），网络名由 `deploy.sh` 自动探测
- 前端同样以容器方式部署：直接复用本目录的 `nginx.conf`（API/WebSocket 反代 + SPA fallback），
  与后端一起 `up -d`，作为单一发布单元

## 可配置项

全部通过**部署目录下的 `.env`** 覆盖，模板见 [`.env.example`](.env.example)：

```bash
cp /opt/wms/.env.example /opt/wms/.env && vi /opt/wms/.env
```

| 变量 | 默认值 | 说明 |
|---|---|---|
| `WMS_DB_CONTAINER` | `mysql8` | 已存在的 MySQL 容器名 |
| `WMS_REDIS_CONTAINER` | `redis7` | 已存在的 Redis 容器名 |
| `WMS_DB_HOST` | 取上面的容器名 | 后端连接用的主机名（数据库不在 docker 网络内时改这里） |
| `WMS_DB_PORT` | `3306` | |
| `WMS_DB_NAME` | `ml_wms` | 数据库名 |
| `WMS_REDIS_HOST` | 取上面的容器名 | |
| `WMS_REDIS_PORT` | `6379` | |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / `root` | 数据库口令 |
| `WMS_NET` | 自动探测 | Docker 网络名，一般不需要设置 |

**最常见的改动**：如果你的 mysql / redis 容器不叫 `mysql8` / `redis7`，只需要改前两个变量。
`deploy.sh` 与 `docker compose` 读的是同一个 `.env`，改一处即可。

## 网络是怎么找到的

不写死网络名，而是从 mysql 容器反查它实际加入的那张网：

```sh
docker inspect -f '{{range $k,$v := .NetworkSettings.Networks}}{{println $k}}{{end}}' "$WMS_DB_CONTAINER"
```

这是因为 compose 生成的网络名是 `<项目名>_backend` 这种形式（取决于 compose 文件所在目录），
并不等于 compose 文件里写的 `backend`。探测不到时才退化为创建/使用 `backend` 网络。

想手动确认，在服务器上执行上面的命令即可。

## Jenkins 侧配置

流水线与仓库里**不含任何主机名、IP、口令**，这些都在 Jenkins 上配，共两处。

### 1. 全局环境变量（必配，否则部署阶段会被跳过）

Manage Jenkins → System → **Global properties** → ☑ Environment variables：

| 名称 | 值 | 说明 |
|---|---|---|
| `WMS_DEPLOY_HOST` | 下一步配置的 SSH 主机名，例如 `my-wms-test` | **为空时 `Deploy` 阶段自动跳过** |
| `WMS_DEPLOY_HOME` | 远端部署目录，例如 `/opt/wms` | 不配则默认 `/opt/wms` |

这样别人 fork 仓库后不做任何配置也能跑通构建（只跳过部署），想部署时自己填这两个变量即可。

### 2. Publish over SSH 主机

Manage Jenkins → Configure System → **Publish over SSH** → SSH Servers 新增一台：

| 字段 | 值 |
|---|---|
| Name | 与上面的 `WMS_DEPLOY_HOST` 保持一致 |
| Hostname / Username / Password（或 SSH Key） | 目标服务器 |
| Remote Directory | 建议填 `WMS_DEPLOY_HOME` 的值 |

**Remote Directory 留空也能部署**（流水线对落点做了兼容），但填了以后路径最直观。原因是 Publish over SSH 的路径语义：

- Remote Directory 留空时，所有远端路径都相对 SSH 用户家目录（root 即 `/root`）
- `remoteDirectory` 的前导 `/` 会被剥掉，`/opt/wms` 会变成 `opt/wms`
- `sourceFiles` 带目录前缀时，会在远端重现该层级（曾导致文件落到 `/root/opt/wms/deploy/...`）

因此流水线改为**只传一个 tar 包**，在远端解包，绕开上述所有歧义。

## 首次部署

正常情况下由 Jenkins 的 `Deploy` 阶段自动完成：工作区组装 `wms-deploy.tar.gz`
→ SFTP 传输 → 远端 `tar xzf` 到部署目录 → 执行 `deploy.sh`。

首次部署前建议做的准备：

```bash
ssh root@<your-server-ip>

# 1. 确认基础设施容器在线，并记下它们的实际名字
docker ps --format '{{.Names}}\t{{.Image}}\t{{.Status}}'

# 2. 容器名不是默认值时，写进部署目录的 .env
cp /opt/wms/.env.example /opt/wms/.env   # 部署过一次之后该文件已存在
vi /opt/wms/.env

# 3. 提前拉基础镜像，减少首次构建时间
docker pull eclipse-temurin:21-jre-alpine
docker pull nginx:1.27-alpine
```

手工部署等价于把发布包解到部署目录后执行：

```bash
mkdir -p /opt/wms && tar xzf wms-deploy.tar.gz -C /opt/wms
sh /opt/wms/deploy.sh
```

## 数据库

`deploy.sh` 会检查 `WMS_DB_NAME`（默认 `ml_wms`）对应的库是否存在，**不存在时才**导入
`init.sql`（`init.sql` 自带 `CREATE DATABASE IF NOT EXISTS`），已存在则跳过，避免误改线上表结构。
导入用的 `mysql` 客户端命令会显式带上 `--default-character-set=utf8mb4`。

> 项目约定：任何表结构变更都必须同步 `wms-server/wms-web/src/main/resources/db/init.sql`。
> 已存在的库需要手工执行对应的 `ALTER TABLE`。

### 字符集（重要）

`mysql:8.0` 官方镜像里的 `mysql` 客户端默认字符集是 **latin1**（容器没有 LANG 环境变量）。
如果服务端没有设置 `--skip-character-set-client-handshake`，它会按 latin1 解释导入的 UTF-8 字节，
把中文写成**双重编码**（界面上显示成 `ç³»ç»Ÿç®¡ç†å‘˜` 这种乱码）。

`deploy.sh` 已经从源头规避（导入时显式指定 utf8mb4）。仍建议给 MySQL 加上该参数，彻底避免
任何客户端覆盖服务端字符集 —— 本仓库根目录的 `docker-compose.yml`（本地开发用）就是这么配的：

```yaml
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci
  - --skip-character-set-client-handshake   # 新增
```

万一库里已经写入了乱码数据，且确认没有需要保留的业务数据，重建即可：

```bash
. /opt/wms/.env   # 读取容器名与口令
docker exec "$WMS_DB_CONTAINER" mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
  --default-character-set=utf8mb4 -e "DROP DATABASE ${WMS_DB_NAME:-ml_wms}"
# 重新触发部署，deploy.sh 会用正确的字符集重新导入
```

## 排查

```bash
docker logs wms-server --tail 100
docker logs wms-web --tail 50
curl -I http://localhost/doc.html     # 后端接口文档
curl -I http://localhost/             # 前端
```

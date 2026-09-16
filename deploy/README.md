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

部署目录（默认 `/opt/wms`）里各文件的来历：

```
/opt/wms/
├── docker-compose.yml          ← CI 每次覆盖（编排定义）
├── deploy.sh / rollback.sh / lib.sh   ← CI 每次覆盖（部署与回滚脚本）
├── .env                        ← 运维自己维护，CI 不会覆盖（模板见 .env.example）
├── release.env                 ← CI 每次覆盖，记录本次发布的标识/commit/构建号
├── init.sql                    ← CI 每次覆盖（首次建库用）
├── .releases/                  ← 脚本维护：history（发布顺序）+ <发布标识>.env（各版本的镜像 tag）
├── server/  Dockerfile + app.jar + wms-web-<版本>.jar
└── web/     Dockerfile + nginx.conf + dist/
```

## 发布、回滚与健康检查

### 镜像 tag 与发布记录

每次发布都用独立 tag：`wms-server:b<构建号>-<短commit>`（`wms-web` 同理），**不再一律用 `latest`**，
所以旧镜像会留在目标服务器上，回滚不需要重新构建、也不需要重传 jar。

如果本次只改了前端，后端不会被重建，它继续沿用上一版发布记录的 tag —— 也就是说每次发布记录
存的是「后端用哪个 tag + 前端用哪个 tag」这一组，回滚时整组一起切回去。记录存在
`/opt/wms/.releases/`：

```
.releases/history            # 发布标识列表，第一行是当前版本，新 → 旧
.releases/<发布标识>.env      # 该版本实际使用的 WMS_SERVER_TAG / WMS_WEB_TAG（+ commit、时间）
```

保留条数由 `WMS_KEEP_RELEASES`（默认 10）控制，它同时也是可回滚的深度上限：超出范围的记录
连同它引用的镜像 tag 会被清理。`latest` 会被保留并始终指向当前版本，方便手工执行 compose。

### 回滚

```bash
sh /opt/wms/rollback.sh                 # 回退到上一个发布
sh /opt/wms/rollback.sh --list          # 列出可回滚的发布（* 为当前版本）
sh /opt/wms/rollback.sh --to b120-1a2b3c4   # 回退到指定发布
```

回滚只是把两个容器的镜像 tag 切回历史记录并重启容器（`docker compose up -d`，不带 `--build`），
所以通常几秒完成。发布顺序会被同步裁剪，连按两次 `rollback.sh` 会继续往回退，不会来回横跳。

### 健康检查与自动回滚

`deploy.sh` 在 `up -d` 之后会轮询探测宿主机上的发布端口（默认后端 `http://127.0.0.1:8080/doc.html`、
前端 `http://127.0.0.1/`，共等 120 秒），**探测失败就返回非 0**，Jenkins 直接红灯 ——
不会出现「容器起来了但接口打不开，流水线还是绿色」的情况。

失败时（默认行为）脚本会自动回滚到上一个发布，并打印回滚后的探测结果；构建历史不足
或上一个版本的镜像已被清理时会明确提示，不再自动回滚。加 `--no-rollback` 可以关掉自动回滚。
探测地址、次数、间隔都可以在 `.env` 里改（见 `.env.example`）。

`docker-compose.yml` 里也给两个容器配了 `healthcheck`，那个只用来让 `docker ps` 显示健康状态，
不影响发布成败判定 —— 即使镜像里没有 `wget` 导致 healthcheck 一直是 `unhealthy`，发布判定仍然
以宿主机侧探测为准（真遇到这种情况，用 `WMS_SERVER_HEALTH_CMD` 换成 curl 版本即可）。

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
| `WMS_KEEP_RELEASES` | `10` | 发布记录保留条数（同时是可回滚深度上限） |
| `WMS_KEEP_JARS` | `5` | 本地保留的 `wms-web-*.jar` 份数 |
| `WMS_ROLLBACK_ON_FAILURE` | `1` | 健康检查失败时是否自动回滚（`0` 只报错不回滚） |
| `WMS_SERVER_HEALTH_URL` / `WMS_WEB_HEALTH_URL` | `/doc.html` / `/` | 发布后探测的地址 |
| `WMS_HEALTH_TRIES` / `WMS_HEALTH_INTERVAL` | `40` / `3` | 探测次数 × 间隔秒数（默认共 120 秒） |
| `WMS_SERVER_HEALTH_CMD` / `WMS_WEB_HEALTH_CMD` | wget 探测 | 容器内 healthcheck 命令，一般不用改 |

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
（只放本次重建服务的产物 + 编排文件与脚本）→ SFTP 传输 → 远端 `tar xzf` 到部署目录
→ 执行 `deploy.sh --release <构建号>-<短commit> --components <server,web>`。

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

不带参数时默认重建 server + web，并用 `manual-<时间戳>` 作为发布标识。想只重建其中一个：

```bash
sh /opt/wms/deploy.sh --components web      # 只重建前端，后端容器不动
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

发布相关的状态与回滚：

```bash
cat /opt/wms/.releases/history          # 发布顺序（第一行是当前版本）
sh /opt/wms/rollback.sh --list          # 可回滚的版本一览
sh /opt/wms/rollback.sh                 # 回退到上一个版本
docker image ls 'wms-*'                 # 本地保留的各版本镜像
docker compose -p wms -f /opt/wms/docker-compose.yml ps   # 需要 WMS_NET/WMS_*_TAG 时套用 deploy.sh 的环境
```

磁盘占用超出预期时，先看 `WMS_KEEP_RELEASES` / `WMS_KEEP_JARS`：镜像由发布记录保护，
两份保留项调小即可（`deploy.sh` 每次发布都会按它们清理）。

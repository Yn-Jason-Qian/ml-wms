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

## Jenkins 主机配置（建议）

Manage Jenkins → Configure System → Publish over SSH → 主机 `tencent-test`，把 **Remote Directory** 设为：

```
/opt/wms
```

**不设也能部署**（流水线对落点做了兼容），但设了以后路径最直观。原因是 Publish over SSH 的路径语义：

- Remote Directory 留空时，所有远端路径都相对 SSH 用户家目录（root 即 `/root`）
- `remoteDirectory` 的前导 `/` 会被剥掉，`/opt/wms` 会变成 `opt/wms`
- `sourceFiles` 带目录前缀时，会在远端重现该层级（曾导致文件落到 `/root/opt/wms/deploy/...`）

因此流水线改为**只传一个 tar 包**，在远端解包，绕开上述所有歧义。

## 首次部署

正常情况下由 Jenkins 的 `Deploy to tencent-test` 阶段自动完成：工作区组装 `wms-deploy.tar.gz`
→ SFTP 传输 → 远端 `tar xzf` 到 `/opt/wms` → 执行 `deploy.sh`。

首次部署前建议先做的准备：

```bash
ssh root@124.222.189.203
# 确认基础设施容器在线
docker ps --format '{{.Names}}\t{{.Status}}' | grep -E 'mysql8|redis7'
# 提前拉基础镜像，减少首次构建时间
docker pull eclipse-temurin:21-jre-alpine
docker pull nginx:1.27-alpine
# 清理早期版本误传的目录（如果存在）
rm -rf /root/opt
```

手工部署等价于把发布包解到 `/opt/wms` 后执行：

```bash
mkdir -p /opt/wms && tar xzf wms-deploy.tar.gz -C /opt/wms
sh /opt/wms/deploy.sh
```

## 数据库

`deploy.sh` 会检查 `ml_wms` 库是否存在，**不存在时才**导入 `init.sql`（`init.sql` 自带
`CREATE DATABASE IF NOT EXISTS`），已存在则跳过，避免误改线上表结构。

> 项目约定：任何表结构变更都必须同步 `wms-server/wms-web/src/main/resources/db/init.sql`。
> 已存在的库需要手工执行对应的 `ALTER TABLE`。

### 字符集（重要）

`mysql:8.0` 官方镜像里的 `mysql` 客户端默认字符集是 **latin1**（容器没有 LANG 环境变量），
而服务器端点若未设置 `--skip-character-set-client-handshake`，就会按 latin1 解释导入的 UTF-8 字节，
把中文写成**双重编码**（界面上显示成 `ç³»ç»Ÿç®¡ç†å‘˜` 这种乱码）。

因此 `deploy.sh` 导入时显式带上 `--default-character-set=utf8mb4`。

若库里已经写入了乱码数据，且确认没有需要保留的业务数据，重建即可：

```bash
docker exec mysql8 mysql -uroot -proot -e "DROP DATABASE ml_wms"
# 重新触发部署，deploy.sh 会用正确的字符集重新导入
```

另外建议给服务器上的 mysql8 加上 `--skip-character-set-client-handshake`（项目自带的
`docker-compose.yml` 里就有），可从根本上避免客户端字符集覆盖服务器设置：

```yaml
command:
  - --character-set-server=utf8mb4
  - --collation-server=utf8mb4_unicode_ci
  - --skip-character-set-client-handshake   # 新增
```

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

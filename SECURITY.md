# 安全政策

## 报告漏洞

**请不要通过公开 issue 报告安全问题。**

请使用 GitHub 的私密漏洞报告：仓库页面 → **Security** → **Report a vulnerability**，
或发邮件到 `yn_qian@yeah.net`。

我们会在 5 个工作日内确认收到；修复发布后会公开致谢（如果你愿意署名）。

## 支持范围

只对 `master` 分支的最新代码提供安全修复。

## 部署安全须知（重要）

本仓库的默认配置面向**开发与演示**，直接暴露到公网前必须改掉以下几项：

| 项 | 默认值 | 位置 |
|---|---|---|
| 管理员账号 | `admin` / `admin123` | `init.sql` 种子数据 |
| 数据库账号 | `root` / `root` | 环境变量 `SPRING_DATASOURCE_*` |
| JWT 签名密钥 | `your-256-bit-secret-key-change-in-production` | 环境变量 `JWT_SECRET` |
| ID 混淆盐值 | `wms-hashid-salt-2026` | 环境变量 `HASHIDS_SALT` |

以上任何一项保持默认，都可能导致他人登录你的系统或伪造令牌。
可参考 `.env.example` 了解全部可配置项。

其他建议：

- 生产环境使用 HTTPS（在 Nginx 或云负载均衡上终止 TLS）
- 数据库与 Redis 端口不要暴露公网，只在内网 / Docker 网络内可达
- 定期检查 `wms_sys_operation_log`（操作日志）中的异常登录与写操作

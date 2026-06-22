# WMS 后续实施计划

> 2026-06-22 | 阶段一+二已完成 | 阶段三+四待实施

---

## 阶段三：PDA 前端（UniApp，约 2 周）

### 3.1 项目初始化

- [ ] HBuilder X 创建 UniApp Vue3 项目
- [ ] uView Plus 3.x 组件库引入
- [ ] 扫码组件封装（连续扫描/振动反馈/防重复）
- [ ] 网络状态监听（离线暂存 → 在线同步）
- [ ] Axios 封装（复用 wms-web 的 request.ts 模式）
- [ ] Pinia 持久化 + Token 管理

### 3.2 PDA 核心页面（8 个）

| 页面 | 路径建议 | 核心功能 |
|------|---------|---------|
| 登录 | `pages/login/index` | 账号密码 + 自动登录 + 仓库切换 |
| 首页 | `pages/home/index` | 今日任务统计（待收货/待上架/待拣货/待盘点） |
| 收货扫描 | `pages/inbound/receive` | 扫描ASN条码 → 扫描SKU → 输入数量/批次 → 确认收货 |
| 上架扫描 | `pages/inbound/putaway` | 扫描收货容器 → 扫描目标库位 → 扫描SKU → 确认上架 |
| 拣货扫描 | `pages/outbound/pick` | 领取任务 → 按路径显示 → 扫描库位 → 扫描SKU → 绑定容器 |
| 复核扫描 | `pages/outbound/check` | 扫描容器号 → 逐件扫描SKU → 系统比对差异提示 |
| 盘点扫描 | `pages/inventory/stocktake` | 领取盘点任务 → 扫描库位 → 逐项输入数量 → 差异标记 |
| 移库扫描 | `pages/inventory/move` | 扫描来源库位 → 扫描SKU → 输入数量 → 扫描目标库位 |

### 3.3 技术要点

- **扫码**: 复用 UniApp `scanCode` API，封装为 `useScanner()` composable
- **离线**: 使用 `uni.setStorage` 暂存，网络恢复后批量提交
- **API**: 所有 PDA 接口后端已就绪（`PUTAWAY/submit`、`PICK/submit` 等）
- **任务系统**: 调用 `POST /api/v1/tasks/{id}/claim|start|complete|cancel`

---

## 阶段四：集成与打磨（约 1 周）

### 4.1 WebSocket 实时推送

- [ ] Spring WebSocket 配置
- [ ] PDA 新任务推送通知
- [ ] Web 端任务状态实时更新（Dashboard）

### 4.2 打印对接

- [ ] ZPL 指令生成（标签模板 → ZPL 文本）
- [ ] 蓝牙打印机连接（PDA 端）
- [ ] 网络打印机（Web 端）
- [ ] 标签类型：收货标签 / 库位标签 / 发货标签 / 托盘标签

### 4.3 ECharts 图表

- [ ] 仪表盘作业趋势图（入库量/出库量/拣货效率 折线图）
- [ ] 库存周转率 / 库龄分析
- [ ] 效期预警可视化

### 4.4 Docker 部署

- [ ] `Dockerfile` 后端（Spring Boot + JRE 21）
- [ ] `Dockerfile` 前端（Nginx + dist 静态文件）
- [ ] `docker-compose.yml` 完整编排（MySQL + Redis + Server + Nginx）
- [ ] 环境变量管理（`application-prod.yml`）

### 4.5 补充优化

- [ ] 数据库索引优化（针对高频查询字段创建索引）
- [ ] Redis 缓存策略（字典数据、SKU 列表）
- [ ] 慢 SQL 监控（MyBatis-Plus 性能拦截器）
- [ ] 单元测试补充（Service 层、Controller 集成测试）
- [ ] 前端错误边界 + 全局 Loading

---

## 实施建议

```
当前进度 ████████████████████░░░░░░░░ 65%
                         ▲
第1步: 阶段三 PDA 前端（核心业务扫描页）
第2步: 阶段四 WebSocket + 打印
第3步: 阶段四 图表 + Docker 部署 + 优化
```

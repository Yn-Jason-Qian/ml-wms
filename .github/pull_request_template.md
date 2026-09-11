## 改了什么

<!-- 简要说明改动内容 -->

## 为什么

<!-- 背景或关联的 issue，例如 Closes #123 -->

## 怎么验证的

<!-- 本地跑了哪些命令、验证了哪些接口或页面 -->

## 自查清单

- [ ] `cd wms-server && mvn install` 通过（含单元测试与格式校验）
- [ ] `cd wms-web && npm run build` 通过
- [ ] 涉及表结构变更时，已同步 `wms-server/wms-web/src/main/resources/db/init.sql`
- [ ] 涉及 API 变更时，已更新相关文档
- [ ] 一个 PR 只做一件事

# 项目优化记录

## 2026-07-07

### 前端考试模块
- 修复 `Exam.vue` 中 Element Plus 图标导入错误：将不存在的 `Left` / `Right` 替换为 `ArrowLeft` / `ArrowRight`。
- 同步更新上一题/下一题按钮的图标引用，避免构建通过但运行时图标缺失。
- 验证：`npm run build` 已通过。

### 前端构建优化
- 在 `vite.config.js` 中增加 `manualChunks` 配置，将 `vue`、`element-plus`、`echarts`、`axios` 拆分为独立 vendor chunk。
- 结果：业务页面 chunk 更轻，第三方大包可独立缓存；构建仍提示 `element` / `charts` chunk 较大，这是依赖体积本身导致，后续可按需引入进一步优化。

### 后端验证
- 已尝试运行 `mvn test`。
- 当前本机 Maven 使用 Java 8：项目要求 Java 17，因此后端编译被环境阻塞，需切换 `JAVA_HOME` 到 JDK 17 后再验证。
### 前端认证模块
- 修复接口返回 401 时只清理 `localStorage`、未同步清空 Pinia 用户状态的问题。
- 现在统一调用 `userStore.logout()`，避免当前会话内路由守卫继续误判为已登录。
- 验证：`npm run build` 已通过。
### 前端考试倒计时
- 修复手动交卷失败后倒计时被重置为整场考试时长的问题。
- 现在提交失败时只恢复原剩余时间继续计时，避免网络异常导致考试被意外加时。
- 验证：`npm run build` 已通过。

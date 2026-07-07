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
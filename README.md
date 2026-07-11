# 在线考试系统 (Online Examination System)

基于 **Spring Boot 3 + Vue 3** 的前后端分离在线考试系统，覆盖题库管理、试卷组卷、在线考试、自动评分、成绩管理与数据分析等完整考试流程。系统内置管理员、教师、学生三类角色，适合作为课程设计、毕业设计、教学管理原型或二次开发基础。

## 项目特性

- **角色权限清晰**：管理员、教师、学生三类角色，前端路由与后端接口双重控制。
- **题库与分类管理**：支持单选题、多选题、判断题，支持分类、搜索、编辑、删除。
- **试卷组卷发布**：可创建试卷、设置时长和及格分、绑定题目、发布考试。
- **在线答题体验**：支持倒计时、题号导航、上一题/下一题、答案本地缓存、超时提交。
- **自动评分与详情**：客观题提交后自动评分，可查看每题答案、得分和解析。
- **成绩与数据分析**：支持成绩列表、排行、分布图、及格率、统计指标。
- **界面优化**：近期已升级为更精致的产品型后台 UI，登录页、工作台、导航和全局组件样式更统一。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Java 17, Spring Boot 3.2, Spring Security, JWT, MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3, Vite, Element Plus, Pinia, Vue Router, Axios, ECharts |
| 文档 | Knife4j / OpenAPI |
| 构建 | Maven, npm |

## 项目结构

```text
online-exam-clone/
├─ README.md
├─ 使用手册.md
├─ PRODUCT.md
├─ CHANGELOG.md
├─ online-exam-backend/
│  ├─ pom.xml
│  └─ src/main/
│     ├─ java/com/online/exam/
│     │  ├─ common/       # 统一响应、异常处理
│     │  ├─ config/       # Security、CORS、MyBatis-Plus 配置
│     │  ├─ controller/   # REST 接口
│     │  ├─ dto/          # 请求参数对象
│     │  ├─ entity/       # 数据库实体
│     │  ├─ mapper/       # MyBatis-Plus Mapper
│     │  ├─ security/     # JWT、用户认证、权限工具
│     │  ├─ service/      # 业务逻辑
│     │  └─ vo/           # 响应视图对象
│     └─ resources/
│        ├─ application.yml
│        └─ sql/
│           ├─ schema.sql # 建库建表脚本
│           └─ data.sql   # 初始化数据
└─ online-exam-frontend/
   ├─ package.json
   ├─ vite.config.js
   └─ src/
      ├─ api/             # 接口封装
      ├─ assets/          # 全局样式
      ├─ layout/          # 主布局
      ├─ router/          # 路由与权限守卫
      ├─ store/           # Pinia 状态
      ├─ utils/           # Axios 请求拦截
      └─ views/           # 页面模块
```

## 环境要求

| 环境 | 建议版本 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Node.js | 18+ |
| npm | 9+ |

## 快速启动

### 1. 初始化数据库

先确认本机 MySQL 服务已启动，然后在项目根目录执行：

```bash
mysql -u root -p < online-exam-backend/src/main/resources/sql/schema.sql
mysql -u root -p < online-exam-backend/src/main/resources/sql/data.sql
```

如果使用 Navicat、DataGrip 等工具，也可以依次打开并执行：

1. `online-exam-backend/src/main/resources/sql/schema.sql`
2. `online-exam-backend/src/main/resources/sql/data.sql`

### 2. 配置数据库连接

打开 `online-exam-backend/src/main/resources/application.yml`，根据本机 MySQL 修改账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_exam?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### 3. 启动后端

```bash
cd online-exam-backend
mvn spring-boot:run
```

后端默认地址：

- API 服务：`http://localhost:8080`
- 接口文档：`http://localhost:8080/doc.html`

### 4. 启动前端

```bash
cd online-exam-frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 测试账号

初始化数据中已内置以下账号，密码均为 `123456`。

| 用户名 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | 管理员 | 拥有题库、试卷、考试、成绩、统计等完整管理权限 |
| `teacher` | 教师 | 可管理题库、组卷发布、查看成绩与统计分析 |
| `student1` | 学生 | 可参加考试、查看个人成绩与答题详情 |
| `student2` | 学生 | 同上 |
| `student3` | 学生 | 同上 |

登录页提供测试账号快捷填充按钮，可直接点击使用。

## 功能模块

### 管理员 / 教师

- **工作台**：查看题目、试卷、发布考试、成绩记录等概览数据。
- **题库管理**：新增、编辑、删除题目；维护分类；按题型、分类、关键字筛选。
- **试卷管理**：创建试卷、编辑基础信息、组卷、发布、删除。
- **成绩管理**：查看全部成绩、按试卷筛选、查看排行榜和答题详情。
- **数据分析**：查看成绩分布、及格率、平均分、最高分、最低分、TOP10 排行。

### 学生

- **工作台**：查看可参加考试、已考次数、快捷入口。
- **在线考试**：进入已发布试卷，完成答题并提交。
- **成绩管理**：查看个人考试记录、得分、是否及格、每题答案与解析。

## 核心接口说明

所有业务接口默认以 `/api` 为前缀，前端通过 Vite 代理到后端 `http://localhost:8080`。

| 模块 | 主要路径 | 说明 |
| --- | --- | --- |
| 认证 | `/api/auth/login`, `/api/auth/register` | 登录、注册 |
| 题库 | `/api/question`, `/api/category` | 题目与分类管理 |
| 试卷 | `/api/paper`, `/api/paper/published` | 试卷管理、学生可见试卷 |
| 考试 | `/api/exam/start`, `/api/exam/submit`, `/api/exam/detail/{recordId}` | 开始考试、提交、答题详情 |
| 成绩 | `/api/score/mine`, `/api/score/all`, `/api/score/ranking` | 我的成绩、全部成绩、排行 |
| 统计 | `/api/stats` | 成绩统计与图表数据 |

## 数据表概览

| 表名 | 说明 |
| --- | --- |
| `user` | 用户与角色 |
| `category` | 题目分类 |
| `question` | 题目、选项、答案、解析、分值 |
| `paper` | 试卷基础信息 |
| `paper_question` | 试卷与题目关联 |
| `exam_record` | 考试记录、开始和结束时间、状态 |
| `exam_answer` | 每题作答记录、得分、是否正确 |
| `score` | 成绩汇总记录 |

## 验证命令

后端编译 / 测试：

```bash
cd online-exam-backend
mvn test
```

前端生产构建：

```bash
cd online-exam-frontend
npm run build
```

说明：当前前端构建可能会出现 Element Plus / ECharts 分包较大的提示，这是依赖体积警告，不影响运行。

## 常见问题

### 1. 登录提示“密码错误”怎么办？

确认数据库中的初始化账号密码哈希是否是最新版。如果数据库是在密码修复前导入的，可重新导入 `data.sql`，或执行以下 SQL：

```sql
USE online_exam;

UPDATE `user`
SET `password` = '$2a$10$EUJKwxg.NW6WBkqW5PwaKeRRlh.xhyCMhVgdXrXpuc2Y.XXewrXQW'
WHERE `username` IN ('admin', 'teacher', 'student1', 'student2', 'student3');
```

### 2. 前端提示“网络异常”怎么办？

检查后端是否已启动在 `http://localhost:8080`，并确认前端 `vite.config.js` 中 `/api` 代理地址正确。

### 3. 后端启动失败，提示数据库连接错误怎么办？

检查：

- MySQL 服务是否启动。
- `online_exam` 数据库是否已创建。
- `application.yml` 中的数据库账号密码是否正确。
- MySQL 端口是否为 `3306`。

### 4. 管理员/教师提示“无权限”怎么办？

请重新登录以刷新 JWT。如果数据库里角色值被手动修改过，确保 `user.role` 为 `ADMIN`、`TEACHER` 或 `STUDENT`。

## 版本记录

详细优化记录见 [CHANGELOG.md](CHANGELOG.md)。

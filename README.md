# 在线考试系统 (Online Examination System)

基于 **Spring Boot + Vue 3** 的前后端分离在线考试系统，实现题库管理、试卷组卷、在线考试、自动评分、成绩管理与数据可视化等完整闭环。

## 一、技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.2、Spring Security、JWT、MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3、Vite、Element Plus、ECharts、Pinia、Vue Router、Axios |
| 其他 | Maven、Knife4j (接口文档) |

## 二、项目结构

```
E:\桌面\java\
├── 在线考试.md                  # 需求文档
├── README.md                    # 本说明文件
├── online-exam-backend\         # 后端工程（Spring Boot）
│   ├── pom.xml
│   └── src\main\
│       ├── java\com\online\exam\
│       │   ├── OnlineExamApplication.java
│       │   ├── common\          # 统一响应、异常处理
│       │   ├── config\          # Security、CORS、MyBatis-Plus 配置
│       │   ├── security\        # JWT 工具、过滤器、用户认证
│       │   ├── entity\          # 8 张表实体
│       │   ├── mapper\          # MyBatis-Plus Mapper
│       │   ├── dto\             # 请求参数对象
│       │   ├── vo\              # 响应视图对象
│       │   ├── service\         # 业务逻辑（含自动评分）
│       │   └── controller\      # 7 个 REST 控制器
│       └── resources\
│           ├── application.yml
│           └── sql\
│               ├── schema.sql   # 建表脚本
│               └── data.sql     # 初始化数据
└── online-exam-frontend\        # 前端工程（Vue 3）
    ├── package.json
    ├── vite.config.js
    └── src\
        ├── api\                 # 接口封装
        ├── utils\               # axios 请求拦截
        ├── store\               # Pinia 状态
        ├── router\              # 路由 + 权限守卫
        ├── layout\              # 主布局
        ├── assets\              # 全局样式
        └── views\               # 页面（登录/首页/题库/试卷/考试/成绩/统计）
```

## 三、环境准备

### 1. 必备环境
- **JDK 17+**（推荐 17）
- **Maven 3.6+**
- **MySQL 8.0**
- **Node.js 16+**（推荐 18+）
- **npm** 或 **pnpm**

### 2. 初始化数据库
登录 MySQL，依次执行：

```bash
# 1. 建库 + 建表
mysql -u root -p < online-exam-backend/src/main/resources/sql/schema.sql

# 2. 导入初始数据
mysql -u root -p < online-exam-backend/src/main/resources/sql/data.sql
```

或使用 Navicat 等工具打开两个 SQL 文件依次执行。

### 3. 配置数据库连接
修改 `online-exam-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_exam?...
    username: root
    password: 你的MySQL密码   # ← 修改这里
```

## 四、启动项目

### 1. 启动后端
```bash
cd online-exam-backend
mvn spring-boot:run
# 后端启动在 http://localhost:8080
```

启动成功后可访问接口文档：http://localhost:8080/doc.html

### 2. 启动前端
```bash
cd online-exam-frontend
npm install        # 安装依赖（首次）
npm run dev        # 启动开发服务器
# 前端启动在 http://localhost:5173
```

浏览器自动打开 http://localhost:5173 即可使用。

## 五、测试账号

初始化数据已内置以下账号（**密码统一为 `123456`**）：

| 用户名 | 角色 | 说明 |
|--------|------|------|
| `admin` | 管理员 | 拥有全部权限 |
| `teacher` | 教师 | 题库/试卷/成绩/统计管理 |
| `student1` `student2` `student3` | 学生 | 参加考试、查看成绩 |

登录页提供一键填充按钮，可直接点击使用测试账号。

## 六、功能说明

### 管理员 / 教师
- **题库管理**：题目录入（单选/多选/判断）、分类管理、修改删除、按题型/分类/关键字筛选
- **试卷管理**：创建试卷、设置时长与及格分、组卷（多选题目）、发布试卷
- **成绩管理**：查看全部成绩、成绩排行榜
- **数据分析**：成绩分布柱状图、及格率饼图、平均分/最高分/最低分统计、TOP10 排行榜

### 学生
- **在线考试**：查看可参加考试、答题界面（倒计时、题号导航、上一题/下一题）、答案本地缓存
- **成绩查询**：查看历次成绩、点击查看答题详情（含正确答案与解析）

### 核心特性
- ✅ **JWT 认证**：无状态登录，Token 24 小时有效
- ✅ **RBAC 权限**：管理员/教师/学生三种角色，接口级与页面级双重控制
- ✅ **自动评分**：提交即判分（单选直接比对、多选排序后比对、判断题字符比对）
- ✅ **超时校验**：前端倒计时 + 后端二次校验考试时长
- ✅ **答题缓存**：考试过程答案实时保存到 localStorage，防止刷新丢失
- ✅ **数据可视化**：ECharts 实现成绩分布、及格率统计

## 七、数据库表说明

| 表名 | 说明 |
|------|------|
| `user` | 用户（含角色） |
| `category` | 题目分类 |
| `question` | 题目（选项存 JSON） |
| `paper` | 试卷 |
| `paper_question` | 试卷-题目关联（组卷） |
| `exam_record` | 考试记录（开始/结束时间、状态） |
| `exam_answer` | 答题明细（支撑答题详情查看） |
| `score` | 成绩 |

## 八、常见问题

**Q: 后端启动报数据库连接失败？**
A: 检查 `application.yml` 中数据库地址、用户名、密码是否正确，确认 MySQL 服务已启动。

**Q: 前端登录提示"网络异常"？**
A: 确认后端已启动（8080 端口），前端通过 Vite 代理 `/api` 到后端。

**Q: 想修改 JWT 密钥？**
A: 修改 `application.yml` 中 `jwt.secret`（需≥64字符）。

---

> 本系统适用于课程设计与毕业设计展示，结构清晰，功能完整，具有较强的实用性与扩展性。

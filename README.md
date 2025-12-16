# 📚 智能学习管理系统

> 一个集任务管理、学习打卡、AI 教练、成就系统于一体的现代化学习助手 🚀

## ✨ 项目简介

还在为学习计划混乱而烦恼吗？还在为学习进度无法追踪而焦虑吗？**智能学习管理系统**来拯救你啦！🎉

这是一个基于 **Spring Boot 3 + Vue 3 + TypeScript + MySQL** 打造的现代化学习管理平台，不仅帮你管理学习任务，还能用 AI 为你制定学习计划，更有成就系统激励你持续学习！

### 🎯 核心特性

- 🏠 **智能主页**：一目了然的学习统计，圆形进度图展示今日任务和总任务完成率
- 📝 **今日任务管理**：灵活设置每日学习目标，勾选即完成，自动同步学习时长
- 📚 **学习任务管理**：支持分类、搜索、分页，任务进度实时追踪
- ✅ **已完成任务**：自动归档已完成任务，查看学习成果
- 🤖 **AI 学习教练**：基于 DeepSeek 大模型，智能生成学习建议和任务拆分计划
- 🏆 **学习成就系统**：多种成就徽章，激励持续学习
- 📊 **本周学习统计**：每 7 天自动清零，追踪每周学习时长
- 🎨 **现代化 UI**：Element Plus 组件库，响应式设计，美观易用

---

## 🛠️ 技术栈

### 后端
- **框架**：Spring Boot 3.2.0
- **ORM**：MyBatis
- **数据库**：MySQL 8.x
- **API 文档**：SpringDoc OpenAPI (Swagger)
- **AI 集成**：DeepSeek API

### 前端
- **框架**：Vue 3 + TypeScript
- **UI 组件**：Element Plus
- **状态管理**：Vuex
- **路由**：Vue Router
- **构建工具**：Vite
- **HTTP 客户端**：Axios

---

## 📋 环境要求

- **JDK**：17.0.11 或更高版本
- **Maven**：3.9.11 或更高版本
- **Node.js**：18+ 版本
- **MySQL**：8.x 版本
- **IDE**：IntelliJ IDEA（推荐）或其他 Java IDE
- **数据库工具**：IntelliJ IDEA 数据库插件（或 Navicat、DBeaver 等）

---

## 🚀 快速开始

### 第一步：数据库准备

1. **创建数据库**
   ```sql
   CREATE DATABASE aistudy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **执行建表脚本**
   - 使用 IntelliJ IDEA 的数据库插件连接 MySQL
   - 打开 `db/schema.sql` 文件
   - 在数据库工具中执行 SQL 脚本，创建三张表：
     - `user`：用户表
     - `study_task`：学习任务表
     - `study_record`：学习记录表

   或者直接在 IDEA 的数据库工具中手动创建表（参考表结构见下方）。

3. **表结构说明**

   **user 表**：
   - `id`：用户ID（主键，自增）
   - `username`：用户名（唯一）
   - `password`：密码（MD5加密）
   - `nickname`：昵称
   - `role`：角色（ADMIN/USER）
   - `create_time`：创建时间

   **study_task 表**：
   - `id`：任务ID（主键，自增）
   - `user_id`：用户ID（外键）
   - `title`：任务标题
   - `description`：任务描述
   - `target_hours`：目标学习时长（小时）
   - `status`：状态（TODO/DOING/DONE）
   - `category`：学习分类
   - `deadline`：截止日期
   - `create_time`：创建时间

   **study_record 表**：
   - `id`：记录ID（主键，自增）
   - `task_id`：任务ID（外键）
   - `study_date`：学习日期
   - `duration_minutes`：学习时长（分钟）
   - `comment`：备注
   - `create_time`：创建时间

### 第二步：后端配置与启动

1. **打开项目**
   - 在 IntelliJ IDEA 中打开 `backend` 目录

2. **配置数据库连接**
   - 编辑 `backend/src/main/resources/application.yml`
   - 修改数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/aistudy?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: root          # 改为你的 MySQL 用户名
       password: 111111        # 改为你的 MySQL 密码
   ```

3. **配置 DeepSeek API**（可选，AI 功能需要）
   ```yaml
   deepseek:
     api-key: sk-your-api-key-here  # 替换为你的 DeepSeek API Key
     base-url: https://api.deepseek.com/chat/completions
     model: deepseek-chat
   ```

4. **启动后端**
   - 运行 `com.ai.study.AiStudyApplication` 主类
   - 看到 "Started AiStudyApplication" 表示启动成功
   - 默认端口：`8080`
   - Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 第三步：前端配置与启动

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

3. **访问系统**
   - 浏览器打开 `http://localhost:5173`（或终端显示的地址）
   - 看到登录页面即表示启动成功

### 第四步：登录系统

- **默认管理员账号**（如果执行了 schema.sql 中的测试数据）：
  - 用户名：`admin`
  - 密码：`123456`

- **或注册新账号**：点击登录页的"立即注册"链接

---

## 🎮 功能详解

### 🏠 主页（Dashboard）

- **顶部统计卡片**：
  - 今日任务完成数 / 总数（点击跳转到今日任务页）
  - 学习任务数（未完成，点击跳转到学习任务页）
  - 已完成任务数（点击跳转到已完成任务页）
  - 累计学习时长（分钟）

- **学习统计**：
  - 今日任务进度：圆形进度图，显示今日完成情况
  - 总任务完成率：圆形进度图，显示所有任务的完成情况
  - 详细统计：已完成任务数、未完成任务数、本周学习时长
  - AI 学习建议按钮（醒目的大按钮）

- **今日任务预览**：
  - 显示今日目标时长和已完成时长
  - 列表展示今日任务（可勾选完成）
  - 自动同步到"今日任务"页面

- **学习成就**：
  - 今日任务完成：完成当日学习目标
  - 连续打卡 7 天：坚持打卡 7 天
  - 任务达人：完成 80% 以上任务

### 📝 今日任务

- **设置今日目标**：可设置今日总目标学习时长（分钟）
- **任务列表**：
  - 显示任务标题、描述、总目标、今日设定时长
  - 勾选即完成，自动计入学习时长
  - 支持删除任务
  - 自动同步到主页和本周学习时长

### 📚 学习任务

- **任务管理**：
  - 支持新增、编辑、删除任务
  - 任务分类管理（如：Java、数据库、英语等）
  - 任务状态：TODO / DOING / DONE

- **搜索与筛选**：
  - 关键字搜索（按标题）
  - 分类筛选（显示所有分类，支持滚动）
  - 分页显示

- **任务进度**：
  - 实时显示任务完成进度条
  - 自动计算已学习时长
  - 进度达到 100% 时自动标记为 DONE

- **加入今日任务**：
  - 一键将学习任务加入今日任务
  - 可设置今日学习时长（分钟）

### ✅ 已完成任务

- 自动显示所有状态为 DONE 的任务
- 显示任务分类、目标时长、完成情况、截止日期、完成时间
- 只读展示，不可编辑或删除

### 🤖 AI 学习教练

- **学习建议**：
  - 基于学习记录生成个性化学习建议
  - 支持重新生成和清除

- **任务拆分计划**：
  - 输入学习目标描述
  - AI 自动生成任务拆分计划
  - 支持一键创建任务（自动提取分类）
  - 保留历史记录

### 🏆 学习成就

- **成就类型**：
  - 今日任务完成
  - 连续打卡 7 天
  - 任务达人（完成 80% 以上任务）
  - 首次任务（完成 1 个任务）
  - 五任务成就（完成 5 个任务）
  - 专注日（今日学习 120 分钟以上）
  - 学习马拉松（累计学习 300 分钟以上）

- **成就展示**：
  - 已解锁成就显示绿色边框和"已解锁"标签
  - 未解锁成就显示灰色边框和"待解锁"标签
  - 点击已解锁成就可查看详情

---

## 🔧 配置说明

### 后端配置（application.yml）

```yaml
server:
  port: 8080
  tomcat:
    connection-timeout: 120000  # AI 请求超时时间

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aistudy?...
    username: root
    password: your_password

deepseek:
  api-key: your-api-key
  base-url: https://api.deepseek.com/chat/completions
  model: deepseek-chat
```

### 前端配置（vite.config.ts）

- 代理配置：`/api` 开头的请求自动代理到 `http://localhost:8080`
- 端口：默认 `5173`（可修改）

---

## 📁 项目结构

```
AIstudy1/
├── backend/                 # 后端项目
│   ├── src/main/java/
│   │   └── com/ai/study/
│   │       ├── AiStudyApplication.java    # 启动类
│   │       ├── controller/                # 控制器
│   │       ├── service/                   # 服务层
│   │       ├── mapper/                    # MyBatis Mapper
│   │       ├── domain/                    # 实体类
│   │       ├── dto/                       # 数据传输对象
│   │       └── config/                   # 配置类
│   └── src/main/resources/
│       ├── application.yml               # 配置文件
│       └── mapper/                       # MyBatis XML
├── frontend/               # 前端项目
│   ├── src/
│   │   ├── views/                         # 页面组件
│   │   ├── api/                          # API 接口
│   │   ├── router/                       # 路由配置
│   │   ├── store/                        # 状态管理
│   │   └── types/                        # TypeScript 类型
│   └── public/                           # 静态资源
│       ├── bg.jpg                        # 登录页背景图
│       └── favicon.ico                   # 网站图标
├── db/
│   └── schema.sql                        # 数据库建表脚本
└── README.md                             # 项目文档
```

---

## 🎨 特色功能

### 1. 智能任务同步
- 今日任务与学习任务双向同步
- 勾选完成自动更新学习时长
- 进度达到 100% 自动标记完成

### 2. 本周学习统计
- 每 7 天自动清零
- 实时追踪本周学习时长
- 跨页面数据同步

### 3. AI 智能分类
- 从学习目标自动提取分类关键词
- 创建任务时自动填入分类
- 支持分类筛选和搜索

### 4. 成就激励系统
- 多种成就类型
- 实时同步成就状态
- 可视化成就展示

---

## 🐛 常见问题

### Q: 后端启动失败，提示数据库连接错误？
A: 检查 `application.yml` 中的数据库配置，确保数据库已创建且用户名密码正确。

### Q: 前端页面空白或报错？
A: 
1. 确保后端已启动（端口 8080）
2. 检查浏览器控制台错误信息
3. 清除浏览器缓存后重试

### Q: AI 功能无法使用？
A: 
1. 检查 DeepSeek API Key 是否正确配置
2. 确认网络连接正常
3. 查看后端日志是否有错误信息

### Q: 今日任务勾选后，主页统计未更新？
A: 刷新页面即可，数据已保存在 localStorage 中。

---

## 📝 开发建议

### 数据库操作
- 使用 IntelliJ IDEA 的数据库插件连接 MySQL
- 可以直接在 IDEA 中查看和编辑表数据
- 建议定期备份数据库

### 代码规范
- 后端：遵循 Spring Boot 最佳实践
- 前端：使用 TypeScript 严格模式，遵循 Vue 3 组合式 API 规范

### 调试技巧
- 后端：使用 Swagger 文档测试 API
- 前端：使用浏览器开发者工具查看网络请求和状态

---

## 🎓 适用场景

- ✅ 个人学习管理
- ✅ 课程设计项目
- ✅ 毕业设计项目
- ✅ 学习 Spring Boot + Vue 3 全栈开发
- ✅ 了解 AI 集成实践

---

## 📄 许可证

本项目仅供学习交流使用。

---

## 🙏 致谢

- Spring Boot 团队
- Vue.js 团队
- Element Plus 团队
- DeepSeek AI 团队

---

**Happy Learning! 🎉**

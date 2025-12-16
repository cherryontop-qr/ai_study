# 🚀 部署指南

本文档介绍如何将智能学习管理系统部署到生产环境。

## 📋 部署前准备

### 1. 服务器要求

- **操作系统**：Linux (Ubuntu 20.04+ / CentOS 7+) 或 Windows Server
- **Java 环境**：JDK 17+
- **Node.js**：18+（仅构建时需要）
- **MySQL**：8.x
- **内存**：建议 2GB+
- **磁盘**：建议 10GB+

### 2. 环境变量配置

#### 后端环境变量

创建 `.env` 文件或设置系统环境变量：

```bash
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/aistudy?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_password

# DeepSeek API 配置
DEEPSEEK_API_KEY=your_api_key
DEEPSEEK_BASE_URL=https://api.deepseek.com/chat/completions
DEEPSEEK_MODEL=deepseek-chat

# 服务器配置
SERVER_PORT=8080
```

#### 前端环境变量

创建 `frontend/.env.production`：

```env
VITE_API_BASE_URL=http://your-backend-domain:8080
```

## 🔧 后端部署

### 方式一：JAR 包部署（推荐）

1. **构建 JAR 包**
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```
   生成的 JAR 包位于：`backend/target/ai-study-1.0.0.jar`

2. **运行 JAR 包**
   ```bash
   java -jar -Dspring.profiles.active=prod target/ai-study-1.0.0.jar
   ```

3. **使用 systemd 管理（Linux）**
   
   创建服务文件 `/etc/systemd/system/ai-study.service`：
   ```ini
   [Unit]
   Description=AI Study Application
   After=network.target mysql.service
   
   [Service]
   Type=simple
   User=your_user
   WorkingDirectory=/path/to/backend
   ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /path/to/backend/target/ai-study-1.0.0.jar
   Restart=always
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```
   
   启动服务：
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable ai-study
   sudo systemctl start ai-study
   sudo systemctl status ai-study
   ```

### 方式二：Docker 部署

1. **创建 Dockerfile**
   ```dockerfile
   FROM openjdk:17-jdk-slim
   WORKDIR /app
   COPY target/ai-study-1.0.0.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **构建镜像**
   ```bash
   docker build -t ai-study:latest .
   ```

3. **运行容器**
   ```bash
   docker run -d \
     --name ai-study \
     -p 8080:8080 \
     -e DB_URL=jdbc:mysql://mysql:3306/aistudy \
     -e DB_USERNAME=root \
     -e DB_PASSWORD=your_password \
     ai-study:latest
   ```

## 🎨 前端部署

### 方式一：静态文件部署（Nginx）

1. **构建生产版本**
   ```bash
   cd frontend
   npm install
   npm run build
   ```
   构建产物位于 `frontend/dist/` 目录

2. **配置 Nginx**
   
   创建配置文件 `/etc/nginx/sites-available/ai-study`：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       root /path/to/frontend/dist;
       index index.html;
       
       # 前端路由
       location / {
           try_files $uri $uri/ /index.html;
       }
       
       # API 代理
       location /api {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```

3. **启用配置**
   ```bash
   sudo ln -s /etc/nginx/sites-available/ai-study /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx
   ```

### 方式二：Docker 部署

1. **创建 Dockerfile**
   ```dockerfile
   FROM node:18-alpine AS builder
   WORKDIR /app
   COPY package*.json ./
   RUN npm install
   COPY . .
   RUN npm run build
   
   FROM nginx:alpine
   COPY --from=builder /app/dist /usr/share/nginx/html
   COPY nginx.conf /etc/nginx/conf.d/default.conf
   EXPOSE 80
   ```

2. **构建和运行**
   ```bash
   docker build -t ai-study-frontend:latest .
   docker run -d -p 80:80 ai-study-frontend:latest
   ```

## 🔒 安全配置

### 1. HTTPS 配置

使用 Let's Encrypt 免费证书：

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

### 2. 数据库安全

- 使用强密码
- 限制数据库访问 IP
- 定期备份数据库

### 3. API 安全

- 生产环境建议使用 JWT 认证（当前为简单 token）
- 启用 CORS 白名单
- 使用 HTTPS 传输

## 📊 监控与日志

### 日志配置

在 `application.yml` 中配置日志：

```yaml
logging:
  level:
    com.ai.study: INFO
  file:
    name: /var/log/ai-study/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 健康检查

访问 `http://your-domain:8080/actuator/health` 检查服务状态（需添加 Spring Boot Actuator 依赖）。

## 🔄 更新部署

1. **停止服务**
   ```bash
   sudo systemctl stop ai-study
   ```

2. **备份数据库**
   ```bash
   mysqldump -u root -p aistudy > backup_$(date +%Y%m%d).sql
   ```

3. **更新代码并构建**
   ```bash
   git pull
   mvn clean package -DskipTests
   ```

4. **重启服务**
   ```bash
   sudo systemctl start ai-study
   ```

## 🐛 故障排查

### 查看日志
```bash
# 应用日志
tail -f /var/log/ai-study/application.log

# 系统日志
journalctl -u ai-study -f
```

### 检查端口
```bash
netstat -tlnp | grep 8080
```

### 检查数据库连接
```bash
mysql -u root -p -h localhost aistudy
```

---

**部署完成后，记得测试所有功能是否正常！** ✅



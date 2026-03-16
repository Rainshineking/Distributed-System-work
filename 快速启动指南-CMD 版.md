# 快速启动指南（CMD 版）

## 前置条件

确保以下软件已安装并配置好环境变量：
- ✅ Java 21+
- ✅ Maven 3.8+
- ✅ Docker Desktop

## 启动步骤

### 步骤 1：启动中间件容器

打开 **CMD**（命令提示符），在项目根目录执行：

```cmd
docker-compose up -d mysql redis rabbitmq
```

等待看到以下输出表示启动成功：
```
✔ Container seckill-mysql     Started
✔ Container seckill-redis     Started
✔ Container seckill-rabbitmq  Started
```

验证容器状态：
```cmd
docker-compose ps
```

### 步骤 2：启动用户服务

**方法 A - 使用启动脚本（推荐）：**

双击运行项目根目录的：
```
start-user-service.bat
```

**方法 B - 手动启动：**

打开新的 CMD 窗口，执行：
```cmd
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

等待看到以下日志表示启动成功：
```
Started UserSystemApplication in 15.123 seconds
```

### 步骤 3：测试服务

打开新的 CMD 窗口，执行测试：

**1. 健康检查：**
```cmd
curl http://localhost:8081/api/users/health
```

预期响应：
```json
{
  "code": 200,
  "message": "User Service is running",
  "data": null
}
```

**2. 用户注册：**
```cmd
curl -X POST http://localhost:8081/api/users/register -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"123456\",\"email\":\"test@example.com\"}"
```

预期响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

**3. 用户登录：**
```cmd
curl -X POST http://localhost:8081/api/users/login -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"123456\"}"
```

预期响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "token": "eyJhbGciOiJIUzUxMiJ9.xxx"
  }
}
```

## 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3307 | 数据库（外部访问端口） |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| RabbitMQ Management | 15672 | 管理界面（浏览器访问） |
| user-service | 8081 | 用户服务实例 1 |

## 常见问题

### 问题 1：Docker 容器启动失败

**错误：** 端口被占用

**解决：**
```cmd
# 查看占用 3307 端口的进程
netstat -ano | findstr :3307

# 杀死进程
taskkill /F /PID <进程 ID>

# 重新启动容器
docker-compose up -d mysql redis rabbitmq
```

### 问题 2：Maven 命令不可用

**错误：** 'mvn' 不是内部或外部命令

**解决：**
1. 确认 Maven 已安装
2. 配置 Maven 环境变量
3. 重启 CMD 窗口

### 问题 3：数据库连接失败

**错误：** Communications link failure

**解决：**
```cmd
# 检查 MySQL 容器是否运行
docker ps | findstr mysql

# 如果没有运行，重新启动
docker-compose up -d mysql

# 查看 MySQL 日志
docker logs seckill-mysql
```

### 问题 4：端口 8081 被占用

**解决：**
```cmd
# 查看占用进程
netstat -ano | findstr :8081

# 杀死进程
taskkill /F /PID <进程 ID>

# 或者修改端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
```

## 停止服务

### 停止用户服务

在运行用户服务的 CMD 窗口按 `Ctrl+C`

### 停止 Docker 容器

```cmd
docker-compose down
```

### 停止并清理数据

```cmd
docker-compose down -v
```

## 快速命令参考

```cmd
# 启动中间件
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 启动用户服务
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

# 测试注册
curl -X POST http://localhost:8081/api/users/register -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"123456\",\"email\":\"test@example.com\"}"

# 测试登录
curl -X POST http://localhost:8081/api/users/login -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"password\":\"123456\"}"
```

## 下一步

用户服务启动成功后，可以：
1. ✅ 测试用户注册登录
2. ⏳ 实现商品服务
3. ⏳ 实现库存服务
4. ⏳ 实现订单服务
5. ⏳ 实现秒杀功能

---

祝你使用愉快！🎉

# 商品库存与秒杀系统 - README

## 项目简介

本项目是一个分布式商品库存与秒杀系统，用于课程作业，主要考察分布式系统的核心能力：

- ✅ 服务拆分与治理（用户、商品、库存、订单服务）
- ✅ 分布式事务与一致性（防止超卖）
- ✅ 高并发处理（Redis + 分布式锁）
- ✅ 负载均衡（Nginx轮询）
- ✅ 容器化部署（Docker + Docker Compose）
- ✅ 缓存策略（Redis缓存 + 缓存问题处理）

## 项目结构

```
Distributed-System-work/
├── pom.xml                           # 父POM（Maven多模块）
├── docker-compose.yml                # Docker编排文件
├── nginx/
│   ├── nginx.conf                    # Nginx配置（负载均衡+动静分离）
│   └── static/                       # 静态资源
│       └── index.html                # 前端页面
├── user-service/                     # 用户服务（改造自UserSystem）
│   ├── src/main/java/...
│   ├── pom.xml
│   └── Dockerfile
├── product-service/                  # 商品服务
├── inventory-service/                # 库存服务
├── order-service/                    # 订单服务
├── api-gateway/                      # API网关
├── common/                           # 公共模块
│   ├── common-core/                  # 核心工具类
│   ├── common-dto/                   # DTO定义
│   └── common-exception/             # 异常处理
├── documentation/                    # 设计文档
│   └── 系统设计文档.md
└── README.md
```

## 技术栈

### 后端技术
- Spring Boot 3.2
- Spring Cloud Gateway (API网关)
- MySQL 8.0
- Redis 7.0
- Redisson (分布式锁)
- RabbitMQ (消息队列)
- JWT (认证)

### 前端技术
- HTML5 + CSS3 + JavaScript

### 运维工具
- Docker
- Docker Compose
- Nginx
- JMeter (压测)

## 快速开始

### 前置条件

- Java 21+
- Maven 3.8+
- Docker + Docker Compose
- MySQL 8.0
- Redis 7.0
- RabbitMQ 3.12

### 本地开发

#### 1. 启动中间件

```bash
# 启动MySQL
docker run -d --name mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=verysecret \
  -e MYSQL_DATABASE=seckill \
  -e MYSQL_USER=seckill \
  -e MYSQL_PASSWORD=secret \
  mysql:8.0

# 启动Redis
docker run -d --name redis -p 6379:6379 redis:7.0-alpine

# 启动RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.12-management
```

#### 2. 启动服务

```bash
# 启动用户服务（多实例）
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

# 启动商品服务
cd ../product-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"

# 启动库存服务
cd ../inventory-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8084"

# 启动订单服务
cd ../order-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8085"

# 启动API网关
cd ../api-gateway
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

#### 3. 访问系统

- 前端页面: http://localhost:80
- API网关: http://localhost:8080
- 健康检查: http://localhost:8080/actuator/health

### Docker Compose部署

```bash
# 一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80 | 负载均衡 + 动静分离 |
| API Gateway | 8080 | API网关 |
| User Service 1 | 8081 | 用户服务实例1 |
| User Service 2 | 8082 | 用户服务实例2 |
| Product Service | 8083 | 商品服务 |
| Inventory Service | 8084 | 库存服务 |
| Order Service | 8085 | 订单服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |

## 核心功能

### 1. 用户注册登录 ✅

**功能特性**:
- ✅ 用户注册（用户名、密码、邮箱）
- ✅ 用户登录（JWT Token 认证）
- ✅ 密码加密存储（BCrypt）
- ✅ 用户信息查询
- ✅ 多实例部署（8081/8082）

**API 接口**:
```
POST /api/users/register    # 用户注册
POST /api/users/login       # 用户登录
GET  /api/users/{id}        # 查询用户
GET  /api/users/health      # 健康检查
```

**快速测试**:
```bash
# 注册
curl -X POST http://localhost:8081/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"123456\",\"email\":\"test@example.com\"}"

# 登录
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"123456\"}"
```

### 2. 秒杀功能

**防止超卖的三层防护**:
1. **Redis 预减库存** - 原子操作，减少数据库压力
2. **分布式锁** - Redisson 实现，保证库存一致性
3. **数据库乐观锁** - 最终兜底方案

**秒杀流程**:
```
用户请求 → Redis 预减 → 分布式锁 → 双重检查 → 创建订单 → 减库存 → 返回结果
```

### 2. 负载均衡

**Nginx配置**:
- 轮询算法（默认）
- 动静分离
- 反向代理

**多实例部署**:
- 用户服务启动2个实例（8081/8082）
- Nginx自动轮询分发请求

### 3. 缓存策略

**Redis缓存**:
- 商品详情缓存
- 用户会话缓存
- 库存缓存

**缓存问题处理**:
- 缓存穿透 - 缓存空值 + 布隆过滤器
- 缓存击穿 - 互斥锁 + 永不过期
- 缓存雪崩 - 随机过期时间 + 多级缓存

## API接口

### 用户服务
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录

### 商品服务
- `GET /api/products/{id}` - 查询商品
- `GET /api/products` - 查询所有商品

### 库存服务
- `POST /api/inventory/seckill` - 秒杀
- `GET /api/inventory/{productId}` - 查询库存

### 订单服务
- `POST /api/orders` - 创建订单
- `GET /api/orders/{id}` - 查询订单

## 项目结构说明

### 父POM (pom.xml)
- 统一管理依赖版本
- 定义Maven多模块结构
- 配置Docker插件

### 公共模块 (common/)
- **common-core**: 核心工具类、统一返回
- **common-dto**: 数据传输对象
- **common-exception**: 异常处理

### 服务模块
每个服务都是独立的Spring Boot项目，包含：
- Controller: 控制层
- Service: 业务层
- Mapper/Repository: 数据访问层
- Entity: 实体类
- Dockerfile: 容器化配置

## 测试

### 单元测试
```bash
mvn test
```

### 压力测试
使用JMeter进行压力测试：
- 并发秒杀场景
- 高并发查询场景
- 系统稳定性测试

### 性能指标
- 响应时间
- QPS
- 错误率
- CPU/内存使用率

## 部署说明

### 本地开发
1. 启动MySQL、Redis、RabbitMQ
2. 修改各服务的 `application.properties` 中的数据库连接
3. 逐个启动服务

### Docker部署
1. 配置 `docker-compose.yml`
2. 运行 `docker-compose up -d`
3. 访问 http://localhost:80

## 常见问题

### 1. 端口冲突
- 检查端口占用：`netstat -ano | findstr :8081`
- 修改 `application.properties` 中的端口

### 2. 数据库连接失败
- 检查MySQL是否启动：`docker ps | grep mysql`
- 检查连接参数是否正确

### 3. Redis连接失败
- 检查Redis是否启动：`docker ps | grep redis`
- 检查连接参数是否正确

## 作业要求完成情况

| 要求 | 状态 | 说明 |
|------|------|------|
| 系统设计文档 | ✅ | `documentation/系统设计文档.md` |
| 服务拆分 | ✅ | 用户、商品、库存、订单服务 |
| API接口定义 | ✅ | RESTful API设计 |
| 数据库ER图 | ✅ | ER图 + 表结构 |
| 技术栈选型 | ✅ | Spring Boot + Redis + MySQL等 |
| Git初始化 | ✅ | 项目结构已搭建 |
| 开发环境 | ✅ | Docker + Docker Compose |
| 用户注册登录 | ✅ | user-service |
| 多实例部署 | ✅ | 用户服务2个实例 |
| Nginx负载均衡 | ✅ | 轮询算法 |
| 动静分离 | ✅ | Nginx配置 |
| Redis缓存 | ✅ | 商品缓存、库存缓存 |
| 缓存问题处理 | ✅ | 穿透、击穿、雪崩 |
| JMeter压测 | ✅ | 压测脚本 |

## 扩展功能

### 已实现
- ✅ 服务拆分与治理
- ✅ 分布式锁（Redisson）
- ✅ 负载均衡（Nginx）
- ✅ 动静分离
- ✅ Redis缓存
- ✅ 缓存问题处理

### 待实现
- ⏳ API网关认证
- ⏳ 服务发现（Eureka）
- ⏳ 配置中心（Config）
- ⏳ 链路追踪（SkyWalking）

## 贡献者

- [你的名字] - 分布式系统课程作业

## 许可证

MIT License

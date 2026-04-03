# 商品库存与秒杀系统 - README

## 项目简介

本项目是一个分布式商品库存与秒杀系统，用于课程作业，主要考察分布式系统的核心能力：

- ✅ 服务拆分与治理（用户、商品、库存、订单服务）
- ✅ 分布式事务与一致性（防止超卖）
- ✅ 高并发处理（Redis + 分布式锁）
- ✅ 负载均衡（Nginx 轮询）
- ✅ 容器化部署（Docker + Docker Compose）
- ✅ 缓存策略（Redis 缓存 + 缓存问题处理）
- ✅ **读写分离**（MySQL 主从复制 + 自动切换）
- ✅ **ElasticSearch 搜索**（商品全文检索）
- ✅ **消息队列**（Kafka 异步下单、削峰填谷）
- ✅ **分库分表**（ShardingSphere-JDBC 按用户 ID 分库、按订单 ID 分表）
- ✅ **幂等性控制**（Redis 防重复下单）
- ✅ **分布式 ID**（雪花算法生成订单 ID）

## 项目结构

```
Distributed-System-work/
├── pom.xml                           # 父 POM（Maven 多模块）
├── docker-compose.yml                # Docker 编排文件
├── nginx/
│   ├── nginx.conf                    # Nginx 配置（负载均衡 + 动静分离）
│   └── static/                       # 静态资源
│       └── index.html                # 前端页面
├── user-service/                     # 用户服务
├── product-service/                  # 商品服务
├── inventory-service/                # 库存服务
├── order-service/                    # 订单服务
├── api-gateway/                      # API 网关
├── common/                           # 公共模块
│   ├── common-core/                  # 核心工具类
│   ├── common-dto/                   # DTO 定义
│   └── common-exception/             # 异常处理
├── documentation/                    # 设计文档
│   └── 系统设计文档.md
└── README.md
```

## 技术栈

### 后端技术
- Spring Boot 3.2
- Spring Cloud Gateway (API 网关)
- MySQL 8.0 (主从复制、读写分离、分库分表)
- Redis 7.0 (缓存、幂等性控制)
- Redisson (分布式锁)
- **Kafka** (消息队列、异步下单、削峰填谷)
- ElasticSearch 8.11 (全文检索)
- ShardingSphere-JDBC 5.3.2 (分库分表)
- Snowflake Algorithm (雪花算法生成订单 ID)
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

### 方式一：Docker Compose 一键启动（推荐 - 全容器化）

```bash
cd d:\大三下资料\分布式\Distributed-System-work
docker-compose up -d
```

**启动后包含以下容器：**
- MySQL Master (端口：3307) - 主库（写库）
- MySQL Slave (端口：3308) - 从库（读库）
- Redis (端口：6379)
- **Zookeeper** (端口：2181) - Kafka 依赖
- **Kafka** (端口：9092) - 消息队列
- ElasticSearch (端口：9200)
- Kibana (端口：5601) - ES 可视化界面
- user-service-1 (端口：8081)
- user-service-2 (端口：8082)
- product-service (端口：8083)
- inventory-service (端口：8084)
- order-service (端口：8085)
- api-gateway (端口：8080)
- nginx (端口：80)

**查看日志：**
```bash
docker-compose logs -f
```

**停止所有服务：**
```bash
docker-compose down
```

**重启某个服务：**
```bash
docker-compose restart product-service
```

### 方式二：本地开发模式（适合调试）

#### 1. 启动中间件

```bash
docker-compose up -d mysql redis rabbitmq nginx
```

#### 2. 安装本地依赖

```bash
mvn clean install -DskipTests -U
```

#### 3. 启动各个服务（需要多个终端）

```bash
# 终端 1 - 用户服务实例 1
cd user-service
mvn spring-boot:run

# 终端 2 - 用户服务实例 2
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"

# 终端 3 - 商品服务
cd product-service
mvn spring-boot:run

# 终端 4 - 库存服务
cd inventory-service
mvn spring-boot:run

# 终端 5 - 订单服务
cd order-service
mvn spring-boot:run
```

**注意：** 本地开发模式下，Nginx 需要手动启动：
```bash
nginx -p . -c nginx/nginx.conf
```

## 服务端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80 | 统一入口，负载均衡 |
| API Gateway | 8080 | API 网关 |
| User Service | 8081/8082 | 用户服务（多实例） |
| Product Service | 8083 | 商品服务 |
| Inventory Service | 8084 | 库存服务 |
| Order Service | 8085 | 订单服务 |
| MySQL Master | 3307 | 数据库主库（写） |
| MySQL Slave | 3308 | 数据库从库（读） |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672 | 消息队列 |
| ElasticSearch | 9200 | 搜索引擎 |
| Kibana | 5601 | ES 可视化界面 |

## API 接口文档

### 1. 用户服务

#### 用户注册
```
POST http://localhost:8081/api/users/register
Content-Type: application/json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com"
}
```

#### 用户登录
```
POST http://localhost:8081/api/users/login
Content-Type: application/json
{
  "username": "testuser",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "token": "eyJhbGciOiJIUzUxMiJ9..."
  }
}
```

### 2. 商品服务

#### 创建商品
```
POST http://localhost:8083/api/products
Content-Type: application/json
Authorization: Bearer {token}
{
  "name": "iPhone 15",
  "price": 7999.00,
  "description": "Apple iPhone 15 128GB",
  "imageUrl": "https://example.com/iphone15.jpg"
}
```

#### 获取商品详情（带 Redis 缓存）
```
GET http://localhost:8083/api/products/{id}
Authorization: Bearer {token}
```

#### 获取所有商品
```
GET http://localhost:8083/api/products
Authorization: Bearer {token}
```

#### 更新商品
```
PUT http://localhost:8083/api/products/{id}
Content-Type: application/json
Authorization: Bearer {token}
{
  "name": "iPhone 15 Pro",
  "price": 8999.00,
  "description": "Apple iPhone 15 Pro 256GB",
  "imageUrl": "https://example.com/iphone15pro.jpg"
}
```

#### 删除商品
```
DELETE http://localhost:8083/api/products/{id}
Authorization: Bearer {token}
```

#### 搜索商品（ElasticSearch 全文检索）
```
GET http://localhost:8083/api/products/search?keyword=iPhone&page=0&size=10
Authorization: Bearer {token}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10
  }
}
```

#### 同步所有商品到 ElasticSearch
```
POST http://localhost:8083/api/products/sync/all
Authorization: Bearer {token}
```

### 3. 库存服务

#### 创建库存
```
POST http://localhost:8084/api/inventory/{productId}?stock=1000
Authorization: Bearer {token}
```

#### 获取库存
```
GET http://localhost:8084/api/inventory/{productId}
Authorization: Bearer {token}
```

#### 扣减库存（分布式锁保护）
```
POST http://localhost:8084/api/inventory/decrease
Content-Type: application/json
Authorization: Bearer {token}
{
  "productId": 1,
  "quantity": 1
}
```

#### 确认库存
```
POST http://localhost:8084/api/inventory/confirm
Content-Type: application/json
Authorization: Bearer {token}
{
  "productId": 1,
  "quantity": 1
}
```

#### 回滚库存
```
POST http://localhost:8084/api/inventory/rollback
Content-Type: application/json
Authorization: Bearer {token}
{
  "productId": 1,
  "quantity": 1
}
```

### 4. 订单服务

#### 创建订单
```
POST http://localhost:8085/api/orders
Content-Type: application/json
Authorization: Bearer {token}
{
  "userId": 1,
  "productId": 1,
  "quantity": 1,
  "totalPrice": 7999.00
}
```

#### 获取订单详情
```
GET http://localhost:8085/api/orders/{id}
Authorization: Bearer {token}
```

#### 获取用户订单列表
```
GET http://localhost:8085/api/orders/user/{userId}
Authorization: Bearer {token}
```

#### 更新订单状态
```
PUT http://localhost:8085/api/orders/{id}/status?status=1
Authorization: Bearer {token}
```

### 5. 健康检查

所有服务都提供健康检查接口：
```
GET http://localhost:{port}/api/{service}/health
GET http://localhost:{port}/api/{service}/status
```

## 数据库设计

### 表结构

#### user (用户表)
```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### product (商品表)
```sql
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  description VARCHAR(1000),
  image_url VARCHAR(500),
  status INT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### inventory (库存表)
```sql
CREATE TABLE inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL UNIQUE,
  stock INT NOT NULL DEFAULT 0,
  locked_stock INT NOT NULL DEFAULT 0,
  version INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### orders (订单表)
```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  status INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 核心功能实现

### 1. Redis 缓存（商品详情）

商品服务使用 Redis 缓存商品详情，减少数据库压力：
- 缓存过期时间：30 分钟
- 先查缓存，缓存未命中则查数据库并回写缓存
- 更新/删除商品时同步删除缓存

### 2. 分布式锁（防止超卖）

库存服务使用 Redisson 实现分布式锁：
- 锁 key：`lock:inventory:{productId}`
- 等待时间：5 秒
- 持有时间：10 秒
- 确保同一商品同一时间只有一个线程扣减库存

### 3. 缓存问题处理

#### 缓存穿透（已实现）
- **问题**: 查询不存在的数据，每次都打到数据库
- **解决方案**: 
  - ✅ 缓存空对象（设置 5 分钟过期时间）
  - 代码位置：`ProductService.getProductById()`
  - 实现逻辑：查询数据库后，如果不存在也缓存空值，防止重复查询

#### 缓存击穿（已实现）
- **问题**: 热点数据过期，大量请求同时打到数据库
- **解决方案**:
  - ✅ 互斥锁（ReentrantLock）：只有一个线程重建缓存
  - ✅ 双重检查锁：防止其他线程已重建缓存
  - 代码位置：`ProductService.getProductById()`

#### 缓存雪崩（已实现）
- **问题**: 大量缓存同时过期
- **解决方案**:
  - ✅ 随机过期时间：基础 30 分钟 ±5 分钟随机波动
  - 代码位置：`ProductService.getProductById()`
  - 实现逻辑：`randomTTL = 30 + random(-5, 5)`

### 4. 读写分离（已实现）

#### MySQL 主从复制
- **主库（Master）**：端口 3307，处理所有写操作（INSERT、UPDATE、DELETE）
- **从库（Slave）**：端口 3308，处理所有读操作（SELECT）
- **复制方式**：GTID 复制
- **配置文件**：
  - `mysql/master.cnf` - 主库配置
  - `mysql/slave.cnf` - 从库配置

#### 自动切换
- **实现方式**：AOP 切面 + 路由数据源
- **代码位置**：`DataSourceAspect`、`RoutingDataSource`
- **切换规则**：
  - 写操作（create/save/add/update/delete/remove）→ 主库
  - 读操作（get/find/query/list/search）→ 从库

### 5. ElasticSearch 全文检索（已实现）

#### 商品搜索
- **分词器**：ik_max_word（中文分词）
- **搜索字段**：商品名称、商品描述
- **代码位置**：`ProductSearchService`、`ProductController.searchProducts()`

#### 数据同步
- **同步时机**：
  - 创建商品时自动同步
  - 更新商品时自动同步
  - 删除商品时自动删除
- **全量同步**：`POST /api/products/sync/all`

#### Kibana 可视化
- **访问地址**：http://localhost:5601
- **索引名称**：products

### 6. 分布式事务与一致性（本次新增）

#### 基于 Redis 的库存预扣减
- **实现方式**：在 Redis 中维护商品库存，使用原子操作进行预扣减
- **防超卖**：通过 Lua 脚本保证库存检查和扣减的原子性
- **限购**：每个用户对同一商品只能秒杀一次（幂等性控制）
- **代码位置**：`RedisInventoryManager.tryReserveStock()`

#### 基于消息的最终一致性
- **实现模式**：TCC（Try-Confirm-Cancel）事务模式
- **消息队列**：Kafka
- **事务流程**：
  1. **Try 阶段**：订单服务创建订单（状态=待支付），发送订单创建消息
  2. **库存预扣减**：库存服务收到消息，基于 Redis 预扣减库存
  3. **Confirm 阶段**：用户支付成功，订单服务发送订单确认消息，库存服务确认库存扣减
  4. **Cancel 阶段**：订单超时或取消，订单服务发送订单取消消息，库存服务回滚库存
- **消息主题**：
  - `seckill-transaction-topic`：订单事务消息
  - `seckill-inventory-topic`：库存事务消息
- **代码位置**：
  - 订单服务：`TransactionMessageProducer`、`OrderKafkaListener`
  - 库存服务：`InventoryTransactionProducer`、`OrderTransactionListener`

#### 幂等性控制
- **实现方式**：Redis + 唯一业务标识
- **场景**：同一用户同一商品只能秒杀一次
- **代码位置**：`IdempotencyUtil.isOperationAllowed()`

## Nginx 配置

### 负载均衡

Nginx 配置了三种负载均衡算法：

#### 1. 轮询（默认）
```nginx
upstream user_services {
    server user-service:8081;
    server user-service:8082;
}
```

#### 2. ip_hash
```nginx
upstream user_services {
    ip_hash;
    server user-service:8081;
    server user-service:8082;
}
```

#### 3. least_conn
```nginx
upstream user_services {
    least_conn;
    server user-service:8081;
    server user-service:8082;
}
```

### 动静分离

```nginx
# 静态资源
location /static/ {
    root /usr/share/nginx/html;
    expires 7d;
}

# 动态请求
location /api/ {
    proxy_pass http://user_services;
}
```

## JMeter 压力测试

### 测试计划

#### 1. 负载均衡测试
- 线程组：1000 线程
- 循环次数：100
- 接口：用户登录
- 验证：8081 和 8082 端口的请求数大致相等

#### 2. 静态资源测试
- 线程组：500 线程
- 循环次数：50
- 资源：/static/index.html
- 验证：响应时间 < 10ms

#### 3. 动态接口测试
- 线程组：200 线程
- 循环次数：100
- 接口：商品详情查询
- 验证：缓存命中率 > 80%

### 测试结果验证

查看服务日志：
```bash
docker logs user-service-1 | grep "POST /api/users/login"
docker logs user-service-2 | grep "POST /api/users/login"
```

## 停止服务

```bash
docker-compose down
```

## 常见问题

### 1. 端口冲突
修改 `docker-compose.yml` 中的端口映射

### 2. MySQL 连接失败
确保 MySQL 容器已启动，检查 JDBC URL 配置

### 3. Redis 连接失败
确保 Redis 容器已启动，检查 Redis 配置

### 4. JWT Token 无效
检查 JWT 密钥配置，确保密钥长度 >= 512 bits

## 作业完成清单

### 基础功能
- ✅ 系统设计文档（`documentation/系统设计文档.md`）
- ✅ 系统架构图（服务拆分）
- ✅ RESTful API 接口定义
- ✅ 数据库 ER 图
- ✅ 技术栈选型说明
- ✅ Git 仓库初始化
- ✅ Spring Boot + JPA + MySQL 环境
- ✅ 用户注册登录功能

### 容器化与部署
- ✅ Docker 容器化部署
- ✅ Nginx 负载均衡
- ✅ 动静分离

### 高并发处理
- ✅ Redis 缓存
- ✅ 分布式锁（Redisson）
- ✅ 缓存穿透/击穿/雪崩处理（代码已实现）

### 高并发读（本次新增）
- ✅ **读写分离**（MySQL 主从复制 + AOP 自动切换）
- ✅ **ElasticSearch 搜索**（商品全文检索）

### 测试
- ✅ JMeter 压力测试（测试计划已创建）

### 作业要求

- ✅ 消息队列实现秒杀下单功能（Kafka 异步处理订单创建，削峰填谷）
- ✅ Redis 缓存库存（基于 Redis 实现库存预扣减，防超卖、限购）
- ✅ 雪花算法生成订单 ID（SnowflakeIdGenerator）
- ✅ 幂等性控制（防止重复下单，同一用户同一商品只能秒杀一次）
- ✅ 数据一致性保障（基于消息的最终一致性 + TCC 事务模式）
- ✅ 分库分表（ShardingSphere-JDBC 按用户 ID 分库、按订单 ID 分表）

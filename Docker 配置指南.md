# Docker 配置镜像加速器指南

## 问题原因
国内网络无法直接访问 Docker Hub，导致拉取镜像失败。

## 解决方案

### 方案 1：配置 Docker Desktop 镜像加速器（推荐）

#### 步骤 1：打开 Docker Desktop 设置

1. 点击 Docker Desktop 托盘图标
2. 点击 **Settings** (设置)
3. 选择 **Docker Engine**

#### 步骤 2：修改配置文件

在 JSON 配置中添加 `"registry-mirrors"` 数组：

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live",
    "https://hub.rat.dev",
    "https://hub.littleredboat.com"
  ]
}
```

#### 步骤 3：应用并重启

1. 点击 **Apply & Restart**
2. 等待 Docker Desktop 重启完成

#### 步骤 4：验证配置

```bash
# 查看 Docker 信息
docker info

# 应该能看到 Registry Mirrors 配置
```

### 方案 2：使用国内镜像仓库（备选）

如果方案 1 不可用，可以使用以下国内镜像仓库：

#### 阿里云容器镜像服务

1. 访问 https://cr.console.aliyun.com/
2. 登录阿里云账号
3. 点击左侧 **镜像加速器**
4. 复制你的专属加速地址
5. 按照方案 1 配置到 Docker Desktop

#### 腾讯云容器镜像服务

1. 访问 https://console.cloud.tencent.com/tcr
2. 登录后复制加速地址
3. 配置到 Docker Desktop

### 方案 3：手动拉取镜像（如果自动拉取失败）

```bash
# 先配置好镜像加速器，然后手动拉取
docker pull mysql:8.0
docker pull redis:7.0-alpine
docker pull rabbitmq:3.12-management
docker pull nginx:alpine

# 然后再启动服务
docker-compose up -d mysql redis rabbitmq
```

## 重启 Docker 服务

配置完成后，执行以下命令：

```bash
# 清理旧数据
docker-compose down -v

# 重新启动
docker-compose up -d mysql redis rabbitmq

# 查看状态
docker-compose ps
```

## 常见问题

### 问题 1：配置后仍然拉取失败

**解决方法：**
```bash
# 重启 Docker Desktop
# 退出 Docker Desktop
# 重新打开 Docker Desktop

# 或者在 PowerShell 中重启 Docker 服务
Restart-Service docker
```

### 问题 2：镜像加速器不可用

有些镜像加速器可能会暂时不可用，可以尝试更换其他加速器：

```json
"registry-mirrors": [
  "https://docker.m.daocloud.io",
  "https://docker.1panel.live",
  "https://hub.rat.dev",
  "https://hub.littleredboat.com",
  "https://c.c1ns.cn"
]
```

### 问题 3：查看当前使用的镜像源

```bash
# 查看 Docker 配置
docker info | findstr "Registry Mirrors"
```

### 问题 4：清理未使用的镜像

```bash
# 清理悬空镜像
docker image prune -f

# 清理所有未使用的镜像
docker image prune -a -f
```

## 推荐的镜像加速器（2024 年可用）

以下镜像加速器按推荐顺序排列：

1. **DaoCloud**: `https://docker.m.daocloud.io`
2. **1Panel**: `https://docker.1panel.live`
3. **Rat**: `https://hub.rat.dev`
4. **LittleRedBoat**: `https://hub.littleredboat.com`
5. **C1ns**: `https://c.c1ns.cn`

## 完整配置示例

```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "insecure-registries": [],
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1panel.live"
  ],
  "features": {
    "buildkit": true,
    "containerd-snapshotter": true
  }
}
```

## 验证是否成功

配置完成后，测试拉取镜像：

```bash
# 测试拉取 MySQL
docker pull mysql:8.0

# 如果成功，说明配置正确
# 然后可以启动项目
docker-compose up -d mysql redis rabbitmq
```

## 其他提示

### 1. 使用 WSL2 后端

确保 Docker Desktop 使用 WSL2 后端，性能更好：

1. Settings → General
2. 勾选 **Use the WSL 2 based engine**

### 2. 分配足够资源

Settings → Resources：
- CPUs: 至少 2 个
- Memory: 至少 4GB
- Swap: 至少 2GB

### 3. 启用 IPv6（可选）

如果网络支持，可以启用 IPv6：

```json
{
  "ipv6": true,
  "fixed-cidr-v6": "2001:db8:1::/64"
}
```

---

配置完成后，再尝试启动服务！

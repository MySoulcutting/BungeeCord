# BungeeCord（NarraFork）

基于 [SpigotMC/BungeeCord](https://github.com/SpigotMC/BungeeCord) 的增强分支，专注于性能优化、安全加固和 API 扩展。

## 与上游的区别

### 新功能

- **动态服务器 API** — `ProxyServer.addServer()` / `removeServer()`，正式的服务器动态管理接口，附带 `ServerAddEvent` / `ServerRemoveEvent` 事件通知，线程安全
- **CompletableFuture 异步 API** — `ServerInfo.pingAsync()` 和 `ProxiedPlayer.connectAsync()`，通过 `default` 方法桥接，完全向后兼容
- **内置负载均衡** — `LoadBalancer` 接口 + 三种策略（`FIRST` / `LOWEST` / `RANDOM`），配置项 `load_balance_strategy` 切换
- **CIDR 子网段限流** — 按 /24 子网聚合计数，防止同一子网多 IP 绕过单 IP 限流
- **内置维护模式** — `/maintenance` 命令运行时切换，维护模式下拒绝新连接
- **玩家连接时长 API** — `ProxiedPlayer.getLoginTime()` 返回登录时间戳
- **中文消息** — 默认 `messages.properties` 已汉化

### 安全加固

- Handshake hostname 长度验证（最大 255 字符）
- 协议包数组长度负数检查，防止 `NegativeArraySizeException`

### Bug 修复

- PluginClassloader 关闭时未从 allLoaders 移除导致内存泄漏
- AsyncEvent.registerIntent() 竞态条件
- DefaultTabList 无效配置值导致未捕获异常
- EntityMap.rewriteVarInt 异常路径 ByteBuf 泄漏
- Metrics BufferedReader 异常路径资源泄漏
- DownstreamBridge 遍历 servers Map 的并发安全问题

### 性能优化

- `BungeeServerInfo.players` ArrayList → HashSet，`removePlayer()` O(n) → O(1)
- 三处队列 LinkedList → ArrayDeque，减少 GC 压力
- `matchPlayer` 预计算 `toLowerCase`，避免 N 次重复转换
- 新增 `BungeeServerInfo.getPlayerCount()` 避免不必要的集合拷贝

## 新增配置项

```yaml
# 负载均衡策略: FIRST(默认) / LOWEST(最少玩家) / RANDOM(随机)
load_balance_strategy: FIRST

# CIDR 子网段限流 (-1 禁用)
connection_throttle_cidr_limit: -1
connection_throttle_cidr_size: 24

# 维护模式
maintenance_mode: false
maintenance_motd: '&c&l维护中'
```

## 新增 API 示例

```java
// 动态注册服务器
ServerInfo server = ProxyServer.getInstance().constructServerInfo(
    "survival", new InetSocketAddress("10.0.0.2", 25565), "生存服", false);
ProxyServer.getInstance().addServer(server);

// 异步 ping
server.pingAsync().thenAccept(ping -> {
    System.out.println("在线: " + ping.getPlayers().getOnline());
});

// 异步连接
player.connectAsync(server).thenAccept(success -> {
    if (!success) player.sendMessage(new TextComponent("连接失败"));
});

// 连接时长
long onlineMs = System.currentTimeMillis() - player.getLoginTime();
```

## 构建

```bash
git clone https://github.com/yourname/BungeeCord.git
cd BungeeCord
mvn package
```

产物位于 `bootstrap/target/BungeeCord.jar`。

## 推荐 JVM 参数

```
java -Xms1G -Xmx1G -XX:+UseG1GC -XX:MaxGCPauseMillis=50
     -XX:G1HeapRegionSize=4M -XX:+ParallelRefProcEnabled
     -XX:+AlwaysPreTouch -XX:+OptimizeStringConcat
     -XX:+UseStringDeduplication -jar BungeeCord.jar
```

| 在线人数 | 建议堆大小 |
|:--------:|:---------:|
| < 500 | 512M |
| 500 - 2000 | 1G |
| 2000 - 5000 | 2G |
| 5000+ | 4G |

## 安全提示

BungeeCord 要求后端服务器以 `online-mode=false` 运行，这意味着玩家可以绕过代理直接连接后端并伪造身份。请务必通过防火墙限制后端服务器仅允许代理 IP 访问，参考 [防火墙配置指南](https://www.spigotmc.org/wiki/firewall-guide/)。

## 许可证

(c) 2012-2026 SpigotMC Pty. Ltd.

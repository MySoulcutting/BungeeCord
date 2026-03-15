# 更新记录

## 2026-03-16

### 新功能

- **动态服务器注册/注销 API**：`ProxyServer` 新增 `addServer(ServerInfo)` / `removeServer(String)` 方法，提供正式的服务器动态管理接口，替代直接操作 `getServers()` Map 的非安全做法。操作成功时触发 `ServerAddEvent` / `ServerRemoveEvent` 事件，`servers` Map 使用 `synchronizedMap` 包装保证线程安全
- **CompletableFuture 异步 API**：`ServerInfo` 新增 `pingAsync()` 返回 `CompletableFuture<ServerPing>`，`ProxiedPlayer` 新增 `connectAsync(ServerInfo)` / `connectAsync(ServerInfo, Reason)` 返回 `CompletableFuture<Boolean>`。通过 `default` 方法桥接到现有 Callback API，完全向后兼容
- **内置负载均衡策略**：新增 `LoadBalancer` 接口和三种内置实现 — `FIRST`（默认，原有行为）、`LOWEST`（最少玩家优先）、`RANDOM`（随机选择）。通过配置项 `load_balance_strategy` 切换，`updateAndGetNextServer()` 改为无状态委托
- **CIDR 子网段限流**：`ConnectionThrottle` 新增按子网段聚合计数，防止同一子网的多个 IP 绕过单 IP 限流。新增配置项 `connection_throttle_cidr_limit`（默认 -1 禁用）和 `connection_throttle_cidr_size`（默认 24，即 /24 子网）
- **内置维护模式**：新增配置项 `maintenance_mode` 和 `maintenance_motd`，维护模式下拒绝新玩家登录。新增 `/maintenance` 命令（权限 `bungeecord.command.maintenance`）支持运行时切换
- **玩家连接时长 API**：`ProxiedPlayer` 新增 `getLoginTime()` 方法，返回玩家登录代理的时间戳（毫秒），无需插件自行在事件中记录

### 安全加固

- **Handshake hostname 验证**：限制客户端发送的 hostname 最大长度为 255 字符，防止恶意超长字符串攻击
- **DefinedPacket 负数长度检查**：`readVarIntArray`、`readStringArray`、`readProperties` 三处数组长度校验补充 `len < 0` 检查，防止恶意客户端发送负数长度触发 `NegativeArraySizeException`

### Bug 修复

- **PluginClassloader 内存泄漏**：`close()` 时从 `allLoaders` 集合中移除自身，防止已卸载插件的 ClassLoader 无法被 GC 回收，同时避免跨插件类查找时访问已关闭的 ClassLoader
- **AsyncEvent 竞态条件**：`registerIntent()` 使用 `computeIfAbsent` 替代 get-then-put，消除多线程同时为同一插件首次注册 intent 时的数据丢失问题
- **DefaultTabList 配置容错**：`Enum.valueOf()` 在值无效时抛出异常而非返回 null，原有的 null 检查为死代码。改为 try-catch 捕获 `IllegalArgumentException`，无效配置值现在会正确降级为 `GLOBAL_PING`
- **EntityMap.rewriteVarInt ByteBuf 泄漏**：将 `data.release()` 移入 try-finally 块，确保中间操作抛异常时 ByteBuf 仍能被释放
- **Metrics 资源泄漏**：将 `BufferedReader` 纳入 try-with-resources，修复异常路径下输入流未关闭的问题

### 代码改进

- **InterruptedException 处理**：BungeeCord 关闭流程中两处 `catch InterruptedException` 增加 `Thread.currentThread().interrupt()` 恢复中断标志
- **NativeCode 加载日志**：`System.loadLibrary` 失败时输出原因，便于排查本地库加载问题

### 性能优化

- **LinkedList → ArrayDeque**：`BungeeServerInfo.packetQueue`、`UserConnection.serverJoinQueue`、`InitialHandler.requestedCookies` 三处队列改用 ArrayDeque，减少节点对象分配和 GC 压力
- **BungeeServerInfo.players ArrayList → HashSet**：`removePlayer()` 从 O(n) 降为 O(1)，天然去重，`getPlayers()` 返回时无需再做去重拷贝
- **matchPlayer 预计算 toLowerCase**：搜索参数的小写转换从每次迭代调用改为预计算一次，避免 N 次重复转换

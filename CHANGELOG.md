# 更新记录

## 2026-03-16

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

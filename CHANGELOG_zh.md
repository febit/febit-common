# Changelog

## [4.2.0] - 尚未发布

## [4.2.0-M1] - 2026-09-30

### New Features

- **rabbit**: 新增 `febit-commons-rabbit` 模块与 `RabbitDelayQueue` —— 基于 RabbitMQ 的延迟投递：消息先进入带 TTL 的
  `wait` 队列，到期前
  内部自动 re-hop，到期后投递到应用自己的 ready exchange。包含 `DelayQueueOptions` / `DelayQueueTopology`、`DelayMessage` /
  `DelayReceipt`、可插拔的 `DelayPolicy`（`FixedRoundingDelayPolicy`）与 `MessageIdGenerator`（`UuidIdGenerator`），以及基于
  Micrometer 的 `DelayQueueMetrics`
- **etcd**: 新增类型化键值存储 `org.febit.common.etcd.store`：`EtcdAccessor`（get / put / delete / txn / 分页扫描 / 类型化
  watch）配
  `KVRecord`、`Put` / `SidePut`、`EtcdPagingScanner`、`TypedWatchObserver` / `TypedWatchListenerAdapter`，codec SPI
  `KVCodec` / `Codec`（`GenericKVCodec`、`JsonCodec`、`PatternCodec`、`CodecUtils`），以及基于 Micrometer 的 `EtcdMetrics`
- **caffeine**: `febit-commons-stuff` 新增 `CaffeineDebouncer`（按 key 防抖，支持最大延迟）与 `CaffeineThrottle`（按 key
  冷却窗口）

### Fixes

- **caffeine**: `CaffeineDebouncer` 改为先置位消费者线程运行标志再启动线程，待执行回调不会再静默停滞
- **jsonrpc2**: 响应无法转换时不再让调用方永久等待 —— 请求 future 会带原因异常完成
- **jsonrpc2**: 通知 handler 的异常现在会被记录，不再逃逸到执行器线程
- **stuff**: 进程尚未创建时 `ProcessFuture.cancel()` 不再抛异常，重复调用也不再清除 `isCancelled()`
- **stuff**: 在进程创建前被取消的进程现在会被销毁，不再遗留运行

### Deprecations

- **rest-client**: 跟随 Spring Framework 7.1 废弃 `RecallJsonResponseErrorHandler` 与
  `StandardRestClients.statusHandlers(...)`，以及
  `RestClientBuilderDecorator.defaultStatusHandler(...)` / `messageConverters(...)` 重载

### Dependencies

- febit-devkit 1.6.2 → 1.7.0
- spring 7.0.9 → 7.1.0-M2
- spring-boot 4.1.1 → 4.2.0-M2
- caffeine 3.2.4 → 3.3.0
- jackson 3.2.2 → 3.2.3
- jooq 3.21.7 → 3.21.8
- nimbus-jose-jwt 10.9.1 → 10.10
- slf4j 2.0.18 → 2.0.19
- protobuf 4.36.1 → 4.36.2
- mockito 5.23.0 → 5.24.0
- 新增 micrometer 1.18.0-M2
- 新增 spring-amqp 4.2.0-M2 与 rabbitmq amqp-client 5.36.0
- 新增 tabletest-junit 1.2.2
- 移除 hierynomus license 插件

### Build

- 用 Spotless 取代 license 插件，统一校验 license header 并规范化代码（import 顺序、无用 import、全限定类名缩短、行尾空白 /
  换行、
  表格测试格式）
- 移除构建脚本中父项目隐式查找的弃用写法（兼容 Gradle 10）

### Tests

- 测试改造为 `@TableTest` 表驱动写法，并补充新的 rabbit / etcd 组件测试

## [4.1.1] - 2026-09-10

### New Features

- **lang**: 新增 `NanosClock`
- **jackson**: `JacksonCodec` 支持序列化（`Serializable`）
- **jooq**: `Json` / `JsonString` / `Jsonb` 转换器支持指定 `JacksonCodec`
- **jpms**: 发布模块补充 `Automatic-Module-Name`

### Improvements

- **lang**: `TimeDelayed` 改用 `instanceof` 模式匹配
- **lang**: 废弃未使用的 `Singleton`
- **pubsub**: 简化 `subjectTypes` fallback

### Build

- 升级依赖与 Gradle Wrapper 至 9.7.1

### Tests

- **etcd**: 修复 `crossThreadUnlockFails` 在嵌入式 etcd 抖动下的不稳定

---

## [4.1.0] - 2026-06-24

### Breaking Changes

- **lang**: `PatternFormatter` 构建时复制 segments（原为共享引用）
- **lang**: 重命名 `JacksonWrapper` → `JacksonCodec`，精简 `JacksonUtils` 为 facade
- **lang**: Jackson 相关类移至 `jackson` 子包，算法类移至 `security` 子包，代理类移至 `proxy` 子包
- 移除废弃代码
- 升级至 Java 21

### New Features

- **etcd**: 新增 `febit-commons-etcd` 模块，基于 jetcd 实现 `EtcdLock` 分布式锁
- **lang**: `PatternFormatter` 新增 `matches()` 方法

### Fixes

- **jsonrpc2**: 修复 poster 失败时请求池泄漏，改用 raw future
- **jsonrpc2**: 修复默认将请求误作 notification dispatch
- **jooq**: 修复 page query 中显式字段选择导致的 mapper cast 异常

### Dependencies

- febit-devkit 1.6.1 → 1.6.2
- jackson 3.1.4 → 3.2.0
- jooq 3.19.33 → 3.21.5
- spring-boot 4.0.7 → 4.1.0
- okhttp 5.3.2 → 5.4.0
- spotbugs 4.9.8 → 4.10.2
- 新增 jetcd 0.8.6
- 新增 testcontainers 2.0.5
- 新增 protobuf 4.35.1
- 新增 h2database 2.4.240

### Build

- 大量补充测试

---

## [4.0.3] - 2026-06-14

### New Features

- **lang**: 新增 `nvl()` 方法，支持 `Supplier` 模式的空值处理

### Dependencies

- Gradle 9.4.1 → 9.5.1
- caffeine 3.2.3 → 3.2.4
- commons-codec 1.21.0 → 1.22.0
- commons-io 2.21.0 → 2.22.0
- jackson 3.1.2 → 3.1.4
- jooq 3.19.32 → 3.19.33
- kafka-clients 4.2.0 → 4.3.0
- nimbus-jose-jwt 10.9 → 10.9.1
- slf4j 2.0.17 → 2.0.18
- spring 7.0.7 → 7.0.8
- spring-boot 4.0.6 → 4.0.7
- swagger 2.2.48 → 2.2.49
- junit 6.0.3 → 6.1.0

---

## [4.0.2] - 2026-04-25

### New Features

- **jackson**: Jackson 3.0 适配 — 新增 `JacksonWrapper.mapper()`，暴露 immutable `Mapper`
- **modeler**: 新增 `ModeledValue`，支持 `DECIMAL` / `BYTE` 类型，改进类型转换

### Dependencies

- febit-devkit 1.6.0 → 1.6.1
- jackson 3.1.0 → 3.1.2
- jooq 3.19.30 → 3.19.32
- nimbus-jose-jwt 10.8 → 10.9
- spring 7.0.6 → 7.0.7
- spring-boot 4.0.3 → 4.0.6
- swagger 2.2.43 → 2.2.48
- 新增 semver4j 6.0.0
- 新增 snakeyaml2 2.6

---

## [4.0.1] - 2026-03-19

### Dependencies

- febit-devkit 1.5.0 → 1.6.0
- jackson 3.0.4 → 3.1.0
- jsonpath 2.10.0 → 3.0.0
- kafka-clients 4.1.1 → 4.2.0
- nimbus-jose-jwt 10.7 → 10.8
- spring 7.0.3 → 7.0.6
- spring-boot 4.0.2 → 4.0.3
- swagger 2.2.42 → 2.2.43
- snakeyaml → snakeyaml-engine 3.0.1
- junit 6.0.2 → 6.0.3
- mockito 5.21.0 → 5.23.0
- 新增 disruptor 4.0.0

---

## [4.0.0] - 2026-02-11

### Breaking Changes

- Jackson 2.x → 3.x (3.0.3)
- nullability 标注迁移至 jspecify 1.0.0
- 移除所有已废弃 API

### New Features

- **rest-client**: 新增 `febit-commons-rest-client` 模块
- **lang**: `Tuple` 支持 nullable
- **lang**: `IResponse` 新增 `ok()` 方法
- **lang**: 新增 `WildcardPathFilter`
- **test**: `JsonPathAssert` 增强泛型和 nullable 支持

### Dependencies

- Spring 7.0.2

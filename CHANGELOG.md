# Changelog

## [4.2.0] - not yet released

## [4.2.0-M1] - 2026-09-30

### New Features

- **rabbit**: Added `febit-commons-rabbit` module with `RabbitDelayQueue` — delayed message delivery over RabbitMQ:
  messages are parked in a
  TTL `wait` queue and re-hopped internally until the deadline, then published to the application's own ready exchange.
  Includes
  `DelayQueueOptions` / `DelayQueueTopology`, `DelayMessage` / `DelayReceipt`, pluggable `DelayPolicy`
  (`FixedRoundingDelayPolicy`) and
  `MessageIdGenerator` (`UuidIdGenerator`), and Micrometer-based `DelayQueueMetrics`
- **etcd**: Added the typed key-value store `org.febit.common.etcd.store`: `EtcdAccessor` (get / put / delete / txn /
  paging scan / typed
  watch) with `KVRecord`, `Put` / `SidePut`, `EtcdPagingScanner`, `TypedWatchObserver` / `TypedWatchListenerAdapter`,
  the codec SPI
  `KVCodec` / `Codec` (`GenericKVCodec`, `JsonCodec`, `PatternCodec`, `CodecUtils`), and Micrometer-based `EtcdMetrics`
- **caffeine**: Added `CaffeineDebouncer` (per-key debounce with optional max delay) and `CaffeineThrottle` (per-key
  cooldown window) to
  `febit-commons-stuff`

### Fixes

- **caffeine**: `CaffeineDebouncer` now arms its consumer thread before starting it, so a pending callback can no longer
  stall silently
- **jsonrpc2**: An unconvertible response no longer leaves the caller waiting — the request future now completes
  exceptionally with the cause
- **jsonrpc2**: Notification handler failures are now logged instead of escaping to the executor thread
- **stuff**: `ProcessFuture.cancel()` no longer throws when the process has not been created yet, and a repeated call no
  longer clears `isCancelled()`
- **stuff**: A process cancelled before it was created is now destroyed instead of being left running

### Deprecations

- **rest-client**: Aligned with Spring Framework 7.1 — deprecated `RecallJsonResponseErrorHandler` and
  `StandardRestClients.statusHandlers(...)`, together with the `RestClientBuilderDecorator.defaultStatusHandler(...)` /
  `messageConverters(...)` overrides

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
- Added micrometer 1.18.0-M2
- Added spring-amqp 4.2.0-M2 and rabbitmq amqp-client 5.36.0
- Added tabletest-junit 1.2.2
- Removed the hierynomus license plugin

### Build

- Replaced the license plugin with Spotless, which now enforces license headers and normalizes code style (import order,
  unused imports,
  shortened fully-qualified types, trailing whitespace / newline, table-test formatting)
- Removed the deprecated implicit lookup of build-script helpers in parent projects (Gradle 10 ready)

### Tests

- Converted test suites to table-driven `@TableTest` cases and added coverage for the new rabbit / etcd components

## [4.1.1] - 2026-09-10

### New Features

- **lang**: Added `NanosClock`
- **jackson**: `JacksonCodec` is now `Serializable`
- **jooq**: `Json` / `JsonString` / `Jsonb` converters now accept a specified `JacksonCodec`
- **jpms**: Added `Automatic-Module-Name` to published modules

### Improvements

- **lang**: `TimeDelayed` uses `instanceof` pattern matching
- **lang**: Deprecated unused `Singleton`
- **pubsub**: Simplified `subjectTypes` fallback

### Build

- Bumped dependencies and Gradle Wrapper to 9.7.1

### Tests

- **etcd**: Stabilized `crossThreadUnlockFails` against embedded etcd latency

---

## [4.1.0] - 2026-06-24

### Breaking Changes

- **lang**: `PatternFormatter` now copies segments on build (previously shared references)
- **lang**: Renamed `JacksonWrapper` → `JacksonCodec`, streamlined `JacksonUtils` to a facade
- **lang**: Moved Jackson classes to `jackson` subpackage, algorithms to `security` subpackage, proxy utilities to
  `proxy` subpackage
- Removed deprecated APIs
- Upgraded to Java 21

### New Features

- **etcd**: Added `febit-commons-etcd` module with `EtcdLock` distributed lock backed by jetcd
- **lang**: Added `PatternFormatter.matches()` method

### Fixes

- **jsonrpc2**: Fixed request pool leak on poster failure, switched to raw future
- **jsonrpc2**: Fixed accidental notification dispatch for requests by default
- **jooq**: Fixed mapper cast exception caused by explicit field selection in page query

### Dependencies

- febit-devkit 1.6.1 → 1.6.2
- jackson 3.1.4 → 3.2.0
- jooq 3.19.33 → 3.21.5
- spring-boot 4.0.7 → 4.1.0
- okhttp 5.3.2 → 5.4.0
- spotbugs 4.9.8 → 4.10.2
- Added jetcd 0.8.6
- Added testcontainers 2.0.5
- Added protobuf 4.35.1
- Added h2database 2.4.240

### Build

- Expanded test coverage

---

## [4.0.3] - 2026-06-14

### New Features

- **lang**: Added `nvl()` method for null handling with `Supplier` fallback

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

- **jackson**: Jackson 3.0 adaptation — added `JacksonWrapper.mapper()`, exposed immutable `Mapper`
- **modeler**: Added `ModeledValue`, support for `DECIMAL` / `BYTE` types, improved type conversion

### Dependencies

- febit-devkit 1.6.0 → 1.6.1
- jackson 3.1.0 → 3.1.2
- jooq 3.19.30 → 3.19.32
- nimbus-jose-jwt 10.8 → 10.9
- spring 7.0.6 → 7.0.7
- spring-boot 4.0.3 → 4.0.6
- swagger 2.2.43 → 2.2.48
- Added semver4j 6.0.0
- Added snakeyaml2 2.6

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
- Added disruptor 4.0.0

---

## [4.0.0] - 2026-02-11

### Breaking Changes

- Jackson 2.x → 3.x (3.0.3)
- Migrated nullability annotations to jspecify 1.0.0
- Removed all deprecated APIs

### New Features

- **rest-client**: Added `febit-commons-rest-client` module
- **lang**: `Tuple` now supports nullable values
- **lang**: Added `IResponse.ok()` method
- **lang**: Added `WildcardPathFilter`
- **test**: Enhanced `JsonPathAssert` with generics and nullable support

### Dependencies

- Spring 7.0.2

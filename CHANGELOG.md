# Changelog

## [4.1.0] - Unreleased

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

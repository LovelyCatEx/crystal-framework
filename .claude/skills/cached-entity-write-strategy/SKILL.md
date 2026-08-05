---
name: cached-entity-write-strategy
description: 为继承 CachedBaseService 的实体实现新建、更新、删除时的数据库与缓存一致性策略。
---

# Cached 实体写入策略

## 触发条件

当修改或新增继承 `CachedBaseService` 的 Service，涉及实体新建、更新、删除、upsert、`updateCache`、`removeCache` 或事务提交后的缓存处理时使用。

## 执行前检查

先阅读 `CachedBaseService` 和 `TransactionExtensions` 的实际方法签名，确认当前版本的缓存行为，不要凭记忆编造方法或自行复制缓存事务逻辑。

重点确认：

- `withInvalidateEntityCacheContext`
- `withUpdateEntityContext`
- `withDeleteEntityContext`
- `withBatchDeleteEntityContext`
- `getByIdOrNull`
- `updateCache`
- `removeCache`
- `runAfterCommitOrNow`

## 新建实体

新建实体默认只做缓存失效，不直接预热缓存。将数据库保存动作放入 `withInvalidateEntityCacheContext`：

```kotlin
val saved = withInvalidateEntityCacheContext(entity.id) {
    repository.save(entity).awaitFirstOrNull()
        ?: throw BusinessException("could not create entity")
}
```

该上下文负责：

1. 执行保存前删除实体缓存；
2. 执行数据库写入；
3. 事务提交后再次删除实体缓存；
4. 事务回滚时不执行提交后回调。

新建后禁止直接调用 `updateCache(entity)`，也禁止在业务 Service 中自行编排 `runAfterCommitOrNow { updateCache(entity) }`。后续读取通过 `getByIdOrNull` 懒加载并回填缓存，避免事务回滚留下不存在的实体缓存。

## 更新实体

已有实体更新必须使用 `withUpdateEntityContext`：

```kotlin
withUpdateEntityContext(existing) {
    // 修改实体
    repository.save(existing).awaitFirstOrNull()
        ?: throw BusinessException("could not update entity")
}
```

不得直接保存已缓存实体后再手动调用 `removeCache` 或 `updateCache`。更新上下文已经复用父类的两阶段缓存失效逻辑。

## 删除实体

单条删除使用 `withDeleteEntityContext(entityId)`，批量删除使用 `withBatchDeleteEntityContext(entityIds)`。删除后不预热已删除实体的缓存。

## 缓存与事务规则

标准流程是：

```text
写入前失效 → 执行数据库写入 → 事务提交后再次失效
```

`runAfterCommitOrNow` 由父类缓存上下文统一调用。业务 Service 不应重复维护提交回调、缓存删除或缓存预热逻辑。

## 影响面约束

- 优先复用 `CachedBaseService` 已有方法，不为单个 Service 修改公共基类的行为或签名。
- 区分缓存失效和缓存预热，不能用预热代替事务一致性处理。
- 数据库操作仍必须通过 Service 层完成，不能在 Controller 中直接注入 Repository。
- 修改完成后检查事务回滚、无事务调用和并发读取场景，确保没有新增缓存残留或陈旧缓存窗口。

---
paths:
  - "**/service/**/*.kt"
  - "**/service/**/*.java"
---

# Service 规范

## 包结构

使用 `service` / `service/impl` 的包结构。

## Service 继承规则

### 普通实体 Service
**必须继承 `CachedBaseService`** 以便使用实体对象缓存。

### Manager Service
对于 ManagerController 对应的 Service：
- **必须放入 `service/manager` 中**
- 遵守 `service/manager/impl` 结构
- **必须继承 `CachedBaseManagerService`**

有实体从属关系的请见 `EntityRelationshipCheckService` 的相关用法。

## 删改操作规则

对于实体的删改操作，必须使用 `CachedBaseService` 中的上下文方法包裹：

- `withUpdateEntityContext` - 包裹更新操作
- `withDeleteEntityContext` - 包裹删除操作

**禁止直接删改实体对象**，必须用 `withXXXContext` 包裹相关对象的操作。

❌ 错误：
```kotlin
repository.save(entity.copy(name = "new"))
```

✅ 正确：
```kotlin
withUpdateEntityContext(entity) {
    repository.save(entity.copy(name = "new"))
}
```

## Repository 规则

Repository 接口**必须继承 `BaseRepository`**，特殊情况（非 BaseEntity 子类的实体类）除外。

## 系统/租户设置读取规则

禁止在业务代码中逐条调用 `getSettings(declaration)` / `getSettings(tenantId, declaration)` 读取多个设置项。

**必须通过 Service 层提供的聚合方法**（如 `getSystemXxxSettings()` / `getTenantSettings(tenantId)`）一次性获取已缓存的设置对象，再从中取值。

逐条 `getSettings()` 仅允许在 Service 实现类内部的聚合方法中使用。

❌ 错误（业务代码）：
```kotlin
val setting1 = settingsService.getSettings(DECLARATION_1)
val setting2 = settingsService.getSettings(DECLARATION_2)
val setting3 = settingsService.getSettings(DECLARATION_3)
```

✅ 正确（业务代码）：
```kotlin
val settings = settingsService.getSystemXxxSettings()
val value1 = settings.field1
val value2 = settings.field2
```

✅ 正确（Service 实现类内部）：
```kotlin
override fun getSystemXxxSettings(): SystemXxxSettings {
    return SystemXxxSettings(
        field1 = getSettings(DECLARATION_1),
        field2 = getSettings(DECLARATION_2),
        field3 = getSettings(DECLARATION_3)
    )
}
```

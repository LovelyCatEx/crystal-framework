# 只读标准化控制器

## 设计意图

`ReadonlyManagerController` 并非重复实现，而是通过继承 `StandardManagerController` + 方法重写，以最小代价复用查询端点，同时禁止变更操作。

## 源码分析

完整源码在 `crystal-shared` 模块的 `com.lovelycatv.crystalframework.shared.controller` 包下：

```kotlin
@Validated
abstract class ReadonlyManagerController<
    SERVICE : CachedBaseManagerService<...>,
    ...
>(
    managerService: SERVICE
) : StandardManagerController<SERVICE, ...>(managerService) {
    override suspend fun create(...) =
        ApiResponse.forbidden("This resource is read-only and cannot be created")
    override suspend fun update(...) =
        ApiResponse.forbidden("This resource is read-only and cannot be updated")
    override suspend fun delete(...) =
        ApiResponse.forbidden("This resource is read-only and cannot be deleted")
}
```

核心逻辑：

- 三个方法被重写返回 `ApiResponse.forbidden()`，HTTP 状态码 403
- `list` 和 `query` 完全继承自 `StandardManagerController`，零改动
- 类型参数约束完全一致（仍然需要四种 DTO），这是 Kotlin 泛型继承的要求

## AOP 拦截链路

只读控制器仍被 `ManagerControllerPermissionAspect` 拦截：

```
StandardManagerController.* (切入点)
    → ManagerControllerPermissionAspect (@Order 较高)
        → @ManagerPermissions 校验
            → create/update/delete → 返回 403（业务层）
```

权限检查先于业务方法执行。即使 `@ManagerPermissions` 配置了写入权限，`create/update/delete` 也不会真正执行——安全策略是双保险。

## 命名规范

```kotlin
// 正确：约定俗成的命名
class ManagerXxxLogController

// 正确：也可以体现只读语义
class ManagerXxxLogReadonlyController
```

## 已知使用位置

| 模块 | Controller | 资源 |
|------|-----------|------|
| `crystal-starter` | `ManagerUserLoginLogController` | 用户登录日志 |
| `crystal-audit` | `ManagerAuditLogController` | 审计日志 |
| `crystal-mail` | `ManagerMailSendLogController` | 邮件发送记录 |

如需新增只读资源，确认该资源的数据完整由系统生成、不允许人工修改后再使用此模式。

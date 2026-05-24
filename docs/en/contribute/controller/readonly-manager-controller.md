# ReadOnly ManagerController

## Design Rationale

`ReadonlyManagerController` is not a separate reimplementation — it inherits from `StandardManagerController` and overrides three methods to block mutations, reusing the read endpoints with zero duplication.

## Source Analysis

Located in the `crystal-shared` module under `com.lovelycatv.crystalframework.shared.controller`:

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

Key design points:

- Three methods return `ApiResponse.forbidden()` (HTTP 403)
- `list` and `query` are inherited unchanged from `StandardManagerController`
- All four DTO types are still required in the type parameters — a Kotlin generics constraint

## AOP Chain

ReadOnly controllers are still intercepted by `ManagerControllerPermissionAspect`:

```
StandardManagerController.* (pointcut)
    → ManagerControllerPermissionAspect (@Order higher)
        → @ManagerPermissions check
            → create/update/delete → 403 (business layer)
```

Permission checks execute before the method body. Even if `@ManagerPermissions` grants write access, `create/update/delete` will never execute — a defense-in-depth approach.

## Known Implementations

| Module | Controller | Resource |
|--------|-----------|----------|
| `crystal-starter` | `ManagerUserLoginLogController` | User login logs |
| `crystal-audit` | `ManagerAuditLogController` | Audit logs |
| `crystal-mail` | `ManagerMailSendLogController` | Mail send records |

Before adding a new read-only resource, verify the data is entirely system-generated and must not be manually modified.

# ReadonlyManagerController

## Design intent

`ReadonlyManagerController` is not a from-scratch reimplementation of read-only CRUD. It extends `StandardManagerController` and overrides three methods, reusing the query endpoints at minimal cost while blocking mutations.

## Source

`crystal-shared/controller/ReadonlyManagerController.kt`:

```kotlin
@Validated
abstract class ReadonlyManagerController<
    SERVICE : CachedBaseManagerService<...>,
    ...
>(
    managerService: SERVICE
) : StandardManagerController<SERVICE, ...>(managerService) {

    override suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: CREATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be created")
    }

    override suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: UPDATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be updated")
    }

    override suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: DELETE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be deleted")
    }
}
```

Structural notes:

- Overrides only the 3 mutation methods, returning `ApiResponse.forbidden` unconditionally
- `list` and `query` remain untouched, fully inherited from `StandardManagerController`
- Type parameters match the parent verbatim — generic inheritance forces bound propagation

## Two-layer defense

Mutation methods return 403 even if called, but before reaching them `ManagerControllerPermissionAspect` runs a permission check first. Complete flow:

```
POST /create
  → ManagerControllerPermissionAspect (AOP)
      ├─ Checks @ManagerPermissions.create
      ├─ No permission → AuthorizationDeniedException (converted to 403 by GlobalExceptionHandler)
      └─ Has permission → continue
  → ReadonlyManagerController.create (business override)
      └─ Always returns ApiResponse.forbidden (403)
```

Motivation for two layers: the permission layer is generic defense (misconfig, mis-assigned role); the business layer is a structural constraint — this Controller type shall not accept writes, encoded in code. Even if permission config is wrong and lets the request through, the business layer catches it.

## Real-world permission configuration

All 5 fields of `@ManagerPermissions` usually hold the same read permission:

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],  // even if AOP passes, business rejects
    update  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
```

If `create` were filled with a nonexistent string, AOP would block first. Using the read permission has a benefit: read-permission holders who call `create` receive the business layer's semantic 403 ("cannot be created") rather than AOP's generic "Access denied", making it easier for the frontend to distinguish "this resource cannot be modified" from "you lack permission".

## Type parameter constraints

Identical to `StandardManagerController`. Kotlin's generic inheritance rules require:

```kotlin
abstract class ReadonlyManagerController<
    SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
    ...
> : StandardManagerController<SERVICE, ...>(managerService)
```

Bounds must be transmitted verbatim; Readonly cannot tighten them.

## AOP interception chain

Readonly Controllers are still caught by `ManagerControllerPermissionAspect` (pointcut is `StandardManagerController.*(..)`, covering all subclasses):

```
StandardManagerController.* (pointcut)
    → ManagerControllerPermissionAspect (@Order — higher priority)
        → @ManagerPermissions check
            → create / update / delete → business method (overridden to 403)
```

The audit aspect `ManagerControllerAuditAspect` covers this too — rejected calls are also recorded, for post-hoc analysis of anomalous access patterns.

## Naming conventions

Pick either style:

- `Manager{Xxx}Controller` (e.g. `ManagerMailSendLogController`) — externally indistinguishable from Standard
- `Manager{Xxx}ReadonlyController` — when you want "immutable resource" spelled out in the class name

Keep naming consistent within a module.

## Real usage locations

| Module | Controller | Resource |
|---|---|---|
| `crystal-audit` | `ManagerAuditLogController` | Audit log |
| `crystal-mail` | `ManagerMailSendLogController` | Mail-send records |
| `crystal-auth` | `ManagerUserLoginLogController` | User login log |

When adding a read-only resource, verify:

1. Data is fully system-generated (via triggers, event listeners, aspects, ...) with no user input
2. Modifications would break business invariants (editing audit logs destroys audit's purpose)

Only when both hold, use the Readonly family; otherwise consider Standard with fine-grained permission control.

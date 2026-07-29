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
    managerService: SERVICE,
    permissions: PermissionMatrix? = null,
) : StandardManagerController<SERVICE, ...>(
    managerService,
    permissions = permissions,
    mutability = Mutability.READ_ONLY,   // Key: CUD endpoints throw ForbiddenException at entry
)
```

Structural notes:

- The only addition is `Mutability.READ_ONLY`; `AbstractManagerController` checks the flag at the CUD endpoint entry and throws `ForbiddenException`
- `list` and `query` remain untouched, fully inherited from `StandardManagerController`
- Type parameters match the parent verbatim — generic inheritance forces bound propagation
- `permissions` is passed through to the parent — recommended: `PermissionMatrix.systemOnlyReadonly(...)` factory

## Two-layer defense

CUD endpoints are blocked by `Mutability.READ_ONLY` even if reached; before that, `StandardManagerController.authorize` (via `PermissionMatrix`) already ran a permission check. Complete flow:

```
POST /create
  → StandardManagerController.authorize
      ├─ Checks permissions.layersFor(SYSTEM, CREATE)
      ├─ No permission → AuthorizationDeniedException (converted to 403 by GlobalExceptionHandler)
      └─ Has permission → continue
  → AbstractManagerController entry
      └─ Mutability.READ_ONLY → throw ForbiddenException (403)
```

Motivation for two layers: the permission layer is generic defense (misconfig, mis-assigned role); the business layer is a structural constraint — this Controller type shall not accept writes, encoded in code. Even if permission config is wrong and lets the request through, the business layer catches it.

## Real-world permission configuration

Prefer `PermissionMatrix.systemOnlyReadonly(...)` — just fill `systemRead` (and optionally `superRead`):

```kotlin
permissions = PermissionMatrix.systemOnlyReadonly(
    systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
)
```

The factory sets the 4 CUD slots to `NEVER_GRANTED` and the 8 tenant slots to `NOT_APPLICABLE` — even if `Mutability` protection is bypassed, `NEVER_GRANTED` at the permission layer keeps CUD denied. That's the essence of defense in depth.

Manually stuffing real mutation permissions into CUD would still be blocked by `Mutability.READ_ONLY`, but you'd lose the permission-layer fail-safe; always use the factory rather than hand-constructing.

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

Readonly Controllers are still caught by `ManagerControllerPermissionAspect` (pointcut is `StandardManagerController.*(..)`, covering all subclasses), but it only kicks in on the legacy path where `permissions == null` and `@ManagerPermissions` is present; new code with `PermissionMatrix` is handled by the parent's `authorize` and passed through by AOP:

```
StandardManagerController.* (pointcut)
    → ManagerControllerPermissionAspect (@Order — higher priority)
        → permissions != null → proceed (authorize handled it)
        → permissions == null + @ManagerPermissions → legacy check path
    → AbstractManagerController → Mutability.READ_ONLY blocks CUD
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

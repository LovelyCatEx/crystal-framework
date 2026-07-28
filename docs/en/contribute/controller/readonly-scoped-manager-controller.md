# ReadonlyScopedManagerController

## Design intent

`ReadonlyScopedManagerController` is a direct subclass of `StandardScopedManagerController`. The design tenor mirrors `ReadonlyManagerController`: extend + override three methods, at minimum cost realizing a "scope-aware read-only resource".

## Source

`crystal-shared/controller/ReadonlyScopedManagerController.kt`:

```kotlin
@Validated
abstract class ReadonlyScopedManagerController<...>(
    managerService: SERVICE,
    permissions: PermissionMatrix? = null,
) : StandardScopedManagerController<SERVICE, ...>(
    managerService,
    permissions,
    mutability = Mutability.READ_ONLY,   // CUD blocked by AbstractManagerController's ForbiddenException
)
```

Structurally symmetric to `ReadonlyManagerController`, only the parent changes to `StandardScopedManagerController`; the only real addition is `Mutability.READ_ONLY`.

## Two-layer defense

The Scoped-family read-only combines the permission-layer `NEVER_GRANTED` fallback with the parent `Mutability.READ_ONLY` block. Full flow:

```
POST /create
  → StandardScopedManagerController.create (parent implementation)
      → assertAccess → checkPermission(scope, scopeId, CREATE, userAuth)
          └─ matrix.layersFor(scope, CREATE) returns [NEVER_GRANTED, NEVER_GRANTED, ...]
              → hasAnyAuthority("!!never_granted!!", ...) = false
                  → ForbiddenException (Layer 1: permission rejection)

Even if the permission layer is bypassed (e.g. subclass overrides checkPermission to always true):
  → AbstractManagerController entry
      └─ Mutability.READ_ONLY → throw ForbiddenException (Layer 2: business rejection)

If CUD slots were manually filled with a real read permission:
  → checkPermission passes (a user holding read perm trips the CREATE slot too)
      → But Mutability.READ_ONLY still blocks   ← fail-safe backstop
```

That's why `PermissionMatrix.readonly(...)` forces CUD slots to `NEVER_GRANTED` — not cosmetic, but the backstop against permission-escalation bugs, combined with `Mutability.READ_ONLY` for two independent layers.

## Reusing the base Delete DTO

Real usage from `ManagerApprovalFlowInstanceController`:

```kotlin
class ManagerApprovalFlowInstanceController(...) : ReadonlyScopedManagerController<
    ApprovalFlowInstanceManagerService,
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO                             // ← use base directly
>
```

Because delete returns 403, the DTO's contents are irrelevant — the business method never runs. Skip the ceremony, avoid an empty-shell DTO file.

Recommendation: only `ManagerReadXxxDTO` needs a proper definition (must carry scope + pagination); the other three may reuse bases or stay minimal.

## Query-endpoint smart filtering

A common pattern with `ReadonlyScopedManagerController`: shape query results by user permission. See `ManagerApprovalFlowInstanceController`:

```kotlin
override suspend fun checkPermission(
    scope, scopeId, operation, userAuth
): Boolean {
    return operation == ScopedOperation.READ   // all logged-in users allowed on read
}

override suspend fun buildQueryResponse(
    dto: ManagerReadApprovalFlowInstanceDTO,
    userAuthentication: UserAuthentication,
): Any {
    val resolvedScope = resolveScope(dto.scope)
    val matrix = permissions ?: error(...)
    val canReadAll = RbacUtils.hasAnyAuthority(*matrix.layersFor(resolvedScope, ScopedOperation.READ))

    val effectiveDto = if (canReadAll) dto
                       else dto.copy(query = appendInitiatorCondition(dto.query, initiatorId))

    return managerService.query(effectiveDto)
}
```

Pattern essentials:

- `checkPermission` always returns true (for read) — permission doesn't decide "can query?" but "how much can be queried?"
- Permission judgment moves into `buildQueryResponse`: hold read permission → full results; otherwise → inject `initiator_id = current user`
- `dto.copy(query = ...)` modifies the QueryNode: `BaseManagerReadDTO.query` is a `QueryNode`-typed condition tree; append `AND initiator_id = X` to the root
- `appendInitiatorCondition` is a private helper using `GroupNode(logic = AND, children = [existing, initiatorCondition])`

Benefit: one endpoint serves both audiences; the frontend doesn't need to know about permission differences — list logic is uniform, permission filtering happens transparently on the backend.

## vs. ReadonlyManagerController

| | ReadonlyManagerController | ReadonlyScopedManagerController |
|---|---|---|
| Parent | `StandardManagerController` | `StandardScopedManagerController` |
| Permission declaration | `PermissionMatrix.systemOnlyReadonly(...)` | `PermissionMatrix.readonly(...)` (16 CUD slots `NEVER_GRANTED`) |
| Scope support | None | Mandatory SYSTEM / TENANT |
| Entity bound | `BaseEntity` | `BaseScopedEntity` |
| Mutability | `READ_ONLY` (parent blocks CUD) | Same |
| NEVER_GRANTED fallback | Covered by `systemOnlyReadonly` at the permission layer | Covered by `readonly` at the permission layer |

## Real usage locations

| Module | Controller | Resource |
|---|---|---|
| `crystal-approval` | `ManagerApprovalFlowInstanceController` | Approval flow instances (read + start, no edits) |
| `crystal-approval` | `ManagerApprovalFlowTaskController` | Approval tasks (read + approve/reject, no edits) |

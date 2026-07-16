# ReadonlyScopedManagerController

## Design intent

`ReadonlyScopedManagerController` is a direct subclass of `StandardScopedManagerController`. The design tenor mirrors `ReadonlyManagerController`: extend + override three methods, at minimum cost realizing a "scope-aware read-only resource".

## Source

`crystal-shared/controller/ReadonlyScopedManagerController.kt`:

```kotlin
@Validated
abstract class ReadonlyScopedManagerController<...>(
    managerService: SERVICE,
    permissions: ScopedPermissionTriad? = null,
) : StandardScopedManagerController<SERVICE, ...>(managerService, permissions) {

    override suspend fun create(userAuthentication, @ModelAttribute dto: CREATE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be created")
    }

    override suspend fun update(userAuthentication, @ModelAttribute dto: UPDATE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be updated")
    }

    override suspend fun delete(userAuthentication, @ModelAttribute dto: DELETE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be deleted")
    }
}
```

Structurally symmetric to `ReadonlyManagerController`, only the parent changes to `StandardScopedManagerController`.

## Triple-layer defense

The Scoped-family read-only adds a `NEVER_GRANTED` layer at the permission level on top of `ReadonlyManagerController`'s design. Full flow:

```
POST /create
  → StandardScopedManagerController.create (parent method, not AOP)
      └─ Overridden by ReadonlyScopedManagerController
          → Returns ApiResponse.forbidden (Layer 1: business rejection)

Even if ReadonlyScoped is bypassed, the parent's create calls assertAccess:
  → checkPermission(scope, scopeId, CREATE, userAuth)
      └─ triad.forScope(scope, CREATE) returns [NEVER_GRANTED, NEVER_GRANTED]
          → hasAnyAuthority("!!never_granted!!", "!!never_granted!!") = false
              → ForbiddenException (Layer 2: permission rejection)

If instead super / system / tenantPem CRUD slots were filled with the real read permission:
  → checkPermission passes (a user holding read perm trips the CREATE slot too)
      → checkOwnership may also pass
          → managerService.create(dto) actually runs   ← Disaster
```

That's why `ScopedPermissionTriad.readonly(...)` forces CRUD slots to `NEVER_GRANTED` — not cosmetic, but the backstop against permission-escalation bugs.

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
    val triad = permissions ?: error(...)
    val canReadAll = RbacUtils.hasAnyAuthority(*triad.forScope(resolvedScope, ScopedOperation.READ))

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
| Permission declaration | `@ManagerPermissions` (class-level, 5 fields) | `ScopedPermissionTriad` (constructor arg, 12 slots) |
| Scope support | None | Mandatory SYSTEM / TENANT |
| Entity bound | `BaseEntity` | `BaseScopedEntity` |
| AOP coverage | Yes | No, inline self-check |
| NEVER_GRANTED fallback | None (unnecessary) | Yes (critical) |

## Real usage locations

| Module | Controller | Resource |
|---|---|---|
| `crystal-approval` | `ManagerApprovalFlowInstanceController` | Approval flow instances (read + start, no edits) |
| `crystal-approval` | `ManagerApprovalFlowTaskController` | Approval tasks (read + approve/reject, no edits) |

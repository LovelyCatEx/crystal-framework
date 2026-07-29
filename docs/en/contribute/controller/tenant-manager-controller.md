# StandardTenantManagerController

## Design intent

`StandardTenantManagerController` is the tenant-resource base predating the Scoped family. It established the dual-layer authorization model (originally "system permission + tenant permission"; under the unified `PermissionMatrix` these become `tenantAdmin` + `tenantPem`) and uses `isXxxInScope` hooks to handle "not-directly-tenant" nested resources (e.g. department members).

Historical timeline:

- v1.x early: all tenant resources used this Controller
- Post-v1.10: extracted the generic `StandardScopedManagerController`, generalizing "cross-SYSTEM/TENANT dual scope"
- Now: legacy tenant resources still live here; permissions unified to `PermissionMatrix` (the 8-String constructor is `@Deprecated` and delegates to `PermissionMatrix.tenantOnly(...)`); new "tenant-only" resources may pick this or Scoped (using it with scope locked to TENANT)

Do not extend this base with new abstractions — new capabilities belong on the Scoped family.

## Source

`crystal-shared/controller/StandardTenantManagerController.kt` (condensed):

```kotlin
@Validated
abstract class StandardTenantManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: PermissionMatrix,   // primary constructor uses Matrix
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long> {

    // Legacy 8-String constructor kept one release with @Deprecated; delegates to the primary
    @Deprecated("Use the primary constructor with PermissionMatrix.tenantOnly(...) instead.")
    constructor(
        managerService, createPermission, scopedCreatePermission,
        readPermission, scopedReadPermission,
        updatePermission, scopedUpdatePermission,
        deletePermission, scopedDeletePermission,
    ) : this(
        managerService,
        permissions = PermissionMatrix.tenantOnly(
            tenantAdminCreate = createPermission, tenantAdminRead = readPermission,
            tenantAdminUpdate = updatePermission, tenantAdminDelete = deletePermission,
            tenantPemCreate = scopedCreatePermission, tenantPemRead = scopedReadPermission,
            tenantPemUpdate = scopedUpdatePermission, tenantPemDelete = scopedDeletePermission,
        ),
    )

    companion object {
        @Deprecated("Use PermissionMatrix.NOT_APPLICABLE instead")
        const val DISABLED_SCOPED_PERMISSION: String = PermissionMatrix.NOT_APPLICABLE
    }

    private suspend fun hasScopedAuthority(authority: String): Boolean {
        // Both empty string (legacy DISABLED_SCOPED_PERMISSION) and NOT_APPLICABLE short-circuit
        if (authority.isBlank() || authority == PermissionMatrix.NOT_APPLICABLE) return false
        return RbacUtils.hasAuthority(authority)
    }

    // Scope-check hooks (override to customize)
    protected suspend fun isCreateInScope(dto: CREATE_DTO, userAuth): Boolean { ... }
    protected suspend fun isQueryInScope(dto: READ_DTO, userAuth): Boolean { ... }
    protected suspend fun isReadAllInScope(tenantId: Long, userAuth): Boolean { ... }
    protected suspend fun isUpdateInScope(dto: UPDATE_DTO, userAuth): Boolean = managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId!!)
    protected suspend fun isDeleteInScope(dto: DELETE_DTO, userAuth): Boolean = managerService.checkIsRelatedToRootParent(dto.ids, userAuth.tenantId!!)

    // Response shaping hooks
    protected suspend fun buildQueryResponse(dto: READ_DTO): Any = managerService.query(dto)
    protected suspend fun buildReadAllResponse(tenantId: Long): Any = managerService.findAllByTenantId(tenantId)

    // Flow-takeover hooks
    protected suspend fun customCreate(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customQuery(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customUpdate(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customDelete(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customReadAll(userAuth, tenantId): ApiResponse<*>? = null

    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> {
        customCreate(userAuth, dto)?.let { return it }

        if (RbacUtils.hasAuthority(createPermission)) {
            managerService.create(dto)
        } else if (hasScopedAuthority(scopedCreatePermission)) {
            userAuth.assertTenantIdNotNull()
            if (isCreateInScope(dto, userAuth)) managerService.create(dto)
            else throw UnauthorizedException()
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    // update / delete / readAll / query are structurally similar
}
```

## Key design decisions

### From 8 Strings to PermissionMatrix.tenantOnly

The Tenant Controller originally used 8 separate String parameters (`createPermission` / `scopedCreatePermission` / …). Its short-comings:

- No explicit `super` layer — cross-tenant admin relied on system permissions, muddling inheritance semantics
- Very long constructor signature — 8 String parameters are easy to reorder incorrectly
- No convenient way to express a "read-only" variant — no factory like `readonly(...)`

`PermissionMatrix.tenantOnly(...)` is now the primary constructor and names the two layers directly: `tenantAdmin*` + `tenantPem*`. The legacy 8-String constructor is retained one release (`@Deprecated`) — it maps `createPermission` → `tenantAdminCreate`, `scopedCreatePermission` → `tenantPemCreate`, etc., and delegates to the primary constructor. Legacy protected properties like `createPermission` also expose compat getters (e.g. `get() = permissions.tenantAdminCreate`), so existing subclasses do not need changes.

### Two-layer: tenantAdmin → tenantPem

Authorization order:

```
1. RbacUtils.hasAuthority(tenantAdminPermission) → true → allow immediately, skip in-scope
2. hasScopedAuthority(tenantPemPermission) → true → run isXxxInScope → true then allow
3. Neither → 403
```

Skipping in-scope for `tenantAdmin` holders is deliberate — the `tenantAdmin` permission itself means "cross-tenant admin capability"; no need to further gate by tenantId. The `tenantPem` permission is "authorization within my own tenant" and must prove the resource lies within. The super / system layers default to `NOT_APPLICABLE` here (Tenant resources have no SYSTEM scope concept).

### `DISABLED_SCOPED_PERMISSION` and `NOT_APPLICABLE`

The legacy `DISABLED_SCOPED_PERMISSION = ""` (empty string) disables the scoped layer; `hasScopedAuthority` short-circuits on empty. `PermissionMatrix.NOT_APPLICABLE` (`"!!not_applicable!!"`) is now preferred — `hasScopedAuthority` short-circuits both, semantically equivalent but clearer:

```kotlin
private suspend fun hasScopedAuthority(authority: String): Boolean {
    // Both empty string (legacy DISABLED_SCOPED_PERMISSION) and NOT_APPLICABLE short-circuit
    if (authority.isBlank() || authority == PermissionMatrix.NOT_APPLICABLE) return false
    return RbacUtils.hasAuthority(authority)
}
```

Contrast `PermissionMatrix.NEVER_GRANTED` — a permission-looking string that never matches; runs through `hasAuthority` and always returns false. `NOT_APPLICABLE` / empty string short-circuit before the check — different intent.

### isUpdateInScope / isDeleteInScope use chain lookup

Default implementation:

```kotlin
protected suspend fun isUpdateInScope(dto: UPDATE_DTO, userAuth): Boolean {
    return managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId!!)
}
```

`checkIsRelatedToRootParent` (from `TenantRelationshipCheckService` in `crystal-shared`):

1. Fetch the child entity by `dto.id`
2. Call `entity.getDirectParentId()` for the direct parent's id
3. Fetch the parent entity by that id
4. If the parent is also `ScopedEntity`, recurse steps 2–3
5. Until reaching top-level (e.g. `TenantEntity`), compare against target tenantId

Not comparing `entity.tenantId == userAuth.tenantId` directly, because nested resources lack a direct tenantId field — a department member's tenant lives on the department, and the department's tenant lives on the tenant itself. Chain walking is the only general solution.

Cost: each update / delete triggers multiple DB queries (one per level). `CachedBaseService` caching on parent entities amortizes this.

### customXxx hooks

Every endpoint has a `customXxx` hook allowing subclasses to fully replace the standard flow:

```kotlin
@PostMapping("/create")
suspend fun create(userAuth, dto): ApiResponse<*> {
    customCreate(userAuth, dto)?.let { return it }   // ← non-null returns immediately
    // Standard flow...
}
```

Design motivation: v1.x early code needed flexible override points per endpoint. Rarely used now — endpoint-specific logic can just override `create` itself. The custom hooks remain because:

- Legacy code references them
- Some cases genuinely need "run custom logic first; if it doesn't apply, fall through to standard" (return null = standard, non-null = short-circuit)

New code should not lean on them.

## `ScopedEntity<Long>` instead of `ScopedEntity<*>`

The Tenant Controller strictly requires `ScopedEntity<Long>` — parent id must be Long. Reasons:

- Default `checkIsRelatedToRootParent` recursion queries DB by Long ids
- Supporting `Any` parent ids would lose recursion type safety and require runtime casts

Contrast DerivedScoped, which uses `ScopedEntity<*>` (star projection), because DerivedScoped doesn't call `getDirectParentId()` — the constraint is only there to guarantee "the child knows its parent".

## Real usage locations

| Module | Controller | Resource |
|---|---|---|
| `crystal-rbac` | `ManagerTenantRoleController` | Tenant roles |
| `crystal-tenant` | `ManagerTenantMemberController` | Tenant members |
| `crystal-tenant` | `ManagerTenantDepartmentController` | Tenant departments |
| `crystal-tenant` | `ManagerTenantDepartmentMemberController` | Department members (chain lookup) |
| `crystal-tenant` | `ManagerTenantMessageChannelController` | Tenant message channels |
| `crystal-tenant` | `ManagerTenantInvitationController` | Tenant invitations |

New tenant-only resources may continue to use this Controller if it fits; if a SYSTEM version might exist someday, consider [StandardScopedManagerController](./scoped-manager-controller) in TENANT-only mode.

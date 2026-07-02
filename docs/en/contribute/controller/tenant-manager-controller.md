# StandardTenantManagerController

## Design intent

`StandardTenantManagerController` is the tenant-resource base predating the Scoped family. It established the "system permission + tenant permission" dual-layer authorization model and uses `isXxxInScope` hooks to handle "not-directly-tenant" nested resources (e.g. department members).

Historical timeline:

- v1.x early: all tenant resources used this Controller
- Post-v1.10: extracted the generic `StandardScopedManagerController`, generalizing "cross-SYSTEM/TENANT dual scope"
- Now: legacy tenant resources still live here; new "tenant-only" resources may pick this or Scoped (using it with scope locked to TENANT)

Do not extend this base with new abstractions — new capabilities belong on the Scoped family.

## Source

`crystal-shared/controller/StandardTenantManagerController.kt` (condensed):

```kotlin
@Validated
abstract class StandardTenantManagerController<...>(
    protected val managerService: SERVICE,
    protected val createPermission: String,        protected val scopedCreatePermission: String,
    protected val readPermission: String,          protected val scopedReadPermission: String,
    protected val updatePermission: String,        protected val scopedUpdatePermission: String,
    protected val deletePermission: String,        protected val scopedDeletePermission: String,
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long> {

    companion object {
        const val DISABLED_SCOPED_PERMISSION: String = ""
    }

    private suspend fun hasScopedAuthority(authority: String): Boolean {
        if (authority.isBlank()) return false
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

### 8 Strings instead of a structured Triad

The Tenant Controller uses 8 separate String parameters: `createPermission` / `scopedCreatePermission` / …. Compared with the Scoped family's `ScopedPermissionTriad` data class:

- No explicit `super` layer — cross-tenant admin relies on system permissions; permission-inheritance semantics get muddled
- Very long constructor signature — 8 String parameters are easy to reorder incorrectly
- No convenient way to express a "read-only" variant — no factory like `Triad.readonly(...)`

Rewriting is expensive; the design lingers.

### Two-layer: system → scoped

Authorization order:

```
1. RbacUtils.hasAuthority(systemPermission) → true → allow immediately, skip in-scope
2. hasScopedAuthority(scopedPermission) → true → run isXxxInScope → true then allow
3. Neither → 403
```

Skipping in-scope for system holders is deliberate — the system permission itself means "cross-tenant admin capability"; no need to further gate by tenantId. The scoped permission is "authorization within my own tenant" and must prove the resource lies within.

### `DISABLED_SCOPED_PERMISSION = ""`

Empty string disables the scoped layer:

```kotlin
private suspend fun hasScopedAuthority(authority: String): Boolean {
    if (authority.isBlank()) return false
    return RbacUtils.hasAuthority(authority)
}
```

Empty string over nullable: in Kotlin, `""` as a constant is simpler than `null` (a `const val` accepts only non-null literals). Semantics: "empty string = scoped permission not configured".

Contrast `ScopedPermissionTriad.NEVER_GRANTED` — a permission-looking string that never matches (`"!!never_granted!!"`); runs through `hasAuthority` and always returns false. Empty string here short-circuits before the check — different intent.

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

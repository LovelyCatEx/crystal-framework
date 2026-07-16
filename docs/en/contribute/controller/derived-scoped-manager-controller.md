# StandardDerivedScopedManagerController

## Design intent

`StandardDerivedScopedManagerController` complements the Scoped family. It handles a specific resource model: scope exists but not on the entity itself; instead it lives at the end of a parent relationship chain. Canonical case: "dict item → dict type → dict type's scope".

Trade-offs:

- Don't force a scope column onto the child — if the dict item redundantly stored `scope` + `scopeId`, two sources must stay in sync (changing a type's scope must cascade to every child item), adding maintenance burden and inconsistency risk
- Don't retrofit `StandardScopedManagerController` — that would clutter the base with conditional branches ("if there's a scope column, go this way, else that way")
- Separate base class: `StandardDerivedScopedManagerController` uses abstract methods to hand off "how to derive scope from context" to subclasses

## Source

`crystal-shared/controller/StandardDerivedScopedManagerController.kt` (condensed):

```kotlin
@Validated
abstract class StandardDerivedScopedManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: ScopedPermissionTriad,      // required, non-nullable
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<*> {

    // Three abstract methods: resolve scope from different sources
    protected abstract suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long>
    protected abstract suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long>
    protected abstract suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long>

    protected open suspend fun checkOwnership(scope, scopeId: Long, operation, userAuth): Boolean {
        return when (scope) {
            SYSTEM -> true
            TENANT -> {
                if (RbacUtils.hasAuthority(permissions.superFor(operation))) true
                else scopeId == userAuth.tenantId
            }
        }
    }

    protected open suspend fun buildQueryResponse(dto, userAuth): Any = managerService.query(dto)

    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> {
        val (scope, scopeId) = resolveScopeFromCreateDTO(dto)
        assertAccess(scope, scopeId, CREATE, userAuth)
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query") suspend fun query(userAuth, dto: READ_DTO): ApiResponse<*> {
        val (scope, scopeId) = resolveScopeFromReadDTO(dto)
        assertAccess(scope, scopeId, READ, userAuth)
        return ApiResponse.success(buildQueryResponse(dto, userAuth))
    }

    @PostMapping("/update") suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> {
        val entity = managerService.getByIdOrThrow(dto.id)
        val (scope, scopeId) = resolveScopeFromEntity(entity)
        assertAccess(scope, scopeId, UPDATE, userAuth)
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete") suspend fun delete(userAuth, dto: DELETE_DTO): ApiResponse<*> {
        val entities = dto.ids.map { managerService.getByIdOrThrow(it) }
        val resolved = entities.map { resolveScopeFromEntity(it) }
        resolved.toSet().forEach { (scope, scopeId) ->
            assertAccess(scope, scopeId, DELETE, userAuth)
        }
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
```

## Key design decisions

### No `/list` endpoint

`StandardScopedManagerController` has `readAll(scope, scopeId)` — full enumeration within a scope. DerivedScoped deliberately omits it:

- Derived entities semantically "belong to a parent"; without parent context the enumerated set is chaotic — thousands of dict items across multiple types thrown together are meaningless
- Forces business logic through a custom endpoint (e.g. `/tree?typeId=xxx`), so list results are always a semantically complete group of children
- Simpler permission-wise too: enumerating rows across multiple scopes requires multiple scope checks in one call — hard to make correct and cheap

### `where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>` union constraint

Kotlin's `where` clause expresses multi-bound generics, requiring simultaneously:

1. `BaseEntity`: guarantees id / timestamps / soft-delete plumbing
2. `ScopedEntity<*>`: guarantees `getDirectParentId()` (usable by `TenantRelationshipCheckService` for chain walks)

`ScopedEntity<*>` uses star projection — the parent id type doesn't matter; DerivedScoped's three `resolveScopeFromXXX` hooks use concrete types and don't call `ScopedEntity<T>.getDirectParentId()`. Star projection preserves the interface contract without pinning the type.

`ScopedEntity` and `getDirectParentId()` are actually consumed by `TenantRelationshipCheckService.checkIsRelatedToRootParent` (used by the Tenant family). Here the constraint only guarantees "the child knows its parent".

### `permissions` parameter is non-nullable

Compared with `StandardScopedManagerController`'s `permissions: ScopedPermissionTriad? = null` (nullable; subclass must then override `checkPermission`), DerivedScoped's `permissions: ScopedPermissionTriad` is strictly non-nullable:

- Derived-entity authorization follows the standard 12-slot pattern
- No reason to fully bypass the Triad — no equivalent of the "any logged-in user can read" pattern seen in `ManagerApprovalFlowInstanceController`
- Non-nullable simplifies the implementation and avoids `?: error(...)` branches

## Update / Delete scope tracing

Same as `StandardScopedManagerController` — scope for update / delete is read from the DB entity; client-sent scope is ignored. Anti-tampering.

Batch delete groups by `resolveScopeFromEntity`'s result to dedupe, checking each distinct `(scope, scopeId)` once.

## Relationship with TenantRelationshipCheckService

You might expect DerivedScoped to reuse `TenantRelationshipCheckService.checkIsRelatedToRootParent` for "walk child to parent scope". Instead it lets the subclass implement `resolveScopeFromXXX` directly. Rationale:

- `TenantRelationshipCheckService` implements a generic recursive chain — each level asks "who's my parent?" up to root. Meaningful for deep chains but overkill for one-hop cases like dict items
- A subclass's `resolveScopeByTypeId(typeId)` makes one Service call and gets scope — faster and clearer
- Deep-chain cases can still call `checkIsRelatedToRootParent` inside `resolveScopeFromXXX` — the base leaves the "call style" abstract without prescribing a path

## Real usage locations

| Module | Controller | Derivation chain |
|---|---|---|
| `crystal-tenant` | `ManagerTenantDictItemController` | Dict item → dict type (`typeId`) → dict type's scope |

Currently one use site in the project. Future "child-scoped-via-parent" cases should reuse this base.

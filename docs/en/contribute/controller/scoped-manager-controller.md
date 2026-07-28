# StandardScopedManagerController

## Design intent

`StandardScopedManagerController` solves what `StandardTenantManagerController` couldn't express cleanly: the same resource may belong to either SYSTEM or TENANT scope. Core abstractions are `ResourceScope` + `PermissionMatrix`, decoupling "what is the scope" from "which permission".

## Three core abstractions

### ResourceScope

```kotlin
enum class ResourceScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);
    companion object {
        fun getById(typeId: Int): ResourceScope? = entries.firstOrNull { it.typeId == typeId }
    }
}
```

`typeId` is the serialized value (a DTO's `scope: Int` is exactly this); the Enum itself never appears in JSON. Choosing `Int` over String is deliberate:

- Compact on-disk footprint
- Adding new scope types later (e.g. `DEPARTMENT(2)`) does not invalidate old data

### ScopedOperation

```kotlin
enum class ScopedOperation { CREATE, READ, UPDATE, DELETE }
```

Four CRUD values, used inside Triad to dispatch permissions by operation.

### PermissionMatrix

```kotlin
data class PermissionMatrix(
    val superCreate: String, val superRead: String, val superUpdate: String, val superDelete: String,
    val systemCreate: String, val systemRead: String, val systemUpdate: String, val systemDelete: String,
    val tenantAdminCreate: String, val tenantAdminRead: String, val tenantAdminUpdate: String, val tenantAdminDelete: String,
    val tenantPemCreate: String, val tenantPemRead: String, val tenantPemUpdate: String, val tenantPemDelete: String,
) {
    fun layersFor(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(op), systemFor(op))
        ResourceScope.TENANT -> arrayOf(superFor(op), tenantAdminFor(op), tenantPemFor(op))
    }.filter { it != NOT_APPLICABLE }.toTypedArray()

    fun crossTenantLayersFor(operation: ScopedOperation): Array<String> = arrayOf(
        superFor(op), tenantAdminFor(op),
    ).filter { it != NOT_APPLICABLE }.toTypedArray()
}
```

16 permissions = 4 layers × 4 operations. Rationale:

- The 4 layers are not "permission inheritance" but "authorization dimensions":
  - `super`: cross-scope (framework admin)
  - `system`: SYSTEM only
  - `tenantAdmin`: TENANT but cross-tenant (ops admin) — extracted from the old Triad's "super doubles as cross-tenant" role into an independent layer
  - `tenantPem`: TENANT and own tenant only
- `layersFor()` returns an array; `hasAnyAuthority(...)` OR-matches it
- SYSTEM requests use `[super, system]`, TENANT requests use `[super, tenantAdmin, tenantPem]`
- `crossTenantLayersFor()` extracts `[super, tenantAdmin]`, consumed by `checkOwnership` to identify the cross-tenant layers
- No `[super, system, tenantPem]` combined form — SYSTEM resources must not be operated on by tenantPem holders (scope isolation)
- `NOT_APPLICABLE` is filtered by `layersFor` / `crossTenantLayersFor`, so "layer doesn't apply" is a no-op rather than a dead policy

## Two sentinel values: NOT_APPLICABLE vs NEVER_GRANTED

```kotlin
companion object {
    /** Layer doesn't apply to the resource (e.g. tenant layers on a SYSTEM-only resource) */
    const val NOT_APPLICABLE: String = "!!not_applicable!!"

    /** Layer exists but the op is sealed (Readonly variants use this for CUD) */
    const val NEVER_GRANTED: String = "!!never_granted!!"

    fun readonly(superRead, systemRead, tenantAdminRead, tenantPemRead) = PermissionMatrix(
        superCreate = NEVER_GRANTED, superRead = superRead, superUpdate = NEVER_GRANTED, superDelete = NEVER_GRANTED,
        systemCreate = NEVER_GRANTED, systemRead = systemRead, systemUpdate = NEVER_GRANTED, systemDelete = NEVER_GRANTED,
        tenantAdminCreate = NEVER_GRANTED, tenantAdminRead = tenantAdminRead, tenantAdminUpdate = NEVER_GRANTED, tenantAdminDelete = NEVER_GRANTED,
        tenantPemCreate = NEVER_GRANTED, tenantPemRead = tenantPemRead, tenantPemUpdate = NEVER_GRANTED, tenantPemDelete = NEVER_GRANTED,
    )
}
```

Both sentinels are deliberate dead strings: the `!!` prefix/suffix violates the project's `<module>.<resource>.<op>` naming convention, they don't belong to any real permission constant, and the `root` role's auto-grant excludes them. But their semantics differ:

- **`NOT_APPLICABLE`**: `layersFor` **filters it out**, excluded from the OR-check. Use it for "layer doesn't apply to this resource" (e.g. tenant layers on a SYSTEM-only resource) — one fewer element in the array, equivalent to "this layer doesn't exist"
- **`NEVER_GRANTED`**: `layersFor` **keeps it as a placeholder** so the OR-check sees it and fails deterministically. Use it for "layer exists but the op is sealed off" (Readonly variants' CUD) — fail-safe, not fail-open

Consider what would happen if `readonly(...)`'s CRUD slots held the read permission:

1. User A holds the read permission
2. `ReadonlyScopedManagerController` gets bypassed or mistakenly refactored to `Standard`
3. `matrix.superFor(CREATE)` returns the read permission
4. `hasAnyAuthority(readPerm, ...) = true`
5. User A's read permission is silently upgraded to write permission

`NEVER_GRANTED` prevents this: even in that scenario, `hasAnyAuthority("!!never_granted!!", ...)` is always false, denying deterministically. Proper defensive coding — error-path failure mode is safe (fail-safe), not silent permission escalation (fail-open).

## Prefix convention & collectPrefixViolations

```kotlin
init {
    collectPrefixViolations(this).forEach { logger.warn("PermissionMatrix: $it. This will become a hard error in a future release.") }
}
```

Each of the 4 layers has a required prefix: `super*` may not carry `system.` / `tenant.` / `i.tenant.`; `system*` must start with `system.`; `tenantAdmin*` with `tenant.`; `tenantPem*` with `i.tenant.`. `init` emits a warn log on violations; `collectPrefixViolations(matrix)` supports strict testing / gating. Currently a soft constraint; will be promoted to a hard error in the future.

## Source structure

`crystal-shared/controller/StandardScopedManagerController.kt` (condensed):

```kotlin
@Validated
abstract class StandardScopedManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: PermissionMatrix? = null,   // may be null; subclass must override checkPermission
) {
    protected open suspend fun checkPermission(scope, scopeId, operation, userAuth): Boolean {
        val matrix = permissions ?: error("...override checkPermission when no Matrix")
        return RbacUtils.hasAnyAuthority(*matrix.layersFor(scope, operation))
    }

    protected open suspend fun checkOwnership(scope, scopeId, operation, userAuth): Boolean {
        return when (scope) {
            SYSTEM -> true
            TENANT -> {
                // super or tenantAdmin layer holder → cross-tenant pass
                if (RbacUtils.hasAnyAuthority(*matrix.crossTenantLayersFor(operation))) true
                else scopeId == userAuth.tenantId
            }
        }
    }

    protected open suspend fun buildQueryResponse(dto, userAuth): Any = managerService.query(dto)
    protected open suspend fun buildReadAllResponse(scopeId): Any = managerService.findAllByScopeId(scopeId)
    protected open fun resolveScope(scopeTypeId: Int): ResourceScope = ...

    @GetMapping("/list") suspend fun readAll(userAuth, scope, scopeId): ApiResponse<*> { ... }
    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> { ... }
    @PostMapping("/query") suspend fun query(userAuth, dto: READ_DTO): ApiResponse<*> { ... }
    @PostMapping("/update") suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> { ... }
    @PostMapping("/delete") suspend fun delete(userAuth, dto: DELETE_DTO): ApiResponse<*> { ... }

    private suspend fun assertAccess(scope, scopeId, operation, userAuth) {
        if (!checkPermission(...)) throw ForbiddenException()
        if (!checkOwnership(...)) throw UnauthorizedException()
    }
}
```

## Key flow: scope resolution for update / delete

The scope for `update` and `delete` is not read from the DTO — it is re-read from the DB entity:

```kotlin
@PostMapping("/update")
suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> {
    val entity = managerService.getByIdOrThrow(dto.id)
    val resolvedScope = resolveScope(entity.scope)      // ← from entity
    assertAccess(resolvedScope, entity.scopeId, UPDATE, userAuth)
    managerService.update(dto)
    return ApiResponse.success(null)
}
```

Reason: prevent clients from forging scope during updates. If the DTO carried scope, a malicious user could "promote" a tenant resource to system scope by tweaking the request (bypassing tenant isolation). Reading scope from the entity treats the stored scope as the source of truth.

`delete` goes further — batch delete groups by `(scope, scopeId)` and checks each group once:

```kotlin
entities.groupBy { it.scope to it.scopeId }.keys.forEach { (scopeType, scopeId) ->
    val resolvedScope = resolveScope(scopeType)
    assertAccess(resolvedScope, scopeId, DELETE, userAuthentication)
}
```

Avoids duplicate checks within the same scope and preserves strictness for cross-scope batches (each scope must pass independently).

## Why not AOP

`StandardManagerController` uses AOP because the permission list is statically readable on the class annotation. The Scoped family cannot:

- Permission decisions depend on `scope` and `scopeId` from the DTO
- At AOP interception time the DTO isn't yet typed (`@ModelAttribute` post-processing requires the controller method signature)
- For `update` / `delete`, permission also depends on the DB entity's scope

So the Scoped family chose "inline explicit check + `checkPermission` / `checkOwnership` hooks", placing permission decisions at the top of the business method — by which point the DTO is typed and `ApplicationContext` is available.

## Real usage locations

| Module | Controller | Notes |
|---|---|---|
| `crystal-tenant` | `ManagerTenantDictTypeController` | Dict types (mountable in system or tenant) |
| `crystal-approval` | `ManagerApprovalFlowDefinitionController` | Approval flow definitions |
| `crystal-approval` | `ManagerApprovalFlowInstanceController` | Approval instances (extends ReadonlyScoped, see its page) |

# StandardScopedManagerController

## Design intent

`StandardScopedManagerController` solves what `StandardTenantManagerController` couldn't express cleanly: the same resource may belong to either SYSTEM or TENANT scope. Core abstractions are `ResourceScope` + `ScopedPermissionTriad`, decoupling "what is the scope" from "which permission".

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

### ScopedPermissionTriad

```kotlin
data class ScopedPermissionTriad(
    val superCreate: String, val superRead: String, val superUpdate: String, val superDelete: String,
    val systemCreate: String, val systemRead: String, val systemUpdate: String, val systemDelete: String,
    val tenantPemCreate: String, val tenantPemRead: String, val tenantPemUpdate: String, val tenantPemDelete: String,
) {
    fun forScope(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(operation), systemFor(operation))
        ResourceScope.TENANT -> arrayOf(superFor(operation), tenantPemFor(operation))
    }
}
```

12 permissions = 3 layers × 4 operations. Rationale:

- The 3 layers are not "permission inheritance" but "authorization dimensions":
  - `super`: cross-scope (framework admin)
  - `system`: SYSTEM only
  - `tenantPem`: TENANT only
- `forScope()` returns an array; `hasAnyAuthority(...)` OR-matches it
- SYSTEM requests use `[super, system]`, TENANT requests use `[super, tenantPem]`
- No `[super, system, tenantPem]` combined form — SYSTEM resources must not be operated on by tenantPem holders (scope isolation)

## The `NEVER_GRANTED` fallback

```kotlin
companion object {
    const val NEVER_GRANTED: String = "!!never_granted!!"
    fun readonly(superRead: String, systemRead: String, tenantPemRead: String) = ScopedPermissionTriad(
        superCreate = NEVER_GRANTED, superRead = superRead, superUpdate = NEVER_GRANTED, superDelete = NEVER_GRANTED,
        systemCreate = NEVER_GRANTED, systemRead = systemRead, systemUpdate = NEVER_GRANTED, systemDelete = NEVER_GRANTED,
        tenantPemCreate = NEVER_GRANTED, tenantPemRead = tenantPemRead, tenantPemUpdate = NEVER_GRANTED, tenantPemDelete = NEVER_GRANTED,
    )
}
```

`NEVER_GRANTED` is a deliberate dead string:

- Does not belong to any real permission constant (not in `SystemPermission` / `TenantPermission`)
- The `root` role's auto-grant also excludes it (it isn't part of `SystemPermission`'s reflected constants)
- The `!!` prefix/suffix violates the project's `<module>.<resource>.<op>` naming convention, so collision with real permissions is impossible by construction

Consider what would happen if `Triad.readonly`'s CRUD slots held the read permission:

1. User A holds the read permission
2. `ReadonlyScopedManagerController` gets bypassed or mistakenly refactored to `Standard`
3. `triad.superFor(CREATE)` returns the read permission
4. `hasAnyAuthority(readPerm, readPerm) = true`
5. User A's read permission is silently upgraded to write permission

`NEVER_GRANTED` prevents this: even in that scenario, `hasAnyAuthority("!!never_granted!!", "!!never_granted!!")` is always false, denying deterministically. Proper defensive coding — error-path failure mode is safe (fail-safe), not silent permission escalation (fail-open).

## Source structure

`crystal-shared/controller/StandardScopedManagerController.kt` (condensed):

```kotlin
@Validated
abstract class StandardScopedManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: ScopedPermissionTriad? = null,   // may be null; subclass must override checkPermission
) {
    protected open suspend fun checkPermission(scope, scopeId, operation, userAuth): Boolean {
        val triad = permissions ?: error("...override checkPermission when no Triad")
        return RbacUtils.hasAnyAuthority(*triad.forScope(scope, operation))
    }

    protected open suspend fun checkOwnership(scope, scopeId, operation, userAuth): Boolean {
        return when (scope) {
            SYSTEM -> true
            TENANT -> {
                if (RbacUtils.hasAuthority(triad.superFor(operation))) true
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

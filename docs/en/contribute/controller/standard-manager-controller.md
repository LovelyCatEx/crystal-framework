# StandardManagerController

## Design intent

`StandardManagerController` is the core abstraction for manager-side CRUD in the framework. It enforces the Controller → Service → Repository layered collaboration; permission checks are now inline via the `authorize` method (backed by `PermissionMatrix`), while audit logging remains automated via AOP. 7 type parameters thread the entire chain (Service, Repository, Entity, four DTOs) together — compile-time type safety.

## Source

`crystal-shared/controller/StandardManagerController.kt`:

```kotlin
@Validated
abstract class StandardManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(userAuthentication: UserAuthentication): ApiResponse<*> {
        return ApiResponse.success(managerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PostMapping("/create", version = "1")
    suspend fun create(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: CREATE_DTO): ApiResponse<*> {
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query", version = "1")
    suspend fun read(userAuthentication: UserAuthentication, @RequestBody @Valid dto: READ_DTO): ApiResponse<*> {
        return ApiResponse.success(managerService.query(dto))
    }

    @PostMapping("/update", version = "1")
    suspend fun update(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: UPDATE_DTO): ApiResponse<*> {
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: DELETE_DTO): ApiResponse<*> {
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
```

## Key structural decisions

- **Method name `read` vs URL `/query`** is a historical artifact. Early APIs used `read` as the method name (with `/read` URL); the path was renamed to `/query` (matching "paginated query" semantics), but the method name persists. The legacy `ManagerControllerPermissionAspect` still reflects by method name to match `@ManagerPermissions.read` — renaming would break that compat path. The `PermissionMatrix` path dispatches via `authorize(ManagerAction, ...)` + `ScopedOperation` and does not depend on method names.

- **`version = "1"`** is a custom property on `@GetMapping` / `@PostMapping`, paired with the framework's customized `RequestMappingHandlerMapping` to produce `/api/v1/xxx` paths.

- **`awaitListWithTimeout()`** is a project extension converting `Flux<T>` into `List<T>` with a timeout, protecting against R2DBC deadlocks dragging the whole coroutine chain.

- **`readAll` calls `getRepository().findAll()` directly** rather than through an aggregation method on Service — full-scan queries need no business handling, and skipping a layer reduces indirection. If a subclass needs filtering, override `readAll` rather than adding an ad-hoc Service method.

## Method naming convention

Method names now serve two matchers: the new `PermissionMatrix` path uses `ManagerAction` → `ScopedOperation` mapping (`readAll` and `read` both fold into `READ`); the legacy `ManagerControllerPermissionAspect` compat path still reflects the method name to pick the `@ManagerPermissions` field:

| Method name | ManagerAction | ScopedOperation (Matrix path) | `@ManagerPermissions` field (legacy) | HTTP |
|---|---|---|---|---|
| `readAll` | READ_ALL | READ | `readAll` (falls back to `read` when empty) | GET `/list` |
| `read` | READ | READ | `read` | POST `/query` |
| `create` | CREATE | CREATE | `create` | POST `/create` |
| `update` | UPDATE | UPDATE | `update` | POST `/update` |
| `delete` | DELETE | DELETE | `delete` | POST `/delete` |

Preserving method names on subclass overrides keeps both the Matrix and AOP paths matching.

## Permission declaration: PermissionMatrix

`PermissionMatrix` is passed via the constructor arg `permissions = ...`. `StandardManagerController.authorize` maps `ManagerAction` to `ScopedOperation` and OR-checks `matrix.layersFor(SYSTEM, op)`:

```kotlin
override suspend fun authorize(action: ManagerAction, userAuthentication, ...) {
    val matrix = permissions ?: return  // permissions == null → fall back to legacy AOP
    val op = when (action) {
        ManagerAction.CREATE -> ScopedOperation.CREATE
        ManagerAction.READ, ManagerAction.READ_ALL -> ScopedOperation.READ
        ManagerAction.UPDATE -> ScopedOperation.UPDATE
        ManagerAction.DELETE -> ScopedOperation.DELETE
    }
    val required = matrix.layersFor(ResourceScope.SYSTEM, op)
    if (!RbacUtils.hasAnyAuthority(*required)) {
        throw AuthorizationDeniedException("Access denied: required any of ${required.toList()}")
    }
}
```

Standard resources use `PermissionMatrix.systemOnly(...)` — tenant layers default to `NOT_APPLICABLE`; the super layer is optional. `layersFor(SYSTEM, op)` filters `NOT_APPLICABLE` and typically returns `[system<op>]` (or `[super<op>, system<op>]`).

## Legacy AOP path

The legacy `@ManagerPermissions` class annotation is handled by `ManagerControllerPermissionAspect`, active only when `permissions == null`:

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        // If the Controller already supplies `permissions`, `authorize` handled it; proceed
        val targetClass = AopUtils.getTargetClass(joinPoint.target)
        val permissions = AnnotationUtils.findAnnotation(targetClass, ManagerPermissions::class.java)
            ?: return joinPoint.proceed()
        // ... reflect method name → match @ManagerPermissions field
    }
}
```

`AopUtils.getTargetClass` peels off CGLIB proxies to get the real class; `AnnotationUtils.findAnnotation` walks the class inheritance chain, allowing `@ManagerPermissions` to be written on an intermediate abstract subclass. This path carries `@Deprecated`, retained one release so legacy Controllers keep working during migration.

### ManagerControllerAuditAspect (in crystal-audit)

The pointcut covers every subclass of `AbstractManagerController`. The audit aspect has `@Order = 0` and wraps the permission safety-net aspect (`@Order = 1000`); it observes the `Mono` returned by permission checks and the business method, then writes the audit log asynchronously when it receives an `onNext`, `onComplete`, or `onError` signal.

The audit aspect records:

- Operator (`userId`, `username`, and `tenantId`) and operation time
- Operation type (`create` / `read` / `update` / `delete`, with `readAll` mapped to `read`)
- Resource type (the Entity's `@Table` name) and resource IDs that can be extracted from the DTO
- Request information (`requestId`, HTTP method, path, client IP, and User-Agent)
- Whether execution succeeded and the error message

The audit aspect only recognises the five standard methods `create`, `read`, `readAll`, `update`, and `delete`. Extra custom endpoints declared in a Manager Controller and ordinary Controllers are not recorded automatically; they require explicit instrumentation or an additional aspect. The current implementation does not record complete request parameters or response results.

## Generic constraint chain

The 7 type parameters build a complete chain:

```
StandardManagerController
    │ SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>
    │               │ REPOSITORY : BaseRepository<ENTITY>
    │               │ ENTITY : BaseEntity
    │               │ CREATE_DTO : Any
    │               │ READ_DTO : BaseManagerReadDTO (extends PageQuery)
    │               │ UPDATE_DTO : BaseManagerUpdateDTO (has id: Long)
    │               │ DELETE_DTO : BaseManagerDeleteDTO (has ids: List<Long>)
    │
    └─ managerService (constructor)
```

Service's internal `query(dto)` / `update(dto)` / `deleteByDTO(dto)` require concrete DTO types for deserialization and field mapping, so all DTO types are exposed through Service's generic parameters, making Service's type signature the central type-info hub for the whole CRUD chain.

## CachedBaseManagerService

The Controller requires `CachedBaseManagerService`, not `BaseManagerService`:

| | BaseManagerService | CachedBaseManagerService |
|---|---|---|
| Cache | None | Caches query results via `withXXXContext` |
| Invalidation | — | Auto-evicts cache on update / delete |
| Fits | Simple CRUD | Standard admin CRUD (recommended) |

## DTO base constraints

| Base | Carries | Purpose |
|---|---|---|
| `BaseManagerReadDTO` | `page`, `pageSize`, `id?`, `query?: QueryNode` | Paginated query + structured condition tree |
| `BaseManagerUpdateDTO` | `id: Long` | Update by ID |
| `BaseManagerDeleteDTO` | `ids: List<Long>` | Batch delete |

`CREATE_DTO` has no base constraint (`Any`); define fields freely per business need.

### `query: QueryNode` on BaseManagerReadDTO

`BaseManagerReadDTO` carries a `QueryNode`-typed structured query tree supporting AND/OR nesting and operators (eq / contains / gte / …). Introduced in v1.5.0; the frontend `FilterBuilder` outputs this tree as JSON.

## Real usage locations

| Module | Controller |
|---|---|
| `crystal-resource` | `ManagerStorageProviderController`, `ManagerFileResourceController` |
| `crystal-rbac` (user) | `ManagerUserRoleController`, `ManagerUserPermissionController` |
| `crystal-tenant` | `ManagerTenantController`, `ManagerTenantTireTypeController` |
| `crystal-mail` | `ManagerMailTemplateController` |

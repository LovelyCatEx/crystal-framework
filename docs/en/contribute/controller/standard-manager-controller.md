# StandardManagerController

## Design intent

`StandardManagerController` is the core abstraction for manager-side CRUD in the framework. It enforces the Controller → Service → Repository layered collaboration and pairs with AOP to automate permission checks and audit logging. 7 type parameters thread the entire chain (Service, Repository, Entity, four DTOs) together — compile-time type safety plus runtime AOP interception.

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

- **Method name `read` vs URL `/query`** is a historical artifact. Early APIs used `read` as the method name (with `/read` URL); the path was renamed to `/query` (matching "paginated query" semantics), but the method name stayed because `ManagerControllerPermissionAspect` reflects by method name to match `@ManagerPermissions.read`. Renaming this method breaks AOP permission mapping — the aspect must be updated in tandem.

- **`version = "1"`** is a custom property on `@GetMapping` / `@PostMapping`, paired with the framework's customized `RequestMappingHandlerMapping` to produce `/api/v1/xxx` paths.

- **`awaitListWithTimeout()`** is a project extension converting `Flux<T>` into `List<T>` with a timeout, protecting against R2DBC deadlocks dragging the whole coroutine chain.

- **`readAll` calls `getRepository().findAll()` directly** rather than through an aggregation method on Service — full-scan queries need no business handling, and skipping a layer reduces indirection. If a subclass needs filtering, override `readAll` rather than adding an ad-hoc Service method.

## Method naming convention

`ManagerControllerPermissionAspect` reflects on method name to match permission config. Method names are a public contract:

| Method name | Matched `@ManagerPermissions` field | HTTP |
|---|---|---|
| `readAll` | `readAll` (falls back to `read` when empty) | GET `/list` |
| `read` | `read` | POST `/query` |
| `create` | `create` | POST `/create` |
| `update` | `update` | POST `/update` |
| `delete` | `delete` | POST `/delete` |

Subclass overrides must preserve the name; otherwise AOP cannot match.

## @ManagerPermissions annotation

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagerPermissions(
    val read: Array<String> = [],
    val readAll: Array<String> = [],
    val create: Array<String> = [],
    val update: Array<String> = [],
    val delete: Array<String> = []
)
```

- `AnnotationTarget.CLASS` — class-level, covers the whole Controller
- Each field is a permission array with OR semantics
- Empty `readAll` falls back to `read` — logic inside the aspect; in most cases only `read` needs to be set
- Empty array means "no check" — the aspect logs a warn and lets the call through; suitable for tests or temporarily open endpoints

## AOP interception

### ManagerControllerPermissionAspect

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val targetClass = AopUtils.getTargetClass(joinPoint.target)
        val permissions = AnnotationUtils.findAnnotation(targetClass, ManagerPermissions::class.java)
            ?: return joinPoint.proceed()

        val methodName = (joinPoint.signature as MethodSignature).method.name
        val requiredPermissions = when (methodName) {
            "readAll" -> permissions.readAll
            "read"    -> permissions.read
            "create"  -> permissions.create
            "update"  -> permissions.update
            "delete"  -> permissions.delete
            else      -> null
        }?.filter { it.isNotEmpty() }?.toList()

        if (requiredPermissions.isNullOrEmpty()) {
            logger.warn("No valid permission required for $methodSignature, skipped.")
            return joinPoint.proceed()
        }

        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication }
            .flatMap { authentication ->
                if (!hasAnyPermission(authentication, requiredPermissions)) {
                    throw AuthorizationDeniedException("Access denied: ...")
                }
                @Suppress("UNCHECKED_CAST")
                joinPoint.proceed() as Mono<Any>
            }
    }
}
```

Pointcut coverage: `StandardManagerController.*(..)` covers Standard and all its subclasses (including Readonly), but not Scoped / DerivedScoped / Tenant. This is why the Scoped family checks permissions inline.

`AopUtils.getTargetClass` peels off CGLIB proxies to get the real class; `AnnotationUtils.findAnnotation` walks the class inheritance chain, allowing `@ManagerPermissions` to be written on an intermediate abstract subclass. `class.getAnnotation()` supports neither.

### ManagerControllerAuditAspect (in crystal-audit)

Same pointcut, `@Order` runs after the permission aspect. Execution order: permission check → audit log → business method.

The audit aspect records:

- Operator (`userAuthentication.userId`), timestamp
- Operation type (`create` / `update` / `delete`)
- Request parameters, response result (post-redaction)
- Resource identifier (entity id)

The audit aspect also only covers the `StandardManagerController` family. Scoped-family audit needs manual instrumentation or a separate aspect.

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

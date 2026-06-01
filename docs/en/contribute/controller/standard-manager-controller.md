# StandardManagerController

::: warning Return Type

When extending `StandardManagerController` / `ReadonlyManagerController`, the base class already returns `ApiResponse<*>`. If you add custom endpoints, **all custom methods must explicitly return `ApiResponse<*>`**. Never return raw types.
:::

## Design Rationale

`StandardManagerController` is the core abstraction for admin CRUD operations. It enforces a layered Controller → Service → Repository pattern and leverages AOP for automated permission checks and audit logging.

## Source Analysis

Located in the `crystal-shared` module under `com.lovelycatv.crystalframework.shared.controller`:

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
    suspend fun readAll(userAuthentication: UserAuthentication): ApiResponse<*> { ... }

    @PostMapping("/create", version = "1")
    suspend fun create(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: CREATE_DTO): ApiResponse<*> { ... }

    @GetMapping("/query", version = "1")
    suspend fun read(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: READ_DTO): ApiResponse<*> { ... }

    @PostMapping("/update", version = "1")
    suspend fun update(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: UPDATE_DTO): ApiResponse<*> { ... }

    @PostMapping("/delete", version = "1")
    suspend fun delete(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: DELETE_DTO): ApiResponse<*> { ... }
}
```

### Method Name Convention

The PermissionAspect resolves permissions by method name — **method names are part of the framework contract** and must not be renamed:

| Method | `@ManagerPermissions` field | HTTP |
|--------|-----------------------------|------|
| `readAll` | `readAll` (falls back to `read` when empty) | GET |
| `read` | `read` | GET |
| `create` | `create` | POST |
| `update` | `update` | POST |
| `delete` | `delete` | POST |

## @ManagerPermissions Annotation

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

- Class-level annotation applied to the concrete controller
- Each field accepts an array of permission identifiers (OR semantics)
- Empty `readAll` falls back to `read`
- Empty array means "no permission check for this action" — AOP logs a warning and proceeds

## AOP Chain

### ManagerControllerPermissionAspect

```kotlin
@Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
```

The pointcut covers all subclasses of `StandardManagerController` (including `ReadonlyManagerController`):

1. Resolves the real class via `AopUtils.getTargetClass` (bypasses CGLIB proxies)
2. Reads `@ManagerPermissions` via `AnnotationUtils.findAnnotation` (supports inherited lookups)
3. Matches the method name to the corresponding permission field
4. Retrieves authentication from `ReactiveSecurityContextHolder`
5. Compares granted authorities against required permissions

### ManagerControllerAuditAspect

In the `crystal-audit` module, a second `@Around` aspect intercepts the same pointcut to record audit logs for every CRUD operation:

- Operator, timestamp, operation type (create/update/delete)
- Request parameters and response (sanitized)
- Resource identifier

The two aspects are ordered via `@Order`: permission checks execute before audit logging.

## Type Parameter Chain

The seven type parameters form a complete type chain:

```
StandardManagerController
    │ SERVICE : CachedBaseManagerService
    │               │ REPOSITORY : BaseRepository
    │               │               └ ENTITY : BaseEntity
    │               │ CREATE_DTO : Any
    │               │ READ_DTO : BaseManagerReadDTO (extends PageQuery)
    │               │ UPDATE_DTO : BaseManagerUpdateDTO (has id: Long)
    │               └ DELETE_DTO : BaseManagerDeleteDTO (has ids: List<Long>)
    └ managerService (injected)
```

## CachedBaseManagerService

StandardManagerController requires `CachedBaseManagerService` (not `BaseManagerService`):

| | BaseManagerService | CachedBaseManagerService |
|--|--|--|
| Cache | None | Auto-caches query results |
| Eviction | — | Auto-evicts on update/delete |
| Use case | Simple CRUD without cache | Standard admin CRUD (recommended) |

## DTO Base Classes

| Base Class | Fields | Purpose |
|------------|--------|---------|
| `BaseManagerReadDTO` | `page`, `pageSize`, `searchKeyword`, `startTime`, `endTime`, `id` | Paginated query |
| `BaseManagerUpdateDTO` | `id: Long` | Update by ID |
| `BaseManagerDeleteDTO` | `ids: List<Long>` | Batch delete |

`CREATE_DTO` has no base class constraint (`Any`) — define fields freely based on business requirements.

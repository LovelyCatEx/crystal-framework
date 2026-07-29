# StandardManagerController

Base class for global CRUD in the admin backend. Resources have no SYSTEM / TENANT distinction — extend this class, plug in 7 type parameters and declare permissions via `permissions = PermissionMatrix.systemOnly(...)`, and 5 CRUD endpoints are generated automatically.

## Applicable scenarios

- Global resources like storage providers, system-level roles, system permissions
- Standard CRUD admin pages
- No SYSTEM / TENANT dual scope needed

Other scenarios:

- Dual-scope resource → [StandardScopedManagerController](./scoped-manager-controller)
- Tenant-only resource → [StandardTenantManagerController](./tenant-manager-controller)
- Read-only resource → [ReadonlyManagerController](./readonly-manager-controller)

## Auto-generated endpoints

| HTTP | Path | Method name | Parameter binding | Frontend Content-Type |
|---|---|---|---|---|
| GET | `/list` | `readAll` | none | — |
| POST | `/create` | `create` | `@ModelAttribute` | form-urlencoded |
| POST | `/query` | `read` | `@RequestBody` | application/json |
| POST | `/update` | `update` | `@ModelAttribute` | form-urlencoded |
| POST | `/delete` | `delete` | `@ModelAttribute` | form-urlencoded |

The pagination endpoint's method name is `read` (not `query`), while the URL is `/query`. This asymmetry is historical.

## Usage steps

Using `storage-provider` as an example.

### 1. Entity

```kotlin
@Table("storage_provider")
class StorageProviderEntity(
    var name: String = "",
    var type: Int = 0,
    var config: String = "",
) : BaseEntity()
```

See [Add Entity](../add-entity).

### 2. Repository

```kotlin
@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity>
```

### 3. Manager Service

Under `service/manager/` + `service/manager/impl/`:

```kotlin
// service/manager/StorageProviderManagerService.kt
interface StorageProviderManagerService : CachedBaseManagerService<
    StorageProviderRepository,
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO,
    ManagerDeleteStorageProviderDTO
>
```

```kotlin
// service/manager/impl/StorageProviderManagerServiceImpl.kt
@Service
class StorageProviderManagerServiceImpl(
    repository: StorageProviderRepository
) : StorageProviderManagerService, CachedBaseManagerServiceImpl<...>(repository) {
    override fun getEntityClass(): KClass<*> = StorageProviderEntity::class
}
```

### 4. Four DTOs

Under `controller/manager/dto/`:

```kotlin
// ManagerCreateStorageProviderDTO.kt
class ManagerCreateStorageProviderDTO(
    @field:NotBlank val name: String = "",
    @field:NotNull val type: Int = 0,
    val config: String = "",
)

// ManagerReadStorageProviderDTO.kt
class ManagerReadStorageProviderDTO(
    override val page: Int = 1,
    override val pageSize: Int = 20,
) : BaseManagerReadDTO(page, pageSize)

// ManagerUpdateStorageProviderDTO.kt
class ManagerUpdateStorageProviderDTO(
    override val id: Long,
    val name: String? = null,
    val type: Int? = null,
    val config: String? = null,
) : BaseManagerUpdateDTO(id)

// ManagerDeleteStorageProviderDTO.kt
class ManagerDeleteStorageProviderDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 5. Controller

Under `controller/manager/`:

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<
    StorageProviderManagerService,
    StorageProviderRepository,
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO,
    ManagerDeleteStorageProviderDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE,
        systemRead   = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE,
    ),
)
```

The Controller needs no method body; all 5 endpoints inherit from the parent.

## Type parameters

| # | Parameter | Constraint |
|---|---|---|
| 1 | `SERVICE` | `CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` |
| 4 | `CREATE_DTO` | `Any` (no base class required) |
| 5 | `READ_DTO` | `BaseManagerReadDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## Permission declaration (PermissionMatrix.systemOnly)

Standard resources have no scope concept, so only the `system` layer is needed. `PermissionMatrix.systemOnly(...)` fills both tenant layers with `NOT_APPLICABLE`; the `super` layer also defaults to `NOT_APPLICABLE` (pass `superCreate = ..., superRead = ..., ...` — 4 optional params — to opt in a super admin).

```kotlin
permissions = PermissionMatrix.systemOnly(
    systemCreate = SystemPermission.ACTION_SYSTEM_XXX_CREATE,
    systemRead   = SystemPermission.ACTION_SYSTEM_XXX_READ,
    systemUpdate = SystemPermission.ACTION_SYSTEM_XXX_UPDATE,
    systemDelete = SystemPermission.ACTION_SYSTEM_XXX_DELETE,
    // Optional: cross-scope super admin
    // superRead = SystemPermission.ACTION_XXX_READ,
    // ...
)
```

Rules:

- `system*` authorities must be prefixed with `system.`; violations emit a warn log at startup
- Permission strings must reference `SystemPermission.XXX` constants — no literals
- `layersFor(SYSTEM, op)` returns the `[super, system]` array; `hasAnyAuthority(...)` OR-matches. `NOT_APPLICABLE` is filtered out, so without a super entry the array is just `[system]`
- The legacy `@ManagerPermissions` class annotation is deprecated; see the [Permission Model Migration Guide](./permission-migration) for the rewrite

## Adding custom endpoints

The standard base allows adding extra methods:

```kotlin
class ManagerStorageProviderController(...) : StandardManagerController<...>(managerService) {

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE}')")
    @PostMapping("/test-connection")
    suspend fun testConnection(
        @RequestParam id: Long
    ): ApiResponse<*> {
        val ok = managerService.testConnection(id)
        return ApiResponse.success(ok)
    }
}
```

Custom methods are not covered by `PermissionMatrix` (`authorize` runs only on the 5 standard endpoints); add `@PreAuthorize` explicitly.

## Frontend integration

The frontend extends `BaseManagerController` (`api/BaseManagerController.ts`):

```typescript
class ManagerStorageProviderController extends BaseManagerController<
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO
    // R/U/D use defaults
> {}

export const managerStorageProviderController = new ManagerStorageProviderController(
    '/manager/storage-provider'
)
```

`create` / `update` / `delete` use `application/x-www-form-urlencoded`; `query` uses `application/json`. `BaseManagerController` wires this up.

## Notes

- Declare permissions via `PermissionMatrix`; do not re-add the `@ManagerPermissions` annotation (`authorize` prefers the `permissions` field; the annotation only serves as a legacy fallback when `permissions == null` and is `@Deprecated`)
- Every `Long` field must carry `@get:JsonSerialize(using = ToStringSerializer::class)`; the frontend accepts it as `string`
- Do not inject Repository into a Controller; DB operations go through Service
- Manager Controllers only inject Manager Services; do not inject generic Services
- All 4 DTOs must be provided — even if an operation is unused, the generic base requires the type parameter

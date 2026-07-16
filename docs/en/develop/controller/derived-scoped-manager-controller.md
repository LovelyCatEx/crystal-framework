# StandardDerivedScopedManagerController

Similar to [`StandardScopedManagerController`](./scoped-manager-controller), but the entity has no scope / scopeId columns of its own — scope must be derived from a parent entity (walking the foreign-key chain). Canonical case: dict items have no scope themselves; the dict type they belong to does.

## Applicable scenarios

- Dict item → dict type (`typeId`) → dict type's scope
- Department member config → department → department's scope (if department itself is Scoped, not Tenant)
- Any "child-owned scope on the parent" resource

Other scenarios:

- Entity carries scope / scopeId columns → [StandardScopedManagerController](./scoped-manager-controller)
- Resource is tenant-only → [StandardTenantManagerController](./tenant-manager-controller)

## Endpoints

No `/list` endpoint — without parent context, enumerating all derived rows is meaningless (they'd span many parents, mixed together with no practical use):

| HTTP | Path | Notes |
|---|---|---|
| POST | `/create` | Derives scope from foreign key on CREATE_DTO |
| POST | `/query` | Derives scope from foreign key on READ_DTO (typically list-children-by-parent) |
| POST | `/update` | Derives scope from DB entity (walks up to parent) |
| POST | `/delete` | Same |

For list semantics, add a custom endpoint (e.g. `/tree?parentId=xxx`).

## Three abstract methods

Subclasses must implement three scope-resolution hooks:

```kotlin
protected abstract suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long>
protected abstract suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long>
protected abstract suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long>
```

Each returns a `(scope, scopeId)` pair. These methods typically query the parent — via your Service or Repository.

## Usage steps

Using dict items as an example. Dict items have no scope themselves; scope comes via `typeId` → dict type → dict type's scope.

### 1. Entity

The dict item implements `ScopedEntity<Long>` rather than extending `BaseScopedEntity` — no scope column of its own, only a `typeId` pointing to parent:

```kotlin
@Table("tenant_dict_item")
class TenantDictItemEntity(
    var typeId: Long = 0,
    var label: String = "",
    var value: String = "",
) : BaseEntity(), ScopedEntity<Long> {

    // ScopedEntity contract: return the direct parent's id
    override fun getDirectParentId(): Long = typeId
}
```

### 2. Service

Extends `CachedBaseManagerService` (entity is `BaseEntity`, not `BaseScopedEntity`):

```kotlin
interface TenantDictItemManagerService : CachedBaseManagerService<
    TenantDictItemRepository,
    TenantDictItemEntity,
    ManagerCreateTenantDictItemDTO,
    ManagerReadTenantDictItemDTO,
    ManagerUpdateTenantDictItemDTO,
    ManagerDeleteTenantDictItemDTO
> {
    suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO>
}
```

### 3. DTOs

Read DTO uses `BaseManagerReadDTO` (not the Scoped variant) — the client only sends the parent's `typeId`; the Controller resolves scope:

```kotlin
class ManagerCreateTenantDictItemDTO(
    @field:NotNull val typeId: Long,
    val label: String,
    val value: String,
)

class ManagerReadTenantDictItemDTO(
    page: Int = 1, pageSize: Int = 20,
    val typeId: Long = 0,
) : BaseManagerReadDTO(page, pageSize)

class ManagerUpdateTenantDictItemDTO(
    override val id: Long,
    val label: String? = null,
    val value: String? = null,
) : BaseManagerUpdateDTO(id)

class ManagerDeleteTenantDictItemDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 4. Controller

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-item")
class ManagerTenantDictItemController(
    managerService: TenantDictItemManagerService,
    private val tenantDictTypeManagerService: TenantDictTypeManagerService,  // used to look up parent's scope
) : StandardDerivedScopedManagerController<
    TenantDictItemManagerService,
    TenantDictItemRepository,
    TenantDictItemEntity,
    ManagerCreateTenantDictItemDTO,
    ManagerReadTenantDictItemDTO,
    ManagerUpdateTenantDictItemDTO,
    ManagerDeleteTenantDictItemDTO
>(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate     = SystemPermission.ACTION_DICT_ITEM_CREATE,
        superRead       = SystemPermission.ACTION_DICT_ITEM_READ,
        superUpdate     = SystemPermission.ACTION_DICT_ITEM_UPDATE,
        superDelete     = SystemPermission.ACTION_DICT_ITEM_DELETE,
        systemCreate    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE,
        systemRead      = SystemPermission.ACTION_SYSTEM_DICT_ITEM_READ,
        systemUpdate    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE,
        systemDelete    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_ITEM_CREATE_PEM,
        tenantPemRead   = TenantPermission.ACTION_TENANT_DICT_ITEM_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_ITEM_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_ITEM_DELETE_PEM,
    ),
) {

    override suspend fun resolveScopeFromCreateDTO(dto: ManagerCreateTenantDictItemDTO): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(dto.typeId)
    }

    override suspend fun resolveScopeFromReadDTO(dto: ManagerReadTenantDictItemDTO): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(dto.typeId)
    }

    override suspend fun resolveScopeFromEntity(entity: TenantDictItemEntity): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(entity.typeId)
    }

    private suspend fun resolveScopeByTypeId(typeId: Long): Pair<ResourceScope, Long> {
        val type = tenantDictTypeManagerService.getByIdOrNull(typeId)
            ?: throw BusinessException("Dict type $typeId not found")
        val scope = ResourceScope.getById(type.scope)
            ?: throw BusinessException("Unknown scope ${type.scope} on dict type $typeId")
        return scope to type.scopeId
    }

    // Custom endpoint: expand as tree by typeId
    @GetMapping("/tree")
    suspend fun tree(
        userAuthentication: UserAuthentication,
        @RequestParam typeId: Long
    ): ApiResponse<List<TenantDictItemTreeVO>> {
        val (scope, scopeId) = resolveScopeByTypeId(typeId)
        if (!RbacUtils.hasAnyAuthority(*permissions.forScope(scope, ScopedOperation.READ))) {
            throw ForbiddenException()
        }
        if (!checkOwnership(scope, scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException()
        }
        return ApiResponse.success(managerService.getTreeByTypeId(typeId))
    }
}
```

## Type parameters

| # | Parameter | Constraint |
|---|---|---|
| 1 | `SERVICE` | `CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` and `ScopedEntity<*>` (union constraint) |
| 4 | `CREATE_DTO` | `Any` |
| 5 | `READ_DTO` | `BaseManagerReadDTO` (not the Scoped variant) |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

The ENTITY union constraint: `where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>` — the entity must both extend BaseEntity and implement ScopedEntity; Kotlin's `where` clause enforces this at compile time.

## Permission model

Same as [StandardScopedManagerController](./scoped-manager-controller) — the 12-permission `ScopedPermissionTriad`. Only the scope source differs: Scoped reads from the entity's scope column; DerivedScoped gets it from the abstract methods.

## Overridable hooks

- `resolveScopeFromCreateDTO(dto)` — must implement
- `resolveScopeFromReadDTO(dto)` — must implement
- `resolveScopeFromEntity(entity)` — must implement (used by update / delete)
- `checkOwnership(scope, scopeId, operation, userAuth)` — optional; defaults match Scoped
- `buildQueryResponse(dto, userAuth)` — optional; defaults to `managerService.query(dto)`

## Notes

- Entity must be both `BaseEntity` and `ScopedEntity<*>`; extending only BaseEntity fails compilation
- `ScopedEntity.getDirectParentId()` is for `TenantRelationshipCheckService.checkIsRelatedToRootParent` (chain walking); the Derived Controller itself doesn't use it — the three `resolveScopeFromXXX` hooks are the real integration points
- Every CRUD triggers a parent lookup — watch performance. If the parent is hot, leverage `CachedBaseService` caching
- Read DTO uses `BaseManagerReadDTO` (not the Scoped variant) — the client only sends the parent id; the backend resolves scope
- No `/list` endpoint — add a custom one when needed, typically with a parent-id parameter

# StandardScopedManagerController

Base class for resources that can live under either SYSTEM or TENANT scope. The entity carries its own `scope` + `scopeId` columns; the Controller uses `ScopedPermissionTriad` (12 permissions) to pick the right permission at runtime.

## Applicable scenarios

- Dictionaries — either system-wide or tenant-private
- Approval flow definitions — either system-level or tenant-level
- Notification templates, system configuration, and other multi-layer mountable resources

Other scenarios:

- Tenant-only → [StandardTenantManagerController](./tenant-manager-controller)
- Global (no scope) → [StandardManagerController](./standard-manager-controller)
- No scope column, derives from parent → [StandardDerivedScopedManagerController](./derived-scoped-manager-controller)
- Read-only variant → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## Endpoints

| HTTP | Path | Notes |
|---|---|---|
| GET | `/list?scope={0\|1}&scopeId={id}` | Full list within a scope |
| POST | `/create` | Body (form-urlencoded) carries scope + scopeId |
| POST | `/query` | Body (JSON) carries scope + scopeId + pagination |
| POST | `/update` | Body carries id (scope resolved from DB entity) |
| POST | `/delete` | Body carries ids (scope resolved from DB entity) |

`scope` is `Int`: `0 = SYSTEM`, `1 = TENANT` (from `ResourceScope.typeId`).

## Permission model: ScopedPermissionTriad

Each CRUD operation has three slots in the Triad (12 permissions total):

```
super × CRUD      Cross-scope admin (root / admin), works in any scope
system × CRUD     SYSTEM scope only
tenantPem × CRUD  Own-tenant scope only
```

Authorization rule:

```
SYSTEM scope → hasAnyAuthority(super<op>, system<op>)
TENANT scope → hasAnyAuthority(super<op>, tenantPem<op>)
```

Ownership rule (inside `checkOwnership`):

- SYSTEM: passes once permission check passes
- TENANT: either hold cross-tenant `super<op>`, or `scopeId == user's tenantId`

## Usage steps

Using `TenantDictTypeEntity` (extends `BaseScopedEntity`, carries `scope` + `scopeId` columns) as an example.

### 1. Entity

```kotlin
@Table("tenant_dict_type")
class TenantDictTypeEntity(
    id: Long = 0,
    var name: String = "",
    var description: String = "",
    scope: Int = 0,          // 0 = SYSTEM, 1 = TENANT
    scopeId: Long = 0,       // 0 for system dict, tenantId for tenant dict
) : BaseScopedEntity(id, scope, scopeId)
```

### 2. Service

Under `service/manager/`, extending `BaseScopedManagerService`:

```kotlin
interface TenantDictTypeManagerService : BaseScopedManagerService<
    TenantDictTypeRepository,
    TenantDictTypeEntity,
    ManagerCreateTenantDictTypeDTO,
    ManagerReadTenantDictTypeDTO,
    ManagerUpdateTenantDictTypeDTO,
    ManagerDeleteTenantDictTypeDTO
>
```

### 3. Four DTOs

DTOs use the Scoped bases:

```kotlin
class ManagerCreateTenantDictTypeDTO(
    scope: Int,
    scopeId: Long,
    val name: String,
    val description: String = "",
) : BaseManagerCreateScopedDTO(scope, scopeId)

class ManagerReadTenantDictTypeDTO(
    page: Int = 1,
    pageSize: Int = 20,
    scope: Int = 0,
    scopeId: Long = 0,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)

// Update / Delete use the plain BaseManagerUpdateDTO / BaseManagerDeleteDTO
// scope is read from the DB entity; the client does not need to send it again
class ManagerUpdateTenantDictTypeDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
) : BaseManagerUpdateDTO(id)

class ManagerDeleteTenantDictTypeDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 4. Controller

The Scoped family does not use `@ManagerPermissions`; permissions go through `ScopedPermissionTriad` in the constructor:

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-type")
class ManagerTenantDictTypeController(
    managerService: TenantDictTypeManagerService
) : StandardScopedManagerController<
    TenantDictTypeManagerService,
    TenantDictTypeRepository,
    TenantDictTypeEntity,
    ManagerCreateTenantDictTypeDTO,
    ManagerReadTenantDictTypeDTO,
    ManagerUpdateTenantDictTypeDTO,
    ManagerDeleteTenantDictTypeDTO
>(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate    = SystemPermission.ACTION_DICT_TYPE_CREATE,
        superRead      = SystemPermission.ACTION_DICT_TYPE_READ,
        superUpdate    = SystemPermission.ACTION_DICT_TYPE_UPDATE,
        superDelete    = SystemPermission.ACTION_DICT_TYPE_DELETE,
        systemCreate   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE,
        systemRead     = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ,
        systemUpdate   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE,
        systemDelete   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM,
        tenantPemRead   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM,
    ),
)
```

The Controller needs no method body; all 5 endpoints inherit.

## Type parameters

| # | Parameter | Constraint |
|---|---|---|
| 1 | `SERVICE` | `BaseScopedManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseScopedEntity` (must carry `scope` + `scopeId` columns) |
| 4 | `CREATE_DTO` | `Any`, in practice must extend `BaseManagerCreateScopedDTO` |
| 5 | `READ_DTO` | `BaseManagerReadScopedDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## Overridable hooks

- `checkPermission(scope, scopeId, operation, userAuth)` — permission decision. Default routes through Triad `hasAnyAuthority`; subclasses must override when no Triad is supplied
- `checkOwnership(scope, scopeId, operation, userAuth)` — ownership check. Default requires `scopeId == tenantId` for TENANT (unless a super holder)
- `buildQueryResponse(dto, userAuth)` — shape `/query` response, defaults to `managerService.query(dto)`
- `buildReadAllResponse(scopeId)` — shape `/list` response, defaults to `managerService.findAllByScopeId(scopeId)`
- `resolveScope(scopeTypeId)` — resolve `ResourceScope` from typeId; defaults to `ResourceScope.getById`

### Overriding checkPermission

Example: read is open to any logged-in user; writes require specific permissions. See `ManagerApprovalFlowInstanceController`:

```kotlin
override suspend fun checkPermission(
    scope: ResourceScope,
    scopeId: Long?,
    operation: ScopedOperation,
    userAuthentication: UserAuthentication
): Boolean {
    return operation == ScopedOperation.READ  // READ always allowed
}
```

## Registering permissions

All 12 permissions in the Triad must be defined in `SystemPermission` / matching `TenantPermission` constants and registered via `SystemRbacRegistry` / `TenantRbacRegistry`. See [System Permission](/en/develop/sdk/system-permission) and [Tenant Permission](/en/develop/sdk/tenant-permission).

## Notes

- DTO bases must use the Scoped variants (`BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`); the plain `BaseManagerReadDTO` cannot propagate scope
- Update / Delete DTOs do not carry scope — the Controller resolves scope from the DB entity; the client should not send it again
- `@ManagerPermissions` has no effect here (AOP pointcut doesn't cover this base)
- All 12 permissions must be filled — even for slots unused in practice, put a real string; a well-designed permission tree usually avoids this
- Entity must extend `BaseScopedEntity`; the generic bound rejects non-compliant entities at compile time

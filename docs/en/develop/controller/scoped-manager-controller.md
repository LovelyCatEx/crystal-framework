# StandardScopedManagerController

Base class for resources that can live under either SYSTEM or TENANT scope. The entity carries its own `scope` + `scopeId` columns; the Controller uses `PermissionMatrix` (4 layers × 4 operations = 16 permissions) to pick the right permission at runtime.

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

## Permission model: PermissionMatrix (4 layers)

Scoped resources cover SYSTEM + TENANT simultaneously; 4 layers × 4 CRUD = 16 permissions:

| Layer          | authority prefix   | Constant source     | Semantics                                    |
|----------------|--------------------|---------------------|----------------------------------------------|
| `super`        | none               | `SystemPermission`  | Cross-scope super admin (SYSTEM + TENANT)     |
| `system`       | `system.`          | `SystemPermission`  | SYSTEM scope only                            |
| `tenantAdmin`  | `tenant.`          | `SystemPermission`  | TENANT scope, cross-tenant (ops admin)        |
| `tenantPem`    | `i.tenant.`        | `TenantPermission`  | TENANT scope, strict tenantId match           |

Authorization rule (`PermissionMatrix.layersFor(scope, op)`):

```
SYSTEM scope → hasAnyAuthority(super<op>, system<op>)
TENANT scope → hasAnyAuthority(super<op>, tenantAdmin<op>, tenantPem<op>)
```

Ownership rule (`checkOwnership`, using `crossTenantLayersFor(op)` to identify cross-tenant layers):

- SYSTEM: passes once permission check passes
- TENANT: hold `super<op>` or `tenantAdmin<op>` (cross-tenant layers) → pass; else require `scopeId == user's tenantId`

### Two sentinel values

- `PermissionMatrix.NOT_APPLICABLE` — the layer doesn't apply (e.g. resource doesn't expose a super admin); `layersFor` filters it out, no role in the decision
- `PermissionMatrix.NEVER_GRANTED` — the layer exists but the op is sealed off (Readonly variants use it for CUD); `layersFor` keeps it as a placeholder but nothing ever matches

**Rule**: use the convenience factories or DSL defaults for unfilled layers; don't overload `NEVER_GRANTED` to mean "not applicable".

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

Permissions for the Scoped family are passed via `PermissionMatrix` in the constructor, with the DSL spelling out the 4 layers:

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
    permissions = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_DICT_TYPE_READ
            update = SystemPermission.ACTION_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_DICT_TYPE_DELETE
        }
        system {
            create = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ
            update = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE
        }
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_TENANT_DICT_TYPE_READ
            update = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE
        }
        tenantPem {
            create = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM
            read   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM
            update = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM
            delete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM
        }
    },
)
```

`` `super` `` is a Kotlin keyword; the DSL requires backticks. Unopened layers default entirely to `NOT_APPLICABLE` and stay out of the decision. The Controller needs no method body; all 5 endpoints inherit.

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

- `checkPermission(scope, scopeId, operation, userAuth)` — permission decision. Default routes through Matrix as `RbacUtils.hasAnyAuthority(*matrix.layersFor(scope, op))`; subclasses must override when no Matrix is supplied
- `checkOwnership(scope, scopeId, operation, userAuth)` — ownership check. Default requires a cross-tenant layer (super / tenantAdmin) or `scopeId == tenantId` for TENANT
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

Every real authority in the Matrix (not `NOT_APPLICABLE` / `NEVER_GRANTED`) must be defined in `SystemPermission` / `TenantPermission` constants and registered via `SystemRbacRegistry` / `TenantRbacRegistry`. See [System Permission](/en/develop/sdk/system-permission) and [Tenant Permission](/en/develop/sdk/tenant-permission).

## Notes

- DTO bases must use the Scoped variants (`BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`); the plain `BaseManagerReadDTO` cannot propagate scope
- Update / Delete DTOs do not carry scope — the Controller resolves scope from the DB entity; the client should not send it again
- Declare permissions via `PermissionMatrix`; the `@ManagerPermissions` annotation and the legacy `ScopedPermissionTriad` / `ScopedPermissionMatrix` alias are deprecated — see the [Permission Model Migration Guide](./permission-migration)
- Convenience factories fill defaults for you; do not manually stuff unused layers with `NEVER_GRANTED`. `NOT_APPLICABLE` (layer doesn't apply) and `NEVER_GRANTED` (layer exists but op is forbidden) mean different things — don't mix them
- Entity must extend `BaseScopedEntity`; the generic bound rejects non-compliant entities at compile time

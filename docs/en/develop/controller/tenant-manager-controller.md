# StandardTenantManagerController

Base class for resources that live exclusively under tenant scope (cannot exist at SYSTEM level). tenantId is the mandatory scope; permissions come as 8 String constructor parameters.

::: tip Legacy design note
`StandardTenantManagerController` predates the Scoped family. For newly designed resources that could span SYSTEM / TENANT, prefer [StandardScopedManagerController](./scoped-manager-controller). This class is primarily for existing resources hard-locked to tenantId.
:::

## Applicable scenarios

- Tenant roles, tenant members, tenant departments, department members, tenant message channels — resources that by design only live in a tenant, with no system-level counterpart
- Maintenance of legacy code

Other scenarios:

- Dual-scope (SYSTEM + TENANT) → [StandardScopedManagerController](./scoped-manager-controller)
- Global (no scope) → [StandardManagerController](./standard-manager-controller)

## Endpoints

| HTTP | Path | Notes |
|---|---|---|
| GET | `/list?tenantId=xxx` | Full list within a tenant |
| POST | `/create` | Body (form-urlencoded) DTO must carry tenantId |
| POST | `/query` | Body (JSON) DTO must carry tenantId |
| POST | `/update` | Body carries id; tenant ownership verified via parent chain |
| POST | `/delete` | Same |

## Permission model

Two layers: each CRUD op has one system permission and one scoped (tenant) permission. Authorization rule:

```
Check system permission → yes → allow immediately, skip in-scope
Check scoped permission → yes → check tenant ownership → allow if ok
Neither → 403
```

The scoped layer is stricter: holders of the scoped permission additionally prove the resource is in their tenant.

Disable the scoped layer: pass `DISABLED_SCOPED_PERMISSION = ""` (empty string) as `scopedXxxPermission`; the endpoint then only accepts system-level callers.

## Usage steps

Using tenant role as an example.

### 1. Entity

Must be both `BaseEntity` and `ScopedEntity<Long>` (parent-chain walking requires `getDirectParentId`):

```kotlin
@Table("tenant_role")
class TenantRoleEntity(
    id: Long = 0,
    var tenantId: Long = 0,
    var name: String = "",
    var description: String = "",
) : BaseEntity(id), ScopedEntity<Long> {
    override fun getDirectParentId(): Long = tenantId
}
```

### 2. Service

Extends `BaseTenantResourceManagerService`:

```kotlin
interface TenantRoleManagerService : BaseTenantResourceManagerService<
    TenantRoleRepository,
    TenantRoleEntity,
    ManagerCreateTenantRoleDTO,
    ManagerReadTenantRoleDTO,
    ManagerUpdateTenantRoleDTO,
    ManagerDeleteTenantRoleDTO
>
```

### 3. Four DTOs

Create / Read DTOs must carry `tenantId` using the dedicated bases:

```kotlin
class ManagerCreateTenantRoleDTO(
    tenantId: Long,
    val name: String,
    val description: String = "",
) : BaseManagerCreateTenantResourceDTO(tenantId)

class ManagerReadTenantRoleDTO(
    page: Int = 1, pageSize: Int = 20,
    tenantId: Long? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)

class ManagerUpdateTenantRoleDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
) : BaseManagerUpdateDTO(id)

class ManagerDeleteTenantRoleDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 4. Controller

No `@ManagerPermissions` — 8 permissions go through the constructor:

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role")
class ManagerTenantRoleController(
    managerService: TenantRoleManagerService
) : StandardTenantManagerController<
    TenantRoleManagerService,
    TenantRoleRepository,
    TenantRoleEntity,
    ManagerCreateTenantRoleDTO,
    ManagerReadTenantRoleDTO,
    ManagerUpdateTenantRoleDTO,
    ManagerDeleteTenantRoleDTO
>(
    managerService,
    createPermission        = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    scopedCreatePermission  = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
    readPermission          = SystemPermission.ACTION_TENANT_ROLE_READ,
    scopedReadPermission    = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
    updatePermission        = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    scopedUpdatePermission  = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
    deletePermission        = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    scopedDeletePermission  = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
)
```

## Type parameters

| # | Parameter | Constraint |
|---|---|---|
| 1 | `SERVICE` | `BaseTenantResourceManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` and `ScopedEntity<Long>` (union constraint, parent id must be Long) |
| 4 | `CREATE_DTO` | `Any`, default logic expects extending `BaseManagerCreateTenantResourceDTO` |
| 5 | `READ_DTO` | `BaseManagerReadTenantResourceDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## isXxxInScope hooks: nested resources

Default in-scope checks:

- `isCreateInScope(dto)`: casts CREATE_DTO to `BaseManagerCreateTenantResourceDTO`, compares `tenantId == userAuth.tenantId`
- `isQueryInScope(dto)`: same idea, casts to `BaseManagerReadTenantResourceDTO`
- `isReadAllInScope(tenantId)`: plain `tenantId == userAuth.tenantId`
- `isUpdateInScope(dto)`: calls `managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId)` — chain lookup
- `isDeleteInScope(dto)`: same, batch version

Why update / delete use chain lookup: nested resources like department members carry no direct tenantId — `department_member.department_id → department.tenant_id` — so walking `ScopedEntity.getDirectParentId()` recursively up to tenant is required. `TenantRelationshipCheckService.checkIsRelatedToRootParent` encapsulates this recursion.

When to override `isXxxInScope`: the default uses "cast + `tenantId` field equality". If your DTO doesn't extend the standard tenant DTO bases (e.g. custom implementation), or you need more complex judgment (e.g. "this person can access other departments but only one field"), override.

## customXxx hooks: full flow takeover

Every CRUD endpoint has a `customXxx` hook (`customCreate`, `customQuery`, `customUpdate`, `customDelete`, `customReadAll`). Returning non-null short-circuits the standard flow:

```kotlin
override suspend fun customCreate(userAuth, dto): ApiResponse<*>? {
    // When you want to fully take over create:
    if (someBusinessCondition) {
        return ApiResponse.success(customResult)
    }
    return null   // fall through to standard flow
}
```

Rarely used; most cases don't touch these.

## `DISABLED_SCOPED_PERMISSION`

Disable the scoped layer, restricting the endpoint to system-permission holders:

```kotlin
scopedCreatePermission = StandardTenantManagerController.DISABLED_SCOPED_PERMISSION,
// or the equivalent empty string ""
```

Implementation: `hasScopedAuthority` returns false immediately for an empty string, bypassing the scoped check.

## Notes

- Entity must implement `ScopedEntity<Long>` — not `ScopedEntity<*>`; parent id type is fixed to Long. Default `checkIsRelatedToRootParent` recursion assumes Long
- Use the tenant DTO bases: `BaseManagerCreateTenantResourceDTO` / `BaseManagerReadTenantResourceDTO`. Using plain DTOs fails the default `isCreateInScope` / `isQueryInScope` casts with `error(...)`
- Don't try to handle system-level resources with this Controller — it doesn't model SYSTEM scope. Use the Scoped family for cross-scope cases
- All 8 permissions are required — even if a scoped permission is temporarily unused, pass `DISABLED_SCOPED_PERMISSION` rather than omitting the argument

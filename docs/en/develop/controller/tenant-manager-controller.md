# StandardTenantManagerController

Base class for resources that live exclusively under tenant scope (cannot exist at SYSTEM level). tenantId is the mandatory scope; permissions are declared via `PermissionMatrix.tenantOnly(...)`.

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

Within the unified `PermissionMatrix`, Tenant resources use two layers: `tenantAdmin` (with `tenant.` prefix, cross-tenant ops) + `tenantPem` (with `i.tenant.` prefix, own tenant). Authorization rule:

```
Check tenantAdmin permission → yes → allow immediately, skip in-scope
Check tenantPem permission → yes → check tenant ownership → allow if ok
Neither → 403
```

The tenantPem layer is stricter: holders additionally prove the resource is in their tenant. The super / system layers default to `NOT_APPLICABLE` (Tenant resources have no SYSTEM scope concept).

To disable a layer, pass `PermissionMatrix.NOT_APPLICABLE` for the corresponding parameter (the legacy `DISABLED_SCOPED_PERMISSION = ""` empty string is still equivalent; `hasScopedAuthority` short-circuits both to false).

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

Permissions come in via `PermissionMatrix.tenantOnly(...)`. Old `xxxPermission` maps to `tenantAdmin*`, old `scopedXxxPermission` maps to `tenantPem*`:

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
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
        tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
        tenantPemCreate   = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
        tenantPemRead     = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
        tenantPemUpdate   = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
        tenantPemDelete   = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
    ),
)
```

The legacy 8-String constructor (`createPermission = ..., scopedCreatePermission = ..., ...`) is `@Deprecated` and delegates internally to `PermissionMatrix.tenantOnly`, retained one release for legacy compatibility; new code uses the primary constructor only.

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

## Disabling a layer

To restrict the endpoint to tenantAdmin holders, fill the tenantPem layer with `NOT_APPLICABLE`:

```kotlin
permissions = PermissionMatrix.tenantOnly(
    tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ,
    tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    tenantPemCreate   = PermissionMatrix.NOT_APPLICABLE,   // disable tenantPem layer
    tenantPemRead     = PermissionMatrix.NOT_APPLICABLE,
    tenantPemUpdate   = PermissionMatrix.NOT_APPLICABLE,
    tenantPemDelete   = PermissionMatrix.NOT_APPLICABLE,
)
```

The legacy empty string `DISABLED_SCOPED_PERMISSION = ""` still works (`hasScopedAuthority` short-circuits both empty string and `NOT_APPLICABLE` to false), but new code should use `PermissionMatrix.NOT_APPLICABLE` for clearer semantics.

## Notes

- Entity must implement `ScopedEntity<Long>` — not `ScopedEntity<*>`; parent id type is fixed to Long. Default `checkIsRelatedToRootParent` recursion assumes Long
- Use the tenant DTO bases: `BaseManagerCreateTenantResourceDTO` / `BaseManagerReadTenantResourceDTO`. Using plain DTOs fails the default `isCreateInScope` / `isQueryInScope` casts with `error(...)`
- Don't try to handle system-level resources with this Controller — it doesn't model SYSTEM scope. Use the Scoped family for cross-scope cases
- Declare permissions via `PermissionMatrix.tenantOnly(...)`; the legacy 8-String constructor stays but is `@Deprecated`, see the [Permission Model Migration Guide](./permission-migration)

# 租户资源控制器（StandardTenantManagerController）

只属于租户（不能挂在 SYSTEM 下）的资源的基类。tenantId 强制作为 scope，权限通过 `PermissionMatrix.tenantOnly(...)` 声明。

::: tip 老设计说明
`StandardTenantManagerController` 早于 Scoped 家族。对于新设计的可跨 SYSTEM / TENANT 的资源，优先使用 [StandardScopedManagerController](./scoped-manager-controller)。此类主要用于已有的、强制以 tenantId 为唯一 scope 的资源。
:::

## 适用场景

- 租户角色、租户成员、租户部门、部门成员、租户消息渠道等——设计时就明确只属于租户，不会有系统级版本
- 老代码继续维护的场景

其他场景：

- 双 scope（SYSTEM + TENANT）资源 → [StandardScopedManagerController](./scoped-manager-controller)
- 无 scope 的全局资源 → [StandardManagerController](./standard-manager-controller)

## 端点

| HTTP | 路径 | 说明 |
|---|---|---|
| GET | `/list?tenantId=xxx` | 按 tenantId 全量查询 |
| POST | `/create` | 请求体（form-urlencoded）中的 DTO 必须携带 tenantId |
| POST | `/query` | 请求体（JSON）中的 DTO 必须携带 tenantId |
| POST | `/update` | 请求体携带 id，tenant 归属通过父链校验 |
| POST | `/delete` | 同上 |

## 权限模型

统一 `PermissionMatrix` 中，Tenant 资源用两层：`tenantAdmin`（`tenant.` 前缀，跨租户运维）+ `tenantPem`（`i.tenant.` 前缀，本租户）。授权规则：

```
先查 tenantAdmin 权限 → 有 → 立即放行，跳过 in-scope 检查
再查 tenantPem 权限 → 有 → 检查 tenant 归属 → 归属正确才放行
两者都无 → 403
```

tenantPem 层比 tenantAdmin 层更严格：持有 tenantPem 的用户还需证明操作的资源在自己租户内。super / system 层默认 `NOT_APPLICABLE`（Tenant 资源无 SYSTEM scope 概念）。

禁用某一层：把对应参数传成 `PermissionMatrix.NOT_APPLICABLE`（老的空字符串 `DISABLED_SCOPED_PERMISSION = ""` 仍等价可用，`hasScopedAuthority` 对二者都短路返回 false）。

## 使用步骤

以租户角色为例。

### 1. Entity

Entity 必须同时是 `BaseEntity` 和 `ScopedEntity<Long>`（父链校验需要 `getDirectParentId`）：

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

继承 `BaseTenantResourceManagerService`：

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

### 3. 四个 DTO

Create / Read DTO 必须携带 `tenantId`，使用专用基类：

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

权限通过 `PermissionMatrix.tenantOnly(...)` 传入构造参数。旧 `xxxPermission` 语义映射到 `tenantAdmin*`，旧 `scopedXxxPermission` 语义映射到 `tenantPem*`：

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

老的 8-String 构造函数（`createPermission = ..., scopedCreatePermission = ..., ...`）已被 `@Deprecated` 标注，内部转发到 `PermissionMatrix.tenantOnly`，保留一版以兼容存量代码；新代码只用主构造函数。

## 类型参数

| # | 参数 | 约束 |
|---|---|---|
| 1 | `SERVICE` | `BaseTenantResourceManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` 且 `ScopedEntity<Long>`（联合约束，parent id 必须是 Long） |
| 4 | `CREATE_DTO` | `Any`，默认逻辑期望扩展 `BaseManagerCreateTenantResourceDTO` |
| 5 | `READ_DTO` | `BaseManagerReadTenantResourceDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## isXxxInScope 钩子：处理嵌套资源

默认的 in-scope 判断：

- `isCreateInScope(dto)`：将 CREATE_DTO 强转为 `BaseManagerCreateTenantResourceDTO`，比较 `tenantId == userAuth.tenantId`
- `isQueryInScope(dto)`：同上，转为 `BaseManagerReadTenantResourceDTO` 比较
- `isReadAllInScope(tenantId)`：直接 `tenantId == userAuth.tenantId`
- `isUpdateInScope(dto)`：调用 `managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId)` 顺链查找
- `isDeleteInScope(dto)`：同上，批量版

update / delete 走链式查找的原因：部门成员这类嵌套资源的 tenant 归属不在自身——`department_member.department_id → department.tenant_id`——需要顺 `ScopedEntity.getDirectParentId()` 递归上查直到 tenant。`TenantRelationshipCheckService.checkIsRelatedToRootParent` 封装了此递归。

override `isXxxInScope` 的时机：默认逻辑使用"强转 + `tenantId` 字段比较"。若 DTO 不继承标准租户 DTO 基类（如自定义实现），或需要更复杂的判断（例如"某人可访问其他部门但只能看某个字段"），需要 override。

## customXxx 钩子：完全接管流程

每个 CRUD 端点都有一个 `customXxx` 钩子（`customCreate`、`customQuery`、`customUpdate`、`customDelete`、`customReadAll`），返回非 null 会短路整个标准逻辑：

```kotlin
override suspend fun customCreate(userAuth, dto): ApiResponse<*>? {
    // 完全接管 create 流程时：
    if (someBusinessCondition) {
        return ApiResponse.success(customResult)
    }
    return null   // 走标准逻辑
}
```

大多数场景不使用这些钩子。

## 禁用某一层

让端点只对 tenantAdmin 权限持有者开放，把 tenantPem 层填成 `NOT_APPLICABLE`：

```kotlin
permissions = PermissionMatrix.tenantOnly(
    tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ,
    tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    tenantPemCreate   = PermissionMatrix.NOT_APPLICABLE,   // 禁用 tenantPem 层
    tenantPemRead     = PermissionMatrix.NOT_APPLICABLE,
    tenantPemUpdate   = PermissionMatrix.NOT_APPLICABLE,
    tenantPemDelete   = PermissionMatrix.NOT_APPLICABLE,
)
```

老的空字符串 `DISABLED_SCOPED_PERMISSION = ""` 仍等价（`hasScopedAuthority` 对空字符串与 `NOT_APPLICABLE` 都短路返回 false），但新代码用 `PermissionMatrix.NOT_APPLICABLE` 语义更清晰。

## 注意事项

- Entity 必须实现 `ScopedEntity<Long>`——不是 `ScopedEntity<*>`，parent id 类型固定为 Long。默认的 `checkIsRelatedToRootParent` 递归使用 Long
- DTO 基类使用租户版：`BaseManagerCreateTenantResourceDTO` / `BaseManagerReadTenantResourceDTO`。使用普通 DTO 会在默认 `isCreateInScope` / `isQueryInScope` 中强转失败并 `error(...)`
- 此类不处理系统级资源——设计上不含 SYSTEM scope 概念。跨 scope 场景使用 Scoped 家族
- 权限声明必须使用 `PermissionMatrix.tenantOnly(...)`；老的 8-String 构造函数仍在但已 `@Deprecated`，见 [权限模型迁移指南](./permission-migration)

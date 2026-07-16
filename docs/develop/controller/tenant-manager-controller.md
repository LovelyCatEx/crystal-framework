# 租户资源控制器（StandardTenantManagerController）

只属于租户（不能挂在 SYSTEM 下）的资源的基类。tenantId 强制作为 scope，权限通过 8 个 String 构造参数传入。

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

双层权限：每个 CRUD 操作对应两个权限，一个 system 级、一个 scoped（tenant 级）。授权规则：

```
先查 system 权限 → 有 → 立即放行，跳过 in-scope 检查
再查 scoped 权限 → 有 → 检查 tenant 归属 → 归属正确才放行
两者都无 → 403
```

scoped 层比 system 层更严格：持有 scoped 权限的用户还需证明操作的资源在自己租户内。

禁用 scoped 权限：把 `scopedXxxPermission` 传成空字符串（`DISABLED_SCOPED_PERMISSION = ""`），端点仅允许 system 级权限持有者调用。

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

不使用 `@ManagerPermissions`——8 个权限通过构造参数传入：

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

## `DISABLED_SCOPED_PERMISSION`

禁用 scoped 层、让端点只对 system 权限持有者开放：

```kotlin
scopedCreatePermission = StandardTenantManagerController.DISABLED_SCOPED_PERMISSION,
// 或等价的空字符串 ""
```

内部实现：`hasScopedAuthority` 遇到空字符串直接返回 false，跳过 scoped 检查。

## 注意事项

- Entity 必须实现 `ScopedEntity<Long>`——不是 `ScopedEntity<*>`，parent id 类型固定为 Long。默认的 `checkIsRelatedToRootParent` 递归使用 Long
- DTO 基类使用租户版：`BaseManagerCreateTenantResourceDTO` / `BaseManagerReadTenantResourceDTO`。使用普通 DTO 会在默认 `isCreateInScope` / `isQueryInScope` 中强转失败并 `error(...)`
- 此类不处理系统级资源——设计上不含 SYSTEM scope 概念。跨 scope 场景使用 Scoped 家族
- 8 个权限一个都不能省——即使某个 scoped 权限暂时不用，也需传 `DISABLED_SCOPED_PERMISSION` 而非省略参数

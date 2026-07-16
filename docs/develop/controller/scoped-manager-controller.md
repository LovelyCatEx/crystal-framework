# 领域范围控制器（StandardScopedManagerController）

同一份资源可以挂在 SYSTEM 也可以挂在 TENANT 下时使用的基类。资源实体自带 `scope` 和 `scopeId` 两列，Controller 通过 `ScopedPermissionTriad`（12 个权限）在运行时按 scope 选择权限。

## 适用场景

- 字典项：可以是系统全局字典，也可以是租户私有字典
- 审批流程定义：可以是系统级也可以是租户级
- 通知模板、系统配置等多层可挂载资源

其他场景：

- 仅属于租户 → [StandardTenantManagerController](./tenant-manager-controller)
- 无 scope 的全局资源 → [StandardManagerController](./standard-manager-controller)
- 自身无 scope 列，需从父实体推 → [StandardDerivedScopedManagerController](./derived-scoped-manager-controller)
- 只读变体 → [ReadonlyScopedManagerController](./readonly-scoped-manager-controller)

## 端点

| HTTP | 路径 | 说明 |
|---|---|---|
| GET | `/list?scope={0\|1}&scopeId={id}` | 按 scope 全量查询 |
| POST | `/create` | 请求体（form-urlencoded）携带 scope + scopeId |
| POST | `/query` | 请求体（JSON）携带 scope + scopeId + 分页参数 |
| POST | `/update` | 请求体携带 id（scope 从数据库中的实体反查） |
| POST | `/delete` | 请求体携带 ids（scope 从数据库中的实体反查） |

`scope` 为 `Int` 类型：`0 = SYSTEM`，`1 = TENANT`（来自 `ResourceScope.typeId`）。

## 权限模型：ScopedPermissionTriad

每个 CRUD 操作在 Triad 中有三个位（共 12 个权限）：

```
super × CRUD      跨 scope 的管理员（root / admin），可操作任意 scope
system × CRUD     仅 SYSTEM scope 内的资源
tenantPem × CRUD  仅当前用户租户内的资源
```

授权规则：

```
SYSTEM scope 请求 → hasAnyAuthority(super<op>, system<op>)
TENANT scope 请求 → hasAnyAuthority(super<op>, tenantPem<op>)
```

Ownership 规则（在 `checkOwnership` 中）：

- SYSTEM scope：权限校验通过即放行
- TENANT scope：持有 `super<op>` 跨租户权限，或 `scopeId == 当前用户的 tenantId`

## 使用步骤

以 `TenantDictTypeEntity`（继承 `BaseScopedEntity`，自带 `scope` + `scopeId` 列）为例。

### 1. Entity

```kotlin
@Table("tenant_dict_type")
class TenantDictTypeEntity(
    id: Long = 0,
    var name: String = "",
    var description: String = "",
    scope: Int = 0,          // 0 = SYSTEM, 1 = TENANT
    scopeId: Long = 0,       // 系统字典填 0，租户字典填 tenantId
) : BaseScopedEntity(id, scope, scopeId)
```

### 2. Service

放入 `service/manager/`，继承 `BaseScopedManagerService`：

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

### 3. 四个 DTO

DTO 基类使用 Scoped 版本：

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

// Update / Delete 使用普通的 BaseManagerUpdateDTO / BaseManagerDeleteDTO
// scope 从数据库中的实体本身读取，无需客户端再传
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

Scoped 家族不使用 `@ManagerPermissions`，权限通过 `ScopedPermissionTriad` 传入构造参数：

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

Controller 无需方法体，5 个端点全部继承。

## 类型参数

| # | 参数 | 约束 |
|---|---|---|
| 1 | `SERVICE` | `BaseScopedManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseScopedEntity`（必须自带 `scope` + `scopeId` 列） |
| 4 | `CREATE_DTO` | `Any`，实际必须扩展 `BaseManagerCreateScopedDTO` |
| 5 | `READ_DTO` | `BaseManagerReadScopedDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## 可 override 的钩子

- `checkPermission(scope, scopeId, operation, userAuth)` — 权限决策。默认按 Triad 走 `hasAnyAuthority`；不使用 Triad 时子类必须 override
- `checkOwnership(scope, scopeId, operation, userAuth)` — 归属校验。默认 TENANT scope 要求 `scopeId == tenantId`（除非持有 super 权限）
- `buildQueryResponse(dto, userAuth)` — `/query` 响应整形，默认返回 `managerService.query(dto)`
- `buildReadAllResponse(scopeId)` — `/list` 响应整形，默认返回 `managerService.findAllByScopeId(scopeId)`
- `resolveScope(scopeTypeId)` — 从 typeId 解析 `ResourceScope`，默认走 `ResourceScope.getById`

### checkPermission 的 override 示例

例如：读操作对所有登录用户开放，写操作按权限校验。参考 `ManagerApprovalFlowInstanceController`：

```kotlin
override suspend fun checkPermission(
    scope: ResourceScope,
    scopeId: Long?,
    operation: ScopedOperation,
    userAuthentication: UserAuthentication
): Boolean {
    return operation == ScopedOperation.READ  // READ 恒放行
}
```

## 权限注册

Triad 中的 12 个权限都需要在对应的 `SystemPermission` / `TenantPermission` 常量类中定义，并通过 `SystemRbacRegistry` / `TenantRbacRegistry` 注册。详见 [系统权限](/develop/sdk/system-permission) 和 [租户权限](/develop/sdk/tenant-permission)。

## 注意事项

- DTO 基类必须使用 Scoped 版本（`BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`），不能使用普通的 `BaseManagerReadDTO`，否则 scope 字段无法传递
- Update / Delete DTO 不携带 scope——由 Controller 从数据库反查实体获取，客户端无需传递
- `@ManagerPermissions` 注解在此类上无效（AOP 的 pointcut 不覆盖）
- 12 个权限一个都不能省。即使某些位业务上暂时用不到（如某资源不允许跨 scope 操作），也需填入真实存在的字符串
- Entity 必须继承 `BaseScopedEntity`，泛型约束会在编译期挡下不合规的实体

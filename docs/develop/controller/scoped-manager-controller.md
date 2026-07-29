# 领域范围控制器（StandardScopedManagerController）

同一份资源可以挂在 SYSTEM 也可以挂在 TENANT 下时使用的基类。资源实体自带 `scope` 和 `scopeId` 两列，Controller 通过 `PermissionMatrix`（4 层 × 4 操作 = 16 个权限）在运行时按 scope 选择权限。

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

## 权限模型：PermissionMatrix（4 层）

Scoped 资源同时覆盖 SYSTEM + TENANT，共 4 层 × 4 CRUD = 16 个权限：

| 层             | authority 前缀     | 常量来源                | 语义                                |
|---------------|-----------------|---------------------|-----------------------------------|
| `super`       | 无前缀             | `SystemPermission`  | 跨 SYSTEM + TENANT 的超级管理员          |
| `system`      | `system.`       | `SystemPermission`  | 仅 SYSTEM scope                    |
| `tenantAdmin` | `tenant.`       | `SystemPermission`  | TENANT scope 且跨租户（"运维管理员"）      |
| `tenantPem`   | `i.tenant.`     | `TenantPermission`  | TENANT scope 且严格匹配 tenantId       |

授权规则（`PermissionMatrix.layersFor(scope, op)`）：

```
SYSTEM scope 请求 → hasAnyAuthority(super<op>, system<op>)
TENANT scope 请求 → hasAnyAuthority(super<op>, tenantAdmin<op>, tenantPem<op>)
```

Ownership 规则（`checkOwnership`，用 `crossTenantLayersFor(op)` 判定跨租户层）：

- SYSTEM scope：权限校验通过即放行
- TENANT scope：持有 `super<op>` 或 `tenantAdmin<op>`（跨租户层）→ 放行；否则要求 `scopeId == 当前用户的 tenantId`

### 两个哨兵值

- `PermissionMatrix.NOT_APPLICABLE` —— 层不适用（如资源不打算暴露 super 管理员），`layersFor` 过滤掉，不参与决策
- `PermissionMatrix.NEVER_GRANTED` —— 层存在但操作被封死（Readonly 变体的 CUD 用它），`layersFor` 保留占位但永不匹配

**规则**：未打开的 layer 走便捷工厂或 DSL 默认值，不要用 `NEVER_GRANTED` 表达"不适用"。

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

Scoped 家族权限通过 `PermissionMatrix` 传入构造参数，用 DSL 展开 4 层：

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

`` `super` `` 是 Kotlin 关键字，DSL 中必须反引号包裹。未打开的 layer 默认全部 `NOT_APPLICABLE`，不参与决策。Controller 无需方法体，5 个端点全部继承。

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

- `checkPermission(scope, scopeId, operation, userAuth)` — 权限决策。默认按 Matrix 走 `RbacUtils.hasAnyAuthority(*matrix.layersFor(scope, op))`；不使用 Matrix 时子类必须 override
- `checkOwnership(scope, scopeId, operation, userAuth)` — 归属校验。默认 TENANT scope 要求持有跨租户层（super / tenantAdmin）或 `scopeId == tenantId`
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

Matrix 中真正使用的 authority（非 `NOT_APPLICABLE` / `NEVER_GRANTED`）都需要在对应的 `SystemPermission` / `TenantPermission` 常量类中定义，并通过 `SystemRbacRegistry` / `TenantRbacRegistry` 注册。详见 [系统权限](/develop/sdk/system-permission) 和 [租户权限](/develop/sdk/tenant-permission)。

## 注意事项

- DTO 基类必须使用 Scoped 版本（`BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`），不能使用普通的 `BaseManagerReadDTO`，否则 scope 字段无法传递
- Update / Delete DTO 不携带 scope——由 Controller 从数据库反查实体获取，客户端无需传递
- 权限声明必须使用 `PermissionMatrix`；`@ManagerPermissions` 注解、旧的 `ScopedPermissionTriad` / `ScopedPermissionMatrix` 别名均已弃用，见 [权限模型迁移指南](./permission-migration)
- 便捷工厂已经把默认值填好，不必手工把不用的层填成 `NEVER_GRANTED`；`NOT_APPLICABLE`（层不适用）与 `NEVER_GRANTED`（层存在但操作禁止）语义不同，别混用
- Entity 必须继承 `BaseScopedEntity`，泛型约束会在编译期挡下不合规的实体

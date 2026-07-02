# 只读领域范围控制器（ReadonlyScopedManagerController)

[`StandardScopedManagerController`](./scoped-manager-controller) 的只读变体。查询端点全部继承，写操作（create / update / delete）由业务层重写返回 403。适用于系统生成、支持 SYSTEM / TENANT 双 scope 展示、但不允许用户修改的资源。

## 适用场景

- 审批流程实例（系统模式和租户模式均可查看，但用户不能直接修改实例）
- 审批任务列表
- 其他系统生成、双 scope 展示的只读记录

其他场景：

- 允许修改的双 scope 资源 → [StandardScopedManagerController](./scoped-manager-controller)
- 全局只读资源（无 scope） → [ReadonlyManagerController](./readonly-manager-controller)

## 端点

继承自 `StandardScopedManagerController`，写操作在业务层返回 403：

| HTTP | 路径 | 行为 |
|---|---|---|
| GET | `/list?scope=&scopeId=` | 正常返回 |
| POST | `/query` | 正常返回 |
| POST | `/create` | 403 Forbidden |
| POST | `/update` | 403 Forbidden |
| POST | `/delete` | 403 Forbidden |

## 使用 `Triad.readonly(...)` 工厂

`ScopedPermissionTriad` 有 12 个权限位，只读场景只需要 3 个读权限。工厂方法 `readonly` 会把其他 9 个 CRUD 位填成 `NEVER_GRANTED`（一个永远不被授予的常量），确保即使某天代码绕过 ReadonlyScoped 直接查权限也拒绝匹配：

```kotlin
permissions = ScopedPermissionTriad.readonly(
    superRead      = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead  = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

## 使用步骤

以审批流程实例（只读）为例。

### 1. Entity

使用 Scoped 家族的 entity（继承 `BaseScopedEntity`）：

```kotlin
@Table("approval_flow_instance")
class ApprovalFlowInstanceEntity(
    id: Long = 0,
    var definitionId: Long = 0,
    var initiatorId: Long = 0,
    var status: Int = 0,
    scope: Int = 0,
    scopeId: Long = 0,
) : BaseScopedEntity(id, scope, scopeId)
```

### 2. Service

继承 `BaseScopedManagerService`：

```kotlin
interface ApprovalFlowInstanceManagerService : BaseScopedManagerService<
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO
>
```

### 3. DTO

即使写操作永远不使用，四个 DTO 依然必须提供。Delete DTO 可以直接复用基类：

```kotlin
class ManagerCreateApprovalFlowInstanceDTO(val placeholder: String = "") : Any
class ManagerReadApprovalFlowInstanceDTO(
    page: Int = 1, pageSize: Int = 20, scope: Int = 0, scopeId: Long = 0,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
class ManagerUpdateApprovalFlowInstanceDTO(override val id: Long) : BaseManagerUpdateDTO(id)
// Delete 直接使用基类 BaseManagerDeleteDTO
```

### 4. Controller

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-instances")
class ManagerApprovalFlowInstanceController(
    managerService: ApprovalFlowInstanceManagerService,
    private val approvalFlowEngine: ApprovalFlowEngine,      // 可注入其他依赖
) : ReadonlyScopedManagerController<
    ApprovalFlowInstanceManagerService,
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO
>(
    managerService,
    permissions = ScopedPermissionTriad.readonly(
        superRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        systemRead    = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        tenantPemRead = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
    ),
) {

    // 可添加自定义端点，如"发起审批"
    @PostMapping("/start")
    suspend fun start(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: StartApprovalFlowDTO
    ): ApiResponse<*> {
        // 自行做权限校验 + 业务逻辑
    }
}
```

## 类型参数

与 [StandardScopedManagerController](./scoped-manager-controller) 一致的 7 个类型参数。

## 常用 override 场景

只读场景经常需要根据用户权限动态调整查询结果——例如普通用户只能查看自己发起的审批实例，管理员能查看全部。此逻辑写在 `buildQueryResponse` 中：

```kotlin
override suspend fun buildQueryResponse(
    dto: ManagerReadApprovalFlowInstanceDTO,
    userAuthentication: UserAuthentication,
): Any {
    val canReadAll = RbacUtils.hasAnyAuthority(*permissions!!.forScope(scope, READ))
    val effectiveDto = if (canReadAll) dto
                       else dto.copy(query = 追加 initiator_id 过滤条件)
    return managerService.query(effectiveDto)
}
```

要点：

- 不要在 `checkPermission` 里判断"读权限过不过"——未通过的用户会直接 403，无法返回。策略是"所有登录用户都能读，但读到什么取决于权限"
- 通过 override `checkPermission` 返回 `operation == READ`（放开所有登录用户），再在 `buildQueryResponse` 中注入过滤条件，比重写 `query` 端点更简洁

## 注意事项

- Triad 必须使用 `readonly(...)` 工厂——手动填 12 位很容易漏 `NEVER_GRANTED`
- 写操作的自定义端点需自行做权限校验——父类的 403 只挡了 `/create` / `/update` / `/delete` 三个端点，新加的 `/mark-as-seen` 之类需要自行添加 `@PreAuthorize` 或在方法内 `checkPermission`
- `@ManagerPermissions` 在此类上无效（同 Scoped 家族）
- Delete DTO 可以直接使用基类（`BaseManagerDeleteDTO`），因为业务层直接返回 403，无需在 DTO 上带额外字段

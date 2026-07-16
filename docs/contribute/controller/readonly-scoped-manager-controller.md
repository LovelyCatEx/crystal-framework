# ReadonlyScopedManagerController

## 设计意图

`ReadonlyScopedManagerController` 是 `StandardScopedManagerController` 的直接子类。设计取向与 `ReadonlyManagerController` 一致：继承 + 三方法重写，以最小代价实现"scope 感知的只读资源"。

## 源码

`crystal-shared/controller/ReadonlyScopedManagerController.kt`：

```kotlin
@Validated
abstract class ReadonlyScopedManagerController<...>(
    managerService: SERVICE,
    permissions: ScopedPermissionTriad? = null,
) : StandardScopedManagerController<SERVICE, ...>(managerService, permissions) {

    override suspend fun create(userAuthentication, @ModelAttribute dto: CREATE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be created")
    }

    override suspend fun update(userAuthentication, @ModelAttribute dto: UPDATE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be updated")
    }

    override suspend fun delete(userAuthentication, @ModelAttribute dto: DELETE_DTO): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be deleted")
    }
}
```

结构与 `ReadonlyManagerController` 对称，只是父类换成 `StandardScopedManagerController`。

## 三重防护体系

Scoped 家族的只读比 `ReadonlyManagerController` 多了权限层的 `NEVER_GRANTED` 兜底。完整链路：

```
POST /create
  → StandardScopedManagerController.create（父类原实现，不走 AOP）
      └─ 被 ReadonlyScopedManagerController override
          → 直接返回 ApiResponse.forbidden（第 1 层：业务层拒绝）

即使 ReadonlyScoped 被绕开，Scoped 家族的父类 create 会调 assertAccess：
  → checkPermission(scope, scopeId, CREATE, userAuth)
      └─ triad.forScope(scope, CREATE) 返回 [NEVER_GRANTED, NEVER_GRANTED]
          → hasAnyAuthority("!!never_granted!!", "!!never_granted!!") = false
              → ForbiddenException（第 2 层：权限层拒绝）

若 super / system / tenantPem 权限位不使用 NEVER_GRANTED 而是填了真实读权限：
  → checkPermission 通过（持有读权限的用户碰到 CREATE 权限位配置为读权限）
      → checkOwnership 也可能通过
          → managerService.create(dto) 被真正调用   ← 灾难
```

这就是 `ScopedPermissionTriad.readonly(...)` 工厂强制 CRUD 位填 `NEVER_GRANTED` 的原因——它不是可选的美化，而是权限升级 bug 的兜底防线。

## Delete DTO 复用基类

`ManagerApprovalFlowInstanceController` 的实际写法：

```kotlin
class ManagerApprovalFlowInstanceController(...) : ReadonlyScopedManagerController<
    ApprovalFlowInstanceManagerService,
    ApprovalFlowInstanceRepository,
    ApprovalFlowInstanceEntity,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    BaseManagerDeleteDTO                             // ← 直接用基类
>
```

因为 delete 会 403，DTO 里塞不塞额外字段无关紧要——走不到业务方法。这是节约信号，代码里少一个空壳 DTO 文件。

推荐做法：只有 `ManagerReadXxxDTO` 需要认真定义（需带 scope 和分页），其他三个能用基类就用基类，或者用最简占位。

## Query 端点的智能过滤

`ReadonlyScopedManagerController` 的典型使用模式：根据用户权限动态过滤查询结果。参考 `ManagerApprovalFlowInstanceController`：

```kotlin
override suspend fun checkPermission(
    scope, scopeId, operation, userAuth
): Boolean {
    return operation == ScopedOperation.READ   // 全部登录用户都能进 query
}

override suspend fun buildQueryResponse(
    dto: ManagerReadApprovalFlowInstanceDTO,
    userAuthentication: UserAuthentication,
): Any {
    val resolvedScope = resolveScope(dto.scope)
    val triad = permissions ?: error(...)
    val canReadAll = RbacUtils.hasAnyAuthority(*triad.forScope(resolvedScope, ScopedOperation.READ))

    val effectiveDto = if (canReadAll) dto
                       else dto.copy(query = appendInitiatorCondition(dto.query, initiatorId))

    return managerService.query(effectiveDto)
}
```

模式关键：

- `checkPermission` 恒返回 true（对读操作）——权限不决定"能不能查"，而决定"能查多少"
- 权限判断挪到 `buildQueryResponse`：持有读权限 → 全量结果；未持有 → 注入 `initiator_id = 当前用户`
- 通过 `dto.copy(query = ...)` 修改 QueryNode：`BaseManagerReadDTO.query` 是 `QueryNode` 类型的结构化条件树，向根节点追加 `AND initiator_id = X` 的过滤
- `appendInitiatorCondition` 是私有辅助：用 `GroupNode(logic = AND, children = [existing, initiatorCondition])` 拼接

优势：单端点服务两类用户，前端不需要感知权限差异——列表逻辑一致，权限过滤在后端透明化。

## 与 ReadonlyManagerController 的对比

| | ReadonlyManagerController | ReadonlyScopedManagerController |
|---|---|---|
| 父类 | `StandardManagerController` | `StandardScopedManagerController` |
| 权限声明 | `@ManagerPermissions`（类注解，5 个字段） | `ScopedPermissionTriad`（构造参数，12 位） |
| Scope 支持 | 无 | 强制 SYSTEM / TENANT 二选一 |
| Entity 约束 | `BaseEntity` | `BaseScopedEntity` |
| AOP 覆盖 | 有 | 无，方法内自校验 |
| NEVER_GRANTED 兜底 | 无（不需要） | 有（关键） |

## 现有真实使用位置

| 模块 | Controller | 承载资源 |
|---|---|---|
| `crystal-approval` | `ManagerApprovalFlowInstanceController` | 审批流程实例（读 + 发起，不可编辑） |
| `crystal-approval` | `ManagerApprovalFlowTaskController` | 审批任务（读 + 审批操作，不可编辑） |

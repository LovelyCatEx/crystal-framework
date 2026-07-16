---
name: add-readonly-scoped-manager-controller
description: 为按 scope 区分且外部不允许写入的实体（如审批实例、审批任务等系统自动生成的数据）添加只读 CRUD 端点，继承 ReadonlyScopedManagerController，create/update/delete 端点直接 403 Forbidden，常配合 override checkPermission 放开 read 给所有 authenticated 用户并在 buildQueryResponse 里注入 initiator/assignee 之类的隐式过滤。
---

# 添加 Readonly Scoped Manager Controller

## 触发条件

当用户要求为一个 **系统自动生成、外部不允许 create/update/delete** 但需要按 scope 查询的实体添加管理接口时使用。典型场景：

- 审批流程实例（`ApprovalFlowInstance`）— 由 approval engine 生成，用户只能读不能改
- 审批任务（`ApprovalFlowTask`）— 由 engine 分配，用户只能 handle（走独立端点），不能 create/update
- 日志类实体的只读管理面板

如果实体允许完整 CRUD → 用 `add-scoped-manager-controller`。

## 判断标准

**用 ReadonlyScopedManagerController（是）**：
- 实体由系统内部逻辑创建 / 修改 / 删除，外部 API 只应该能查
- 但仍需要按 scope（SYSTEM / TENANT）授权可见性
- 可能还需要"授权 read，但结果集里只包含当前用户相关行"（如"只看自己发起的审批"）

**不用**：
- 实体允许外部 write → `add-scoped-manager-controller`
- 无 scope 概念 → `add-standard-manager-controller`
- 完全自定义端点（不走标准 CRUD 路由）→ `add-generic-controller`

## 输入格式

用户需要提供：
1. Entity 类（`BaseScopedEntity` 或派生 scoped）
2. 端点前缀
3. 需要的 read 权限（最少 3 个：superRead / systemRead / tenantAdminRead / tenantPemRead 中的某几个）
4. 结果过滤策略（"admin 可见全量 / 普通用户只见自己发起的"、或"只见自己被指派的"）

## 前提信息

### 与标准 Scoped 的关系

`ReadonlyScopedManagerController` **继承自** `StandardScopedManagerController`，只做一件事：把 create / update / delete 三个端点 override 成 `return ApiResponse.forbidden(...)`。`/list` `/query` 端点原封不动继承。

所有 scope 解析 / 权限校验 / 溯源逻辑 **完全一样**。

### 只读 Matrix Factory

用 `ScopedPermissionMatrix.readonly(...)` 构造，只填 4 个 read 权限：

```kotlin
permissions = ScopedPermissionMatrix.readonly(
    superRead = SystemPermission.ACTION_XXX_READ,
    systemRead = SystemPermission.ACTION_XXX_READ,        // SYSTEM 里读全部
    tenantAdminRead = SystemPermission.ACTION_TENANT_XXX_READ,
    tenantPemRead = TenantPermission.ACTION_TENANT_XXX_READ_PEM,
)
```

其余 12 个槽位（create/update/delete 的 4 层）自动填 `NEVER_GRANTED`（不授予给任何角色的哨兵）。

### 三种典型 override 模式

**模式 1 — 严格 read-all**（默认 4 层 Matrix，谁没权限就 403）：

```kotlin
class ManagerXxxController(managerService: XxxManagerService) : ReadonlyScopedManagerController<...>(
    managerService,
    permissions = ScopedPermissionMatrix.readonly(...)
)
```

`/query` `/list` 都必须持有 read 权限才能访问。

**模式 2 — 全 authenticated 都可 read，但结果集自动过滤到"自己相关"的**：

override `checkPermission` 放开 READ，在 `buildQueryResponse` 里根据是否 read-all 决定要不要注入 initiator / assignee 过滤：

```kotlin
override suspend fun checkPermission(scope, scopeId, operation, auth): Boolean {
    return operation == ScopedOperation.READ
}

override suspend fun buildQueryResponse(dto: ReadDTO, auth: UserAuthentication): Any {
    val matrix = permissions!!
    val canReadAll = RbacUtils.hasAnyAuthority(*matrix.layersFor(resolveScope(dto.scope), READ))

    // id 短路径必须 read-all，防止别的用户凭 id 越权
    if (dto.id != null && !canReadAll) {
        throw ForbiddenException("Id lookup requires read-all authority")
    }

    val effectiveDto = if (canReadAll) {
        dto
    } else {
        val filterId = when (resolveScope(dto.scope)) {
            SYSTEM -> auth.userId
            TENANT -> auth.tenantMemberId ?: throw ForbiddenException(...)
        }
        dto.copy(query = appendXxxCondition(dto.query, filterId))
    }
    return managerService.query(effectiveDto)
}
```

`ApprovalFlowInstanceController` 就是这个模式。

**模式 3 — 硬编码 self-only，完全无 read-all 概念**：

`buildQueryResponse` 无条件注入 `assignee_id = self`，任何调用者都只能看自己的：

```kotlin
override suspend fun buildQueryResponse(dto: ReadDTO, auth: UserAuthentication): Any {
    val assigneeId = resolveAssigneeId(resolveScope(dto.scope), auth)
    if (dto.id != null) {
        // id 短路径也要校验：拿出目标行检查 assignee
        val task = managerService.getByIdOrNull(dto.id)
        if (task != null && task.assigneeId != assigneeId) {
            throw ForbiddenException("Task not assigned to current user")
        }
    }
    return managerService.query(dto.copy(query = appendAssigneeCondition(dto.query, assigneeId)))
}
```

`ApprovalFlowTaskController` 就是这个模式。此模式下往往不传 `permissions`（或传但用 `NEVER_GRANTED` 全填）。

### 独立的 "my" 端点（自愿加）

`/query` 里塞条件过滤的模式有个 **id 短路径漏洞**：`BaseManagerService.query` 在 `dto.id != null` 时会走 `getByIdOrNull` 直接返回，跳过 `buildQueryCriteria`，前端凭 id 就能拿到别人的数据。防守方案有二：

1. **在 `buildQueryResponse` 里前置校验 id**（如模式 2/3 所示）
2. **加一个独立 `POST /my` 端点**：不走 Matrix 校验，硬编码 `dto.id = null` + 注入 initiator/assignee 过滤

```kotlin
@PostMapping("/my", version = "1")
suspend fun queryMy(
    userAuthentication: UserAuthentication,
    @Valid @RequestBody dto: ReadDTO,
): ApiResponse<*> {
    val filterId = resolveFilterId(resolveScope(dto.scope), userAuthentication)
    val forcedDto = dto.copy(
        id = null,   // 关键：清 id 防绕过
        query = appendXxxCondition(dto.query, filterId),
    )
    return ApiResponse.success(managerService.query(forcedDto))
}
```

前端"我的审批""我的任务"这种页面走 `/my`，管理端读全量走 `/query`。语义清晰。

### id 短路径漏洞（务必注意）

`BaseManagerService.query` 里：
```kotlin
if (dto.id != null) {
    return getByIdOrNull(dto.id)   // 不走 buildQueryCriteria
}
```

**只在 `buildQueryResponse` 里 append query 条件不足以保护 id 查询**。任何在 `buildQueryResponse` 注入过滤条件的 override，都必须**同时处理 `dto.id != null` 的分支**，否则默认放行等于漏洞。

## 执行步骤

1. **明确采用哪种模式**（1 / 2 / 3）
2. **权限常量** —（`add-system-permission` skill）
   - 模式 1：4 层 read 权限
   - 模式 2：同上 + 明确"什么权限算 read-all"
   - 模式 3：无需 read 权限（`permissions` 传 null，或 override checkPermission 直接返回 true）
3. **4 个 DTO**（Create/Update/Delete DTO 用 `BaseManagerXxx` 通用类型，因为不会被端点使用）：
   ```kotlin
   ManagerXxxController : ReadonlyScopedManagerController<
       XxxManagerService, XxxRepository, XxxEntity,
       ManagerCreateApprovalFlowXxxDTO,      // 占位（不会被真正使用）
       ManagerReadXxxDTO,
       ManagerUpdateApprovalFlowXxxDTO,      // 占位
       BaseManagerDeleteDTO
   >(...)
   ```
4. **Controller override**：按选定的模式 override `checkPermission` / `buildQueryResponse`
5. **可选加 `/my` 端点**（模式 2 / 3 通常都加）
6. **可选加自定义业务端点**（如 approval instance 的 `/start`，approval task 的 `/handle`）—— 端点内部走独立授权（用 `resolveScopeFromEntity` 或 `checkPermission` 手动调用）

## 常见错误

| 错误 | 修正 |
|---|---|
| `permissions = null` 但没 override `checkPermission` | 默认 `checkPermission` 会 error；必须 override 或传 permissions |
| 忘 override `buildQueryResponse` 就直接调 `managerService.query(dto)` | 结果集会不带用户过滤，admin 之外的用户看到全部；必须按模式 2/3 注入过滤 |
| override `buildQueryResponse` 但没处理 `dto.id != null` 分支 | id 短路径可以绕过 —— 用户凭 id 拿别人的数据 |
| `ScopedPermissionMatrix.readonly()` 里的 `systemRead` 复用 `super READ` 常量 | 语义不对：系统级 read 应该有独立的 `ACTION_SYSTEM_XXX_READ` 常量 |
| 前端调 `/create` `/update` `/delete` 得到 403，怀疑是权限配错 | 是设计如此，Readonly controller 主动拒绝三个 mutating 端点 |
| 加 `/my` 端点忘了强制 `dto.id = null` | id 短路径同样能绕过 initiator/assignee 过滤 |
| 加 `/my` 端点但没在前端"我的"页面切过去 | 前端仍用 `/query`，admin 视角污染依然存在 |

## 快速参考

| 模式 | permissions | checkPermission | buildQueryResponse | 端点建议 |
|---|---|---|---|---|
| 1 — 严格 read-all | 完整 `readonly(...)` Matrix | 默认 | 默认 | 只 `/query` `/list` |
| 2 — read + 结果集过滤 | 完整 `readonly(...)` Matrix | override 返回 `operation == READ` | override 按 canReadAll 分支 + 处理 `dto.id != null` | 加 `/my` 分离 |
| 3 — 硬 self-only | null 或全 NEVER_GRANTED | override 返回 `operation == READ` | override 无条件注入 `assignee = self` + 处理 `dto.id != null` | 加 `/my` |

## 输出格式

完成后说明：
1. 采用的模式（1/2/3）
2. 新增 Controller 路径
3. 权限常量列表
4. override 了哪些 hook（checkPermission / buildQueryResponse / 各种自定义端点）
5. 是否加了 `/my` 端点

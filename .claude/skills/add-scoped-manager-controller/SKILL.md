---
name: add-scoped-manager-controller
description: 为需要按 scope（SYSTEM/TENANT）区分数据可见性的实体添加 CRUD 管理端点，继承 StandardScopedManagerController，采用 4 层权限矩阵（super/system/tenantAdmin/tenantPem），同一 controller 覆盖直接 scoped 和派生 scoped（无 scope 列、靠父实体溯源）两种场景。
---

# 添加 Scoped Manager Controller

## 触发条件

当用户要求为一个 **按 scope 区分数据可见性** 的实体添加 CRUD 端点时使用。两种子场景都由这个 skill 覆盖：

- **直接 scoped**：实体自带 `scope + scope_id` 列（继承 `BaseScopedEntity`）
- **派生 scoped**：实体本身没有 scope 列，通过父实体外键（如 `type_id`）溯源到父的 scope

如果实体既非 scoped 又非派生 scoped → 用 `add-standard-manager-controller`。
如果资源只读（外部不能写）→ 用 `add-readonly-scoped-manager-controller`。

## 判断标准

**直接 scoped**：
- Entity 继承 `BaseScopedEntity`（`scope: Int, scopeId: Long` 两列自带）
- Service 继承 `BaseScopedManagerService`（**默认已实现** `resolveRootScope`，读 entity.scope/scopeId 直读）
- DTO Create/Read 继承 `BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`（带 `scope + scopeId` 字段）

**派生 scoped**：
- Entity 只是 `BaseEntity`（无 scope 列），但实现 `ScopedEntity<PARENT_ID>`（提供 `getDirectParentId()` 返回父外键）
- Service 继承 `BaseTenantResourceManagerService` 或 `CachedBaseManagerService`，**并额外实现** `ScopedRelationshipCheckService`
- Service impl 必须 override `resolveRootScope(id)` — 通过父 Service 的 `resolveRootScope` 递归溯源到根
- DTO 里 Create/Read 带的是父外键（如 `typeId`），**不是** `scope + scopeId`
- Controller 层必须 override `resolveScopeFromCreateDTO` / `resolveScopeFromReadDTO` 走 Service 的桥接方法

## 输入格式

用户需要提供：
1. Entity 类
2. 端点前缀（如 `/manager/xxx`）
3. **12 个权限常量**（Matrix 4 层 × 4 op：super/system/tenantAdmin/tenantPem 各 CRUD）

## 前提信息

### 权限：4 层 Matrix

`ScopedPermissionMatrix` 是四层 × 四 op 的授权表：

| 层 | name 前缀 | 定义位置 | 语义 |
|---|---|---|---|
| **super** | `<res>.<op>` | `SystemPermission` | 跨 scope（既能 SYSTEM 又能 TENANT） |
| **system** | `system.<res>.<op>` | `SystemPermission` | 仅 SYSTEM scope |
| **tenantAdmin** | `tenant.<res>.<op>` | `SystemPermission` | TENANT scope 跨租户（"运维管理员"） |
| **tenantPem** | `i.tenant.<res>.<op>` | `TenantPermission` | TENANT scope 内本 tenant（严格匹配 tenantId） |

授权规则（由 `StandardScopedManagerController` 自动执行）：
- **checkPermission**：`SYSTEM` 请求走 `super + system`；`TENANT` 请求走 `super + tenantAdmin + tenantPem`
- **checkOwnership**：`TENANT` scope 下持 super / tenantAdmin 直通跨租户；持 tenantPem 必须 `scopeId == auth.tenantId`（用 `resolveRootScope` 解析后的**根 tenantId**，不是任何中间父）

### 端点集

跟标准 Manager 一致：`/list` `/create` `/query` `/update` `/delete`。区别只在授权路径。

### Service 继承要求

**直接 scoped**：
```kotlin
interface XxxManagerService : BaseScopedManagerService<
    XxxRepository, XxxEntity,
    ManagerCreateXxxDTO, ManagerReadXxxDTO,
    ManagerUpdateXxxDTO, ManagerDeleteXxxDTO
>
```

**派生 scoped**：
```kotlin
interface XxxManagerService : BaseTenantResourceManagerService<...>,
    ScopedRelationshipCheckService {
    /** 桥接方法：从父外键（父 id）拿到根 (scope, scopeId)，controller 的 create/query DTO 里没有 item id 时用 */
    suspend fun resolveRootScopeFromParentId(parentId: Long): Pair<ResourceScope, Long>?
}
```

Impl 必须 override：
```kotlin
override suspend fun resolveRootScope(id: Long): Pair<ResourceScope, Long>? {
    val item = getByIdOrNull(id) ?: return null
    return parentService.resolveRootScope(item.parentId)   // 递归溯源
}

override suspend fun resolveRootScopeFromParentId(parentId: Long): Pair<ResourceScope, Long>? {
    return parentService.resolveRootScope(parentId)
}
```

### Controller 结构

**直接 scoped**（最简，5 个 DTO + Matrix 声明）：

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")
class ManagerXxxController(
    managerService: XxxManagerService
) : StandardScopedManagerController<...>(
    managerService,
    permissions = ScopedPermissionMatrix(
        superCreate = SystemPermission.ACTION_XXX_CREATE,
        superRead = SystemPermission.ACTION_XXX_READ,
        superUpdate = SystemPermission.ACTION_XXX_UPDATE,
        superDelete = SystemPermission.ACTION_XXX_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_XXX_CREATE,
        // ... 其余 3 层 × 4 op 全部列出
        tenantPemCreate = TenantPermission.ACTION_TENANT_XXX_CREATE_PEM,
        // ...
    ),
)
```

**派生 scoped**：额外 override 两个 DTO 解析 hook：

```kotlin
override suspend fun resolveScopeFromCreateDTO(dto: ManagerCreateXxxDTO): Pair<ResourceScope, Long> {
    return managerService.resolveRootScopeFromParentId(dto.parentId)
        ?: throw BusinessException("Parent ${dto.parentId} not found")
}

override suspend fun resolveScopeFromReadDTO(dto: ManagerReadXxxDTO): Pair<ResourceScope, Long> {
    return managerService.resolveRootScopeFromParentId(dto.parentId)
        ?: throw BusinessException("Parent ${dto.parentId} not found")
}
```

`resolveScopeFromEntity` **不需要 override** —— 默认调 `managerService.resolveRootScope(entity.id)`，正好命中 Service impl 里的溯源逻辑。

### 前端 scope + scopeId 参数

前端调用时 Read DTO 必须带 `scope + scopeId`：
```typescript
XxxManagerController.query({
    ...props,
    scope: ResourceScope.TENANT,    // 或 SYSTEM
    scopeId: tenantId,               // string 类型
})
```

Create 同理（DTO 继承 `BaseManagerCreateScopedDTO`）。

## 执行步骤

### 直接 scoped 场景

1. **12 + 3 个权限常量** —— 用 `add-system-permission` skill 加：`ACTION_XXX_{CRUD}`（super）+ `ACTION_SYSTEM_XXX_{CRUD}`（system）+ `ACTION_TENANT_XXX_{CRUD}`（tenantAdmin）+ TenantPermission 里 `ACTION_TENANT_XXX_{CRUD}_PEM`（tenantPem）+ 3 个 MENU（super/system/tenantAdmin 端各一个）
2. **admin 角色绑定新权限** —— `SystemRolePermissionRelation.ROLE_ADMIN` 加上 super + tenantAdmin + system 三层
3. **DTO 4 个** —— Create/Read 继承 `BaseManagerCreateScopedDTO` / `BaseManagerReadScopedDTO`
4. **Service 继承 `BaseScopedManagerService`** —— 默认 resolveRootScope 已实现
5. **Controller 继承 `StandardScopedManagerController`**，permissions 传 `ScopedPermissionMatrix(...)`
6. 前端 API 层继承 `BaseManagerController`，调用时带 `scope + scopeId`

### 派生 scoped 场景

在直接 scoped 基础上：

7. Service 接口额外实现 `ScopedRelationshipCheckService`，加桥接方法（如 `resolveRootScopeFromParentId`）
8. Service impl override `resolveRootScope` 和桥接方法，都委托给父 Service 的 `resolveRootScope`
9. Controller override `resolveScopeFromCreateDTO` / `resolveScopeFromReadDTO`（走桥接方法），`resolveScopeFromEntity` 保持默认

## 常见错误

| 错误 | 修正 |
|---|---|
| Matrix 只填 3 层，忘了 tenantAdmin | 必须填齐 4 层 16 个 permission，未使用位用 `ScopedPermissionMatrix.NEVER_GRANTED` |
| tenantPem 权限用 `SystemPermission.ACTION_TENANT_XXX_YYY` | tenantPem 必须来自 `TenantPermission.ACTION_TENANT_XXX_YYY_PEM`（`i.` 前缀） |
| 派生场景 Service impl 只 override `checkIsRelatedToRootParent` 忘了 `resolveRootScope` | 两者不同：前者返回 boolean（tenant 侧），后者返回 pair（scoped 侧）；scoped controller 只调后者 |
| 派生场景 controller 没 override 两个 DTO hook，或者 override 后走 `entity.id` | Create/Query DTO 里没有 item id，只有父外键；必须调 Service 的桥接方法 |
| 派生场景 Service impl 里 `resolveRootScope` 走单跳一层就返回 | 必须调**父 Service** 的 `resolveRootScope` 递归；否则深嵌套会拿到中间层 scope 而非根 tenantId |
| Controller 直接注入父 Service 来溯源 | 溯源逻辑放 Service 层，通过 Service 接口的公开桥接方法暴露给 Controller，避免 Controller 跨模块依赖 |
| tenantPem 用户调用被 checkOwnership 挡下 | 检查 `resolveRootScope` 返回的 tenantId 是否等于 `auth.tenantId`；派生场景常见是父 Service 的 `resolveRootScope` 逻辑错 |
| DTO 里 `scope` 用 `String` | 必须 `Int`（`ResourceScope.typeId`）；`scopeId` 用 `Long`（前端接收自动转 `string`） |

## 快速参考

| 场景 | Entity 基类 | Service 继承 | Service impl 需 override | Controller override |
|---|---|---|---|---|
| **直接 scoped** | `BaseScopedEntity` | `BaseScopedManagerService`（自动带 `ScopedRelationshipCheckService`） | 无 | 无（默认全 OK） |
| **派生 scoped** | `BaseEntity` + `ScopedEntity<PID>` | `BaseTenantResourceManagerService` + 显式加 `ScopedRelationshipCheckService` | `resolveRootScope`（走父 Service）+ 桥接方法 | `resolveScopeFromCreateDTO` + `resolveScopeFromReadDTO`（走桥接方法） |

## 输出格式

完成后说明：
1. 场景（直接 scoped / 派生 scoped）
2. 新增 Controller 路径 + 端点前缀
3. 4 个 DTO 路径
4. 引用的 12 个权限常量（super/system/tenantAdmin 在 SystemPermission，tenantPem 在 TenantPermission）
5. Service impl 里新增 override 的方法（派生场景）
6. 前端 API 文件路径

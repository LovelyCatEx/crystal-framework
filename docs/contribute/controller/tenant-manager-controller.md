# StandardTenantManagerController

## 设计意图

`StandardTenantManagerController` 是 Scoped 家族之前的租户资源专用基类。它建立了"system 级权限 + 租户级权限"的双层授权模型，通过 `isXxxInScope` 钩子处理"非直接租户"的嵌套资源（如部门成员）。

历史脉络：

- v1.x 早期：所有租户资源都使用此 Controller
- v1.10 后：抽出通用的 `StandardScopedManagerController`，把"跨 SYSTEM / TENANT 的双 scope"能力泛化
- 目前：老的租户资源仍留在此 Controller；新写"只属于租户"的资源可选此类，也可选 Scoped（scope 固定 TENANT 的用法）

不建议扩展此 base 引入新的抽象——新能力应向 Scoped 家族添加。

## 源码

`crystal-shared/controller/StandardTenantManagerController.kt`（精简版）：

```kotlin
@Validated
abstract class StandardTenantManagerController<...>(
    protected val managerService: SERVICE,
    protected val createPermission: String,        protected val scopedCreatePermission: String,
    protected val readPermission: String,          protected val scopedReadPermission: String,
    protected val updatePermission: String,        protected val scopedUpdatePermission: String,
    protected val deletePermission: String,        protected val scopedDeletePermission: String,
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long> {

    companion object {
        const val DISABLED_SCOPED_PERMISSION: String = ""
    }

    private suspend fun hasScopedAuthority(authority: String): Boolean {
        if (authority.isBlank()) return false
        return RbacUtils.hasAuthority(authority)
    }

    // Scope 检查钩子（可 override）
    protected suspend fun isCreateInScope(dto: CREATE_DTO, userAuth): Boolean { ... }
    protected suspend fun isQueryInScope(dto: READ_DTO, userAuth): Boolean { ... }
    protected suspend fun isReadAllInScope(tenantId: Long, userAuth): Boolean { ... }
    protected suspend fun isUpdateInScope(dto: UPDATE_DTO, userAuth): Boolean = managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId!!)
    protected suspend fun isDeleteInScope(dto: DELETE_DTO, userAuth): Boolean = managerService.checkIsRelatedToRootParent(dto.ids, userAuth.tenantId!!)

    // 响应整形钩子
    protected suspend fun buildQueryResponse(dto: READ_DTO): Any = managerService.query(dto)
    protected suspend fun buildReadAllResponse(tenantId: Long): Any = managerService.findAllByTenantId(tenantId)

    // 完全接管流程的钩子
    protected suspend fun customCreate(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customQuery(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customUpdate(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customDelete(userAuth, dto): ApiResponse<*>? = null
    protected suspend fun customReadAll(userAuth, tenantId): ApiResponse<*>? = null

    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> {
        customCreate(userAuth, dto)?.let { return it }

        if (RbacUtils.hasAuthority(createPermission)) {
            managerService.create(dto)
        } else if (hasScopedAuthority(scopedCreatePermission)) {
            userAuth.assertTenantIdNotNull()
            if (isCreateInScope(dto, userAuth)) managerService.create(dto)
            else throw UnauthorizedException()
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    // update / delete / readAll / query 结构类似
}
```

## 关键设计决策

### 8 个 String 而非结构化 Triad

Tenant Controller 使用 8 个独立的 String 参数：`createPermission` / `scopedCreatePermission` / …。相比 Scoped 家族的 `ScopedPermissionTriad` 数据类，此设计的问题：

- 没有 `super` 层的显式表达——跨租户运维需依赖 system 权限，权限继承语义模糊
- 构造函数签名冗长——8 个 String 参数容易顺序错乱
- 无法方便地表达"只读"变体——没有类似 `Triad.readonly(...)` 的工厂

但这是老代码的历史遗留，重写成本高，暂时保留。

### 双层权限：system → scoped

授权顺序：

```
1. RbacUtils.hasAuthority(systemPermission) → true → 立即放行，跳过 in-scope 检查
2. hasScopedAuthority(scopedPermission) → true → 检查 isXxxInScope → true 才放行
3. 两者都无 → 403
```

system 级放行不查 in-scope 是刻意的——system 权限本身即表示"跨租户运维能力"，无需再限制 tenantId。scoped 权限则是"在自己租户内的授权"，必须证明操作的资源在自己租户内。

### `DISABLED_SCOPED_PERMISSION = ""` 常量

用空字符串禁用 scoped 层：

```kotlin
private suspend fun hasScopedAuthority(authority: String): Boolean {
    if (authority.isBlank()) return false
    return RbacUtils.hasAuthority(authority)
}
```

选用空字符串而非 nullable 的原因：Kotlin 中 `""` 作为 constant 使用比 `null` 简洁（`const val` 只能是非空字面量）。语义等价："空字符串 = 未配置 scoped 权限"。

对比 `ScopedPermissionTriad.NEVER_GRANTED`：那是一个"看起来像权限但永不匹配"的字符串（`"!!never_granted!!"`），走 `hasAuthority` 校验但恒返回 false；空字符串是在校验前就短路，两种设计意图不同。

### isUpdateInScope / isDeleteInScope 走链式查找

默认实现：

```kotlin
protected suspend fun isUpdateInScope(dto: UPDATE_DTO, userAuth): Boolean {
    return managerService.checkIsRelatedToRootParent(dto.id, userAuth.tenantId!!)
}
```

`checkIsRelatedToRootParent` 来自 `TenantRelationshipCheckService`（在 `crystal-shared`），逻辑：

1. 通过 `dto.id` 查子实体
2. 调 `entity.getDirectParentId()` 拿直接父的 id
3. 用父 id 查父实体
4. 若父实体也是 `ScopedEntity`，递归 step 2-3
5. 直到到达顶层（如 `TenantEntity` 本身），比较 id 是否等于目标 tenantId

不直接比较 `entity.tenantId == userAuth.tenantId` 的原因：嵌套资源没有直接的 tenantId 字段——部门成员的 tenant 挂在部门上，部门的 tenant 挂在租户自身。链式查找是唯一通用方案。

代价：每次 update / delete 触发多次 DB 查询（每层一次）。可通过 `CachedBaseService` 缓存父实体降低成本。

### customXxx 钩子设计

每个端点都有一个 `customXxx` 钩子，允许子类完全取代标准流程：

```kotlin
@PostMapping("/create")
suspend fun create(userAuth, dto): ApiResponse<*> {
    customCreate(userAuth, dto)?.let { return it }   // ← 非 null 直接返回
    // 标准流程...
}
```

设计动机：v1.x 早期为让子类灵活覆盖某个端点，添加了此套钩子。目前几乎不使用——单个端点的特殊需求可直接 override `create` 方法。custom 钩子保留是因为：

- 老代码有引用
- 部分"先跑一段自定义逻辑，不通过就走标准逻辑"的场景（返回 null 走标准，返回非 null 短路）

新代码不建议依赖 custom 钩子。

## `ScopedEntity<Long>` 而非 `ScopedEntity<*>`

Tenant Controller 严格要求 `ScopedEntity<Long>`——parent id 必须是 Long。原因：

- 默认 `checkIsRelatedToRootParent` 递归拿 Long 型 id 查数据库
- 支持 `Any` 型 parent id 会让递归查询失去类型安全，需要 runtime cast

对比 DerivedScoped 使用 `ScopedEntity<*>`（星投影），因为 DerivedScoped 不使用 `getDirectParentId()`，只是复用类型约束保证"子实体知道父"。

## 现有真实使用位置

| 模块 | Controller | 承载资源 |
|---|---|---|
| `crystal-rbac` | `ManagerTenantRoleController` | 租户角色 |
| `crystal-tenant` | `ManagerTenantMemberController` | 租户成员 |
| `crystal-tenant` | `ManagerTenantDepartmentController` | 租户部门 |
| `crystal-tenant` | `ManagerTenantDepartmentMemberController` | 部门成员（走链式查找） |
| `crystal-tenant` | `ManagerTenantMessageChannelController` | 租户消息渠道 |
| `crystal-tenant` | `ManagerTenantInvitationController` | 租户邀请 |

新增 tenant-only 资源若场景合适，可继续使用此 Controller；若预见未来可能有 SYSTEM 版本，考虑 [StandardScopedManagerController](./scoped-manager-controller) 走 TENANT-only 用法。

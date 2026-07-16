# StandardDerivedScopedManagerController

## 设计意图

`StandardDerivedScopedManagerController` 是 Scoped 家族的补集。它承担一类特定的资源模型：scope 概念存在，但不在实体自身，而是挂在父实体的关系链末端。典型示例是"字典项 → 字典类型 → 字典类型的 scope"。

设计取舍：

- 不硬塞 scope 列到子实体——若字典项自身冗余存一份 `scope` + `scopeId`，需要维护两处一致（改父类型的 scope 必须联动改所有子项），增加维护负担和数据不一致风险
- 不改造 `StandardScopedManagerController`——会让 base 类背负条件分支（"有 scope 列走这条路，没有走那条路"）
- 独立一个基类：`StandardDerivedScopedManagerController` 用抽象方法把"如何从上下文推导 scope"交给子类

## 源码

`crystal-shared/controller/StandardDerivedScopedManagerController.kt`（精简版）：

```kotlin
@Validated
abstract class StandardDerivedScopedManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: ScopedPermissionTriad,      // 必须提供，非 nullable
) where ENTITY : BaseEntity, ENTITY : ScopedEntity<*> {

    // 三个抽象方法：从不同来源解析 scope
    protected abstract suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long>
    protected abstract suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long>
    protected abstract suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long>

    protected open suspend fun checkOwnership(scope, scopeId: Long, operation, userAuth): Boolean {
        return when (scope) {
            SYSTEM -> true
            TENANT -> {
                if (RbacUtils.hasAuthority(permissions.superFor(operation))) true
                else scopeId == userAuth.tenantId
            }
        }
    }

    protected open suspend fun buildQueryResponse(dto, userAuth): Any = managerService.query(dto)

    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> {
        val (scope, scopeId) = resolveScopeFromCreateDTO(dto)
        assertAccess(scope, scopeId, CREATE, userAuth)
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query") suspend fun query(userAuth, dto: READ_DTO): ApiResponse<*> {
        val (scope, scopeId) = resolveScopeFromReadDTO(dto)
        assertAccess(scope, scopeId, READ, userAuth)
        return ApiResponse.success(buildQueryResponse(dto, userAuth))
    }

    @PostMapping("/update") suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> {
        val entity = managerService.getByIdOrThrow(dto.id)
        val (scope, scopeId) = resolveScopeFromEntity(entity)
        assertAccess(scope, scopeId, UPDATE, userAuth)
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete") suspend fun delete(userAuth, dto: DELETE_DTO): ApiResponse<*> {
        val entities = dto.ids.map { managerService.getByIdOrThrow(it) }
        val resolved = entities.map { resolveScopeFromEntity(it) }
        resolved.toSet().forEach { (scope, scopeId) ->
            assertAccess(scope, scopeId, DELETE, userAuth)
        }
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
```

## 关键设计决策

### 无 `/list` 端点

`StandardScopedManagerController` 有 `readAll(scope, scopeId)`——按 scope 全量枚举。DerivedScoped 故意省略此端点，理由：

- 派生实体的语义就是"隶属于某个父实体"，无父上下文时枚举出的集合是混沌的——上万个字典项跨若干个字典类型，堆在一起对使用方毫无意义
- 强制业务通过自定义端点（如 `/tree?typeId=xxx`）传入父上下文，保证列表结果是语义完整的一组子级
- 权限层面也更清晰：一次列出跨多个 scope 的记录需要多次校验，性能和语义都难处理

### `where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>` 联合约束

Kotlin 的 `where` 子句表达多重上界，同时要求：

1. `BaseEntity`：保证 id / 时间戳 / 软删除等基础字段可用
2. `ScopedEntity<*>`：保证有 `getDirectParentId()` 方法（可被 `TenantRelationshipCheckService` 用于顺链查找）

`ScopedEntity<*>` 使用星投影——父实体的 id 类型不重要，DerivedScoped 的三个 `resolveScopeFromXXX` 用具体类型，没使用 `ScopedEntity<T>.getDirectParentId()`。星投影保留接口约定但不细究类型。

`ScopedEntity` 与 `getDirectParentId()` 的实际使用者是 `TenantRelationshipCheckService.checkIsRelatedToRootParent`（在 Tenant 家族用），此处仅复用类型约束保证子实体"知道自己的父"。

### permissions 参数为非空

对比 `StandardScopedManagerController` 的 `permissions: ScopedPermissionTriad? = null`（允许为空、子类必须 override `checkPermission`），DerivedScoped 的 `permissions: ScopedPermissionTriad` 是强制非空的：

- 派生实体的鉴权非常规律（12 位）
- 没有理由完全绕过 Triad 走自定义权限——不像 Scoped 家族里 `ManagerApprovalFlowInstanceController` 那种"允许所有登录用户读"的特殊需求
- 强制非空简化实现，避免 `?: error(...)` 分支

## Update / Delete 的 scope 溯源

与 `StandardScopedManagerController` 一致——update / delete 的 scope 从数据库反查实体获取，不接受客户端传入的 scope 值。防止篡改。

delete 批量场景先 `groupBy` `resolveScopeFromEntity` 的结果去重，只对 distinct 的 `(scope, scopeId)` 各校验一次。

## 与 TenantRelationshipCheckService 的关系

看似应该复用 `TenantRelationshipCheckService.checkIsRelatedToRootParent` 完成"从子实体顺链到父 scope"的逻辑，但 DerivedScoped 选择让子类自行实现 `resolveScopeFromXXX`。理由：

- `TenantRelationshipCheckService` 走通用递归链——每次从子级问"我的父是谁"再问祖父，直到 root。对深链场景有意义，但字典项这类"一跳到父"的场景过于重
- 子类实现 `resolveScopeByTypeId(typeId)` 直接一次 Service 调用拿到 scope，更快也更清晰
- 深链场景子类可以在 `resolveScopeFromXXX` 内自行调用 `checkIsRelatedToRootParent`——base 保持"呼叫方式"的抽象但不强制走某条路径

## 现有真实使用位置

| 模块 | Controller | 派生链 |
|---|---|---|
| `crystal-tenant` | `ManagerTenantDictItemController` | 字典项 → 字典类型（`typeId`）→ 字典类型的 scope |

目前项目内仅此一处使用。未来其他"子级实体、scope 挂父级"的场景应优先复用此基类。

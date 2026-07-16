# StandardScopedManagerController

## 设计意图

`StandardScopedManagerController` 解决了 `StandardTenantManagerController` 无法优雅表达的场景：同一份资源既可能属于 SYSTEM 也可能属于 TENANT。核心抽象是 `ResourceScope` + `ScopedPermissionTriad`，把"scope 是什么"和"哪个权限"两件事解耦。

## 三个核心抽象

### ResourceScope

```kotlin
enum class ResourceScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);
    companion object {
        fun getById(typeId: Int): ResourceScope? = entries.firstOrNull { it.typeId == typeId }
    }
}
```

`typeId` 是序列化值（DTO 中 `scope: Int` 即为该值），Enum 本身不出现在 JSON 里。选用 `Int` 而非 String 的原因：

- 存到数据库时占位更小
- 未来新增 scope 类型（如 `DEPARTMENT(2)`）不影响老数据

### ScopedOperation

```kotlin
enum class ScopedOperation { CREATE, READ, UPDATE, DELETE }
```

四个 CRUD 值，用于 Triad 内部按操作分派权限。

### ScopedPermissionTriad

```kotlin
data class ScopedPermissionTriad(
    val superCreate: String, val superRead: String, val superUpdate: String, val superDelete: String,
    val systemCreate: String, val systemRead: String, val systemUpdate: String, val systemDelete: String,
    val tenantPemCreate: String, val tenantPemRead: String, val tenantPemUpdate: String, val tenantPemDelete: String,
) {
    fun forScope(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(operation), systemFor(operation))
        ResourceScope.TENANT -> arrayOf(superFor(operation), tenantPemFor(operation))
    }
}
```

12 个权限 = 3 层 × 4 操作。设计理由：

- 3 层不是"权限继承"关系，而是"授权维度"：
  - `super`：跨 scope（框架管理员）
  - `system`：仅 SYSTEM 内
  - `tenantPem`：仅 TENANT 内
- `forScope()` 返回一个数组，`hasAnyAuthority(...)` 对数组做 OR 匹配
- SYSTEM 请求走 `[super, system]`，TENANT 请求走 `[super, tenantPem]`
- 不存在 `[super, system, tenantPem]` 全组合——SYSTEM 资源不能被 tenantPem 权限持有者操作（会破坏 scope 隔离）

## `NEVER_GRANTED` 兜底

```kotlin
companion object {
    const val NEVER_GRANTED: String = "!!never_granted!!"
    fun readonly(superRead: String, systemRead: String, tenantPemRead: String) = ScopedPermissionTriad(
        superCreate = NEVER_GRANTED, superRead = superRead, superUpdate = NEVER_GRANTED, superDelete = NEVER_GRANTED,
        systemCreate = NEVER_GRANTED, systemRead = systemRead, systemUpdate = NEVER_GRANTED, systemDelete = NEVER_GRANTED,
        tenantPemCreate = NEVER_GRANTED, tenantPemRead = tenantPemRead, tenantPemUpdate = NEVER_GRANTED, tenantPemDelete = NEVER_GRANTED,
    )
}
```

`NEVER_GRANTED` 是刻意的死字符串：

- 不属于任何真实权限常量（`SystemPermission` / `TenantPermission` 中均无）
- `root` 角色的全权自动授权也不包含它（因为不在 `SystemPermission` 反射得到的清单里）
- 前后缀 `!!` 违反项目 `<module>.<resource>.<op>` 命名规范，与真实权限不可能冲突

设想若 `Triad.readonly` 的 CRUD 位填的是读权限：

1. 用户 A 持有读权限
2. 某天 `ReadonlyScopedManagerController` 被绕开或误改为 `Standard`
3. `triad.superFor(CREATE)` 返回读权限
4. `hasAnyAuthority(readPerm, readPerm) = true`
5. 用户 A 的读权限被静默升级为写权限

`NEVER_GRANTED` 用于兜底：即使发生上述情况，`hasAnyAuthority("!!never_granted!!", "!!never_granted!!")` 恒为 false，拒绝到底。此为防御性编程的正确用法——让错误路径的失败模式安全（fail-safe），而非静默升级权限（fail-open）。

## 源码结构

`crystal-shared/controller/StandardScopedManagerController.kt`（精简版）：

```kotlin
@Validated
abstract class StandardScopedManagerController<...>(
    protected val managerService: SERVICE,
    protected val permissions: ScopedPermissionTriad? = null,   // 允许为 null，此时子类必须 override checkPermission
) {
    protected open suspend fun checkPermission(scope, scopeId, operation, userAuth): Boolean {
        val triad = permissions ?: error("...override checkPermission when no Triad")
        return RbacUtils.hasAnyAuthority(*triad.forScope(scope, operation))
    }

    protected open suspend fun checkOwnership(scope, scopeId, operation, userAuth): Boolean {
        return when (scope) {
            SYSTEM -> true
            TENANT -> {
                if (RbacUtils.hasAuthority(triad.superFor(operation))) true
                else scopeId == userAuth.tenantId
            }
        }
    }

    protected open suspend fun buildQueryResponse(dto, userAuth): Any = managerService.query(dto)
    protected open suspend fun buildReadAllResponse(scopeId): Any = managerService.findAllByScopeId(scopeId)
    protected open fun resolveScope(scopeTypeId: Int): ResourceScope = ...

    @GetMapping("/list") suspend fun readAll(userAuth, scope, scopeId): ApiResponse<*> { ... }
    @PostMapping("/create") suspend fun create(userAuth, dto: CREATE_DTO): ApiResponse<*> { ... }
    @PostMapping("/query") suspend fun query(userAuth, dto: READ_DTO): ApiResponse<*> { ... }
    @PostMapping("/update") suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> { ... }
    @PostMapping("/delete") suspend fun delete(userAuth, dto: DELETE_DTO): ApiResponse<*> { ... }

    private suspend fun assertAccess(scope, scopeId, operation, userAuth) {
        if (!checkPermission(...)) throw ForbiddenException()
        if (!checkOwnership(...)) throw UnauthorizedException()
    }
}
```

## 关键流程：update / delete 的 scope 解析

`update` 和 `delete` 的 scope 不从 DTO 读取，而是从数据库反查实体获取：

```kotlin
@PostMapping("/update")
suspend fun update(userAuth, dto: UPDATE_DTO): ApiResponse<*> {
    val entity = managerService.getByIdOrThrow(dto.id)
    val resolvedScope = resolveScope(entity.scope)      // ← 从实体
    assertAccess(resolvedScope, entity.scopeId, UPDATE, userAuth)
    managerService.update(dto)
    return ApiResponse.success(null)
}
```

理由：防止客户端在更新时伪造 scope。若 DTO 携带 scope，用户可能通过修改请求把租户资源"升级"为系统资源（绕过 tenant 隔离）。从实体反查等于以实际存储的 scope 作为权威来源。

`delete` 更进一步——批量删除时按 `(scope, scopeId)` 分组，每组只校验一次：

```kotlin
entities.groupBy { it.scope to it.scopeId }.keys.forEach { (scopeType, scopeId) ->
    val resolvedScope = resolveScope(scopeType)
    assertAccess(resolvedScope, scopeId, DELETE, userAuthentication)
}
```

避免对同一 scope 的重复校验，也保证跨 scope 批量删除时的权限严谨（每个 scope 独立校验一次）。

## 为什么不走 AOP

`StandardManagerController` 用 AOP 是因为权限清单在类注解上静态可读。Scoped 家族不能采用此方案：

- 权限决策依赖 DTO 中的 `scope` 和 `scopeId`
- AOP 拦截时 DTO 尚未反序列化为 typed 对象（`@ModelAttribute` 的后处理需要 controller method signature）
- `update` / `delete` 场景权限还依赖数据库查询结果（拿实体的 scope）

因此 Scoped 家族选择"方法内显式校验 + `checkPermission` / `checkOwnership` 钩子"，把权限决策放在业务方法链的开头，此时 DTO 已 typed、`ApplicationContext` 已可用。

## 现有真实使用位置

| 模块 | Controller | 备注 |
|---|---|---|
| `crystal-tenant` | `ManagerTenantDictTypeController` | 字典类型（可系统 / 租户挂载） |
| `crystal-approval` | `ManagerApprovalFlowDefinitionController` | 审批流程定义 |
| `crystal-approval` | `ManagerApprovalFlowInstanceController` | 审批实例（走 ReadonlyScoped，见对应页） |

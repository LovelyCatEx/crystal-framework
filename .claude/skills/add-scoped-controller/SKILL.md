---
name: add-scoped-controller
description: 添加 StandardScopedManagerController（支持 scope + scopeId 的双作用域 CRUD 控制器），适用于同时支持系统级和租户级的资源管理。
---

# 添加 StandardScopedManagerController

## 触发条件

当资源同时支持**系统级**（scope=SYSTEM, scopeId=0）和**租户级**（scope=TENANT, scopeId=tenantId）两种作用域，且需要对不同作用域进行独立权限控制时使用。

典型场景：审批流程定义、字典类型等"系统有一份、每个租户也可以有自己一份"的资源。

**与其他 Controller 类型的区别：**

| 场景 | 应使用 |
|------|--------|
| 只有系统管理员操作，无作用域概念 | `StandardManagerController`（见 `add-controller` skill） |
| 只有租户资源，系统管理员可跨租户管理 | `StandardTenantManagerController` |
| 同时存在系统级和租户级数据，权限互相隔离 | **`StandardScopedManagerController`**（本 skill） |

## 前提

### Entity 必须继承 BaseScopedEntity

`BaseScopedEntity`（`crystal-shared-types`）已内置 `scope: Int` 和 `scopeId: Long` 字段（含 `@Column` 和 `ToStringSerializer`），Entity 不需要重复声明这两个字段。

```kotlin
@Table("my_resource")
class MyResourceEntity(
    id: Long = 0,
    scope: Int = ResourceScope.TENANT.typeId,
    scopeId: Long = 0,
    // ... 业务字段 ...
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime)
```

### 三层权限模型（核心要点）

权限必须按作用域**严格隔离**，拥有系统级权限不能连带操作租户数据，反之亦然：

| 权限层级 | 位置 | 作用域 | 说明 |
|---------|------|--------|------|
| 系统级权限 | `SystemPermission` | SYSTEM scope only | 仅操作 scopeId=0 的系统资源 |
| 系统管理租户权限 | `SystemPermission` | TENANT scope | 系统管理员操作任意租户的资源 |
| 租户成员权限 | `TenantPermission` | TENANT scope (own) | 租户成员操作自己租户的资源 |

**禁止**将系统级权限用于 TENANT scope 的 `hasAnyAuthority` 检查中。

### DTO 继承关系

| DTO 用途 | 继承 | 关键字段 |
|---------|------|---------|
| Create | `BaseManagerCreateScopedDTO(scope, scopeId)` | `scope: Int`, `scopeId: Long` |
| Read/Query | `BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)` | `scope: Int`, `scopeId: Long`（非空） |
| Update | `BaseManagerUpdateDTO(id)` | `id: Long` |
| Delete | `BaseManagerDeleteDTO(ids)` | `ids: List<Long>` |

### Service 继承关系

ManagerService 接口必须继承 `BaseScopedManagerService`，它同时实现了 `CachedBaseManagerService` 和 `TenantRelationshipCheckService`。

必须实现的方法：
- `create(dto)` — 创建实体（使用 `snowIdGenerator.nextId()` + `newEntity true`）
- `applyDTOToEntity(dto, original)` — 更新时应用 DTO 到已有实体
- `findAllByScopeId(scopeId)` — 按 scopeId 获取全部记录

`buildQueryCriteria` 已由 `BaseScopedManagerService` 默认实现（自动注入 `WHERE scope = ? AND scope_id = ?`），无需覆写。

### Controller 核心：checkPermission

`StandardScopedManagerController` 只有一个抽象方法需要实现：

```kotlin
override suspend fun checkPermission(
    scope: ResourceScope,
    scopeId: Long?,
    operation: ScopedOperation,
    userAuthentication: UserAuthentication
): Boolean
```

实现模板（三层隔离）：

```kotlin
override suspend fun checkPermission(
    scope: ResourceScope,
    scopeId: Long?,
    operation: ScopedOperation,
    userAuthentication: UserAuthentication
): Boolean {
    return when (scope) {
        ResourceScope.SYSTEM -> when (operation) {
            ScopedOperation.CREATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_XXX_CREATE)
            ScopedOperation.READ -> RbacUtils.hasAuthority(SystemPermission.ACTION_XXX_READ)
            ScopedOperation.UPDATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_XXX_UPDATE)
            ScopedOperation.DELETE -> RbacUtils.hasAuthority(SystemPermission.ACTION_XXX_DELETE)
        }
        ResourceScope.TENANT -> when (operation) {
            ScopedOperation.CREATE -> RbacUtils.hasAnyAuthority(
                SystemPermission.ACTION_TENANT_XXX_CREATE,        // 系统管理租户权限
                TenantPermission.ACTION_TENANT_XXX_CREATE_PEM     // 租户成员权限
            )
            ScopedOperation.READ -> RbacUtils.hasAnyAuthority(
                SystemPermission.ACTION_TENANT_XXX_READ,
                TenantPermission.ACTION_TENANT_XXX_READ_PEM
            )
            ScopedOperation.UPDATE -> RbacUtils.hasAnyAuthority(
                SystemPermission.ACTION_TENANT_XXX_UPDATE,
                TenantPermission.ACTION_TENANT_XXX_UPDATE_PEM
            )
            ScopedOperation.DELETE -> RbacUtils.hasAnyAuthority(
                SystemPermission.ACTION_TENANT_XXX_DELETE,
                TenantPermission.ACTION_TENANT_XXX_DELETE_PEM
            )
        }
    }
}
```

### checkOwnership（默认行为）

`StandardScopedManagerController` 内置的 `checkOwnership` 默认逻辑：
- SYSTEM scope → 始终通过（权限检查已足够）
- TENANT scope → `scopeId == userAuthentication.tenantId`（租户成员只能操作自己租户的数据）

如需跨租户管理等特殊逻辑，覆写此方法。

### 自动提供的端点

| 方法 | 路径 | 参数来源 | 说明 |
|------|------|---------|------|
| GET | `/list?scope=&scopeId=` | `@RequestParam` | 按 scopeId 获取全部 |
| POST | `/create` | `@ModelAttribute` | 新增（scope/scopeId 从 DTO 读取） |
| POST | `/query` | `@RequestBody` (JSON) | 分页查询（scope/scopeId 从 DTO 读取） |
| POST | `/update` | `@ModelAttribute` | 更新（scope 从已有实体读取） |
| POST | `/delete` | `@ModelAttribute` | 删除（scope 从已有实体读取） |

### Repository

需要添加 `findAllByScopeId` 方法（R2DBC 按命名约定自动生成查询）：

```kotlin
interface MyResourceRepository : BaseRepository<MyResourceEntity> {
    fun findAllByScopeId(scopeId: Long): Flux<MyResourceEntity>
}
```

## 执行步骤

**1. Entity**：继承 `BaseScopedEntity`，移除手动声明的 scope/scopeId 字段

**2. 权限常量**：
- `SystemPermission.kt` 添加系统级权限（`ACTION_XXX_*`）
- `SystemPermission.kt` 添加系统管理租户权限（`ACTION_TENANT_XXX_*`）
- `TenantPermission.kt` 添加租户成员权限（`ACTION_TENANT_XXX_*_PEM` + `TenantPermissionDeclaration`）

**3. DTO**：创建四个 DTO 文件放入 `controller/manager/dto/`
- `ManagerCreateXxxDTO` extends `BaseManagerCreateScopedDTO(scope, scopeId)`
- `ManagerReadXxxDTO` extends `BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)`
- `ManagerUpdateXxxDTO` extends `BaseManagerUpdateDTO(id)`
- `ManagerDeleteXxxDTO` extends `BaseManagerDeleteDTO(ids)`

**4. Repository**：添加 `findAllByScopeId(scopeId: Long): Flux<Entity>`

**5. ManagerService 接口**：继承 `BaseScopedManagerService<REPO, ENTITY, C, R, U, D>`，放入 `service/manager/`

**6. ManagerService 实现**：放入 `service/manager/impl/`
- 注入 `repository`、`SnowIdGenerator`、`ReactiveRedisService`、`ApplicationEventPublisher`、`R2dbcEntityTemplate`
- 实现 `create()`、`applyDTOToEntity()`、`findAllByScopeId()`

**7. Controller**：放入 `controller/manager/`
- `@Validated` + `@RestController` + `@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")`
- 继承 `StandardScopedManagerController<SERVICE, REPO, ENTITY, C, R, U, D>(managerService)`
- 实现 `checkPermission()` — 严格按三层隔离

**8. pom.xml**：如需引用 `TenantPermission`，添加 `crystal-rbac` 依赖

**9. 编译验证**：`./mvnw clean compile -pl <module> -am -DskipTests`

## 前端对应

前端通常为该资源创建两个页面（参照字典模式）：
- **系统管理页面**（admin 路由）：固定 `scope=ResourceScope.SYSTEM, scopeId='0'`
- **租户管理页面**（tenant 路由）：固定 `scope=ResourceScope.TENANT, scopeId=currentTenantId`

前端 API 使用 `BaseManagerController` 继承，ReadDTO 继承 `BaseManagerReadScopedDTO`（定义在 `types/api.types.ts`）。

## 输出格式

完成后说明：
1. Controller 类型、路径、权限模型
2. 自动提供的端点列表
3. 新增/修改的文件路径
4. 依赖的权限常量（系统级 + 系统管理租户 + 租户成员，三组）

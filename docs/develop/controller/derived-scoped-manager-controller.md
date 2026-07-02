# 派生范围控制器（StandardDerivedScopedManagerController）

与 [`StandardScopedManagerController`](./scoped-manager-controller) 类似，但资源实体自身没有 scope / scopeId 列——scope 需要从父实体（沿外键 chain 查找）推导。典型场景：字典项自身不知道 scope，但它归属的字典类型知道。

## 适用场景

- 字典项 → 字典类型（`typeId`）→ 字典类型的 scope
- 部门成员配置 → 部门 → 部门的 scope（若部门本身是 Scoped 而非 Tenant）
- 其他"实体是子级、scope 挂在父级"的资源

其他场景：

- 资源自带 scope / scopeId 列 → [StandardScopedManagerController](./scoped-manager-controller)
- 资源明确只属于租户 → [StandardTenantManagerController](./tenant-manager-controller)

## 端点

无 `/list` 端点——没有父上下文时无法枚举所有派生实体（可能跨多个父实体，混在一起无实际意义）：

| HTTP | 路径 | 说明 |
|---|---|---|
| POST | `/create` | 从 CREATE_DTO 中的外键推导 scope |
| POST | `/query` | 从 READ_DTO 中的外键推导 scope（通常按父 id 查子） |
| POST | `/update` | 从数据库中的实体推导 scope（反查父实体） |
| POST | `/delete` | 同上 |

需要 list 的场景需自行添加自定义端点（如 `/tree?parentId=xxx`）。

## 三个抽象方法

继承时必须实现三个 scope 解析钩子：

```kotlin
protected abstract suspend fun resolveScopeFromCreateDTO(dto: CREATE_DTO): Pair<ResourceScope, Long>
protected abstract suspend fun resolveScopeFromReadDTO(dto: READ_DTO): Pair<ResourceScope, Long>
protected abstract suspend fun resolveScopeFromEntity(entity: ENTITY): Pair<ResourceScope, Long>
```

每个返回 `(scope, scopeId)` 对。这些方法通常查一次父实体——通过 Service 直接查，或通过 Repository。

## 使用步骤

以字典项为例。字典项自身无 scope，通过 `typeId` → 字典类型 → 字典类型的 scope。

### 1. Entity

字典项实现 `ScopedEntity<Long>` 而非继承 `BaseScopedEntity`——自身没有 scope 列，只有 `typeId` 指向父：

```kotlin
@Table("tenant_dict_item")
class TenantDictItemEntity(
    var typeId: Long = 0,
    var label: String = "",
    var value: String = "",
) : BaseEntity(), ScopedEntity<Long> {

    // ScopedEntity 接口要求：返回直接父的 id
    override fun getDirectParentId(): Long = typeId
}
```

### 2. Service

继承 `CachedBaseManagerService`（entity 是 `BaseEntity` 而非 `BaseScopedEntity`）：

```kotlin
interface TenantDictItemManagerService : CachedBaseManagerService<
    TenantDictItemRepository,
    TenantDictItemEntity,
    ManagerCreateTenantDictItemDTO,
    ManagerReadTenantDictItemDTO,
    ManagerUpdateTenantDictItemDTO,
    ManagerDeleteTenantDictItemDTO
> {
    suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO>
}
```

### 3. DTO

Read DTO 使用 `BaseManagerReadDTO`（不是 Scoped 版）——客户端只需传父的 `typeId`，scope 由 Controller 反查：

```kotlin
class ManagerCreateTenantDictItemDTO(
    @field:NotNull val typeId: Long,
    val label: String,
    val value: String,
)

class ManagerReadTenantDictItemDTO(
    page: Int = 1, pageSize: Int = 20,
    val typeId: Long = 0,
) : BaseManagerReadDTO(page, pageSize)

class ManagerUpdateTenantDictItemDTO(
    override val id: Long,
    val label: String? = null,
    val value: String? = null,
) : BaseManagerUpdateDTO(id)

class ManagerDeleteTenantDictItemDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 4. Controller

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-item")
class ManagerTenantDictItemController(
    managerService: TenantDictItemManagerService,
    private val tenantDictTypeManagerService: TenantDictTypeManagerService,  // 用于查父类型的 scope
) : StandardDerivedScopedManagerController<
    TenantDictItemManagerService,
    TenantDictItemRepository,
    TenantDictItemEntity,
    ManagerCreateTenantDictItemDTO,
    ManagerReadTenantDictItemDTO,
    ManagerUpdateTenantDictItemDTO,
    ManagerDeleteTenantDictItemDTO
>(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate     = SystemPermission.ACTION_DICT_ITEM_CREATE,
        superRead       = SystemPermission.ACTION_DICT_ITEM_READ,
        superUpdate     = SystemPermission.ACTION_DICT_ITEM_UPDATE,
        superDelete     = SystemPermission.ACTION_DICT_ITEM_DELETE,
        systemCreate    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE,
        systemRead      = SystemPermission.ACTION_SYSTEM_DICT_ITEM_READ,
        systemUpdate    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE,
        systemDelete    = SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_ITEM_CREATE_PEM,
        tenantPemRead   = TenantPermission.ACTION_TENANT_DICT_ITEM_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_ITEM_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_ITEM_DELETE_PEM,
    ),
) {

    override suspend fun resolveScopeFromCreateDTO(dto: ManagerCreateTenantDictItemDTO): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(dto.typeId)
    }

    override suspend fun resolveScopeFromReadDTO(dto: ManagerReadTenantDictItemDTO): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(dto.typeId)
    }

    override suspend fun resolveScopeFromEntity(entity: TenantDictItemEntity): Pair<ResourceScope, Long> {
        return resolveScopeByTypeId(entity.typeId)
    }

    private suspend fun resolveScopeByTypeId(typeId: Long): Pair<ResourceScope, Long> {
        val type = tenantDictTypeManagerService.getByIdOrNull(typeId)
            ?: throw BusinessException("Dict type $typeId not found")
        val scope = ResourceScope.getById(type.scope)
            ?: throw BusinessException("Unknown scope ${type.scope} on dict type $typeId")
        return scope to type.scopeId
    }

    // 自定义端点：按 typeId 树形展开
    @GetMapping("/tree")
    suspend fun tree(
        userAuthentication: UserAuthentication,
        @RequestParam typeId: Long
    ): ApiResponse<List<TenantDictItemTreeVO>> {
        val (scope, scopeId) = resolveScopeByTypeId(typeId)
        if (!RbacUtils.hasAnyAuthority(*permissions.forScope(scope, ScopedOperation.READ))) {
            throw ForbiddenException()
        }
        if (!checkOwnership(scope, scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException()
        }
        return ApiResponse.success(managerService.getTreeByTypeId(typeId))
    }
}
```

## 类型参数

| # | 参数 | 约束 |
|---|---|---|
| 1 | `SERVICE` | `CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` 且 `ScopedEntity<*>`（联合约束） |
| 4 | `CREATE_DTO` | `Any` |
| 5 | `READ_DTO` | `BaseManagerReadDTO`（不是 Scoped 版） |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

ENTITY 的联合约束：`where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>`——entity 必须同时继承 BaseEntity 和实现 ScopedEntity，由 Kotlin 泛型的 `where` 子句在编译期强制。

## 权限模型

与 [StandardScopedManagerController](./scoped-manager-controller) 一致——12 个权限的 `ScopedPermissionTriad`。区别在 scope 来源：Scoped 从实体的 scope 列读，DerivedScoped 从抽象方法返回。

## 可 override 的钩子

- `resolveScopeFromCreateDTO(dto)` — 必须实现
- `resolveScopeFromReadDTO(dto)` — 必须实现
- `resolveScopeFromEntity(entity)` — 必须实现（用于 update / delete）
- `checkOwnership(scope, scopeId, operation, userAuth)` — 可选覆盖，默认逻辑同 Scoped
- `buildQueryResponse(dto, userAuth)` — 可选覆盖，默认返回 `managerService.query(dto)`

## 注意事项

- Entity 必须同时是 `BaseEntity` 和 `ScopedEntity<*>`，只继承 BaseEntity 会在编译期被挡下
- `ScopedEntity.getDirectParentId()` 用于 `TenantRelationshipCheckService.checkIsRelatedToRootParent`（顺链查找），Derived Controller 本身不用它——三个 `resolveScopeFromXXX` 才是核心
- 每次 CRUD 都会查一次父实体，注意性能。若父实体访问频繁，可利用 `CachedBaseService` 的缓存
- Read DTO 使用 `BaseManagerReadDTO` 而非 Scoped 版——客户端只传父 id，scope 由后端反查
- 无 `/list` 端点——需要枚举时自行添加自定义端点，通常带一个父 id 参数

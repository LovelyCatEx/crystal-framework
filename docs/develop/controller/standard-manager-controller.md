# 标准化控制器（StandardManagerController）

管理后台全局 CRUD 的基类。资源无 SYSTEM / TENANT 之分——继承此类，配置 7 个类型参数和 1 个 `@ManagerPermissions` 注解，即自动生成 5 个 CRUD 端点。

## 适用场景

- 存储提供者、系统级角色、系统级权限项等全局资源
- 后台需要标准增删改查页面
- 无 SYSTEM / TENANT 双 scope 需求

其他场景请参考：

- 双 scope 资源 → [StandardScopedManagerController](./scoped-manager-controller)
- 仅属于租户的资源 → [StandardTenantManagerController](./tenant-manager-controller)
- 只读资源 → [ReadonlyManagerController](./readonly-manager-controller)

## 自动生成的端点

| HTTP | 路径 | 方法名 | 参数绑定 | 前端 Content-Type |
|---|---|---|---|---|
| GET | `/list` | `readAll` | 无 | — |
| POST | `/create` | `create` | `@ModelAttribute` | form-urlencoded |
| POST | `/query` | `read` | `@RequestBody` | application/json |
| POST | `/update` | `update` | `@ModelAttribute` | form-urlencoded |
| POST | `/delete` | `delete` | `@ModelAttribute` | form-urlencoded |

分页查询的方法名是 `read`（不是 `query`），但 URL 是 `/query`；`@ManagerPermissions` 中对应字段名为 `read`。此处的不对称是历史遗留。

## 使用步骤

以 `storage-provider`（存储提供者）为例。

### 1. Entity

```kotlin
@Table("storage_provider")
class StorageProviderEntity(
    var name: String = "",
    var type: Int = 0,
    var config: String = "",
) : BaseEntity()
```

详见 [添加实体类](../add-entity)。

### 2. Repository

```kotlin
@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity>
```

### 3. Manager Service

放入 `service/manager/` + `service/manager/impl/`：

```kotlin
// service/manager/StorageProviderManagerService.kt
interface StorageProviderManagerService : CachedBaseManagerService<
    StorageProviderRepository,
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO,
    ManagerDeleteStorageProviderDTO
>
```

```kotlin
// service/manager/impl/StorageProviderManagerServiceImpl.kt
@Service
class StorageProviderManagerServiceImpl(
    repository: StorageProviderRepository
) : StorageProviderManagerService, CachedBaseManagerServiceImpl<...>(repository) {
    override fun getEntityClass(): KClass<*> = StorageProviderEntity::class
}
```

### 4. 四个 DTO

放入 `controller/manager/dto/`：

```kotlin
// ManagerCreateStorageProviderDTO.kt
class ManagerCreateStorageProviderDTO(
    @field:NotBlank val name: String = "",
    @field:NotNull val type: Int = 0,
    val config: String = "",
)

// ManagerReadStorageProviderDTO.kt
class ManagerReadStorageProviderDTO(
    override val page: Int = 1,
    override val pageSize: Int = 20,
) : BaseManagerReadDTO(page, pageSize)

// ManagerUpdateStorageProviderDTO.kt
class ManagerUpdateStorageProviderDTO(
    override val id: Long,
    val name: String? = null,
    val type: Int? = null,
    val config: String? = null,
) : BaseManagerUpdateDTO(id)

// ManagerDeleteStorageProviderDTO.kt
class ManagerDeleteStorageProviderDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
```

### 5. Controller

放入 `controller/manager/`：

```kotlin
@ManagerPermissions(
    read = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    readAll = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    create = [SystemPermission.ACTION_STORAGE_PROVIDER_CREATE],
    update = [SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE],
    delete = [SystemPermission.ACTION_STORAGE_PROVIDER_DELETE],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<
    StorageProviderManagerService,
    StorageProviderRepository,
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO,
    ManagerReadStorageProviderDTO,
    ManagerUpdateStorageProviderDTO,
    ManagerDeleteStorageProviderDTO
>(managerService)
```

Controller 无需方法体，5 个端点全部从父类继承。

## 类型参数

| # | 参数 | 约束 |
|---|---|---|
| 1 | `SERVICE` | `CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>` |
| 2 | `REPOSITORY` | `BaseRepository<ENTITY>` |
| 3 | `ENTITY` | `BaseEntity` |
| 4 | `CREATE_DTO` | `Any`（无强制基类） |
| 5 | `READ_DTO` | `BaseManagerReadDTO` |
| 6 | `UPDATE_DTO` | `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | `BaseManagerDeleteDTO` |

## @ManagerPermissions

```kotlin
@ManagerPermissions(
    read = [权限1, 权限2],       // 也是 /query 端点使用的权限
    readAll = [权限1],            // 空数组时自动降级到 read
    create = [权限1],
    update = [权限1],
    delete = [权限1],
)
```

规则：

- 类级别注解，覆盖 5 个端点
- 每个字段是权限数组，用户持有其中任一即通过（OR 语义）
- `readAll` 空数组时自动降级到 `read`
- 空数组表示不设权限校验（AOP 打 warn 日志），不推荐
- 权限字符串必须引用 `SystemPermission.XXX` 等常量，禁止字面量

## 添加自定义端点

标准化 Controller 允许添加额外方法：

```kotlin
class ManagerStorageProviderController(...) : StandardManagerController<...>(managerService) {

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE}')")
    @PostMapping("/test-connection")
    suspend fun testConnection(
        @RequestParam id: Long
    ): ApiResponse<*> {
        val ok = managerService.testConnection(id)
        return ApiResponse.success(ok)
    }
}
```

自定义方法不受 `@ManagerPermissions` 约束（AOP 仅匹配固定的 5 个方法名），需要自行添加 `@PreAuthorize`。

## 前端对接

前端继承 `BaseManagerController`（`api/BaseManagerController.ts`）：

```typescript
class ManagerStorageProviderController extends BaseManagerController<
    StorageProviderEntity,
    ManagerCreateStorageProviderDTO
    // R/U/D 使用默认
> {}

export const managerStorageProviderController = new ManagerStorageProviderController(
    '/manager/storage-provider'
)
```

`create` / `update` / `delete` 走 `application/x-www-form-urlencoded`，`query` 走 `application/json`。`BaseManagerController` 已封装。

## 注意事项

- `@ManagerPermissions` 仅对 Standard 和 Readonly 生效。给 Scoped / DerivedScoped / Tenant 添加此注解无效——AOP 的 pointcut 写死在 `StandardManagerController.*(..)`
- 所有 `Long` 字段必须加 `@get:JsonSerialize(using = ToStringSerializer::class)`，前端接为 `string`
- Controller 内禁止注入 Repository，数据库操作走 Service 层
- Manager Controller 只能注入 Manager Service，禁止注入普通 Service
- 4 个 DTO 必须全部提供，即使某个操作用不到，泛型基类要求参数就位

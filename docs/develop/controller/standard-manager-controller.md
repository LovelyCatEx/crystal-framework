# 标准化控制器

## 概述

`StandardManagerController` 是框架提供的抽象 CRUD 基类。继承它即可获得五个标准管理端接口，无需编写任何 Controller 方法。

## 适用场景

需要标准 CRUD 的管理后台页面：用户管理、角色管理、邮件模板管理等。

## 类型参数

基类有 7 个类型参数，按顺序依次是：

| # | 参数 | 说明 |
|---|------|------|
| 1 | `SERVICE` | 继承 `CachedBaseManagerService` 的 service 类 |
| 2 | `REPOSITORY` | 继承 `BaseRepository` 的 repository 类 |
| 3 | `ENTITY` | 继承 `BaseEntity` 的实体类 |
| 4 | `CREATE_DTO` | 创建接口的请求参数 |
| 5 | `READ_DTO` | 分页查询接口的请求参数，继承 `BaseManagerReadDTO` |
| 6 | `UPDATE_DTO` | 更新接口的请求参数，继承 `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | 删除接口的请求参数，继承 `BaseManagerDeleteDTO` |

## 使用步骤

### 1. 创建 Service

```kotlin
@Service
class ExtMyPluginItemManagerService(
    repository: ExtMyPluginItemRepository
) : CachedBaseManagerService<
    ExtMyPluginItemRepository,
    ExtMyPluginItemEntity,
    ManagerCreateExtMyPluginItemDTO,
    ManagerReadExtMyPluginItemDTO,
    ManagerUpdateExtMyPluginItemDTO,
    ManagerDeleteExtMyPluginItemDTO
>(repository) {
    override fun getEntityClass(): KClass<*> = ExtMyPluginItemEntity::class
}
```

### 2. 创建 DTO

```kotlin
class ManagerCreateExtMyPluginItemDTO(
    var name: String = "",
    var description: String = "",
)

class ManagerReadExtMyPluginItemDTO : BaseManagerReadDTO()

class ManagerUpdateExtMyPluginItemDTO : BaseManagerUpdateDTO()

class ManagerDeleteExtMyPluginItemDTO : BaseManagerDeleteDTO()
```

### 3. 创建 Controller

```kotlin
@ManagerPermissions(
    read = ["ext_my_plugin.item_read"],
    readAll = ["ext_my_plugin.item_read"],
    create = ["ext_my_plugin.item_create"],
    update = ["ext_my_plugin.item_update"],
    delete = ["ext_my_plugin.item_delete"],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ext-my-plugin-items")
class ExtMyPluginItemController(
    managerService: ExtMyPluginItemManagerService
) : StandardManagerController<
    ExtMyPluginItemManagerService,
    ExtMyPluginItemRepository,
    ExtMyPluginItemEntity,
    ManagerCreateExtMyPluginItemDTO,
    ManagerReadExtMyPluginItemDTO,
    ManagerUpdateExtMyPluginItemDTO,
    ManagerDeleteExtMyPluginItemDTO
>(managerService)
```

## 自动提供的端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/list` | 查询全部数据 |
| `POST` | `/create` | 创建 |
| `GET` | `/query` | 分页查询（支持关键字搜索、时间范围） |
| `POST` | `/update` | 更新 |
| `POST` | `/delete` | 删除（批量） |

## @ManagerPermissions

注解在类级别，按操作分别指定权限：

- `readAll` 为空时回退到 `read`
- 每个操作支持多个权限值，**任一匹配**即放行（OR 语义）

## 关键点

- Controller 类不需要写任何方法体
- Service 必须继承 `CachedBaseManagerService`（带缓存失效），放在 `service/manager/` + `service/manager/impl/` 包下
- **禁止直接注入 Repository，所有数据库操作必须通过 Service 层进行**
- **Manager Controller 只能注入 Manager Service，禁止注入普通 Service**
- BaseManagerReadDTO 继承 `PageQuery`，自带 `page`、`pageSize`、`searchKeyword`、`startTime`、`endTime`
- BaseManagerUpdateDTO 携带 `id: Long`
- BaseManagerDeleteDTO 携带 `ids: List<Long>`

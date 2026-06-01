# StandardManagerController

## Overview

`StandardManagerController` is the framework's abstract CRUD base class. Extend it to get five standard admin endpoints without writing any controller methods.

## When to Use

Admin pages requiring standard CRUD: user management, role management, mail template management, etc.

## Type Parameters

The base class has 7 type parameters:

| # | Parameter | Description |
|---|-----------|-------------|
| 1 | `SERVICE` | Service extending `CachedBaseManagerService` |
| 2 | `REPOSITORY` | Repository extending `BaseRepository` |
| 3 | `ENTITY` | Entity extending `BaseEntity` |
| 4 | `CREATE_DTO` | Create request DTO |
| 5 | `READ_DTO` | Query request DTO, extends `BaseManagerReadDTO` |
| 6 | `UPDATE_DTO` | Update request DTO, extends `BaseManagerUpdateDTO` |
| 7 | `DELETE_DTO` | Delete request DTO, extends `BaseManagerDeleteDTO` |

## Usage

### 1. Create the Service

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

### 2. Create DTOs

```kotlin
class ManagerCreateExtMyPluginItemDTO(
    var name: String = "",
    var description: String = "",
)

class ManagerReadExtMyPluginItemDTO : BaseManagerReadDTO()

class ManagerUpdateExtMyPluginItemDTO : BaseManagerUpdateDTO()

class ManagerDeleteExtMyPluginItemDTO : BaseManagerDeleteDTO()
```

### 3. Create the Controller

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

## Auto-Provided Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/list` | List all records |
| `POST` | `/create` | Create a record |
| `GET` | `/query` | Paginated query (keyword search, time range) |
| `POST` | `/update` | Update a record |
| `POST` | `/delete` | Delete records (batch) |

## @ManagerPermissions

Class-level annotation specifying permissions per action:

- `readAll` falls back to `read` when empty
- Multiple permissions per action are allowed (OR semantics — any match grants access)

## Key Points

- No method body needed in the controller class
- Service must extend `CachedBaseManagerService` (includes cache eviction)
- **Never inject Repository directly — all DB operations must go through Service layer**
- **Manager Controller must only inject Manager Service, never a regular Service**
- `BaseManagerReadDTO` extends `PageQuery` with `page`, `pageSize`, `searchKeyword`, `startTime`, `endTime`
- `BaseManagerUpdateDTO` carries `id: Long`
- `BaseManagerDeleteDTO` carries `ids: List<Long>`

::: warning Return Type
When extending `StandardManagerController`, no method body is needed — the base class already returns `ApiResponse<*>`. However, if you add custom endpoints, **all methods must explicitly return `ApiResponse<*>`**. Never return raw types.
:::

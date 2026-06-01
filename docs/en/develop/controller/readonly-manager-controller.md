# ReadOnly ManagerController

## Overview

`ReadonlyManagerController` is a read-only variant of `StandardManagerController`. It inherits the `list` and `query` endpoints but rejects `create`, `update`, and `delete` operations.

## When to Use

System-generated data that should not be mutated through the admin interface:

- Login logs
- Audit logs
- Mail send records

## Usage

### 1. Create the Service

After creating the Entity and Repository (see [Add Entity](../add-entity)), create a ManagerService:

```kotlin
@Service
class ExtMyPluginLogManagerService(
    repository: ExtMyPluginLogRepository
) : CachedBaseManagerService<
    ExtMyPluginLogRepository,
    ExtMyPluginLogEntity,
    ManagerCreateExtMyPluginLogDTO,
    ManagerReadExtMyPluginLogDTO,
    ManagerUpdateExtMyPluginLogDTO,
    ManagerDeleteExtMyPluginLogDTO
>(repository) {
    override fun getEntityClass(): KClass<*> = ExtMyPluginLogEntity::class
}
```

### 2. Create DTOs

All four DTOs are required (even for read-only controllers):

```kotlin
class ManagerReadExtMyPluginLogDTO : BaseManagerReadDTO()
class ManagerUpdateExtMyPluginLogDTO : BaseManagerUpdateDTO()
class ManagerDeleteExtMyPluginLogDTO : BaseManagerDeleteDTO()
class ManagerCreateExtMyPluginLogDTO(
    // your business fields...
)
```

### 3. Create the Controller

```kotlin
@ManagerPermissions(
    read = ["ext_my_plugin.log_read"],
    readAll = ["ext_my_plugin.log_read"],
    create = ["ext_my_plugin.log_read"],
    update = ["ext_my_plugin.log_read"],
    delete = ["ext_my_plugin.log_read"],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ext-my-plugin-logs")
class ExtMyPluginLogController(
    managerService: ExtMyPluginLogManagerService
) : ReadonlyManagerController<
    ExtMyPluginLogManagerService,
    ExtMyPluginLogRepository,
    ExtMyPluginLogEntity,
    ManagerCreateExtMyPluginLogDTO,
    ManagerReadExtMyPluginLogDTO,
    ManagerUpdateExtMyPluginLogDTO,
    ManagerDeleteExtMyPluginLogDTO
>(managerService)
```

Key points:

- Set the same permission for all five actions since this is read-only
- All type parameters (service, repository, entity, 4 DTOs) are required by the base class constraint
- No methods needed in the controller body — everything is inherited

::: warning Return Type
When extending `ReadonlyManagerController`, no method body is needed — the base class already returns `ApiResponse<*>`. If you add custom endpoints, all methods must explicitly return `ApiResponse<*>`.
:::

## Auto-Provided Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/list` | List all records |
| `GET` | `/query` | Paginated query (supports keyword search & time range) |

`/create`, `/update`, `/delete` are inherited but overridden to always return 403 Forbidden.

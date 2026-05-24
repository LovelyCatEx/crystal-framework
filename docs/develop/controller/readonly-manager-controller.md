# 只读标准化控制器

## 概述

`ReadonlyManagerController` 是 `StandardManagerController` 的只读变体，继承 `list` 和 `query` 两个查询端点，但拒绝 `create`、`update`、`delete` 操作。

## 适用场景

系统自动生成、不应由用户通过管理端修改的数据：

- 登录日志
- 审计日志
- 邮件发送记录

## 使用步骤

### 1. 创建 Service

参考[添加实体类](../add-entity)完成 Entity、Repository 后，创建 ManagerService：

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

### 2. 创建 DTO

每个 ManagerController 需要四种 DTO（即使只读也需要）：

```kotlin
// 继承框架提供的基类
class ManagerReadExtMyPluginLogDTO : BaseManagerReadDTO()
class ManagerUpdateExtMyPluginLogDTO : BaseManagerUpdateDTO()
class ManagerDeleteExtMyPluginLogDTO : BaseManagerDeleteDTO()
class ManagerCreateExtMyPluginLogDTO(
    // 你的业务字段...
)
```

### 3. 创建 Controller

```kotlin
@ManagerPermissions(
    read = ["ext_my_plugin.log.read"],
    readAll = ["ext_my_plugin.log.read"],
    create = ["ext_my_plugin.log.read"],
    update = ["ext_my_plugin.log.read"],
    delete = ["ext_my_plugin.log.read"],
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

关键点：

- `read` / `readAll` / `create` / `update` / `delete` 全部设置相同权限，因为只读控制器本质上只有查询能力
- service / repository / entity / 四种 DTO 的类型参数一个都不能少（基类约束）
- Controller 本身不需要写任何方法——所有行为从父类继承

## 自动提供的端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/list` | 返回全部记录 |
| `GET` | `/query` | 分页查询（支持关键字搜索、时间范围） |

`/create`、`/update`、`/delete` 继承自 `StandardManagerController`，但被重写为始终返回 403 Forbidden。

# StandardManagerController

## 设计意图

`StandardManagerController` 是框架管理端 CRUD 的核心抽象。它约束了 Controller → Service → Repository 的分层协作模式，权限校验由 `authorize` 方法内联完成（新走 `PermissionMatrix`），审计日志仍由 AOP 自动处理。7 个类型参数把整条链路（Service、Repository、Entity、四个 DTO）串联起来，编译期保证类型一致。

## 源码

`crystal-shared/controller/StandardManagerController.kt`：

```kotlin
@Validated
abstract class StandardManagerController<
        SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
>(
    protected val managerService: SERVICE
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(userAuthentication: UserAuthentication): ApiResponse<*> {
        return ApiResponse.success(managerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PostMapping("/create", version = "1")
    suspend fun create(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: CREATE_DTO): ApiResponse<*> {
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/query", version = "1")
    suspend fun read(userAuthentication: UserAuthentication, @RequestBody @Valid dto: READ_DTO): ApiResponse<*> {
        return ApiResponse.success(managerService.query(dto))
    }

    @PostMapping("/update", version = "1")
    suspend fun update(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: UPDATE_DTO): ApiResponse<*> {
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: DELETE_DTO): ApiResponse<*> {
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
```

## 关键结构决策

- **方法名 `read` vs URL `/query`** 的历史遗留。早期 API 用 `read` 作为方法名（对应 `/read` URL），后来路径调整为 `/query`（更符合"分页查询"语义），但方法名保留至今。旧的 `ManagerControllerPermissionAspect` 仍按方法名反射匹配 `@ManagerPermissions.read`，改动此方法名会破坏兼容路径下的 AOP 权限映射；`PermissionMatrix` 走的是 `authorize(ManagerAction, ...)` + `ScopedOperation` 分派，不依赖方法名。

- **`version = "1"`** 是 `@GetMapping` / `@PostMapping` 的自定义属性，配合框架自定义的 `RequestMappingHandlerMapping` 生成 `/api/v1/xxx` 路径。

- **`awaitListWithTimeout()`** 是项目扩展函数，将 `Flux<T>` 转换为 `List<T>` 并附加超时保护，防止 R2DBC 死锁拖挂协程链。

- **`readAll` 直接调用 `getRepository().findAll()`** 而非 Service 层聚合方法——全量查询无业务处理需求，直接走底层减少一层间接。若子类需要过滤，必须 override `readAll`，而不是靠 Service 提供一个方法。

## 方法命名约定

方法名承担两种匹配用途：新的 `PermissionMatrix` 走 `ManagerAction` → `ScopedOperation` 映射（`readAll` 和 `read` 都归为 `READ`）；老的 `ManagerControllerPermissionAspect` 兼容路径按方法名反射匹配 `@ManagerPermissions` 字段：

| 方法名 | ManagerAction | ScopedOperation（Matrix 路径） | `@ManagerPermissions` 字段（兼容路径） | HTTP |
|---|---|---|---|---|
| `readAll` | READ_ALL | READ | `readAll`（空数组时回退到 `read`） | GET `/list` |
| `read` | READ | READ | `read` | POST `/query` |
| `create` | CREATE | CREATE | `create` | POST `/create` |
| `update` | UPDATE | UPDATE | `update` | POST `/update` |
| `delete` | DELETE | DELETE | `delete` | POST `/delete` |

Subclass override 时保留方法名可同时兼容 Matrix 和 AOP 两条路径。

## 权限声明：PermissionMatrix

`PermissionMatrix` 通过构造参数 `permissions = ...` 传入。`StandardManagerController.authorize` 按 `ManagerAction` 转成 `ScopedOperation`，再 OR-check `matrix.layersFor(SYSTEM, op)`：

```kotlin
override suspend fun authorize(action: ManagerAction, userAuthentication, ...) {
    val matrix = permissions ?: return  // permissions == null → fall back to legacy AOP
    val op = when (action) {
        ManagerAction.CREATE -> ScopedOperation.CREATE
        ManagerAction.READ, ManagerAction.READ_ALL -> ScopedOperation.READ
        ManagerAction.UPDATE -> ScopedOperation.UPDATE
        ManagerAction.DELETE -> ScopedOperation.DELETE
    }
    val required = matrix.layersFor(ResourceScope.SYSTEM, op)
    if (!RbacUtils.hasAnyAuthority(*required)) {
        throw AuthorizationDeniedException("Access denied: required any of ${required.toList()}")
    }
}
```

Standard 资源用 `PermissionMatrix.systemOnly(...)` 构造 —— tenant 层默认 `NOT_APPLICABLE`；super 层可选。`layersFor(SYSTEM, op)` 过滤 `NOT_APPLICABLE` 后通常返回 `[system<op>]` 单元素数组（或 `[super<op>, system<op>]`）。

## 兼容 AOP 路径

老的 `@ManagerPermissions` 类注解由 `ManagerControllerPermissionAspect` 处理，仅在 `permissions == null` 时生效：

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        // 若 Controller 已设 permissions，直接放行（authorize 已处理）
        val targetClass = AopUtils.getTargetClass(joinPoint.target)
        val permissions = AnnotationUtils.findAnnotation(targetClass, ManagerPermissions::class.java)
            ?: return joinPoint.proceed()
        // ... 按方法名反射匹配 @ManagerPermissions 字段
    }
}
```

`AopUtils.getTargetClass` 穿透 CGLIB 代理拿到真实类；`AnnotationUtils.findAnnotation` 沿类继承链查找 `@ManagerPermissions`，允许注解写在中间层的抽象子类上。此路径已带 `@Deprecated`，保留一版以便迁移期间不破坏老 Controller。

### ManagerControllerAuditAspect（在 crystal-audit）

切点覆盖 `AbstractManagerController` 的所有子类。审计切面的 `@Order` 为 `0`，位于权限安全网切面（`@Order = 1000`）外层；它观察权限检查和业务方法返回的 `Mono`，并在收到 `onNext`、`onComplete` 或 `onError` 信号时异步写入审计日志。

审计切面记录：

- 操作人（`userId`、`username`、`tenantId`）和操作时间
- 操作类型（`create` / `read` / `update` / `delete`，其中 `readAll` 归为 `read`）
- 资源类型（Entity 的 `@Table` 表名）以及可从 DTO 提取的资源 ID
- 请求信息（`requestId`、HTTP Method、Path、客户端 IP、User-Agent）
- 执行是否成功以及错误消息

审计切面只识别 `create`、`read`、`readAll`、`update`、`delete` 五个标准方法。Manager Controller 中额外定义的自定义端点以及普通 Controller 不会被自动记录，需要显式打点或另行扩展切面。当前实现不记录完整请求参数或响应结果。

## 类型参数约束链

7 个类型参数建立完整的类型链：

```
StandardManagerController
    │ SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>
    │               │ REPOSITORY : BaseRepository<ENTITY>
    │               │ ENTITY : BaseEntity
    │               │ CREATE_DTO : Any
    │               │ READ_DTO : BaseManagerReadDTO (extends PageQuery)
    │               │ UPDATE_DTO : BaseManagerUpdateDTO (has id: Long)
    │               │ DELETE_DTO : BaseManagerDeleteDTO (has ids: List<Long>)
    │
    └─ managerService (constructor)
```

Service 内部的 `query(dto)` / `update(dto)` / `deleteByDTO(dto)` 需要拿到具体的 DTO 类型来做反序列化和字段映射，故通过 Service 的泛型参数暴露所有 DTO 类型，使 Service 的类型签名成为整个 CRUD 链路的类型信息中枢。

## CachedBaseManagerService

Controller 强制 Service 是 `CachedBaseManagerService` 而非 `BaseManagerService`：

| | BaseManagerService | CachedBaseManagerService |
|---|---|---|
| 缓存 | 无 | 通过 `withXXXContext` 缓存查询结果 |
| 失效策略 | — | update / delete 时自动 evict 缓存 |
| 适用 | 简单 CRUD | 管理端标准 CRUD（推荐） |

## DTO 基类约束

| 基类 | 携带字段 | 用途 |
|---|---|---|
| `BaseManagerReadDTO` | `page`, `pageSize`, `id?`, `query?: QueryNode` | 分页查询 + 结构化条件树 |
| `BaseManagerUpdateDTO` | `id: Long` | 按 ID 更新 |
| `BaseManagerDeleteDTO` | `ids: List<Long>` | 批量删除 |

`CREATE_DTO` 无基类约束（`Any`），按业务需求自由定义。

### BaseManagerReadDTO 的 `query: QueryNode` 字段

`BaseManagerReadDTO` 携带一个 `QueryNode` 类型的结构化查询条件树，支持 AND/OR 嵌套和多种操作符（eq / contains / gte / …）。此机制自 v1.5.0 引入，前端 `FilterBuilder` 组件输出的即为这棵树的 JSON 表示。

## 现有真实使用位置

| 模块 | Controller |
|---|---|
| `crystal-resource` | `ManagerStorageProviderController`、`ManagerFileResourceController` |
| `crystal-rbac`（user） | `ManagerUserRoleController`、`ManagerUserPermissionController` |
| `crystal-tenant` | `ManagerTenantController`、`ManagerTenantTireTypeController` |
| `crystal-mail` | `ManagerMailTemplateController` |

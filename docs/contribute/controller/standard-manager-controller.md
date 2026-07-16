# StandardManagerController

## 设计意图

`StandardManagerController` 是框架管理端 CRUD 的核心抽象。它约束了 Controller → Service → Repository 的分层协作模式，配合 AOP 实现权限校验和审计日志的自动化。7 个类型参数把整条链路（Service、Repository、Entity、四个 DTO）串联起来，编译期保证类型一致，运行期由 AOP 完成权限拦截。

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

- **方法名 `read` vs URL `/query`** 的历史遗留。早期 API 用 `read` 作为方法名（对应 `/read` URL），后来路径调整为 `/query`（更符合"分页查询"语义），但方法名保留是因为 `ManagerControllerPermissionAspect` 按方法名反射匹配 `@ManagerPermissions.read`。修改此方法名会破坏 AOP 权限映射，改动前必须同步修改 aspect。

- **`version = "1"`** 是 `@GetMapping` / `@PostMapping` 的自定义属性，配合框架自定义的 `RequestMappingHandlerMapping` 生成 `/api/v1/xxx` 路径。

- **`awaitListWithTimeout()`** 是项目扩展函数，将 `Flux<T>` 转换为 `List<T>` 并附加超时保护，防止 R2DBC 死锁拖挂协程链。

- **`readAll` 直接调用 `getRepository().findAll()`** 而非 Service 层聚合方法——全量查询无业务处理需求，直接走底层减少一层间接。若子类需要过滤，必须 override `readAll`，而不是靠 Service 提供一个方法。

## 方法命名约定

`ManagerControllerPermissionAspect` 按方法名反射匹配权限配置。方法名是约定接口：

| 方法名 | 匹配 `@ManagerPermissions` 字段 | HTTP |
|---|---|---|
| `readAll` | `readAll`（空数组时回退到 `read`） | GET `/list` |
| `read` | `read` | POST `/query` |
| `create` | `create` | POST `/create` |
| `update` | `update` | POST `/update` |
| `delete` | `delete` | POST `/delete` |

Subclass override 时不能修改方法名，否则 AOP 无法匹配。

## @ManagerPermissions 注解

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagerPermissions(
    val read: Array<String> = [],
    val readAll: Array<String> = [],
    val create: Array<String> = [],
    val update: Array<String> = [],
    val delete: Array<String> = []
)
```

- `AnnotationTarget.CLASS` — 类级别注解，作用于整个 Controller
- 每个字段是权限标识数组，OR 语义
- `readAll` 空数组时降级到 `read`——逻辑在 aspect 内部；大多数场景只需配置 `read` 即可同时覆盖 list 和 query
- 空数组等同于"不校验"，aspect 打 warn 日志并放行；适合测试或临时开放接口

## AOP 拦截体系

### ManagerControllerPermissionAspect

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val targetClass = AopUtils.getTargetClass(joinPoint.target)
        val permissions = AnnotationUtils.findAnnotation(targetClass, ManagerPermissions::class.java)
            ?: return joinPoint.proceed()

        val methodName = (joinPoint.signature as MethodSignature).method.name
        val requiredPermissions = when (methodName) {
            "readAll" -> permissions.readAll
            "read"    -> permissions.read
            "create"  -> permissions.create
            "update"  -> permissions.update
            "delete"  -> permissions.delete
            else      -> null
        }?.filter { it.isNotEmpty() }?.toList()

        if (requiredPermissions.isNullOrEmpty()) {
            logger.warn("No valid permission required for $methodSignature, skipped.")
            return joinPoint.proceed()
        }

        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication }
            .flatMap { authentication ->
                if (!hasAnyPermission(authentication, requiredPermissions)) {
                    throw AuthorizationDeniedException("Access denied: ...")
                }
                @Suppress("UNCHECKED_CAST")
                joinPoint.proceed() as Mono<Any>
            }
    }
}
```

切入点覆盖范围：`StandardManagerController.*(..)` 覆盖 Standard 及其所有子类（含 Readonly），不包括 Scoped / DerivedScoped / Tenant 家族。这是 Scoped 家族在方法体内自行校验权限的原因。

`AopUtils.getTargetClass` 穿透 CGLIB 代理拿到真实类；`AnnotationUtils.findAnnotation` 沿类继承链查找 `@ManagerPermissions`，允许注解写在中间层的抽象子类上。直接调用 `class.getAnnotation()` 不支持这两种场景。

### ManagerControllerAuditAspect（在 crystal-audit）

同一切入点，`@Order` 排在权限切面之后。执行顺序：权限检查 → 审计日志 → 业务方法。

审计切面记录：

- 操作人（`userAuthentication.userId`）、操作时间
- 操作类型（`create` / `update` / `delete`）
- 请求参数、响应结果（脱敏后）
- 资源标识（entity id）

审计切面同样只覆盖 `StandardManagerController` 家族。Scoped 家族的审计需要手动打点或另写 aspect。

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

# 标准化控制器

::: warning 返回值类型

继承 `StandardManagerController` / `ReadonlyManagerController` 的类不需要自己写方法体，基类已经统一返回 `ApiResponse<*>`。但如果需要自定义端点（如新增非 CRUD 接口），**所有自定义方法必须显式返回 `ApiResponse<*>`**，禁止返回裸类型。
:::

## 设计意图

`StandardManagerController` 是框架管理端 CRUD 的核心抽象。它约束了 Controller → Service → Repository 的分层协作模式，配合 AOP 实现权限校验和审计日志的自动化。

## 源码分析

完整源码在 `crystal-shared` 模块的 `com.lovelycatv.crystalframework.shared.controller` 包下：

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
    suspend fun readAll(userAuthentication: UserAuthentication): ApiResponse<*> { ... }

    @PostMapping("/create", version = "1")
    suspend fun create(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: CREATE_DTO): ApiResponse<*> { ... }

    @GetMapping("/query", version = "1")
    suspend fun read(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: READ_DTO): ApiResponse<*> { ... }

    @PostMapping("/update", version = "1")
    suspend fun update(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: UPDATE_DTO): ApiResponse<*> { ... }

    @PostMapping("/delete", version = "1")
    suspend fun delete(userAuthentication: UserAuthentication, @ModelAttribute @Valid dto: DELETE_DTO): ApiResponse<*> { ... }
}
```

### 方法命名约定

PermissionAspect 通过方法名反射匹配权限配置。**方法名是约定接口**，不能随意改名：

| 方法名 | 匹配 `@ManagerPermissions` 字段 | HTTP |
|--------|-------------------------------|------|
| `readAll` | `readAll` (为空时回退到 `read`) | GET |
| `read` | `read` | GET |
| `create` | `create` | POST |
| `update` | `update` | POST |
| `delete` | `delete` | POST |

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

- 类级别注解，作用于整个 Controller
- 每个字段接受权限标识数组，满足任一即放行（OR 语义）
- `readAll` 为空数组时自动降级到 `read`
- 空数组表示"该操作不设权限校验"，AOP 打印 warn 日志并放行

## AOP 拦截体系

### ManagerControllerPermissionAspect

```kotlin
@Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
```

切入点覆盖所有 StandardManagerController 的子类（含 ReadonlyManagerController）：

1. 通过 `AopUtils.getTargetClass` 获取真实类（绕过 CGLIB 代理）
2. 通过 `AnnotationUtils.findAnnotation` 获取 `@ManagerPermissions`（支持继承查找）
3. 根据方法名匹配对应的权限配置
4. 从 `ReactiveSecurityContextHolder` 获取当前认证信息
5. 对比 granted authorities 和 required permissions

### ManagerControllerAuditAspect

在 `crystal-audit` 模块中，另一个 `@Around` 切面拦截相同的切入点，记录每次 CRUD 操作的审计日志：

- 操作人、操作时间、操作类型（create/update/delete）
- 请求参数和响应结果（脱敏后）
- 资源标识

这两个切面通过 `@Order` 控制执行顺序：权限检查先于审计日志。

## 类型参数约束

七个类型参数建立了完整的类型链：

```
StandardManagerController
    │ SERVICE : CachedBaseManagerService
    │               │ REPOSITORY : BaseRepository
    │               │               └ ENTITY : BaseEntity
    │               │ CREATE_DTO : Any
    │               │ READ_DTO : BaseManagerReadDTO (extends PageQuery)
    │               │ UPDATE_DTO : BaseManagerUpdateDTO (has id: Long)
    │               └ DELETE_DTO : BaseManagerDeleteDTO (has ids: List<Long>)
    └ managerService (注入)
```

## CachedBaseManagerService

StandardManagerController 要求 Service 必须是 `CachedBaseManagerService`（而非 `BaseManagerService`），区别在于：

| | BaseManagerService | CachedBaseManagerService |
|--|--|--|
| 缓存 | 无 | 自动缓存查询结果 |
| 失效策略 | — | update/delete 后自动 evict 缓存 |
| 适用 | 不需要缓存的简单 CRUD | 管理端标准 CRUD（推荐） |

## DTO 基类说明

| 基类 | 携带字段 | 用途 |
|------|---------|------|
| `BaseManagerReadDTO` | `page`, `pageSize`, `searchKeyword`, `startTime`, `endTime`, `id` | 分页查询 |
| `BaseManagerUpdateDTO` | `id: Long` | 按 ID 更新 |
| `BaseManagerDeleteDTO` | `ids: List<Long>` | 批量删除 |

CREATE_DTO 无基类约束（`Any`），按业务需求定义字段即可。

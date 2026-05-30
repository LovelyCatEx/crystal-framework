---
name: add-controller
description: 为项目添加新的 Controller，包括标准 CRUD 管理控制器、只读管理控制器和普通自定义控制器。
---

# 添加 Controller

## 触发条件

当用户需要新增 API 接口时使用。需要先确认接口类型：
- 管理后台的 CRUD 操作 → `StandardManagerController`
- 管理后台的只读查询（日志类） → `ReadonlyManagerController`
- 自定义业务逻辑（登录、上传、非 CRUD） → 普通 `@RestController`

## 路径规范

所有接口路径必须以 `${GlobalConstants.REQUEST_MAPPING_PREFIX}` 开头：

| 接口类型 | 路径前缀示例 |
|---------|------------|
| 管理端 | `${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx` |
| 用户端 | `${GlobalConstants.REQUEST_MAPPING_PREFIX}/xxx` |

---

## 类型一：StandardManagerController（标准 CRUD）

适用于需要增删改查的管理后台页面。继承后自动获得 5 个端点，无需写任何方法体。

### 自动提供的端点

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/list` | 查询全部 |
| `GET` | `/query` | 分页查询（支持 `page`、`pageSize`、`searchKeyword`、`startTime`、`endTime`） |
| `POST` | `/create` | 创建 |
| `POST` | `/update` | 更新 |
| `POST` | `/delete` | 批量删除 |

### 执行步骤

**1. 创建四个 DTO**

```kotlin
// ManagerCreateXxxDTO.kt — 业务字段自定义
class ManagerCreateXxxDTO(
    var name: String = "",
    var description: String = "",
)

// ManagerReadXxxDTO.kt — 继承 BaseManagerReadDTO，可追加过滤字段
data class ManagerReadXxxDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val customFilter: String? = null,   // 可选的额外过滤字段
) : BaseManagerReadDTO(page, pageSize)

// ManagerUpdateXxxDTO.kt
class ManagerUpdateXxxDTO : BaseManagerUpdateDTO()

// ManagerDeleteXxxDTO.kt
class ManagerDeleteXxxDTO : BaseManagerDeleteDTO()
```

**2. 创建 ManagerService**

```kotlin
@Service
class XxxManagerService(
    repository: XxxRepository
) : CachedBaseManagerService<
    XxxRepository,
    XxxEntity,
    ManagerCreateXxxDTO,
    ManagerReadXxxDTO,
    ManagerUpdateXxxDTO,
    ManagerDeleteXxxDTO
>(repository) {
    override fun getEntityClass(): KClass<*> = XxxEntity::class
}
```

**3. 创建 Controller**

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_XXX_READ],
    readAll = [SystemPermission.ACTION_XXX_READ],
    create  = [SystemPermission.ACTION_XXX_CREATE],
    update  = [SystemPermission.ACTION_XXX_UPDATE],
    delete  = [SystemPermission.ACTION_XXX_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")
class XxxManagerController(
    managerService: XxxManagerService
) : StandardManagerController<
    XxxManagerService,
    XxxRepository,
    XxxEntity,
    ManagerCreateXxxDTO,
    ManagerReadXxxDTO,
    ManagerUpdateXxxDTO,
    ManagerDeleteXxxDTO
>(managerService)
```

### @ManagerPermissions 说明

- `readAll` 为空时回退到 `read`
- 每个操作支持多个权限值，任一匹配即放行（OR 语义）
- 权限常量定义在 `SystemPermission.kt` 或模块自己的 `XxxPermission.kt`

---

## 类型二：ReadonlyManagerController（只读）

适用于系统自动生成、不允许用户修改的数据（审计日志、邮件发送记录等）。

继承方式与 `StandardManagerController` 完全相同，区别是：
- 只提供 `/list` 和 `/query` 两个端点
- `/create`、`/update`、`/delete` 被重写为始终返回 403

`@ManagerPermissions` 的 `create`、`update`、`delete` 通常设置为与 `read` 相同的权限（因为这些端点永远不会被调用）。

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_XXX_READ],
    readAll = [SystemPermission.ACTION_XXX_READ],
    create  = [SystemPermission.ACTION_XXX_READ],
    update  = [SystemPermission.ACTION_XXX_READ],
    delete  = [SystemPermission.ACTION_XXX_READ],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/xxx")
class XxxManagerController(
    managerService: XxxManagerService
) : ReadonlyManagerController<...>(managerService)
```

---

## 类型三：普通 @RestController（自定义）

适用于非 CRUD 的业务接口。

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/xxx")
class XxxController(
    private val xxxService: XxxService,
) {
    // 需要登录，需要权限
    @PreAuthorize("hasAuthority('${XxxPermission.ACTION_XXX_READ}')")
    @GetMapping("/data")
    suspend fun getData(): ApiResponse<*> {
        return ApiResponse.success(xxxService.getData())
    }

    // 需要登录，不需要特定权限
    @GetMapping("/profile")
    suspend fun getProfile(userAuthentication: UserAuthentication): ApiResponse<*> {
        return ApiResponse.success(xxxService.getProfile(userAuthentication.userId))
    }

    // 完全公开，不需要登录
    @Unauthorized
    @GetMapping("/public")
    suspend fun getPublic(): ApiResponse<*> {
        return ApiResponse.success(xxxService.getPublicData())
    }
}
```

### 关键注解

| 注解 | 说明 |
|------|------|
| `@PreAuthorize("hasAuthority('...')")` | 校验单个权限 |
| `@PreAuthorize("hasAnyAuthority('...', '...')")` | 任一权限匹配即放行 |
| `@Unauthorized` | 完全跳过认证，无需 token |
| `UserAuthentication` | 方法参数，框架自动注入当前用户信息（userId、tenantId 等） |

### 参数接收方式

| 场景 | 注解 |
|------|------|
| JSON body | `@RequestBody` |
| 表单 / query string | `@ModelAttribute` |
| 单个 query 参数 | `@RequestParam` |
| 文件上传 | `@RequestPart` |
| 参数校验 | 配合 `@Valid` 使用 |

---

## 异常处理

直接抛异常，框架全局捕获并转换为 `ApiResponse`，不要手动 try-catch：

| 异常 | 对应 code | 说明 |
|------|-----------|------|
| `BusinessException(message)` | 400 | 业务校验不通过，message 透传到前端 |
| `ForbiddenException(message)` | 403 | 无权限 |
| `UnauthorizedException(message)` | 401 | 未登录 |

---

## 强制约束

**Controller 禁止直接注入 Repository，所有数据库操作必须通过 Service 层进行。** 如果 Service 接口中没有所需方法，应先在 Service 接口和实现中添加对应方法，再由 Controller 调用。

**Manager Controller 与普通 Controller 的 Service 必须严格分离，禁止复用：**

| Controller 类型 | 对应 Service | 包路径 |
|---|---|---|
| `StandardManagerController` / `ReadonlyManagerController` | 继承 `CachedBaseManagerService` 的 Manager Service | `service/manager/` + `service/manager/impl/` |
| 普通 `@RestController` | 普通 Service 接口 + 实现 | `service/` + `service/impl/` |

即使同一个实体同时有 Manager Controller 和普通 Controller，也必须分别创建独立的 Service，不得让普通 Controller 注入 Manager Service，也不得让 Manager Controller 注入普通 Service。

---

## 执行步骤总结

1. 确认接口类型（Standard / Readonly / 普通）
2. 如需权限，先在 `SystemPermission.kt` 或模块常量文件中定义权限常量（参考 `AddSystemPermission` skill）
3. 创建 DTO（Standard/Readonly 需要四个，普通按需）
4. 创建 Service（Standard/Readonly 需要 ManagerService）
5. 创建 Controller，填写正确的 `@RequestMapping` 路径
6. 确认权限已在 `SystemRbacConfigurer` 中注册并绑定到角色

## 输出格式

完成后说明：
1. Controller 类型和路径
2. 自动提供的端点列表（Standard/Readonly）或自定义端点列表（普通）
3. 新增/修改的文件路径
4. 依赖的权限常量（如有）

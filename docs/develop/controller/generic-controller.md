# 普通控制器

普通控制器指不继承任何框架基类的 `@RestController`。CRUD 场景请使用 Manager 家族，其他场景（登录、上传、动作触发、状态查询）使用普通控制器。

## 适用场景

- 非 CRUD 业务端点（登录、注册、修改密码、发送验证码）
- 面向用户的 API（非管理后台）
- 上传下载、代理转发、Webhook 接收
- 权限规则超出 `@ManagerPermissions` / `ScopedPermissionTriad` 表达能力

## 基本骨架

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/ext/my-plugin/tools")
class ExtMyPluginToolsController(
    private val myPluginService: ExtMyPluginService,
) {
    @PreAuthorize("hasAnyAuthority('ext_my_plugin.tool_execute')")
    @PostMapping("/execute")
    suspend fun execute(
        userAuthentication: UserAuthentication,
        @RequestBody @Valid dto: ExecuteToolDTO
    ): ApiResponse<*> {
        val result = myPluginService.execute(dto, userAuthentication.userId)
        return ApiResponse.success(result)
    }

    @Unauthorized
    @GetMapping("/status")
    suspend fun status(): ApiResponse<*> {
        return ApiResponse.success(mapOf("online" to true))
    }
}
```

必备注解：

- `@Validated` — 启用 JSR-303 参数校验
- `@RestController` — 声明 REST 控制器
- `@RequestMapping` — 用 `GlobalConstants.REQUEST_MAPPING_PREFIX` 拼路径，路径按模块业务归属命名

## 权限校验

普通控制器不能使用 `@ManagerPermissions`（该注解仅对 `StandardManagerController` 家族生效）。使用 Spring Security 的 `@PreAuthorize`：

```kotlin
@PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
@PostMapping("/update-profile")
suspend fun updateProfile(...): ApiResponse<*>
```

规则：

- 权限名必须引用 `SystemPermission` 或对应模块的 `Permission` 常量，禁止字符串字面量
- 统一使用 `hasAnyAuthority(...)`（复数），即使只传一个权限
- 完全匿名访问（登录、注册、公开接口）使用 `@Unauthorized` 标注方法：

```kotlin
@Unauthorized
@PostMapping("/login")
suspend fun login(@RequestBody dto: LoginDTO): ApiResponse<*> { ... }
```

启动时 `UnauthorizedPathScanner` 扫描所有 `@RestController` 中带 `@Unauthorized` 的路径，加入 Spring Security 的 `.permitAll()` 白名单，`CustomAuthFilter` 同时跳过这些路径的 JWT 校验。

未添加 `@PreAuthorize` 且未添加 `@Unauthorized` 的端点需要合法 token 但不校验具体权限。

## 参数注入

`UserAuthentication` 由框架自动注入，包含 `userId` / `tenantId` / `tenantMemberId` 等身份信息：

```kotlin
@GetMapping("/my-tenants")
suspend fun listMyTenants(userAuthentication: UserAuthentication): ApiResponse<*> {
    return ApiResponse.success(tenantService.findByUserId(userAuthentication.userId))
}
```

## 请求参数绑定

参数绑定的三种方式与对应的前端 Content-Type：

| 后端注解 | 场景 | 前端调用 |
|---|---|---|
| `@RequestBody` | JSON 请求体（POST / PUT） | `doPost(url, body, { 'Content-Type': 'application/json' })` |
| `@ModelAttribute` | form-urlencoded 请求体 | `doPost(url, body)`（默认） |
| `@RequestParam` | GET 查询参数 | `doGet(url, { param: value })` |

`@RequestBody` 用于 `@PostMapping`；`@ModelAttribute` 多用于 `@PostMapping` 承接表单 DTO；`@RequestParam` 用于 `@GetMapping`。

## DTO 使用规则

自定义 Controller 的请求 DTO 禁止继承 `BaseManagerReadDTO` / `BaseManagerCreateDTO` / `BaseManagerUpdateDTO` / `BaseManagerDeleteDTO`——这四个基类专供 `StandardManagerController` 体系。

需要分页的自定义 DTO 继承轻量基类 `PageQuery`：

```kotlin
class ExtMyPluginQueryDTO(
    override val page: Int,
    override val pageSize: Int,
    val statusFilter: String? = null,
) : PageQuery(page, pageSize)
```

## 权限注册

`@PreAuthorize` 引用的权限字符串必须注册到 `SystemRbacRegistry`：

```kotlin
@Component
class ExtMyPluginPermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.registerPermission(
            SystemRbacPermissionDeclaration.action(
                name = "ext_my_plugin.tool_execute",
                description = "Execute plugin tool",
                group = "ext_my_plugin"
            )
        )
    }
}
```

详见 [系统权限](/develop/sdk/system-permission)。

## 规则

- Controller 头部必须携带 `@Validated`、`@RestController`、`@RequestMapping`
- 方法必须显式返回 `ApiResponse<*>`，禁止返回裸对象、`Mono`、`Flow`
- Controller 内禁止注入 Repository，数据库操作走 Service 层
- 普通 Controller 只能注入普通 Service（`service/` + `service/impl/`），禁止注入 Manager Service
- 权限字符串必须引用 `SystemPermission` / `TenantPermission` 等常量，禁止字面量

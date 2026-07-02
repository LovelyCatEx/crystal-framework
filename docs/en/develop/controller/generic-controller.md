# Generic Controller

A generic Controller is a `@RestController` without any framework base class. Use the Manager family for CRUD; use generic Controllers for everything else (login, upload, action trigger, health check).

## Applicable scenarios

- Non-CRUD business endpoints (login, sign-up, password reset, send-verify-code)
- User-facing APIs (not admin backend)
- Uploads, downloads, proxy forwarding, webhook receivers
- Permission rules too complex for `@ManagerPermissions` or `ScopedPermissionTriad`

## Skeleton

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

Required annotations:

- `@Validated` — enables JSR-303 parameter validation
- `@RestController` — declares a REST controller
- `@RequestMapping` — path built from `GlobalConstants.REQUEST_MAPPING_PREFIX`, named after the module's business area

## Permission checks

Generic Controllers cannot use `@ManagerPermissions` (that annotation only applies to the `StandardManagerController` family). Use Spring Security's `@PreAuthorize`:

```kotlin
@PreAuthorize("hasAnyAuthority('${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
@PostMapping("/update-profile")
suspend fun updateProfile(...): ApiResponse<*>
```

Rules:

- Permission names must reference `SystemPermission` or the module's `Permission` constants — no literals
- Always use `hasAnyAuthority(...)` (plural), even for a single permission
- Endpoints open to anonymous access (login, sign-up, public APIs) carry `@Unauthorized`:

```kotlin
@Unauthorized
@PostMapping("/login")
suspend fun login(@RequestBody dto: LoginDTO): ApiResponse<*> { ... }
```

At startup, `UnauthorizedPathScanner` scans all `@RestController` classes for `@Unauthorized`-annotated methods and adds those paths to Spring Security's `.permitAll()`. `CustomAuthFilter` also skips JWT validation for these paths.

Endpoints without both `@PreAuthorize` and `@Unauthorized` still require a valid token but do not enforce specific permissions.

## Argument injection

`UserAuthentication` is auto-injected and carries `userId` / `tenantId` / `tenantMemberId`:

```kotlin
@GetMapping("/my-tenants")
suspend fun listMyTenants(userAuthentication: UserAuthentication): ApiResponse<*> {
    return ApiResponse.success(tenantService.findByUserId(userAuthentication.userId))
}
```

## Request parameter binding

Three binding styles and the matching frontend Content-Type:

| Backend annotation | Use | Frontend call |
|---|---|---|
| `@RequestBody` | JSON body (POST / PUT) | `doPost(url, body, { 'Content-Type': 'application/json' })` |
| `@ModelAttribute` | form-urlencoded body | `doPost(url, body)` (default) |
| `@RequestParam` | GET query params | `doGet(url, { param: value })` |

`@RequestBody` for `@PostMapping`; `@ModelAttribute` mostly for `@PostMapping` receiving a form DTO; `@RequestParam` for `@GetMapping`.

## DTO usage rule

Custom Controller request DTOs must not extend `BaseManagerReadDTO` / `BaseManagerCreateDTO` / `BaseManagerUpdateDTO` / `BaseManagerDeleteDTO` — these bases are reserved for the `StandardManagerController` system.

For a custom paginated DTO, extend the lightweight `PageQuery`:

```kotlin
class ExtMyPluginQueryDTO(
    override val page: Int,
    override val pageSize: Int,
    val statusFilter: String? = null,
) : PageQuery(page, pageSize)
```

## Registering permissions

Permission strings referenced in `@PreAuthorize` must be registered via `SystemRbacRegistry`:

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

See [System Permission](/en/develop/sdk/system-permission).

## Rules

- Class header must carry `@Validated`, `@RestController`, `@RequestMapping`
- Methods must explicitly return `ApiResponse<*>` — no bare objects, `Mono`, or `Flow`
- Controllers must not inject Repositories; DB operations go through Service
- Generic Controllers inject only non-Manager Services (`service/` + `service/impl/`); never inject Manager Services
- Permission strings must reference `SystemPermission` / `TenantPermission` constants, never literals

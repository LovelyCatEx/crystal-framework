---
name: add-generic-controller
description: 添加不继承任何 base controller 的普通 REST 端点（非 CRUD 或非标准 CRUD 的自定义业务接口），仍然要遵守项目的类注解、参数绑定、返回类型、权限、Long 序列化、Repository 直注禁令等一系列规则。
---

# 添加 Generic Controller（自定义业务端点）

## 触发条件

当用户要求添加 **不属于标准 CRUD 5 端点** 的接口时使用。典型场景：

- 认证类：`/user/register` `/user/resetPassword` `/user/uploadAvatar`
- 触发型动作：`/approval-flow-instances/start` `/approval-flow-tasks/handle`
- 只读的聚合信息：`/system/integrated-info` `/liveness` `/readiness`
- 自定义资源上传下载：文件上传、导出等

如果只是要添加标准 CRUD 端点 → 用 `add-standard-manager-controller` / `add-scoped-manager-controller` / `add-readonly-scoped-manager-controller`。

## 判断标准

**用 Generic Controller（是）**：
- 端点集合不是"5 个标准 CRUD"
- 或者端点集合与 CRUD 语义不重合（比如只有 `/register` `/resetPassword` 之类）
- 或者是"标准 Controller + 少量自定义端点"—— 自定义端点部分按本 skill 规则写

**不用**：
- 完整标准 CRUD → 用对应的 Manager Controller skill
- CRUD + 极少量额外端点 → 用 Manager Controller skill，在同一 Controller 类里再加自定义方法（这本 skill 的规则同样适用于那些自定义方法）

## 输入格式

用户需要提供：
1. 端点前缀（如 `/user`）
2. 端点列表 + 每个端点：HTTP method / 参数 / 是否免登 / 权限要求
3. 依赖的 Service（不允许直接依赖 Repository）

## 前提信息

### 类级注解（三件套，必须齐全）

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/xxx")
class XxxController(
    private val xxxService: XxxService,
)
```

- `@Validated` — 让 `@Valid` 在 DTO 上生效
- `@RestController` — 声明为 Spring REST 控制器（不能用 `@Controller`，返回值不是 view）
- `@RequestMapping` 前缀必须用 `${GlobalConstants.REQUEST_MAPPING_PREFIX}` 拼接，禁止硬编码 `/api/v1/...`

### 端点方法签名（返回类型）

**所有端点方法必须返回 `ApiResponse<*>`**：

```kotlin
@PostMapping("/xxx", version = "1")
suspend fun doSomething(...): ApiResponse<*> {
    val result = xxxService.doIt(...)
    return ApiResponse.success(result)
}
```

- 成功用 `ApiResponse.success(data)` 或 `ApiResponse.success(null, "ok")`
- 失败抛异常（`BusinessException` / `ForbiddenException` / `UnauthorizedException`），全局 exception handler 会转成 `ApiResponse.failed(...)`
- 禁止裸返回实体或 List

### 参数绑定（三选一，必须严格匹配）

| 后端注解 | 适用场景 | 前端调用 |
|---|---|---|
| `@RequestBody` | 复杂 JSON body（嵌套对象、可空字段多） | `doPost(url, body, {'Content-Type': 'application/json'})` |
| `@ModelAttribute` | 简单 form 表单 / DTO 平铺字段 | `doPost(url, body)`（默认 form-urlencoded） |
| `@RequestParam` | GET 请求单值参数 | `doGet(url, { param: value })` |

**Content-Type 前后端必须严格对齐**。选错后端注解 = 前端拿到 415 或空 DTO。

### 授权：`@Unauthorized` 与 RBAC

**默认所有端点都要求登录**（JWT / session 校验）。免登需显式加：

```kotlin
@Unauthorized
@PostMapping("/register", version = "1")
suspend fun register(...): ApiResponse<*> { ... }
```

**RBAC 精细控制**：
- 简单授权：类级 `@ManagerPermissions`（仅对继承 `StandardManagerController` 的 5 端点生效）
- 端点级：方法内手动调 `RbacUtils.hasAuthority(...)` 或 `RbacUtils.hasAnyAuthority(...)` 判断
- Scoped 授权：直接注入 `UserAuthentication` 参数，调 `ScopedPermissionMatrix` / 手写规则

### `UserAuthentication` 注入

需要当前登录用户信息时，在方法参数上直接声明：

```kotlin
suspend fun doSomething(
    userAuthentication: UserAuthentication,   // 未登录会被 JWT filter 挡下（除非 @Unauthorized）
    // 或者用可空版本兼容 @Unauthorized 端点：
    userAuthentication: UserAuthentication?,
    // ...
)
```

字段：
- `userAuthentication.userId: Long`
- `userAuthentication.tenantId: Long?`（当前登录的 tenant，非 tenant 上下文时为 null）
- `userAuthentication.tenantMemberId: Long?`

### 禁止直接注入 Repository

**Controller 只能注入 Service**。业务逻辑（含事务、缓存、验证）都放 Service 层：

```kotlin
// ❌
class XxxController(private val xxxRepository: XxxRepository)

// ✓
class XxxController(private val xxxService: XxxService)
```

### DTO 分包

`Xxx` 相关的 DTO 放 `controller/dto/`（普通端点）或 `controller/manager/xxx/dto/`（Manager 端点）。**单文件单定义**。

DTO 必须 `data class`。前端传后端的类叫 **DTO**，后端返给前端的响应类叫 **VO**（分包 `controller/vo/`）。

### Long 序列化

DTO / VO / entity 里所有 `Long` 字段（`id` / `xxxId` / `xxxTime`）序列化到前端都必须是 `string`：

- Entity 里 `Long` 字段加 `@get:JsonSerialize(using = ToStringSerializer::class)`（可空 `Long?` 尤其要加）
- DTO / VO 是 data class 无需额外注解，全局 Jackson module 会处理
- 前端 TypeScript 定义用 `id: string`

### Aspect / Filter 若涉及

如果这个 Controller 上要挂新的 aspect / filter：

- 类必须加 `@Order(GlobalConstants.AspectPriority.XXX)` 或 `@Order(GlobalConstants.FilterPriority.XXX)`
- **禁止硬编码 int**，优先级都必须来自 `GlobalConstants` 常量

## 执行步骤

1. **确认 Service 就绪**（业务逻辑放 Service 层，Controller 只做 HTTP 层薄封装）
2. **DTO/VO 分包创建**，单文件单定义，`data class`
3. **建 Controller 类**：类级三件套注解 + 服务注入
4. **每个端点**：
   - 选好 HTTP method + 参数绑定注解
   - 决定 `@Unauthorized` 与否
   - 决定授权粒度（`@ManagerPermissions` / `RbacUtils.hasAuthority` / 无 RBAC）
   - 方法签名声明必要的 `UserAuthentication` 参数
   - 返回 `ApiResponse<*>`
5. **前端 API 对接**：
   - Manager 端点走 `BaseManagerController` 或衍生方法
   - 自定义端点直接用 `doPost` / `doGet`，Content-Type 严格对齐后端参数注解
6. **异常兜底**：所有错误抛 `BusinessException`（带描述性消息），别 `println` 或吞异常

## 常见错误

| 错误 | 修正 |
|---|---|
| 类上没加 `@Validated` | 加，否则 DTO 上的 `@Valid` 不生效 |
| 端点返回 `String` / `List<...>` 裸值 | 用 `ApiResponse.success(...)` 包装 |
| 前端发 JSON 但后端用 `@ModelAttribute` | 后端改 `@RequestBody`；或前端改 form-urlencoded |
| 端点忘加 `@Unauthorized` 但意图是"免登注册" | 加 `@Unauthorized`，否则会被 JWT filter 挡下 |
| Controller 直接注入 Repository | 只注 Service，Controller 是 HTTP 层薄封装 |
| DTO 用 `class` 不是 `data class` | 用 `data class`，`copy()` 等场景需要 |
| 硬编码 `/api/v1/xxx` | 用 `${GlobalConstants.REQUEST_MAPPING_PREFIX}` |
| 多个 DTO 挤一个文件 | 单文件单定义，每类独立 `.kt` |
| Long 字段传给前端拿到数值（`123456789012345`） | Entity 的 Long 字段加 `@get:JsonSerialize(using = ToStringSerializer::class)`；可空 `Long?` 尤其必要 |
| 手写异常返回 `ApiResponse.failed(...)` | 抛 `BusinessException(...)` 让 exception handler 处理，减少样板 |
| 端点方法名跟 `StandardManagerController` 的重名（如 `read`）覆盖了基类行为 | 自定义端点名要跟基类 5 个方法名（`readAll` / `create` / `read` / `update` / `delete`）区分 |

## 快速参考：常见端点模板

**免登 form 表单**：
```kotlin
@Unauthorized
@PostMapping("/register", version = "1")
suspend fun register(@ModelAttribute @Valid dto: RegisterDTO): ApiResponse<*> {
    userService.register(dto.username, dto.password, dto.email)
    return ApiResponse.success(null)
}
```

**登录后 JSON body + 权限**：
```kotlin
@PostMapping("/start", version = "1")
suspend fun start(
    userAuthentication: UserAuthentication,
    @Valid @RequestBody dto: StartDTO,
): ApiResponse<*> {
    if (!RbacUtils.hasAuthority(SystemPermission.ACTION_XXX_START)) {
        throw ForbiddenException()
    }
    val result = xxxService.start(userAuthentication.userId, dto)
    return ApiResponse.success(result)
}
```

**GET query 参数**：
```kotlin
@GetMapping("/profile")
suspend fun profile(
    userAuthentication: UserAuthentication?,
    @RequestParam("id", required = false, defaultValue = "0") userId: Long,
): ApiResponse<*> {
    val target = if (userId > 0) userId else userAuthentication?.userId ?: throw BusinessException("...")
    return ApiResponse.success(userService.getProfileVO(target))
}
```

**文件上传**：
```kotlin
@PostMapping("/uploadAvatar")
suspend fun uploadAvatar(
    userAuthentication: UserAuthentication,
    @RequestPart("file") file: FilePart,
): ApiResponse<*> {
    userService.uploadAvatar(userAuthentication.userId, file)
    return ApiResponse.success(null)
}
```

## 输出格式

完成后说明：
1. 新增 Controller 路径 + `@RequestMapping` 前缀
2. 每个端点：method + path + 参数绑定 + 是否 `@Unauthorized` + 授权方式
3. 引用的 Service / DTO / VO 路径
4. 前端 API 文件里对应的调用代码

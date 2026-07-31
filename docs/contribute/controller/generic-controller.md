# 普通控制器

## 设计意图

框架不强制所有 Controller 继承基类。CRUD 场景由 Manager 家族的 6 个基类覆盖；其他场景（认证、上传、代理转发、Webhook）使用标准 Spring `@RestController`。

## 与 Manager 家族的正交性

普通 Controller 与 Manager Controller 是互补关系，不互相替代：

| 维度 | 普通 Controller | Manager Controller |
|---|---|---|
| 定位 | 业务 API | 管理后台 CRUD |
| 路由前缀 | 自由（`/api/{version}/ext/...` / `/api/{version}/oauth/...`） | 强制 `/api/{version}/manager/...` |
| 权限机制 | `@PreAuthorize` + Spring Security SpEL | `PermissionMatrix`（构造参数，authorize 内联校验） |
| 审计切面 | 通过 `@Audit` 显式记录 | 5 个标准方法由 `ManagerControllerAuditAspect` 自动记录，自定义方法使用 `@Audit` |
| 参数注入 | 支持 `UserAuthentication` | 支持 `UserAuthentication` |
| 端点数 | 完全自定义 | 5 个标准端点（可 override，可加自定义） |

## 审计日志

普通 Controller 和 Manager Controller 的自定义方法需要使用 `@Audit` 显式声明审计信息：

```kotlin
@Audit(
    action = AuditAction.READ,
    resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
)
@PostMapping("/simulate")
suspend fun simulate(
    userAuthentication: UserAuthentication,
    @Valid @RequestBody dto: SimulateStorageProviderRoutingRuleDTO,
): ApiResponse<SimulationResultVO> {
    authorize(ManagerAction.READ, userAuthentication)
    return ApiResponse.success(managerService.simulate(dto))
}
```

`action` 和 `resourceType` 必填，`resourceIds` 可选。`resourceIds` 是基于方法参数名的 SpEL 表达式，结果必须是 `Long` 或 `Collection<Long>`；不需要记录资源 ID 时留空。方法参数中必须包含 `UserAuthentication`，否则切面直接跳过记录。

切面复用请求上下文和统一的 `AuditLogService`，自动记录操作者、请求信息、成功状态和错误消息。Manager Controller 的 `create`、`read`、`readAll`、`update`、`delete` 已由 `ManagerControllerAuditAspect` 自动记录，不要重复添加 `@Audit`；只有额外定义的自定义方法需要显式标注。

## 权限校验实现

普通 Controller 走 Spring Security 原生的 SpEL 校验路径。

### @PreAuthorize + SpEL

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_CREATE}')")
```

- Spring Security 在方法调用前通过 SpEL 求值
- `hasAnyAuthority()` 从 `SecurityContext.authentication.authorities` 中查匹配
- 校验失败抛 `AuthorizationDeniedException`，由 `GlobalExceptionHandler.handleAuthorizationDeniedException` 转 403

### `${...}` 而非常量引用

`@PreAuthorize` 的参数是编译期常量字符串，Kotlin 中必须用字符串模板 `"${...}"` 嵌入 `const val`，禁止硬编码字面量。

### hasAnyAuthority vs hasAuthority

框架统一使用复数形式的 `hasAnyAuthority`，即使单权限也传单元素数组。原因：扩展权限时不需要修改注解——新增权限直接追加参数，不必将 `hasAuthority` 改为 `hasAnyAuthority`。

### RbacUtils

部分场景下权限判断不是二元通过/拒绝，而是影响响应内容（字段脱敏、按权限过滤集合）。此时使用 `RbacUtils.hasAuthority`：

```kotlin
if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_LIST_ALL_PEM)) {
    return allMembers
} else {
    return allMembers.map { it.masked() }
}
```

`RbacUtils` 通过 `ReactiveSecurityContextHolder` 获取当前 authorities，与 `@PreAuthorize` 共享数据。

## @Unauthorized 处理机制

`@Unauthorized` 注解的完整签名：

```kotlin
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class Unauthorized(
    val reason: String = ""
)
```

`Target = ANNOTATION_CLASS + FUNCTION`：既可标在方法上，也可作为元注解嵌套。

### UnauthorizedPathScanner

启动阶段扫描逻辑（位于 `crystal-auth/config/UnauthorizedPathScanner.kt`）：

1. 遍历 Spring 中所有 `@RestController` bean
2. 反射拿到每个方法，检查是否带 `@Unauthorized`
3. 结合 class-level 的 `@RequestMapping` + method-level 的 `@GetMapping/@PostMapping` 拼出完整路径
4. 将路径加入白名单列表

### SecurityConfig 消费白名单

`SecurityConfig` 从 `UnauthorizedPathScanner` 获取路径列表，在配置阶段调用 `.pathMatchers(...).permitAll()` 放行。这些路径同时被 `CustomAuthFilter` 跳过 JWT 校验。

双点白名单同时覆盖 Spring Security 授权链和 JWT 认证链，避免任一层拦截匿名请求。

## 路由约定

- 业务 API：`/api/{version}/ext/{plugin-name}/...` — 插件扩展
- 管理端非 CRUD：`/api/{version}/manager/xxx/...` — 系统内置的管理端定制端点
- 认证相关：`/api/{version}/oauth/...` / `/api/{version}/auth/...`
- 统一通过 `GlobalConstants.REQUEST_MAPPING_PREFIX` 引用 `/api/{version}` 前缀，禁止硬编码

## 参数解析器

`UserAuthentication` 参数由自定义 `HandlerMethodArgumentResolver` 从 `ReactiveSecurityContextHolder` 中提取，产出包含 `userId` / `tenantId` / `tenantMemberId` / `authorities` 的值对象。

此能力不局限于任何 Controller 基类——所有 `@RestController` 均可注入。

## 现有真实使用位置

| 模块 | Controller | 用途 |
|---|---|---|
| `crystal-auth` | `UserAuthController` | 登录、注册、修改密码 |
| `crystal-auth` | `OAuthAccountController` | 系统级 OAuth 账号 |
| `crystal-auth` | `TenantOAuthAccountController` | 租户级 OAuth 账号 |
| `crystal-resource` | `FileResourceController` | 文件上传 / 下载 |
| `crystal-approval` | `ApprovalFlowInstanceController` | 审批发起 + 用户提交表单 |

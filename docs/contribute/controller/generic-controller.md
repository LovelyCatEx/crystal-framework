# 普通控制器

::: warning 返回值类型

**所有 Controller 方法必须显式返回 `ApiResponse<*>` 类型**，使用 `ApiResponse.success(data)` 或 `ApiResponse.failed(message)` 包装。禁止返回裸实体、List、Map 等原始类型——框架不提供统一包装层，前端 `doGet` / `doPost` 依赖 `ApiResponse` 结构解析。
:::

## 设计意图

框架不强制所有 Controller 继承基类。当业务逻辑无法被 CRUD 模板覆盖时（如认证、文件上传、多步骤操作），使用标准 Spring `@RestController`。

## 与 ManagerController 的关系

普通控制器和 ManagerController 是两个正交的 Controller 体系，互不替代：

| | 普通 Controller | ManagerController |
|--|--|--|
| 定位 | 业务 API | 管理端 CRUD |
| 路由前缀 | 自由定义 | `/manager/...` |
| 权限机制 | `@PreAuthorize` | `@ManagerPermissions` + AOP |
| 审计 | 手动或无需审计 | `ManagerControllerAuditAspect` 自动审计 |
| UserAuthentication | 参数注入（同） | 参数注入（同） |

## 权限校验

### @PreAuthorize

使用 Spring Security SpEL 表达式，字符串常量引用：

```kotlin
@PreAuthorize("hasAnyAuthority('\${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
@PostMapping("/update")
suspend fun updateTenantProfile(...): ApiResponse<*>
```

### @Unauthorized 公开接口

不加 `@PreAuthorize` 的端点仍需合法 token。如需完全匿名访问（登录、注册等），在方法上添加 `@Unauthorized`：

```kotlin
@Unauthorized
@PostMapping("/register")
suspend fun register(...): ApiResponse<*> { ... }
```

`SecurityConfig` 通过 `UnauthorizedPathScanner` 扫描所有 `@RestController` 中带 `@Unauthorized` 的路径，将其加入 `.permitAll()`，同时 `CustomAuthFilter` 会跳过这些路径的 JWT 校验。

### hasAnyAuthority vs hasAuthority

框架统一使用 `hasAnyAuthority`（复数），即使只传一个参数。这是为了后续扩展权限时不需要改注解。

### RbacUtils

部分场景需要 RBAC 条件判断，而非简单的是/否放行（如根据权限决定返回字段的可见性）：

```kotlin
if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_PROFILE_READ_PEM)) {
    // 全部可见
} else {
    // 部分字段脱敏
}
```

## 参数注入

普通控制器同样支持 `UserAuthentication` 参数注入——这是框架的 `ReactiveControllerAdvice` 或参数解析器统一处理，与是否继承基类无关。

```kotlin
@GetMapping("/profile")
suspend fun getProfile(
    userAuthentication: UserAuthentication,  // 自动注入
): ApiResponse<*>
```

## 路由约定

- 业务 API：`/api/{version}/ext/{plugin-name}/...`（插件扩展）
- 管理端非 CRUD：`/api/{version}/manager/...`（系统内置）
- 使用 `GlobalConstants.REQUEST_MAPPING_PREFIX` 引用 `/api/{version}` 前缀

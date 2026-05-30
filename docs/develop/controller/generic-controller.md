# 普通控制器

## 概述

普通控制器即标准的 Spring `@RestController`，不继承任何框架基类。适用于业务逻辑复杂、无法用 CRUD 模板表达的接口。

## 适用场景

- 非 CRUD 操作（登录、注册、上传）
- 需要自定义权限校验的业务接口
- 面向用户而非管理端的 API

## 与 ManagerController 的区别

| | ManagerController | 普通 Controller |
|--|--|--|
| 基类 | 继承 `StandardManagerController` | 无，纯 Spring |
| 权限校验 | `@ManagerPermissions` 注解 + 框架 AOP | `@PreAuthorize` |
| 端点 | 框架提供 5 个标准端点 | 完全自定义 |
| 方法体 | 通常为 0 | 手写 |

## 使用步骤

### 1. 直接创建 Controller 类

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/ext/my-plugin/custom")
class ExtMyPluginCustomController(
    private val myService: ExtMyPluginService,
) {
    @PreAuthorize("hasAnyAuthority('ext_my_plugin.custom_execute')")
    @PostMapping("/execute")
    suspend fun execute(
        userAuthentication: UserAuthentication,
        @RequestBody body: Map<String, Any>
    ): ApiResponse<*> {
        myService.execute(body)
        return ApiResponse.success(null)
    }

    @GetMapping("/status")
    suspend fun status(): ApiResponse<*> {
        return ApiResponse.success(mapOf("online" to true))
    }
}
```

### 2. 注册权限

在 `SystemRbacConfigurer` 中注册权限常量：

```kotlin
@Component
class MyPermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.registerPermission(
            SystemRbacPermissionDeclaration.action(
                "ext_my_plugin.custom_execute",
                "Execute custom operation",
                group = "ext_my_plugin"
            )
        )
    }
}
```

::: warning 返回值类型

**所有 Controller 方法必须显式返回 `ApiResponse<*>` 类型**，使用 `ApiResponse.success(data)` 或 `ApiResponse.failed(message)` 包装。禁止返回裸实体、List、Map 等原始类型——框架不提供统一包装层，前端 `doGet` / `doPost` 依赖 `ApiResponse` 结构（`code`、`message`、`data`）解析。
:::

## 关键点

- `UserAuthentication` 作为方法参数由框架自动注入，可直接获取当前用户/租户信息
- 不加 `@PreAuthorize` 的接口仍需合法 token 才能访问；如需完全公开（免登录），在方法上添加 `@Unauthorized` 注解
- 返回值统一使用 `ApiResponse.success(...)` / `ApiResponse.error(...)`
- **禁止直接注入 Repository，所有数据库操作必须通过 Service 层进行**
- **普通 Controller 必须注入普通 Service（`service/` + `service/impl/`），禁止注入 Manager Service（`service/manager/`）**

::: tip DTO 使用提醒
自定义 Controller 的请求参数 DTO **不应继承** `BaseManagerReadDTO`、`BaseManagerCreateDTO`、`BaseManagerUpdateDTO`、`BaseManagerDeleteDTO` 这四个标准 CRUD DTO。这些 DTO 专为 `StandardManagerController` 体系设计，自定义端点应使用更轻量的基类如 `PageQuery`。
:::

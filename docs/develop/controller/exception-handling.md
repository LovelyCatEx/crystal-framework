# 异常处理

框架的 `GlobalExceptionHandler` 全局兜底 Controller 抛出的异常，业务代码在失败路径直接抛异常即可，不需要 try-catch 或手动返回 `ApiResponse.badRequest(...)`。

## 可用异常

| 异常 | 前端 code | 用途 |
|---|---|---|
| `BusinessException(message)` | 400 | 业务校验失败，`message` 透传到前端 |
| `UnauthorizedException(message)` | 401 | 未登录 / token 过期 |
| `ForbiddenException(message)` | 403 | 已认证但权限不足 |
| `@Valid` 校验失败 | 400 | 无需手动抛，框架自动处理 |

三个自定义异常都在 `com.lovelycatv.crystalframework.shared.exception` 包，均继承 `RuntimeException`。

## 使用示例

### 业务异常

```kotlin
@Service
class UserService(...) {
    suspend fun changePassword(userId: Long, newPassword: String) {
        if (newPassword.length < 8) {
            throw BusinessException("Password must be at least 8 characters")
        }
        // ...
    }
}
```

`message` 会原样出现在前端 `ApiResponse.message` 中，应使用面向用户的文案，避免系统内部术语。

### 权限异常

```kotlin
if (approval.initiatorId != userAuthentication.userId) {
    throw ForbiddenException("You can only cancel your own approvals")
}
```

### 未认证异常

`UnauthorizedException` 仅在自定义认证逻辑中使用。框架级登录校验由 `CustomAuthFilter` 处理，业务代码通常无需接触。

### 参数校验

```kotlin
class ManagerCreateUserDTO(
    @field:NotBlank(message = "username is required")
    val username: String = "",

    @field:Email(message = "invalid email format")
    val email: String = "",
)

@PostMapping("/create")
suspend fun create(
    @ModelAttribute @Valid dto: ManagerCreateUserDTO
): ApiResponse<*> {
    // 校验失败由框架直接返回 400，方法体不会被调用
    userService.create(dto)
    return ApiResponse.success(null)
}
```

违反 `@field:NotBlank` / `@field:Email` / `@field:Min` 等约束时，框架自动拼接字段错误信息作为 `message` 返回。

## 限制

- 禁止在业务代码中自行 try-catch 后返回 `ApiResponse`——框架已统一处理
- 禁止在业务代码中写 `logger.error("...", e)`——全局处理器已按级别打印
- 禁止抛出 `RuntimeException` 或未定义异常——会被识别为 500 内部错误，前端展示不友好

## 自动映射清单

| 场景 | 对应异常 | code |
|---|---|---|
| DTO 字段校验失败 | `WebExchangeBindException`（自动） | 400 |
| `@RequestParam` 缺失 | `MissingRequestValueException`（自动） | 400 |
| `@PreAuthorize` 拦截 | `AuthorizationDeniedException`（自动） | 403 |
| 数据库唯一键冲突 | `DuplicateKeyException`（自动） | 400 |
| 其他未捕获异常 | `Exception` 兜底 | 500 |

以上均为框架自动映射，业务代码只需按需抛 `BusinessException` / `ForbiddenException`。

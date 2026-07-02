# ApiResponse

`ApiResponse` 是框架所有 Controller 方法的统一返回类型。前端通过响应体的 `code` 字段判断请求结果，不依赖 HTTP 状态码。

## 结构

```kotlin
data class ApiResponse<T>(
    val code: Int,       // 业务状态码，对应常见 HTTP 状态码
    val message: String, // 提示信息
    val data: T?         // 响应数据，可为 null
)
```

## 工厂方法

| 方法 | code | 用途 |
|---|---|---|
| `ApiResponse.success(data)` | 200 | 正常返回 |
| `ApiResponse.success(null)` | 200 | 无数据的成功（create / update / delete） |
| `ApiResponse.badRequest(message)` | 400 | 参数或业务校验不通过 |
| `ApiResponse.unauthorized(message)` | 401 | 未登录或 token 失效 |
| `ApiResponse.forbidden(message)` | 403 | 无权限 |
| `ApiResponse.internalServerError(message)` | 500 | 服务端未预期异常 |

## 使用示例

### 有数据的成功

```kotlin
@GetMapping("/profile")
suspend fun getProfile(userAuthentication: UserAuthentication): ApiResponse<*> {
    val profile = userService.getProfile(userAuthentication.userId)
    return ApiResponse.success(profile)
}
```

### 无数据的成功

```kotlin
@PostMapping("/mark-read")
suspend fun markAsRead(
    userAuthentication: UserAuthentication,
    @RequestParam id: Long,
): ApiResponse<*> {
    notificationService.markAsRead(id)
    return ApiResponse.success(null)
}
```

### 失败：抛异常而非手动返回

框架的 `GlobalExceptionHandler` 自动将 `BusinessException` / `ForbiddenException` / `UnauthorizedException` 转换为对应的 `ApiResponse`。业务代码中失败路径直接抛异常即可：

```kotlin
if (email.isBlank()) {
    throw BusinessException("Email is required")
}
```

详见 [异常处理](./exception-handling)。

::: warning 返回类型必须显式声明
Controller 方法必须显式声明 `: ApiResponse<*>`，不允许使用 `: Any`、返回裸对象或省略返回类型。前端 `doGet` / `doPost` 依据 `code` / `message` / `data` 三字段解析响应。
:::

## 数据类型建议

- 业务方法可以精确指定 `data` 类型，如 `ApiResponse<UserVO>`，向前端传递更明确的类型信息
- 基类方法签名统一使用 `ApiResponse<*>`（星投影），子类无需为了统一而向下具体化
- `data` 类型为 `T?`，`ApiResponse.success(null)` 序列化后 JSON 中的 `data` 字段值为 `null`（字段保留）

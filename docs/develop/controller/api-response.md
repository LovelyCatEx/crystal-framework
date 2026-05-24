# ApiResponse

## 概述

`ApiResponse` 是框架统一的 API 响应封装，所有 Controller 方法均返回此类型。前端通过 `code` 判断请求结果，无需解析 HTTP 状态码。

## 结构

```kotlin
data class ApiResponse<T>(
    val code: Int,      // 业务状态码
    val message: String, // 提示信息
    val data: T?         // 响应数据（可为 null）
)
```

## 工厂方法

| 方法 | code | 说明 |
|------|------|------|
| `success(data)` | 200 | 操作成功 |
| `badRequest(message)` | 400 | 参数错误或业务校验不通过 |
| `unauthorized(message)` | 401 | 未登录或 token 失效 |
| `forbidden(message)` | 403 | 无权限访问 |
| `internalServerError(message)` | 500 | 服务端异常 |

### 使用示例

```kotlin
// 成功，带数据
@GetMapping("/profile")
suspend fun getProfile(): ApiResponse<*> {
    val profile = userService.getProfile()
    return ApiResponse.success(profile)
}

// 成功，无数据
@PostMapping("/create")
suspend fun create(@RequestBody body: Map<String, Any>): ApiResponse<*> {
    service.create(body)
    return ApiResponse.success(null)
}

// 参数错误
@PostMapping("/register")
suspend fun register(@RequestBody body: RegisterDTO): ApiResponse<*> {
    if (body.email.isBlank()) {
        return ApiResponse.badRequest("Email is required")
    }
    // ...
}
```

## 关键点

- Controller **总是**声明返回值类型为 `ApiResponse<*>`（星投影）
- 成功路径使用 `ApiResponse.success(data)`，错误路径请直接抛异常（详见[异常处理](./exception-handling)）
- `data` 为 null 时传 `null` 而非 `Unit` 或 `Any()`

# ApiResponse

## 设计意图

`ApiResponse` 是 API 响应的唯一外观。框架故意不提供 `ResponseBodyAdvice` 之类的透明包装机制，所有 Controller 方法必须显式声明 `: ApiResponse<*>`。理由：

- 返回类型在源码里可见，无需查框架配置即可知道客户端拿到什么
- 泛型参数 `T` 保留 data 字段的具体类型，前端 TypeScript 类型定义可直接对应后端签名
- 前端跨 HTTP 状态码统一解析（后端遇到 500 也返回 HTTP 200 + `code: 500` 的响应体），HTTP 状态码只对基础设施层（网关、CDN、监控）有意义

## 源码

位于 `crystal-shared` 的 `com.lovelycatv.crystalframework.shared.response.ApiResponse`：

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
) {
    companion object {
        const val SUCCESS_CODE = 200
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val INTERNAL_SERVER_ERROR_CODE = 500

        fun <T> success(data: T?, message: String = "success") =
            ApiResponse(SUCCESS_CODE, message, data)

        fun <T> unauthorized(message: String, data: T? = null) =
            ApiResponse(UNAUTHORIZED, message, data)

        fun <T> forbidden(message: String, data: T? = null) =
            ApiResponse(FORBIDDEN, message, data)

        fun <T> badRequest(message: String, data: T? = null) =
            ApiResponse(BAD_REQUEST, message, data)

        fun <T> internalServerError(message: String, data: T? = null) =
            ApiResponse(INTERNAL_SERVER_ERROR_CODE, message, data)
    }
}
```

## 关键设计决策

### `data class` 而非普通类

使用 `data class` 自动获得 `equals` / `hashCode` / `toString` / `copy`，便于测试和调试。`copy(data = ...)` 在包装第三方 SDK 返回值时特别有用。

### `T?` 而非 `T`

`data: T?` 使用可空类型，`ApiResponse.success(null)` 序列化后 `data` 字段值为 `null`（JSON 保留字段但值为 null）。这是刻意的：前端类型定义 `data: T | null` 总能拿到字段，无需处理"字段缺失"和"字段为 null"两种情况。

### 与 HTTP 状态码解耦

`code` 使用 HTTP 状态码语义（200/400/401/403/500），但实际 HTTP 响应恒为 200（除非发生网络层错误由 `AbstractErrorWebExceptionHandler` 兜底）。理由：

- 前端只有一处 code 判断逻辑（`request.ts`），无需同时处理 HTTP 状态码和业务码
- 中间件、日志、监控看到的都是 200，业务失败不会污染基础设施层的错误统计
- 扩展新业务码（如引入 `429` 限流）不影响运维体系

## 与 GlobalExceptionHandler 的配合

Controller 抛异常后，`GlobalExceptionHandler`（`@RestControllerAdvice`）调用 `ApiResponse` 对应的工厂方法转换：

```
BusinessException          → ApiResponse.badRequest(e.message)
ForbiddenException         → ApiResponse.forbidden(e.message)
UnauthorizedException      → ApiResponse.unauthorized(e.message)
WebExchangeBindException   → ApiResponse.badRequest("<拼接字段错误>")
其他 Exception             → ApiResponse.internalServerError(...)
```

详见 [异常处理](./exception-handling)。

## 现有调用位置

- 所有 `crystal-shared/controller` 下的基类（Standard / Readonly / Scoped 家族）
- 所有子类 Controller 的 `custom*` 端点
- `GlobalExceptionHandler` 的所有 `@ExceptionHandler` 方法
- `GlobalErrorWebExceptionHandler` 的路由级兜底

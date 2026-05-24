# ApiResponse

## 概述

`ApiResponse` 是框架统一的 API 响应封装，位于 `crystal-shared` 模块的 `com.lovelycatv.crystalframework.shared.response` 包下。它与 `GlobalExceptionHandler` 配合构成了完整的响应体系。

## 源码分析

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

- 使用 `data class`，Kotlin 自动生成 `equals`、`hashCode`、`toString`、`copy`
- `T` 泛型参数使序列化时保留 data 字段的具体类型信息
- `data: T?` 为可空，失败响应可只传 message

## 设计要点

- **不自动包装**：框架没有 `ResponseBodyAdvice` 自动将返回值包装为 `ApiResponse`，Controller 必须显式返回 `ApiResponse<*>`，使返回类型在接口签名中清晰可见
- **data 为 null**：`data: T?` 使用可空类型，`ApiResponse.success(null)` 序列化后 `data` 字段为 `null`，而不是缺失该字段

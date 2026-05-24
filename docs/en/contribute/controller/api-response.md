# ApiResponse

## Overview

`ApiResponse` is the framework's unified response type, defined in the `crystal-shared` module under `com.lovelycatv.crystalframework.shared.response`. Together with `GlobalExceptionHandler`, it forms a complete response pipeline.

## Source Analysis

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

- `data class` provides `equals`, `hashCode`, `toString`, `copy` for free
- The generic `T` preserves concrete type information through serialization
- `data: T?` is nullable — error responses can omit data entirely

## Design Decisions

- **No auto-wrapping**: The framework does not use `ResponseBodyAdvice` to automatically wrap return values. Controllers must explicitly return `ApiResponse<*>`, making the return type visible in the interface signature.
- **Nullable data**: `data: T?` means `ApiResponse.success(null)` serializes to `"data": null` rather than omitting the field entirely.

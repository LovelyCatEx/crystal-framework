# ApiResponse

## Design intent

`ApiResponse` is the sole facade for API responses. The framework deliberately avoids transparent wrapping mechanisms such as `ResponseBodyAdvice`; every Controller method must explicitly declare `: ApiResponse<*>`. Rationale:

- The return type is visible in source — clients can be understood without consulting framework config
- The `T` generic preserves the specific data-field type, letting frontend TypeScript definitions mirror backend signatures directly
- The frontend parses across HTTP status codes uniformly (a backend 500 still comes out as HTTP 200 + `code: 500` in the body); HTTP status codes only matter to infrastructure (gateways, CDN, monitoring)

## Source

Located at `crystal-shared`'s `com.lovelycatv.crystalframework.shared.response.ApiResponse`:

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

## Key design decisions

### `data class` instead of a plain class

Using `data class` auto-generates `equals` / `hashCode` / `toString` / `copy`, which helps testing and debugging. `copy(data = ...)` is particularly useful when wrapping third-party SDK responses.

### `T?` instead of `T`

`data: T?` uses a nullable type; `ApiResponse.success(null)` serializes to `"data": null` (JSON field preserved with value `null`). Intentional: frontend types `data: T | null` always see the field, avoiding a distinction between "missing field" and "null field".

### Decoupled from HTTP status codes

`code` uses HTTP-status-code semantics (200/400/401/403/500), but the actual HTTP response is always 200 (unless a network-layer error is caught by `AbstractErrorWebExceptionHandler` as a last resort). Reasons:

- Frontend has one code-check path (in `request.ts`); it does not juggle HTTP codes and business codes
- Middleware, logs, and monitoring all see 200 — business failures don't pollute infra-level error metrics
- New business codes (e.g. `429` for rate limiting) can be added without ripple through ops

## Coordination with GlobalExceptionHandler

When a Controller throws, `GlobalExceptionHandler` (`@RestControllerAdvice`) calls the corresponding `ApiResponse` factory:

```
BusinessException          → ApiResponse.badRequest(e.message)
ForbiddenException         → ApiResponse.forbidden(e.message)
UnauthorizedException      → ApiResponse.unauthorized(e.message)
WebExchangeBindException   → ApiResponse.badRequest("<concatenated field errors>")
Other Exception            → ApiResponse.internalServerError(...)
```

See [Exception Handling](./exception-handling).

## Known call sites

- All base classes under `crystal-shared/controller` (Standard / Readonly / Scoped families)
- All `custom*` endpoints on subclass Controllers
- Every `@ExceptionHandler` method in `GlobalExceptionHandler`
- The routing-level fallback in `GlobalErrorWebExceptionHandler`

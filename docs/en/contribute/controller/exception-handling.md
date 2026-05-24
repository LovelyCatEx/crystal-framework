# Exception Handling

## Design Overview

Exception handling consists of two layers: `GlobalExceptionHandler` (`@RestControllerAdvice`) catches regular controller exceptions, and `GlobalErrorWebExceptionHandler` (`AbstractErrorWebExceptionHandler`) serves as a fallback for routing-level errors. Both convert exceptions into `ApiResponse`.

## GlobalExceptionHandler Source Analysis

Located in the `crystal-shared` module under `com.lovelycatv.crystalframework.shared.exception`:

```kotlin
@Component
@RestControllerAdvice
class GlobalExceptionHandler(private val auditEventRepository: AuditEventRepository) {

    fun handle(e: Exception): ApiResponse<*> = when (e) {
        is ForbiddenException -> handleForbiddenException(e)
        is UnauthorizedException -> handleUnauthorizedException(e)
        is BusinessException -> handleBusinessException(e)
        is MissingRequestValueException -> handleMissingRequestValueException(e)
        is AuthorizationDeniedException -> handleAuthorizationDeniedException(e)
        is WebExchangeBindException -> handleWebExchangeBindException(e)
        else -> handleException(e)
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> =
        ApiResponse.badRequest(e.localizedMessage ?: e.message ?: "bad request")
    // ... other handlers follow the same pattern
}
```

Exception-to-Response mapping:

| Exception | Method | code |
|-----------|--------|------|
| `BusinessException` | `ApiResponse.badRequest()` | 400 |
| `ForbiddenException` | `ApiResponse.forbidden()` | 403 |
| `UnauthorizedException` | `ApiResponse.unauthorized()` | 401 |
| `AuthorizationDeniedException` | `ApiResponse.forbidden()` | 403 |
| `WebExchangeBindException` | `ApiResponse.badRequest()` (concatenated field errors) | 400 |
| `MissingRequestValueException` | `ApiResponse.badRequest()` (identifies missing parameter) | 400 |
| Other `Exception` | `ApiResponse.internalServerError()` | 500 |

Two notable design choices:

- **`handle()` is public**: `GlobalErrorWebExceptionHandler` calls this method directly, reusing the same conversion logic instead of duplicating it
- **Log level by exception type**: Known business exceptions log at `debug`, unexpected ones at `error` — reducing noise from expected failures

## GlobalErrorWebExceptionHandler Source Analysis

```kotlin
@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    ...
    private val globalExceptionHandler: GlobalExceptionHandler
) : AbstractErrorWebExceptionHandler(...) {
    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request ->
            val error = getError(request)
            when (error) {
                is Exception -> ServerResponse.status(HttpStatus.OK)
                    .bodyValue(globalExceptionHandler.handle(error))
                else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .bodyValue(ApiResponse.internalServerError(...))
            }
        }
    }
}
```

`AbstractErrorWebExceptionHandler` is Spring WebFlux's last-resort error handler, covering:

- Route mismatches (404)
- Exceptions thrown in filters
- Scenarios not caught by `GlobalExceptionHandler`

`@Order(-2)` gives it higher priority than Spring Boot's default error handler.

## Exception Classes

| Class | Module | Purpose |
|-------|--------|---------|
| `BusinessException` | `crystal-shared/exception` | Business validation failure |
| `ForbiddenException` | `crystal-shared/exception` | No permission |
| `UnauthorizedException` | `crystal-shared/exception` | Not authenticated |

All three extend `RuntimeException` and carry a custom message.

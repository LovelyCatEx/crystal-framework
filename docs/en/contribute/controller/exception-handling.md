# Exception Handling

## Design overview

Exception handling has two defense lines:

1. `GlobalExceptionHandler` (`@RestControllerAdvice`) catches exceptions thrown from Controller methods
2. `GlobalErrorWebExceptionHandler` (`AbstractErrorWebExceptionHandler`) handles routing-level errors — unmatched routes, exceptions from Filters, cases not covered by `GlobalExceptionHandler`

Both end up calling `ApiResponse` factory methods to produce uniform responses.

## GlobalExceptionHandler source

Located at `crystal-shared/exception/GlobalExceptionHandler.kt`:

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

    // Each concrete handler is re-exposed via @ExceptionHandler
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ApiResponse<*> = ...
}
```

### Dual exposure: `when` dispatch + `@ExceptionHandler`

Each handler is invoked both by the `when` in `handle()` and exposed via `@ExceptionHandler`. Not duplication — this lets `GlobalErrorWebExceptionHandler` reuse the mapping:

- `@RestControllerAdvice` can only catch exceptions thrown from within a **method invocation chain**
- Exceptions during Filter, routing, and authentication phases never reach `@ExceptionHandler`
- `GlobalErrorWebExceptionHandler` catches those and calls `handle(error as Exception)` directly, reusing the same mapping table

### Exception mapping table

| Exception | Conversion | code | Log level |
|---|---|---|---|
| `BusinessException` | `ApiResponse.badRequest()` | 400 | debug |
| `ForbiddenException` | `ApiResponse.forbidden()` | 403 | debug |
| `UnauthorizedException` | `ApiResponse.unauthorized()` | 401 | debug |
| `AuthorizationDeniedException` | `ApiResponse.forbidden()` | 403 | debug |
| `WebExchangeBindException` | `ApiResponse.badRequest()` (joins field errors) | 400 | debug |
| `MissingRequestValueException` | `ApiResponse.badRequest()` (names the missing param) | 400 | debug |
| `ServerWebInputException` | `ApiResponse.badRequest()` | 400 | debug |
| `DuplicateKeyException` | `ApiResponse.badRequest("duplicate resource id")` | 400 | debug |
| Other `Exception` | `ApiResponse.internalServerError()` | 500 | error |

### Log-level rationale

Known business exceptions log at debug; unknown exceptions log at error. This prevents high-volume `BusinessException` traffic from drowning ops alerts — only unexpected exceptions trigger dashboards.

## GlobalErrorWebExceptionHandler source

```kotlin
@Component
@Order(-2)
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext,
    private val globalExceptionHandler: GlobalExceptionHandler
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request ->
            val error = getError(request)
            when (error) {
                is Exception -> ServerResponse.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(globalExceptionHandler.handle(error))

                else -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ApiResponse.internalServerError<Nothing>(...))
            }
        }
    }
}
```

### Key points

- `@Order(-2)`: higher priority than Spring Boot's default `DefaultErrorWebExceptionHandler` (default `-1`), ensuring uniform business response format
- Returns HTTP 200 + JSON body: even on business errors, HTTP status stays 200 — the frontend only checks `code`
- `else` branch returns HTTP 500: `getError(request)` may return something that isn't an `Exception` (e.g. `Error`); those severe cases genuinely deserve HTTP 500 for monitoring
- Reuses `globalExceptionHandler.handle(...)`: mapping logic stays in `GlobalExceptionHandler`; this class only forwards

## Exception class structure

The three custom exceptions live in `crystal-shared.exception`:

```kotlin
// BusinessException
open class BusinessException(
    message: String = "",
    cause: Exception? = null
) : RuntimeException(message, cause)

// UnauthorizedException / ForbiddenException share the same shape
```

Design notes:

- `open` rather than `final`: business modules can subclass into more specific exceptions (e.g. `TenantMemberQuotaExceededException`)
- `message` defaults to empty string: never null, so handler fallbacks `?: e.message ?: "..."` stay deterministic
- `cause: Exception?`: convenient for wrapping lower-level exceptions while preserving the stack chain

## Coupling with other infrastructure

- `ManagerControllerPermissionAspect` throws `AuthorizationDeniedException`, converted to 403 by `handleAuthorizationDeniedException`
- Spring Security auth failures also route through `AuthorizationDeniedException`
- `@Valid` + JSR-303 failures throw `WebExchangeBindException`; `handleWebExchangeBindException` concatenates each field's `defaultMessage`
- R2DBC unique-key violations throw `DuplicateKeyException`, converted to `"duplicate resource id"` 400

## Known usage locations

- Globally registered as `@Component` + `@RestControllerAdvice`; business code does not reference it
- `GlobalErrorWebExceptionHandler` is also `@Component`, auto-wired into Spring Boot's WebFlux error chain
- Business code only decides which exception to throw

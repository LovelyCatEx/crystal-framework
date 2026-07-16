# ApiResponse

`ApiResponse` is the uniform return type for every Controller method in the framework. The frontend inspects the response body's `code` field to determine the outcome, rather than relying on the HTTP status code.

## Structure

```kotlin
data class ApiResponse<T>(
    val code: Int,       // Business status code, aligned with common HTTP codes
    val message: String, // Prompt message
    val data: T?         // Response data, nullable
)
```

## Factory methods

| Method | code | Usage |
|---|---|---|
| `ApiResponse.success(data)` | 200 | Normal return |
| `ApiResponse.success(null)` | 200 | Success without a payload (create / update / delete) |
| `ApiResponse.badRequest(message)` | 400 | Parameter or business validation failure |
| `ApiResponse.unauthorized(message)` | 401 | Not logged in or token invalid |
| `ApiResponse.forbidden(message)` | 403 | No permission |
| `ApiResponse.internalServerError(message)` | 500 | Unexpected server-side exception |

## Examples

### Success with data

```kotlin
@GetMapping("/profile")
suspend fun getProfile(userAuthentication: UserAuthentication): ApiResponse<*> {
    val profile = userService.getProfile(userAuthentication.userId)
    return ApiResponse.success(profile)
}
```

### Success without data

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

### Failure: throw rather than return manually

`GlobalExceptionHandler` auto-converts `BusinessException` / `ForbiddenException` / `UnauthorizedException` into the corresponding `ApiResponse`. On the failure path, business code simply throws:

```kotlin
if (email.isBlank()) {
    throw BusinessException("Email is required")
}
```

See [Exception Handling](./exception-handling).

::: warning Return type must be declared
Every Controller method must declare `: ApiResponse<*>`. Do not use `: Any`, return a bare object, or omit the return type. The frontend's `doGet` / `doPost` parses responses by the `code` / `message` / `data` triple.
:::

## Data type guidance

- Business methods may narrow the `data` type, e.g. `ApiResponse<UserVO>`, giving the frontend clearer type information
- Base classes declare `ApiResponse<*>` (star projection) as the uniform signature; subclasses do not need to narrow for consistency
- `data` is `T?`; `ApiResponse.success(null)` serializes to `"data": null` in JSON (field preserved)

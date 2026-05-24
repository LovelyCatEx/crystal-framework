# ApiResponse

## Overview

`ApiResponse` is the framework's unified API response wrapper. All controller methods return this type, and the frontend determines the result by inspecting `code` rather than parsing HTTP status codes.

## Structure

```kotlin
data class ApiResponse<T>(
    val code: Int,      // Business status code
    val message: String, // Human-readable message
    val data: T?         // Response payload (nullable)
)
```

## Factory Methods

| Method | code | Description |
|--------|------|-------------|
| `success(data)` | 200 | Operation succeeded |
| `badRequest(message)` | 400 | Invalid parameters or business validation failed |
| `unauthorized(message)` | 401 | Not logged in or token expired |
| `forbidden(message)` | 403 | Insufficient permissions |
| `internalServerError(message)` | 500 | Server-side error |

### Usage Examples

```kotlin
// Success with data
@GetMapping("/profile")
suspend fun getProfile(): ApiResponse<*> {
    val profile = userService.getProfile()
    return ApiResponse.success(profile)
}

// Success without data
@PostMapping("/create")
suspend fun create(@RequestBody body: Map<String, Any>): ApiResponse<*> {
    service.create(body)
    return ApiResponse.success(null)
}

// Bad request
@PostMapping("/register")
suspend fun register(@RequestBody body: RegisterDTO): ApiResponse<*> {
    if (body.email.isBlank()) {
        return ApiResponse.badRequest("Email is required")
    }
    // ...
}
```

## Key Points

- Always declare the return type as `ApiResponse<*>` (star-projection)
- Use `ApiResponse.success(data)` for success; throw exceptions for errors (see [Exception Handling](./exception-handling))
- Pass `null` explicitly when there is no data (not `Unit` or `Any()`)

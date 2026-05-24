# Exception Handling

## Overview

The framework catches all controller exceptions globally via `GlobalExceptionHandler` and converts them to `ApiResponse` automatically. Just throw the exception in your business code — no try-catch needed.

## Available Exceptions

| Exception | Resulting code | Description |
|-----------|---------------|-------------|
| `BusinessException(message)` | 400 | Business validation failed, frontend shows the message |
| `UnauthorizedException(message)` | 401 | Not logged in or token expired |
| `ForbiddenException(message)` | 403 | No permission |
| Parameter validation error | 400 | Triggered automatically by `@Valid`, no manual throw needed |

## Usage

### Business Exception

```kotlin
@Service
class MyService {
    fun getById(id: Long): Entity {
        val entity = repository.findById(id) ?: throw BusinessException("Entity not found")
        return entity
    }
}
```

### Permission Exception

```kotlin
if (!hasPermission) {
    throw ForbiddenException("You cannot access this resource")
}
```

### Parameter Validation

Use `@Valid` on DTO parameters — the framework handles validation errors automatically:

```kotlin
@PostMapping("/create")
suspend fun create(
    @ModelAttribute @Valid dto: CreateDTO
): ApiResponse<*> {
    // If validation fails, the framework returns 400 without entering this method
}
```

## Key Points

- Do NOT catch exceptions in controllers and return `ApiResponse.badRequest()` manually — just `throw BusinessException`
- The `message` of `BusinessException` is passed through to the frontend, so write user-facing messages
- Logging is handled by the framework — no need for `logger.error` in business code

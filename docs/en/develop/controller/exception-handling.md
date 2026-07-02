# Exception Handling

The framework's `GlobalExceptionHandler` catches every exception thrown by Controllers. On error paths, business code throws — no try-catch, no manual `ApiResponse.badRequest(...)`.

## Available exceptions

| Exception | Frontend `code` | Usage |
|---|---|---|
| `BusinessException(message)` | 400 | Business validation failed; `message` propagates to the frontend |
| `UnauthorizedException(message)` | 401 | Not logged in / token expired |
| `ForbiddenException(message)` | 403 | Authenticated but lacks permission |
| `@Valid` failure | 400 | No manual throw needed; framework auto-handles |

All three custom exceptions live in `com.lovelycatv.crystalframework.shared.exception` and extend `RuntimeException`.

## Examples

### Business exceptions

```kotlin
@Service
class UserService(...) {
    suspend fun changePassword(userId: Long, newPassword: String) {
        if (newPassword.length < 8) {
            throw BusinessException("Password must be at least 8 characters")
        }
        // ...
    }
}
```

`message` appears verbatim in the frontend's `ApiResponse.message`; use user-facing wording rather than internal jargon.

### Permission exceptions

```kotlin
if (approval.initiatorId != userAuthentication.userId) {
    throw ForbiddenException("You can only cancel your own approvals")
}
```

### Unauthenticated exceptions

`UnauthorizedException` applies only inside custom auth logic. Framework-level login checks live in `CustomAuthFilter` and never surface to business code.

### Parameter validation

```kotlin
class ManagerCreateUserDTO(
    @field:NotBlank(message = "username is required")
    val username: String = "",

    @field:Email(message = "invalid email format")
    val email: String = "",
)

@PostMapping("/create")
suspend fun create(
    @ModelAttribute @Valid dto: ManagerCreateUserDTO
): ApiResponse<*> {
    // Framework returns 400 on failure; the method body is not entered
    userService.create(dto)
    return ApiResponse.success(null)
}
```

When `@field:NotBlank` / `@field:Email` / `@field:Min` constraints fail, the framework composes the field-error `message` automatically.

## Restrictions

- Do not try-catch and translate to `ApiResponse` manually in business code — the framework already does that
- Do not write `logger.error("...", e)` in business code — the global handler already logs at the right level
- Do not throw `RuntimeException` or undefined exceptions — they become 500 Internal Server Error, degrading UX

## Auto-mapped cases

| Situation | Exception | code |
|---|---|---|
| DTO field validation failed | `WebExchangeBindException` (auto) | 400 |
| `@RequestParam` missing | `MissingRequestValueException` (auto) | 400 |
| `@PreAuthorize` denied | `AuthorizationDeniedException` (auto) | 403 |
| Unique-key conflict | `DuplicateKeyException` (auto) | 400 |
| Other uncaught | `Exception` fallback | 500 |

All auto-mapped by the framework — business code only throws `BusinessException` / `ForbiddenException` where relevant.

# Generic Controller

::: warning Return Type

**All controller methods must explicitly return `ApiResponse<*>`** using `ApiResponse.success(data)` or `ApiResponse.failed(message)`. Never return raw entities, List, Map, or other primitive types — the framework does not provide a uniform wrapping layer, and the frontend `doGet` / `doPost` rely on the `ApiResponse` structure.
:::

## Design Rationale

The framework does not force all controllers to extend a base class. When business logic cannot be expressed as CRUD templates (authentication, file uploads, multi-step operations), use a standard Spring `@RestController`.

## Relationship with ManagerController

Generic controllers and ManagerControllers are two orthogonal controller systems:

| | Generic Controller | ManagerController |
|--|--|--|
| Purpose | Business API | Admin CRUD |
| Route prefix | Free-form | `/manager/...` |
| Permission | `@PreAuthorize` | `@ManagerPermissions` + AOP |
| Audit | Manual or not needed | `ManagerControllerAuditAspect` auto-audit |
| UserAuthentication | Parameter injection (same) | Parameter injection (same) |

## Permission Check

### @PreAuthorize

Uses Spring Security SpEL with string constant references:

```kotlin
@PreAuthorize("hasAnyAuthority('\${TenantPermission.ACTION_TENANT_PROFILE_UPDATE_PEM}')")
@PostMapping("/update")
suspend fun updateTenantProfile(...): ApiResponse<*>
```

### @Unauthorized for Public Endpoints

Endpoints without `@PreAuthorize` still require a valid token. For fully anonymous access (login, registration, etc.), add `@Unauthorized` to the method:

```kotlin
@Unauthorized
@PostMapping("/register")
suspend fun register(...): ApiResponse<*> { ... }
```

`SecurityConfig` uses `UnauthorizedPathScanner` to scan all `@RestController` beans for `@Unauthorized` paths and adds them to `.permitAll()`. The `CustomAuthFilter` also skips JWT validation for these paths.

### hasAnyAuthority vs hasAuthority

The framework consistently uses `hasAnyAuthority` (plural), even for a single permission. This makes extending permissions later easier without changing annotations.

### RbacUtils

For conditional RBAC logic (e.g., field-level visibility based on permissions):

```kotlin
if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_PROFILE_READ_PEM)) {
    // full visibility
} else {
    // mask sensitive fields
}
```

## Parameter Injection

Generic controllers also support `UserAuthentication` parameter injection — it's handled by the framework's argument resolver, independent of base class inheritance.

```kotlin
@GetMapping("/profile")
suspend fun getProfile(
    userAuthentication: UserAuthentication,  // auto-injected
): ApiResponse<*>
```

## Route Conventions

- Business API: `/api/{version}/ext/{plugin-name}/...` (plugin extensions)
- Admin non-CRUD: `/api/{version}/manager/...` (built-in)
- Reference the prefix via `GlobalConstants.REQUEST_MAPPING_PREFIX`

::: tip DTO Usage
Custom controller request DTOs **should not extend** `BaseManagerReadDTO`, `BaseManagerCreateDTO`, `BaseManagerUpdateDTO`, or `BaseManagerDeleteDTO`. These four CRUD DTOs are designed exclusively for the `StandardManagerController` hierarchy. Use lighter base classes like `PageQuery` for custom endpoints.
:::

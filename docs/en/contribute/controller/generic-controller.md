# Generic Controller

## Design intent

The framework does not force every Controller to extend a base class. CRUD is covered by the 6 Manager-family bases; the rest (auth, upload, proxy, webhooks) uses plain Spring `@RestController`.

## Orthogonal to the Manager family

Generic Controllers and Manager Controllers are complementary, not substitutes:

| Dimension | Generic Controller | Manager Controller |
|---|---|---|
| Purpose | Business API | Admin CRUD |
| Path prefix | Free (`/api/{version}/ext/...` / `/api/{version}/oauth/...`) | Mandatory `/api/{version}/manager/...` |
| Permission mechanism | `@PreAuthorize` + Spring Security SpEL | `PermissionMatrix` (constructor arg, inline authorize check) |
| Audit aspect | Recorded explicitly with `@Audit` | The 5 standard methods are recorded automatically by `ManagerControllerAuditAspect`; custom methods use `@Audit` |
| Argument injection | `UserAuthentication` supported | `UserAuthentication` supported |
| Endpoints | Fully custom | 5 standard endpoints (overridable, extendable) |

## Audit log

Generic Controllers and custom methods in Manager Controllers must declare audit information explicitly with `@Audit`:

```kotlin
@Audit(
    action = AuditAction.READ,
    resourceType = TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES,
)
@PostMapping("/simulate")
suspend fun simulate(
    userAuthentication: UserAuthentication,
    @Valid @RequestBody dto: SimulateStorageProviderRoutingRuleDTO,
): ApiResponse<SimulationResultVO> {
    authorize(ManagerAction.READ, userAuthentication)
    return ApiResponse.success(managerService.simulate(dto))
}
```

`action` and `resourceType` are required; `resourceIds` is optional. `resourceIds` is a SpEL expression based on method parameter names, and its result must be a `Long` or `Collection<Long>`; leave it empty when no resource ID is needed. The method parameters must include `UserAuthentication`; otherwise the aspect skips recording.

The aspect reuses the request context and the shared `AuditLogService`, automatically recording the operator, request information, success state, and error message. The Manager Controller methods `create`, `read`, `readAll`, `update`, and `delete` are already recorded by `ManagerControllerAuditAspect`; do not add `@Audit` to them. Only extra custom methods need the explicit annotation.

## Permission check implementation

Generic Controllers use Spring Security's native SpEL evaluation path.

### @PreAuthorize + SpEL

```kotlin
@PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_CREATE}')")
```

- Spring Security evaluates SpEL before method invocation
- `hasAnyAuthority()` checks against `SecurityContext.authentication.authorities`
- Failure throws `AuthorizationDeniedException`, converted to 403 by `GlobalExceptionHandler.handleAuthorizationDeniedException`

### `${...}` instead of constant reference

`@PreAuthorize` requires a compile-time constant string; in Kotlin, embed a `const val` via the string template `"${...}"`. Do not hard-code literals.

### hasAnyAuthority vs hasAuthority

The framework standardizes on the plural `hasAnyAuthority`, even for single-permission cases. Reason: adding a permission is just appending an argument — no swap from `hasAuthority` to `hasAnyAuthority`.

### RbacUtils

Sometimes permission is not binary but shapes the response (field masking, filtering collections by permission). Use `RbacUtils.hasAuthority`:

```kotlin
if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_LIST_ALL_PEM)) {
    return allMembers
} else {
    return allMembers.map { it.masked() }
}
```

`RbacUtils` pulls current authorities from `ReactiveSecurityContextHolder`, sharing the same data source as `@PreAuthorize`.

## @Unauthorized machinery

Full signature:

```kotlin
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
annotation class Unauthorized(
    val reason: String = ""
)
```

`Target = ANNOTATION_CLASS + FUNCTION`: applies to methods, and can also be nested as a meta-annotation.

### UnauthorizedPathScanner

Boot-time scan (`crystal-auth/config/UnauthorizedPathScanner.kt`):

1. Iterate all `@RestController` beans in Spring
2. Reflect over each method for `@Unauthorized`
3. Combine class-level `@RequestMapping` with method-level `@GetMapping/@PostMapping` to build the full path
4. Push these paths onto a whitelist

### SecurityConfig consumes the whitelist

`SecurityConfig` reads the path list from `UnauthorizedPathScanner` and calls `.pathMatchers(...).permitAll()`. `CustomAuthFilter` also skips JWT validation for the same paths.

The two-layered whitelist covers both Spring Security's authorization chain and the JWT authentication chain, preventing any layer from blocking anonymous requests.

## Routing conventions

- Business API: `/api/{version}/ext/{plugin-name}/...` — plugin extensions
- Admin non-CRUD: `/api/{version}/manager/xxx/...` — built-in admin custom endpoints
- Authentication: `/api/{version}/oauth/...` / `/api/{version}/auth/...`
- Always reference `/api/{version}` via `GlobalConstants.REQUEST_MAPPING_PREFIX`; never hard-code

## Argument resolver

The `UserAuthentication` argument is populated by a custom `HandlerMethodArgumentResolver` reading from `ReactiveSecurityContextHolder`, producing a value object carrying `userId` / `tenantId` / `tenantMemberId` / `authorities`.

Not exclusive to any Controller base — any `@RestController` can inject it.

## Real usage locations

| Module | Controller | Purpose |
|---|---|---|
| `crystal-auth` | `UserAuthController` | Login, sign-up, password change |
| `crystal-auth` | `OAuthAccountController` | System-level OAuth accounts |
| `crystal-auth` | `TenantOAuthAccountController` | Tenant-level OAuth accounts |
| `crystal-resource` | `FileResourceController` | File upload / download |
| `crystal-approval` | `ApprovalFlowInstanceController` | Approval initiation + user form submit |

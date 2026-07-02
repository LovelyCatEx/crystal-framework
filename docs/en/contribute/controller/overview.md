# Controller Family Overview

The 6 Controller base classes in `crystal-shared` emerged as the framework's handling of multi-tenant and system-level resources evolved. This page traces their design intent and evolution path, to guide impact assessment when modifying Controller infrastructure under `shared`.

## Evolution path

```
StandardManagerController                          v1.0 seed, global CRUD baseline
  ├─ ReadonlyManagerController                     System-generated, immutable (logs)
  ├─ StandardTenantManagerController               pre-v1.4, tenant-only resource
  │                                                8 String permissions + isXxxInScope hooks
  │                                                Hard-codes tenantId as the sole scope
  └─ StandardScopedManagerController               v1.10, extracted ResourceScope abstraction
      │                                            Introduced ScopedPermissionTriad (12 permissions)
      │                                            Resource can live under SYSTEM or TENANT
      ├─ ReadonlyScopedManagerController           Read-only variant of the Scoped family
      │                                            Uses Triad.readonly() + NEVER_GRANTED fallback
      └─ StandardDerivedScopedManagerController    Resource has no scope column of its own
                                                   Scope derived from parent entity chain
                                                   No /list (no way to enumerate w/o parent)
```

`StandardTenantManagerController` predates `StandardScopedManagerController`. For new code that must work in both SYSTEM and TENANT scopes, prefer the Scoped family.

## Positioning per base class

| Base class | Key abstraction | Data model | Permission model |
|---|---|---|---|
| `StandardManagerController` | Generic chain + AOP permissions | Global resource, no scope | `@ManagerPermissions` 5 string arrays, OR semantics |
| `ReadonlyManagerController` | Extends Standard with 3 method overrides | Same, read-only specialization | Same |
| `StandardScopedManagerController` | `BaseScopedEntity` with explicit scope columns | Entity carries `scope` + `scopeId` | `ScopedPermissionTriad` 3-layer × 4-op |
| `ReadonlyScopedManagerController` | Extends Scoped with 3 method overrides | Same as Scoped, read-only | `Triad.readonly(...)` + `NEVER_GRANTED` fallback |
| `StandardDerivedScopedManagerController` | 3 abstract `resolveScopeFromXXX` hooks | Scope resolved via parent chain | Same as Scoped |
| `StandardTenantManagerController` | 8 String permissions + `isXxxInScope` hooks | tenantId hard-locked as scope | Two-layer permissions (system + scoped) |

## Three generations of the permission model

### Generation 1: @ManagerPermissions (AOP)

`StandardManagerController` uses class annotation + AOP interception.

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? { ... }
}
```

Notes:

- Pointcut hard-wired to `StandardManagerController.*(..)`, covering Standard and Readonly (subclass)
- Matches by method name via reflection (`readAll` / `read` / `create` / `update` / `delete`)
- `AopUtils.getTargetClass` sees through CGLIB proxies; `AnnotationUtils.findAnnotation` supports annotation inheritance

Limitation: an array can only declare a static permission list, and cannot express "pick the permission based on runtime scope". This motivated the Scoped family's switch to Triad.

### Generation 2: ScopedPermissionTriad (constructor arg)

The Scoped family packs 12 permissions into a `ScopedPermissionTriad` data class:

```
super × CRUD      Cross-scope admin authority
system × CRUD     SYSTEM scope only
tenantPem × CRUD  TENANT scope only
```

Match rule:

```kotlin
SYSTEM scope → hasAnyAuthority(super<op>, system<op>)
TENANT scope → hasAnyAuthority(super<op>, tenantPem<op>)
```

The `NEVER_GRANTED` constant is the deliberate fallback. `Triad.readonly(...)` fills only the read slots and stuffs `"!!never_granted!!"` into every other CRUD slot — a string that is not a real permission, not part of `SystemPermission`, and not auto-granted to `root`. If any caller bypasses `ReadonlyScopedManagerController` and looks up `triad.superFor(CREATE)`, the result still fails to match, preventing the read-only wrapper from silently leaking write permission.

### Generation 3: 8 String parameters (legacy Tenant design)

`StandardTenantManagerController` predates Triad and has a flatter permission model:

```kotlin
createPermission,      scopedCreatePermission,     // system + tenant
readPermission,        scopedReadPermission,
updatePermission,      scopedUpdatePermission,
deletePermission,      scopedDeletePermission,
```

Match rule: check system-level → check scoped-level → 403. Missing here is the "cross-scope admin" (super) layer — `super` gets folded into the system slot, meaning cross-tenant operations require system-wide permissions. This friction motivated the Triad design in the Scoped family.

`DISABLED_SCOPED_PERMISSION = ""` disables tenant-scoped access entirely, restricting the endpoint to system-level callers.

## AOP interception vs explicit checks

| Family | Permission check style | When |
|---|---|---|
| Standard / Readonly | AOP aspect (`ManagerControllerPermissionAspect`) | Before method invocation |
| Scoped / DerivedScoped / ReadonlyScoped | Inline `assertAccess` | First line of endpoint |
| Tenant | Inline `RbacUtils.hasAuthority` | Inside endpoint body |

The Scoped family avoids AOP because permission checks depend on `scope` and `scopeId` fields inside the DTO. The aspect cannot access the typed DTO — deserialization happens after; `update` / `delete` also require DB queries. Inline "explicit check + `checkPermission` / `checkOwnership` hooks" fits better.

The Standard family is the opposite — the permission list is statically readable on the class annotation, and AOP is the lightest option.

## Generic chain

Using Standard as an example:

```
StandardManagerController
  <SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
   REPOSITORY : BaseRepository<ENTITY>,
   ENTITY : BaseEntity,
   CREATE_DTO : Any,
   READ_DTO : BaseManagerReadDTO,
   UPDATE_DTO : BaseManagerUpdateDTO,
   DELETE_DTO : BaseManagerDeleteDTO>
```

7 type parameters build a complete chain: Service constrains Repository / Entity / 4 DTOs, and Controller re-constrains the same set of types. Consequences:

- When a subclass Controller declares `<..., ..., ManagerXxxCreateDTO, ...>`, the compiler enforces alignment with the Service's CREATE parameter
- IDE completion resolves the full field/method surface
- Inside `managerService.create(dto)`, `dto` is typed as `CREATE_DTO` — no unsafe cast

The Scoped family adds `ENTITY : BaseScopedEntity`; DerivedScoped uses the union constraint `where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>`; Tenant uses `where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long>`.

## Coupling with other infrastructure

- `GlobalExceptionHandler` — converts thrown exceptions into `ApiResponse`
- `ManagerControllerAuditAspect` (in `crystal-audit`) — same pointcut, `@Order` runs after the permission aspect, records CRUD audit trail
- `ReactiveSecurityContextHolder` — propagates `Authentication` through the WebFlux reactor context; the `UserAuthentication` argument is extracted by an `ArgumentResolver`
- `UnauthorizedPathScanner` — scans `@Unauthorized` at boot and adds those paths to `permitAll()`

## Real usage locations

| Family | Module | Controller |
|---|---|---|
| Standard | `crystal-resource` | `ManagerStorageProviderController`, `ManagerFileResourceController` |
| Standard | `crystal-rbac` | `ManagerUserRoleController`, `ManagerUserPermissionController` |
| Readonly | `crystal-audit` | `ManagerAuditLogController` |
| Readonly | `crystal-mail` | `ManagerMailSendLogController` |
| Readonly | `crystal-auth` | `ManagerUserLoginLogController` |
| Scoped | `crystal-tenant` | `ManagerTenantDictTypeController` |
| Scoped | `crystal-approval` | `ManagerApprovalFlowDefinitionController` |
| DerivedScoped | `crystal-tenant` | `ManagerTenantDictItemController` (walks `typeId` → parent's scope) |
| ReadonlyScoped | `crystal-approval` | `ManagerApprovalFlowInstanceController`, `ManagerApprovalFlowTaskController` |
| Tenant | `crystal-rbac` | `ManagerTenantRoleController` |
| Tenant | `crystal-tenant` | `ManagerTenantMemberController`, `ManagerTenantDepartmentController`, `ManagerTenantDepartmentMemberController`, `ManagerTenantMessageChannelController` |

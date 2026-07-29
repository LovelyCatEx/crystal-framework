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

Unified to PermissionMatrix (current): the three permission models (@ManagerPermissions
/ ScopedPermissionTriad / 8 Strings) merge into one 4-layer × 4-operation = 16-permission
`PermissionMatrix` data class; every base receives it via constructor arg `permissions = ...`.
`ScopedPermissionMatrix` stays as a typealias; `StandardTenantManagerController` keeps a
compat constructor — both `@Deprecated`, one release, to guide migration.
```

`StandardTenantManagerController` predates `StandardScopedManagerController`. For new code that must work in both SYSTEM and TENANT scopes, prefer the Scoped family.

## Positioning per base class

| Base class | Key abstraction | Data model | Permission declaration (PermissionMatrix factory) |
|---|---|---|---|
| `StandardManagerController` | Generic chain + `authorize` method | Global resource, no scope | `PermissionMatrix.systemOnly(...)` |
| `ReadonlyManagerController` | Extends Standard + `Mutability.READ_ONLY` | Same, read-only specialization | `PermissionMatrix.systemOnlyReadonly(...)` |
| `StandardScopedManagerController` | `BaseScopedEntity` with explicit scope columns | Entity carries `scope` + `scopeId` | `PermissionMatrix.of { ... }` |
| `ReadonlyScopedManagerController` | Extends Scoped + `Mutability.READ_ONLY` | Same as Scoped, read-only | `PermissionMatrix.readonly(...)` (16 CUD slots `NEVER_GRANTED`) |
| `StandardDerivedScopedManagerController` | 3 abstract `resolveScopeFromXXX` hooks | Scope resolved via parent chain | `PermissionMatrix.of { ... }` |
| `StandardTenantManagerController` | `isXxxInScope` hooks + `preflight` | tenantId hard-locked as scope | `PermissionMatrix.tenantOnly(...)` |

## Four generations of the permission model

### Generation 1: @ManagerPermissions (AOP, deprecated)

`StandardManagerController` originally used class annotation + AOP interception:

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

Limitation: an array can only declare a static permission list, and cannot express "pick the permission based on runtime scope". That motivated first the Scoped family's Triad and eventually unification via `PermissionMatrix`. The AOP path is retained only as a legacy fallback when `permissions == null`, and now carries `@Deprecated`.

### Generation 2: ScopedPermissionTriad (constructor arg, deprecated)

The Scoped family used to pack 12 permissions into a `ScopedPermissionTriad` data class:

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

Limitation: no "TENANT but cross-tenant" layer — a SYSTEM admin editing tenant data had to hold `super*`, conflating "cross-scope" with "cross-tenant ops". The `ScopedPermissionTriad` type is now merged into `PermissionMatrix`; the `ScopedPermissionMatrix` typealias is retained one release with `@Deprecated`.

### Generation 3: 8 String parameters (legacy Tenant design, deprecated)

`StandardTenantManagerController` predates Triad and has a flatter permission model:

```kotlin
createPermission,      scopedCreatePermission,     // system + tenant
readPermission,        scopedReadPermission,
updatePermission,      scopedUpdatePermission,
deletePermission,      scopedDeletePermission,
```

Match rule: check system-level → check scoped-level → 403. Missing here is the "cross-scope admin" (super) layer. The legacy 8-String constructor is `@Deprecated` and delegates to `PermissionMatrix.tenantOnly(...)`; retained one release.

### Generation 4: PermissionMatrix (current)

`PermissionMatrix` (`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`) unifies the three declarations into 4 layers × 4 operations = 16 permissions:

| Layer          | authority prefix   | Constant source     | Semantics                                    |
|----------------|--------------------|---------------------|----------------------------------------------|
| `super`        | none               | `SystemPermission`  | Cross-scope super admin (SYSTEM + TENANT)     |
| `system`       | `system.`          | `SystemPermission`  | SYSTEM scope only                            |
| `tenantAdmin`  | `tenant.`          | `SystemPermission`  | TENANT scope, cross-tenant                   |
| `tenantPem`    | `i.tenant.`        | `TenantPermission`  | TENANT scope, strict tenantId match           |

Match rule (`layersFor(scope, op)`):

```
SYSTEM scope → hasAnyAuthority(super, system)
TENANT scope → hasAnyAuthority(super, tenantAdmin, tenantPem)
```

Key design points:

- **Two sentinel values**: `NOT_APPLICABLE` (layer doesn't apply, `layersFor` filters it out, excluded from decision) and `NEVER_GRANTED` (layer exists but the op is sealed, `layersFor` keeps it as a placeholder but nothing ever matches). Motivation matches the legacy `Triad.NEVER_GRANTED` — make error paths fail-safe rather than fail-open
- **Prefix convention**: `super*` may not carry a prefix; `system*` must start with `system.`; `tenantAdmin*` with `tenant.`; `tenantPem*` with `i.tenant.`. `PermissionMatrix.init` emits `logger.warn` on violation (will be promoted to a hard error), and `collectPrefixViolations(matrix)` supports strict testing / gating
- **DSL + 4 convenience factories**: `of { ... }` / `systemOnly(...)` / `tenantOnly(...)` / `readonly(...)` / `systemOnlyReadonly(...)`; unopened layers default to `NOT_APPLICABLE`
- **Compat layer**: the legacy `ScopedPermissionMatrix` typealias, the Tenant 8-String constructor, and the `@ManagerPermissions` AOP fallback are all retained one release with `@Deprecated`; migration is non-breaking

## Aspect interception vs explicit checks

| Family | Permission check style | When |
|---|---|---|
| Standard / Readonly | `StandardManagerController.authorize` OR-checks `matrix.layersFor(SYSTEM, op)` inline | At the top of every endpoint |
| Scoped / DerivedScoped / ReadonlyScoped | Inline `assertAccess` | First line of endpoint |
| Tenant | Inline `RbacUtils.hasAuthority`, dispatched via `PermissionMatrix.tenantAdminFor` / `tenantPemFor` | Inside endpoint body |

The Scoped family always used inline checks; after `PermissionMatrix` landed, Standard / Readonly also moved from AOP to the `authorize` method (AOP is kept as a fallback for legacy controllers still on `@ManagerPermissions`). All families now share typed-DTO-plus-typed-Matrix decisions; AOP survives only for compatibility.

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

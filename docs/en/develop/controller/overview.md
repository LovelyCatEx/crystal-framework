# Controller Family Overview

`crystal-shared` provides 6 manager-side CRUD Controller base classes covering different resource shapes. Choose the base class by resource characteristics before writing.

## Base class lookup

All bases declare permissions **uniformly through `PermissionMatrix`** (constructor arg `permissions = ...`). The legacy `@ManagerPermissions` class annotation, `ScopedPermissionTriad`, and 8-String constructor parameters are deprecated; see the [Permission Model Migration Guide](./permission-migration).

| Base class | Matrix convenience factory | Endpoints | Applicable resource |
|---|---|---|---|
| `StandardManagerController` | `PermissionMatrix.systemOnly(...)` | `list` / `create` / `query` / `update` / `delete` | Global CRUD without tenant/system distinction |
| `ReadonlyManagerController` | `PermissionMatrix.systemOnlyReadonly(...)` | `list` / `query` (mutations return 403) | System-generated, immutable log-like data |
| `StandardScopedManagerController` | `PermissionMatrix.of { ... }` | `list?scope&scopeId` / `create` / `query` / `update` / `delete` | Entity carries `scope` + `scopeId` columns |
| `ReadonlyScopedManagerController` | `PermissionMatrix.readonly(...)` | `list` / `query` (mutations return 403) | Read-only variant of the Scoped family |
| `StandardDerivedScopedManagerController` | `PermissionMatrix.of { ... }` + three abstract hooks | `create` / `query` / `update` / `delete` (no `list`) | Entity has no scope column; scope derived from parent |
| `StandardTenantManagerController` | `PermissionMatrix.tenantOnly(...)` | `list?tenantId` / `create` / `query` / `update` / `delete` | Scope hard-locked to tenantId |

## Selection guide

Read top to bottom, pick the first matching row:

| Condition | Choice |
|---|---|
| Not CRUD (login, upload, action trigger) | [Generic Controller](./generic-controller) |
| System-generated and user-immutable (logs, audit) | Global: [ReadonlyManagerController](./readonly-manager-controller); scoped: [ReadonlyScopedManagerController](./readonly-scoped-manager-controller) |
| Tenant-only (tenant role, tenant member) | [StandardTenantManagerController](./tenant-manager-controller) |
| Dual SYSTEM / TENANT scope | Has scope column: [StandardScopedManagerController](./scoped-manager-controller); derives from parent: [StandardDerivedScopedManagerController](./derived-scoped-manager-controller) |
| Plain global CRUD | [StandardManagerController](./standard-manager-controller) |

## Unified permission model (PermissionMatrix)

`PermissionMatrix` (`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`) is a data class with 4 layers × 4 operations = 16 authority fields. Passed to the base via `permissions = ...`; `layersFor(scope, op)` returns the set of authorities to OR-check.

### Four-layer semantics

| Layer          | authority prefix   | Constant source     | Semantics                                    |
|----------------|--------------------|---------------------|----------------------------------------------|
| `super`        | none               | `SystemPermission`  | Cross-scope super admin (SYSTEM + TENANT)     |
| `system`       | `system.`          | `SystemPermission`  | SYSTEM scope only                            |
| `tenantAdmin`  | `tenant.`          | `SystemPermission`  | TENANT scope, cross-tenant (ops admin)        |
| `tenantPem`    | `i.tenant.`        | `TenantPermission`  | TENANT scope, strict tenantId match           |

Match rule: SYSTEM requests OR-check `[super, system]`; TENANT requests OR-check `[super, tenantAdmin, tenantPem]`.

### Two sentinel values

- `PermissionMatrix.NOT_APPLICABLE` — the layer does not apply to the resource (e.g. tenant layers on a SYSTEM-only resource); `layersFor` filters it out
- `PermissionMatrix.NEVER_GRANTED` — the layer exists but the operation is sealed off (e.g. CUD on Readonly); `layersFor` keeps it but no real authority matches

### Quick start

```kotlin
// SYSTEM-only resource
permissions = PermissionMatrix.systemOnly(
    systemCreate = SystemPermission.ACTION_SYSTEM_XXX_CREATE,
    systemRead   = SystemPermission.ACTION_SYSTEM_XXX_READ,
    systemUpdate = SystemPermission.ACTION_SYSTEM_XXX_UPDATE,
    systemDelete = SystemPermission.ACTION_SYSTEM_XXX_DELETE,
)

// SYSTEM + TENANT dual-scope (Scoped / DerivedScoped family)
permissions = PermissionMatrix.of {
    `super`     { create = ...; read = ...; update = ...; delete = ... }
    system      { create = ...; read = ...; update = ...; delete = ... }
    tenantAdmin { create = ...; read = ...; update = ...; delete = ... }
    tenantPem   { create = ...; read = ...; update = ...; delete = ... }
}
```

`` `super` `` is a Kotlin keyword; the DSL requires backticks. Unopened layers default entirely to `NOT_APPLICABLE`.

### Factory lookup

| Factory                                    | For                       |
|--------------------------------------------|---------------------------|
| `PermissionMatrix.of { ... }`               | Full DSL, any combination |
| `PermissionMatrix.systemOnly(...)`          | Global CRUD without scope (Standard) |
| `PermissionMatrix.tenantOnly(...)`          | TENANT-only resources (Tenant) |
| `PermissionMatrix.systemOnlyReadonly(...)`  | SYSTEM-only + read-only   |
| `PermissionMatrix.readonly(...)`            | Read-only variant of Scoped |

Full migration steps, pitfalls, and FAQ live in the [Permission Model Migration Guide](./permission-migration).

## Required components

| Type | Requirement |
|---|---|
| Entity | Extend `BaseEntity`; Scoped family extends `BaseScopedEntity`; DerivedScoped / Tenant implement `ScopedEntity<Long>` |
| Repository | Extend `BaseRepository` |
| Service | Standard / Readonly: `CachedBaseManagerService`; Scoped family: `BaseScopedManagerService`; Tenant: `BaseTenantResourceManagerService` |
| DTO | 4 DTOs (Create / Read / Update / Delete); base classes vary by family — see per-base-class docs |

## Package structure

Manager-side code lives under `controller/manager/` and `service/manager/`:

```
your-module/
├── controller/
│   ├── manager/
│   │   ├── ManagerXxxController.kt
│   │   ├── dto/
│   │   │   ├── ManagerCreateXxxDTO.kt
│   │   │   ├── ManagerReadXxxDTO.kt
│   │   │   ├── ManagerUpdateXxxDTO.kt
│   │   │   └── ManagerDeleteXxxDTO.kt
│   │   └── vo/
│   └── xxx/
├── service/
│   └── manager/
│       ├── XxxManagerService.kt
│       └── impl/
│           └── XxxManagerServiceImpl.kt
└── entity/
    └── XxxEntity.kt
```

## Common rules

- Controller headers must carry `@Validated`, `@RestController`, `@RequestMapping`
- Every method's return type must be `ApiResponse<*>` (see [ApiResponse](./api-response))
- Controllers must not inject Repositories directly; DB operations go through Service
- Manager Controllers only inject Manager Services; generic Controllers only inject generic Services
- Request payloads named `XxxDTO` under `dto/`; response payloads named `XxxVO` under `vo/`
- Every `Long` field in DTO / VO carries `@get:JsonSerialize(using = ToStringSerializer::class)`; the frontend receives it as `string`
- Publicly accessible endpoints are explicitly annotated with `@Unauthorized`

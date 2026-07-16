# Controller Family Overview

`crystal-shared` provides 6 manager-side CRUD Controller base classes covering different resource shapes. Choose the base class by resource characteristics before writing.

## Base class lookup

| Base class | Permission mechanism | Endpoints | Applicable resource |
|---|---|---|---|
| `StandardManagerController` | `@ManagerPermissions` class annotation + AOP | `list` / `create` / `query` / `update` / `delete` | Global CRUD without tenant/system distinction |
| `ReadonlyManagerController` | Same, mutations overridden to 403 | `list` / `query` (mutations return 403) | System-generated, immutable log-like data |
| `StandardScopedManagerController` | `ScopedPermissionTriad` (12 permissions) | `list?scope&scopeId` / `create` / `query` / `update` / `delete` | Entity carries `scope` + `scopeId` columns |
| `ReadonlyScopedManagerController` | `ScopedPermissionTriad.readonly(...)` | `list` / `query` (mutations return 403) | Read-only variant of the Scoped family |
| `StandardDerivedScopedManagerController` | `ScopedPermissionTriad` + three abstract hooks | `create` / `query` / `update` / `delete` (no `list`) | Entity has no scope column; scope derived from parent |
| `StandardTenantManagerController` | 8 String constructor parameters | `list?tenantId` / `create` / `query` / `update` / `delete` | Scope hard-locked to tenantId |

## Selection guide

Read top to bottom, pick the first matching row:

| Condition | Choice |
|---|---|
| Not CRUD (login, upload, action trigger) | [Generic Controller](./generic-controller) |
| System-generated and user-immutable (logs, audit) | Global: [ReadonlyManagerController](./readonly-manager-controller); scoped: [ReadonlyScopedManagerController](./readonly-scoped-manager-controller) |
| Tenant-only (tenant role, tenant member) | [StandardTenantManagerController](./tenant-manager-controller) |
| Dual SYSTEM / TENANT scope | Has scope column: [StandardScopedManagerController](./scoped-manager-controller); derives from parent: [StandardDerivedScopedManagerController](./derived-scoped-manager-controller) |
| Plain global CRUD | [StandardManagerController](./standard-manager-controller) |

## Permission mechanisms

| Mechanism | Declared at | Applicable base |
|---|---|---|
| `@ManagerPermissions(read=..., create=..., ...)` | Class annotation | `StandardManagerController` / `ReadonlyManagerController` |
| `ScopedPermissionTriad(...)` | Constructor argument | Scoped / DerivedScoped / ReadonlyScoped |
| 8 String constructor parameters | Constructor argument | Tenant |

`@ManagerPermissions` + AOP has its pointcut hard-wired to `StandardManagerController.*(..)`, effective only on Standard / Readonly. Other families check permissions inline.

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

# Permission Model Migration Guide (PermissionMatrix + Naming Redesign)

The project's permission model went through two unification passes:

1. **Model unification**: the Manager Controller family in `crystal-shared` used to carry three parallel permission-declaration styles:
   - `@ManagerPermissions` class annotation + AOP (Standard / Readonly)
   - `ScopedPermissionTriad` (Scoped / DerivedScoped / ReadonlyScoped)
   - 8 String constructor parameters (Tenant)

   The unified `PermissionMatrix` (`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`) replaces all three with 4 layers × 4 operations = 16 authority fields. Every Manager Controller now receives the same data class via `permissions = ...`.

2. **Naming redesign** (2026-07-29): permission constants were upgraded from `const val String` to `val Declaration`, `_PEM` doubling was eliminated, and the 4-layer prefix convention (`x.` / `system.` / `tenant.` / `i.tenant.`) is now strictly enforced.

This guide covers both migrations with rewrites and common pitfalls.

## Four-layer quick reference

| Layer          | authority prefix   | Constant source     | Semantics                                                             |
|----------------|--------------------|---------------------|-----------------------------------------------------------------------|
| `super`        | `x.`               | `SystemPermission`  | Cross-scope super admin — always allowed regardless of scope           |
| `system`       | `system.`          | `SystemPermission`  | SYSTEM scope only                                                     |
| `tenantAdmin`  | `tenant.`          | `SystemPermission`  | TENANT scope, cross-tenant — no tenantId match (ops admin)             |
| `tenantPem`    | `i.tenant.`        | `TenantPermission`  | TENANT scope, strict tenantId match                                    |

Match rule (implemented by `PermissionMatrix.layersFor(scope, op)`):

- **SYSTEM request** → OR-check `[super<op>, system<op>]` (`NOT_APPLICABLE` filtered out)
- **TENANT request** → OR-check `[super<op>, tenantAdmin<op>, tenantPem<op>]` (`NOT_APPLICABLE` filtered out)

Cross-tenant check (`crossTenantLayersFor(op)`, used by `StandardScopedManagerController.checkOwnership`): only `super` + `tenantAdmin` qualify as cross-tenant; their holders skip the tenantId equality check.

## Two sentinel values

| Constant                             | Semantics                                                     | `layersFor` behavior                    |
|--------------------------------------|---------------------------------------------------------------|-----------------------------------------|
| `PermissionMatrix.NOT_APPLICABLE`     | This layer does not apply to the resource (e.g. tenant layers on a SYSTEM-only resource) | **Filtered out**, does not join the OR-check |
| `PermissionMatrix.NEVER_GRANTED`      | The layer exists but the operation is forbidden (e.g. CUD on Readonly) | **Kept**, but never matches any real authority — always fails |

Rule of thumb: **"the layer shouldn't exist" → `NOT_APPLICABLE`; "the layer exists but the op is sealed off" → `NEVER_GRANTED`.** The convenience factories fill these in correctly by default; you rarely need to hand-write them.

## Four convenience factories

`PermissionMatrix.of { ... }` is the full DSL covering any combination. Three additional Kotlin extension factories cover common cases:

| Factory                                  | For                       | Unfilled layers                            |
|------------------------------------------|---------------------------|--------------------------------------------|
| `PermissionMatrix.of { ... }`             | Full DSL, any combination | Unopened layers all default to `NOT_APPLICABLE` |
| `PermissionMatrix.systemOnly(...)`        | Global CRUD without scope (Standard) | All tenant layers `NOT_APPLICABLE`      |
| `PermissionMatrix.tenantOnly(...)`        | TENANT-only resources (Tenant) | super + system layers all `NOT_APPLICABLE` |
| `PermissionMatrix.systemOnlyReadonly(...)` | SYSTEM-only + read-only  | Tenant layers `NOT_APPLICABLE`, SYSTEM CUD `NEVER_GRANTED` |
| `PermissionMatrix.readonly(...)`          | Read-only variant of Scoped | All CUD `NEVER_GRANTED`, only 4 reads populated |

## Naming redesign: from String constants to Declarations

The 2026-07-29 naming redesign made the following changes:

- Every `const val String` in `SystemPermission` was upgraded to `val SystemRbacPermissionDeclaration`, constructed via one of three factories (`.action(...)` / `.menu(...)` / `.component(...)`) with `description` embedded in the Declaration. The old `DESCRIPTIONS: Map<String, String>` was deleted
- `TenantPermission`'s `const val ACTION_XXX_PEM = "i.tenant.xxx"` + `val ACTION_XXX = TenantPermissionDeclaration(name = ACTION_XXX_PEM, ...)` **doubling was eliminated**: only `val Declaration` remains, the `_PEM` suffix is dropped, and constant names no longer carry a `TENANT` segment
- `SystemRbacPermissionDeclaration` moved from `crystal-sdk` to `crystal-shared-types` (package `com.lovelycatv.crystalframework.shared.types.rbac.system`)
- Legacy SYSTEM-only permissions were **bulk-prefixed** with `system.` (old names like `user.create` / `role.create` / `tenant.create` all violated the prefix rule and directly triggered hard-errors)
- Cross-scope permissions were **bulk-prefixed** with `x.` (`x.dict.type.create` / `x.message.channel.create` / `x.approval.flow.definition.create`)
- MENU / COMPONENT permissions were **bulk-prefixed** with their layer prefix (the old prefix-less `permission:/manager/user-permissions` becomes `system.permission:/manager/user-permissions`)
- `PermissionMatrix.init` was upgraded from `logger.warn` to `throw IllegalStateException` — a prefix violation now fails startup immediately
- Reference style: `SystemPermission.ACTION_SYSTEM_USER_READ` (returns the Declaration object); use `.name` wherever a string is needed
- Verification tests: `PermissionNameConventionTest` (scans every name in `allPermissions()` for prefix + constant-segment consistency); `PreAuthorizeCoverageTest` (scans every `@PreAuthorize` literal in the project against the `.name` set from `allPermissions()`)
- Flyway migration `V20260729.01__reset_permissions_for_naming_redesign.sql` hard-deletes legacy `user_permissions` / `tenant_permissions` rows and their role-permission bindings; on restart the Configurers re-register the full catalog and restore built-in bindings from `SystemRolePermissionRelation` / `TenantRolePermissionRelation`

User-defined role-permission bindings are lost — this is the cost of the reset and was explicitly accepted. Full design record is in `.claude/research/permission-naming-redesign.md`.

### Old → new naming table

| Case | Old name | New name | Layer |
|---|---|---|---|
| User CRUD | `user.create` | `system.user.create` | system |
| Role CRUD | `role.create` | `system.role.create` | system |
| System permission CRUD | `permission.create` | `system.permission.create` | system |
| Manage tenant list | `tenant.create` (ambiguous) | `system.tenant.create` | system |
| Manage tenant tires | `tenant.tire.type.create` (ambiguous) | `system.tenant.tire.type.create` | system |
| System menu (legacy) | `permission:/manager/user-permissions` | `system.permission:/manager/user-permissions` | system |
| Cross-scope dict admin | `dict.type.create` (prefix-less) | `x.dict.type.create` | super |
| Cross-tenant dict admin | `tenant.dict.type.create` | `tenant.dict.type.create` (unchanged) | tenantAdmin |
| Own-tenant dict | `i.tenant.dict.type.create` | `i.tenant.dict.type.create` (unchanged) | tenantPem |

Tenant-side constant names are also simplified: `ACTION_TENANT_ROLE_CREATE_PEM` → `ACTION_ROLE_CREATE` (drop `TENANT` segment, drop `_PEM` suffix).

## Before / after samples

### Sample 1: Standard (@ManagerPermissions → systemOnly)

**Before**:

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    readAll = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    create  = [SystemPermission.ACTION_STORAGE_PROVIDER_CREATE],
    update  = [SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE],
    delete  = [SystemPermission.ACTION_STORAGE_PROVIDER_DELETE],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<...>(managerService)
```

**After**:

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<...>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE.name,
        systemRead   = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE.name,
    ),
)
```

Notes:
- Drop the class-level `@ManagerPermissions` annotation
- Pass `permissions` via the constructor
- `SystemPermission.ACTION_SYSTEM_XXX` is now a `SystemRbacPermissionDeclaration` object; `PermissionMatrix` wants a string, so append `.name`
- `system<op>` authorities must carry the `system.` prefix; violations `throw IllegalStateException` and block startup
- Need a cross-scope super admin as well? `systemOnly` also accepts `superCreate = ..., superRead = ..., ...` (4 optional params, default `NOT_APPLICABLE`)

### Sample 2: Scoped (ScopedPermissionTriad → PermissionMatrix.of DSL)

**Before**:

```kotlin
) : StandardScopedManagerController<...>(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate     = SystemPermission.ACTION_DICT_TYPE_CREATE,
        superRead       = SystemPermission.ACTION_DICT_TYPE_READ,
        superUpdate     = SystemPermission.ACTION_DICT_TYPE_UPDATE,
        superDelete     = SystemPermission.ACTION_DICT_TYPE_DELETE,
        systemCreate    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE,
        systemRead      = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ,
        systemUpdate    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE,
        systemDelete    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM,
        tenantPemRead   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM,
    ),
)
```

**After**:

```kotlin
) : StandardScopedManagerController<...>(
    managerService,
    permissions = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_X_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_X_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_X_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_X_DICT_TYPE_DELETE.name
        }
        system {
            create = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE.name
        }
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_TENANT_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE.name
        }
        tenantPem {
            create = TenantPermission.ACTION_DICT_TYPE_CREATE.name
            read   = TenantPermission.ACTION_DICT_TYPE_READ.name
            update = TenantPermission.ACTION_DICT_TYPE_UPDATE.name
            delete = TenantPermission.ACTION_DICT_TYPE_DELETE.name
        }
    },
)
```

Notes:
- Upgrade from 12 permissions to 16 (new `tenantAdmin` layer) — the old Triad lacked a cross-tenant ops layer, forcing SYSTEM admins to hold `super*` to edit tenant data; the new `tenantAdmin` layer (with `tenant.` prefix) covers exactly this role
- **`` `super` `` is a Kotlin reserved word; the DSL uses backticks**
- Unopened layers default entirely to `NOT_APPLICABLE` (excluded from decisions); no need to declare them
- Prefix convention: `super*` must start with `x.`; `system*` must start with `system.`; `tenantAdmin*` must start with `tenant.`; `tenantPem*` must start with `i.tenant.`
- `TenantPermission` constant names dropped their `TENANT` segment and `_PEM` suffix

### Sample 3: Tenant (8 String → tenantOnly)

**Before**:

```kotlin
) : StandardTenantManagerController<...>(
    managerService,
    createPermission        = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    scopedCreatePermission  = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
    readPermission          = SystemPermission.ACTION_TENANT_ROLE_READ,
    scopedReadPermission    = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
    updatePermission        = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    scopedUpdatePermission  = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
    deletePermission        = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    scopedDeletePermission  = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
)
```

**After**:

```kotlin
) : StandardTenantManagerController<...>(
    managerService,
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE.name,
        tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE.name,
        tenantPemCreate   = TenantPermission.ACTION_ROLE_CREATE.name,
        tenantPemRead     = TenantPermission.ACTION_ROLE_READ.name,
        tenantPemUpdate   = TenantPermission.ACTION_ROLE_UPDATE.name,
        tenantPemDelete   = TenantPermission.ACTION_ROLE_DELETE.name,
    ),
)
```

Notes:
- Old `xxxPermission` maps to `tenantAdmin*` (cross-tenant)
- Old `scopedXxxPermission` maps to `tenantPem*` (own tenant)
- super / system layers stay `NOT_APPLICABLE` (factory default) — Tenant resources have no SYSTEM scope concept
- The legacy 8-String constructor is `@Deprecated` but retained one release, delegating to the new API; new code uses the primary constructor only

### Sample 4: Readonly (@ManagerPermissions → systemOnlyReadonly)

**Before**:

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    update  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
class ManagerMailSendLogController(...) : ReadonlyManagerController<...>(managerService)
```

**After**:

```kotlin
class ManagerMailSendLogController(
    managerService: MailSendLogManagerService
) : ReadonlyManagerController<...>(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ.name,
    ),
)
```

Notes:
- Just fill `systemRead` — the factory sets the 4 CUD slots to `NEVER_GRANTED` and the 8 tenant slots to `NOT_APPLICABLE`
- Defense in depth is preserved: even if the permission layer is misconfigured, `Mutability.READ_ONLY` still blocks CUD

### Sample 5: ReadonlyScoped (Triad.readonly → PermissionMatrix.readonly)

**Before**:

```kotlin
permissions = ScopedPermissionTriad.readonly(
    superRead      = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead  = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

**After**:

```kotlin
permissions = PermissionMatrix.readonly(
    superRead       = SystemPermission.ACTION_X_APPROVAL_FLOW_INSTANCE_READ.name,
    systemRead      = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ.name,
    tenantPemRead   = TenantPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ.name,
)
```

Notes:
- The new factory adds `tenantAdminRead` (cross-tenant read)
- All 12 CUD slots auto-filled with `NEVER_GRANTED`
- If a layer does not apply to this resource (e.g. approval instance has no SYSTEM scope), pass `PermissionMatrix.NOT_APPLICABLE` explicitly

## Migration steps

1. **Grep for class-top** `@ManagerPermissions` — remove the annotation, move the 5 fields' authorities into `PermissionMatrix.systemOnly(...)`, and pass as `permissions = ...` to the parent constructor
2. **Grep for type references** `ScopedPermissionTriad` / `ScopedPermissionMatrix` — replace with `PermissionMatrix`; `ScopedPermissionMatrix` is a typealias that compiles fine but emits `@Deprecated`
3. **Grep for factory calls** `ScopedPermissionTriad.readonly(...)` — replace with `PermissionMatrix.readonly(...)` and add the `tenantAdminRead` argument
4. **Grep for constructor args** `createPermission = ...` / `scopedCreatePermission = ...` — replace with `PermissionMatrix.tenantOnly(...)`, mapping old `xxxPermission` → `tenantAdmin*` and `scopedXxxPermission` → `tenantPem*`
5. **Fix prefixes** — `super*` needs `x.`, `system*` needs `system.`, `tenantAdmin*` needs `tenant.`, `tenantPem*` needs `i.tenant.`
6. **Switch references to Declarations** — `SystemPermission.ACTION_XXX` is now a Declaration; `PermissionMatrix` wants strings, so append `.name`. On `TenantPermission`, drop the `_PEM` suffix and `TENANT` segment from all constant names
7. **Verify at startup** — a prefix violation now `throw IllegalStateException` from `PermissionMatrix.init`, failing startup and listing every violation

## Common pitfalls

### 1. `super` needs backticks

`super` is a Kotlin keyword; the DSL builder declares the method as `` `super` `` and callers must backtick it too:

```kotlin
PermissionMatrix.of {
    `super` { create = "..."; ... }  // OK
    // super { ... }                  // Syntax error
}
```

### 2. `super` isn't "permission inheritance"

The new `tenantAdmin` layer is easily misread as "a subclass of super". Actually all 4 layers are **independent authorization dimensions**:

- User holds `super*` → SYSTEM + TENANT both allowed, cross-tenant
- User holds `tenantAdmin*` → only TENANT allowed, cross-tenant
- User holds `system*` → only SYSTEM allowed
- User holds `tenantPem*` → only TENANT + own tenant

`layersFor` returns an OR array; holding any one passes. `super` does not "include" the others — it merely happens to cover more ground.

### 3. `NOT_APPLICABLE` vs `NEVER_GRANTED` are not interchangeable

- Wrong direction: filling tenant layers of a SYSTEM-only resource with `NEVER_GRANTED` (should be `NOT_APPLICABLE`) — no functional impact, but exposing the layer array shows a never-matches element, which is misleading in logs / audits
- Wrong direction: filling Readonly CUD slots with `NOT_APPLICABLE` (should be `NEVER_GRANTED`) — `layersFor` filters and returns an empty array, `hasAnyAuthority()` then returns false so behavior looks equivalent, but semantics are entirely different; if anyone later calls `layersFor(SYSTEM, CREATE)` expecting a "deny sentinel", they get an empty array and mistake it for "no permission required"

**Rule**: use `readonly(...)` / `systemOnlyReadonly(...)` factories rather than constructing Matrix by hand.

### 4. Don't add `@ManagerPermissions` to Standard / Readonly anymore

The new `StandardManagerController.authorize` prefers the `permissions` field; only when `permissions == null` does it fall back to the legacy `@ManagerPermissions` + AOP path (which now carries `@Deprecated`). New code uses `permissions` exclusively.

### 5. Prefix violations are now hard errors

`PermissionMatrix.init` now `throw IllegalStateException("PermissionMatrix prefix violations: ...")` on violation, failing startup. All legacy prefix-less names (`permission.create` / `user.create` / `tenant.create` and friends) are no longer valid and must be rewritten as `system.permission.create` / `system.user.create` / `system.tenant.create`.

## FAQ

**Q: Can legacy `ScopedPermissionMatrix.readonly(...)` calls stay?**
A: They compile (`ScopedPermissionMatrix` is a typealias; `readonly` inherits through the alias) but emit deprecation warnings. Replace gradually and don't write new code against it.

**Q: Was `StandardTenantManagerController`'s 8-String constructor removed?**
A: No — marked `@Deprecated` and retained one release, delegating internally to `PermissionMatrix.tenantOnly(...)`. Legacy `createPermission` etc. properties also have compat getters. New code uses the primary constructor only.

**Q: Can I still use empty string `DISABLED_SCOPED_PERMISSION = ""`?**
A: Yes — equivalent to `PermissionMatrix.NOT_APPLICABLE` (Tenant Controller's `hasScopedAuthority` short-circuits both empty string and `NOT_APPLICABLE` to false). New code should use `PermissionMatrix.NOT_APPLICABLE` for clearer semantics.

**Q: Why introduce the `tenantAdmin` layer?**
A: The old Scoped family had no cross-tenant ops layer — SYSTEM admins editing tenant data had to hold `super*`, conflating "cross-scope" and "cross-tenant ops". The new `tenantAdmin` (with `tenant.` prefix) owns "TENANT scope but cross-tenant", separating ops duty from super-admin duty.

**Q: Can I fill only the super layer and leave the rest?**
A: Technically yes (the rest default to `NOT_APPLICABLE`), but that means only super admins can operate on the resource. Unless it's truly a super-admin-only diagnostic / ops endpoint, split the authority across concrete layers.

# Permission Model Migration Guide (PermissionMatrix)

The Manager Controller family in `crystal-shared` used to carry three parallel permission-declaration styles:

- `@ManagerPermissions` class annotation + AOP (Standard / Readonly)
- `ScopedPermissionTriad` (Scoped / DerivedScoped / ReadonlyScoped)
- 8 String constructor parameters (Tenant)

The unified `PermissionMatrix` (`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`) replaces all three with 4 layers × 4 operations = 16 authority fields. Every Manager Controller now receives the same data class via `permissions = ...`.

This guide gives side-by-side rewrites against legacy code and lists common pitfalls.

## Four-layer quick reference

| Layer          | authority prefix   | Constant source     | Semantics                                                             |
|----------------|--------------------|---------------------|-----------------------------------------------------------------------|
| `super`        | none               | `SystemPermission`  | Cross-scope super admin — always allowed regardless of scope           |
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
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE,
        systemRead   = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE,
    ),
)
```

Notes:
- Drop the class-level `@ManagerPermissions` annotation
- Pass `permissions` via the constructor
- `system<op>` authorities must carry the `system.` prefix; violations emit a warn log
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
            create = SystemPermission.ACTION_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_DICT_TYPE_READ
            update = SystemPermission.ACTION_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_DICT_TYPE_DELETE
        }
        system {
            create = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ
            update = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE
        }
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_TENANT_DICT_TYPE_READ
            update = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE
        }
        tenantPem {
            create = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM
            read   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM
            update = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM
            delete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM
        }
    },
)
```

Notes:
- Upgrade from 12 permissions to 16 (new `tenantAdmin` layer) — the old Triad lacked a cross-tenant ops layer, forcing SYSTEM admins to hold `super*` to edit tenant data; the new `tenantAdmin` layer (with `tenant.` prefix) covers exactly this role
- **`` `super` `` is a Kotlin reserved word; the DSL uses backticks**
- Unopened layers default entirely to `NOT_APPLICABLE` (excluded from decisions); no need to declare them
- Prefix convention: `super*` may not carry `system.` / `tenant.` / `i.tenant.`; `system*` must start with `system.`; `tenantAdmin*` must start with `tenant.`; `tenantPem*` must start with `i.tenant.`

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
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
        tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
        tenantPemCreate   = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
        tenantPemRead     = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
        tenantPemUpdate   = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
        tenantPemDelete   = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
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
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
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
    superRead       = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead      = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_INSTANCE_READ,
    tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead   = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

Notes:
- The new factory adds `tenantAdminRead` (cross-tenant read)
- All 12 CUD slots auto-filled with `NEVER_GRANTED`

## Migration steps

1. **Grep for class-top** `@ManagerPermissions` — remove the annotation, move the 5 fields' authorities into `PermissionMatrix.systemOnly(...)`, and pass as `permissions = ...` to the parent constructor
2. **Grep for type references** `ScopedPermissionTriad` / `ScopedPermissionMatrix` — replace with `PermissionMatrix`; `ScopedPermissionMatrix` is a typealias that compiles fine but emits `@Deprecated`
3. **Grep for factory calls** `ScopedPermissionTriad.readonly(...)` — replace with `PermissionMatrix.readonly(...)` and add the `tenantAdminRead` argument
4. **Grep for constructor args** `createPermission = ...` / `scopedCreatePermission = ...` — replace with `PermissionMatrix.tenantOnly(...)`, mapping old `xxxPermission` → `tenantAdmin*` and `scopedXxxPermission` → `tenantPem*`
5. **Add the new `tenantAdmin` layer's permission constants** — see the `add-system-permission` skill or the module's `Permission` constants class; naming is generally `ACTION_TENANT_<RES>_<OP>` (`tenant.` prefix)
6. **Fix prefixes** — `system*` needs `system.`, `tenantAdmin*` needs `tenant.`, `tenantPem*` needs `i.tenant.`, `super*` no prefix
7. **Verify at startup** — prefix violations cause `PermissionMatrix.init` to emit `PermissionMatrix: super* permission '...' violates prefix convention` warn logs (currently a soft constraint; will become a hard error in the future)

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

### 5. Prefix violations are soft warnings — don't ignore them

`PermissionMatrix.init` emits `logger.warn` on prefix violations; it does not block startup. Ignoring them means they'll all fire at once when the check is promoted to a hard error. Treat every `PermissionMatrix: ... violates prefix convention` as tech debt to pay down.

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

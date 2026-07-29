# Tenant Permission

When contributing to the framework source code, add tenant permissions (the `tenantPem` layer) by modifying the constants in the `crystal-rbac` module.

## Files Involved

| File | Purpose |
|------|---------|
| `TenantPermission.kt` | `TenantPermissionDeclaration` constants |
| `TenantRole.kt` | `TenantRoleDeclaration` constants |
| `TenantRolePermissionRelation.kt` | Role-permission bindings |

## Prefix Convention

`TenantPermission` carries only the `tenantPem` layer. Every permission's string `name` must start with `i.tenant.` (meaning: in-tenant / internal-tenant — the user acting inside their own tenant). Constant names no longer carry a `TENANT` segment (redundant, since the whole class is about tenants) nor a `_PEM` suffix (permissions are no longer doubled as string + Declaration).

| Layer | authority prefix | Constant segment | Semantics | Example name |
|---|---|---|---|---|
| `tenantPem` | `i.tenant.` | `ACTION_XXX` / `MENU_XXX` | TENANT scope with strict tenantId match | `i.tenant.role.create` |

The other three layers — cross-scope (`x.`), SYSTEM scope (`system.`), cross-tenant (`tenant.`) — live in `SystemPermission.kt` — see [System Permission](./system-permission.md).

The prefix rule is **hard-enforced by `PermissionMatrix.init`** at construction time (`throw IllegalStateException`) and additionally verified by `PermissionNameConventionTest` scanning `allPermissions()` statically.

## Step 1: Add Permission Declarations

In `crystal-rbac/src/main/kotlin/.../rbac/tenant/constants/TenantPermission.kt`, append `val`s under a business-domain block:

```kotlin
// ============================================================
//   Report
// ============================================================
val MENU_REPORT = TenantPermissionDeclaration(
    name = "i.tenant.report",
    description = "My tenant reports menu",
    type = TenantPermissionType.MENU,
    path = "/manager/tenant/reports",
)

val ACTION_REPORT_READ = TenantPermissionDeclaration(
    name = "i.tenant.report.read",
    description = "Read reports within own tenant",
    type = TenantPermissionType.ACTION,
)

val ACTION_REPORT_EXPORT = TenantPermissionDeclaration(
    name = "i.tenant.report.export",
    description = "Export reports within own tenant",
    type = TenantPermissionType.ACTION,
)
```

Notes:

- Every permission is a `val TenantPermissionDeclaration` (**`_PEM` doubling is gone**: previously each permission required both a `const val String` and a `val Declaration`; now only the Declaration exists — use `.name` wherever a string is needed)
- Construct `TenantPermissionDeclaration(...)` directly with `type = TenantPermissionType.ACTION / MENU` (MENU requires `path`)
- Constant names are `ACTION_XXX` / `MENU_XXX` — no `TENANT` segment
- `TenantBuiltinRbacConfigurer` calls `TenantPermission.allPermissions()` to register everything — new entries are picked up automatically

## Step 2: Add a Role (optional)

Add to `TenantRole.kt`. Supports hierarchy via `parentRoleName`:

```kotlin
val CUSTOM_REPORTER = TenantRoleDeclaration(
    name = "custom_reporter",
    description = "Report viewer",
    parentRoleName = null,
)
```

`parentRoleName` names the parent role; child roles inherit all of the parent's permissions.

## Step 3: Bind Permissions to Role

Add to the `mapping` in `TenantRolePermissionRelation.kt`. Bindings reference Declarations, not strings:

```kotlin
mapping = mapOf(
    TenantRole.ADMIN to listOf(
        // ... existing permissions ...
        TenantPermission.ACTION_REPORT_READ,
    ),
    TenantRole.CUSTOM_REPORTER to listOf(
        TenantPermission.MENU_REPORT,
        TenantPermission.ACTION_REPORT_READ,
        TenantPermission.ACTION_REPORT_EXPORT,
    ),
)
```

## Step 4: Set Default Roles (optional)

Configure in `TenantBuiltinRbacConfigurer`:

```kotlin
registry.defaultOwnerRole(TenantRole.ROOT.name)
registry.defaultMemberRole(TenantRole.MEMBER.name)
```

## Referencing in `@PreAuthorize`

Spring's `@PreAuthorize` only accepts compile-time constant strings, so you cannot use `TenantPermission.ACTION_REPORT_READ.name` (not `const`). Use a string literal:

```kotlin
@PreAuthorize("hasAuthority('i.tenant.report.read')")
@GetMapping("/my-endpoint")
fun myEndpoint(): ApiResponse<Data> { ... }
```

`PreAuthorizeCoverageTest` scans every `@PreAuthorize` literal in the project; if the string cannot be matched against a `.name` in `SystemPermission.allPermissions()` or `TenantPermission.allPermissions()`, the test fails immediately — so typos are caught by CI.

## Database Migration

This naming redesign ships with Flyway `V20260729.01__reset_permissions_for_naming_redesign.sql`, which hard-deletes all legacy permission rows and role-permission bindings. On startup the Configurers re-register the full permission catalog under the new constants and restore the built-in bindings from `TenantRolePermissionRelation.mapping`. Tenant-admin custom role-permission bindings (created via the UI) are lost — announce the deployment so each tenant admin can re-bind their custom roles.

Design background and the old→new name table are in the [Permission Model Migration Guide](../develop/controller/permission-migration.md) and `.claude/research/permission-naming-redesign.md`.

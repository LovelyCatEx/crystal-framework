# System Permission

When contributing to the framework source code, add system permissions by modifying the constants in the `crystal-shared` module.

## Files Involved

| File | Purpose |
|------|---------|
| `SystemPermission.kt` | `SystemRbacPermissionDeclaration` constants |
| `SystemRole.kt` | Role name constants |
| `SystemRolePermissionRelation.kt` | Role-permission bindings |

## Four-Layer Prefix Convention

`SystemPermission` covers three layers: `super` / `system` / `tenantAdmin`. Every permission's string `name` must start with one prefix from the table below, and the constant-name segment must match that prefix exactly.

| Layer | authority prefix | Constant segment | Semantics | Example name |
|---|---|---|---|---|
| `super` | `x.` | `ACTION_X_XXX` / `MENU_X_XXX` / `COMPONENT_X_XXX` | Cross-scope super admin (SYSTEM + TENANT) | `x.dict.type.create` |
| `system` | `system.` | `ACTION_SYSTEM_XXX` / `MENU_SYSTEM_XXX` / `COMPONENT_SYSTEM_XXX` | SYSTEM scope only | `system.user.create` |
| `tenantAdmin` | `tenant.` | `ACTION_TENANT_XXX` / `MENU_TENANT_XXX` | TENANT scope, cross-tenant (ops layer) | `tenant.member.create` |

The fourth layer `tenantPem` (`i.tenant.` prefix) lives in `TenantPermission.kt` — see [Tenant Permission](./tenant-permission.md).

The prefix rule is **hard-enforced by `PermissionMatrix.init`** at construction time (`throw IllegalStateException`) and additionally verified by `PermissionNameConventionTest` scanning `allPermissions()` statically.

## Step 1: Add Permission Declarations

In `crystal-shared/src/main/kotlin/.../shared/constants/SystemPermission.kt`, append `val`s under a business-domain block:

```kotlin
// ============================================================
//   MyPlugin  (system)
// ============================================================
val ACTION_SYSTEM_MYPLUGIN_DATA_READ = SystemRbacPermissionDeclaration.action(
    name = "system.myplugin.data.read",
    description = "Read myplugin data",
)
val ACTION_SYSTEM_MYPLUGIN_DATA_UPDATE = SystemRbacPermissionDeclaration.action(
    name = "system.myplugin.data.update",
    description = "Update myplugin data",
)
val MENU_SYSTEM_MYPLUGIN_MANAGER = SystemRbacPermissionDeclaration.menu(
    name = "system.myplugin",
    path = "/manager/myplugin",
    description = "Manage myplugin menu",
)
val COMPONENT_SYSTEM_MYPLUGIN_WIDGET = SystemRbacPermissionDeclaration.component(
    name = "system.myplugin.widget",
    path = "myplugin.widget",
    description = "Myplugin widget",
)
```

Notes:

- Every permission is a `val SystemRbacPermissionDeclaration` (no more `const val String`)
- Use one of three factories: `.action(name, description)` / `.menu(name, path, description)` / `.component(name, path, description)`
- If the business domain spans multiple layers (e.g. `x + system + tenantAdmin`), keep all three layers' `val`s in the same block for side-by-side reference
- Anywhere a string is needed, use `.name`
- No `Map<String, String>` needs to be kept in sync for descriptions (description is embedded in the Declaration)
- `SystemSystemRbacConfigurer` calls `SystemPermission.allPermissions()` to register everything — new entries are picked up automatically

## Step 2: Add a Role (optional)

Add to `SystemRole.kt`:

```kotlin
const val ROLE_MYPLUGIN_MANAGER = "myplugin_manager"
```

## Step 3: Bind Permissions to Role

Add to the `mapping` in `SystemRolePermissionRelation.kt`. Bindings reference Declarations, not strings:

```kotlin
mapping = mapOf(
    SystemRole.ROLE_ADMIN to listOf(
        // ... existing permissions ...
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ,
    ),
    SystemRole.ROLE_MYPLUGIN_MANAGER to listOf(
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ,
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_UPDATE,
        SystemPermission.MENU_SYSTEM_MYPLUGIN_MANAGER,
    ),
)
```

New roles must be added both to `SystemRole.kt` and to `mapping`; `SystemSystemRbacConfigurer` writes bindings back to the database on startup.

## Referencing in `@PreAuthorize`

Spring's `@PreAuthorize` only accepts compile-time constant strings, so you cannot use `SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ.name` (not `const`). Use a string literal:

```kotlin
@PreAuthorize("hasAuthority('system.myplugin.data.read')")
@GetMapping("/my-endpoint")
fun myEndpoint(): ApiResponse<Data> { ... }
```

`PreAuthorizeCoverageTest` scans every `@PreAuthorize` literal in the project; if the string cannot be matched against a `.name` in `SystemPermission.allPermissions()` or `TenantPermission.allPermissions()`, the test fails immediately — so typos are caught by CI.

## Database Migration

This naming redesign ships with Flyway `V20260729.01__reset_permissions_for_naming_redesign.sql`, which hard-deletes all legacy permission rows and role-permission bindings. On startup the Configurers re-register the full permission catalog under the new constants and restore the built-in bindings from `SystemRolePermissionRelation.mapping`. User-defined role-permission bindings (custom roles created via the UI) are lost — announce the deployment so each admin can re-bind their custom roles.

Design background and the old→new name table are in the [Permission Model Migration Guide](../develop/controller/permission-migration.md) and `.claude/research/permission-naming-redesign.md`.

# Tenant Permission

When contributing to the framework source code, add tenant permissions by modifying the constants in `crystal-starter`.

## Files Involved

| File | Purpose |
|------|---------|
| `TenantPermission.kt` | `TenantPermissionDeclaration` objects |
| `TenantRole.kt` | `TenantRoleDeclaration` objects |
| `TenantRolePermissionRelation.kt` | Role-permission bindings |

## Step 1: Add Permissions

Add to `crystal-starter/src/main/kotlin/.../tenant/constants/TenantPermission.kt`:

```kotlin
val CUSTOM_REPORT_VIEW = TenantPermissionDeclaration(
    name = "custom.report.view",
    description = "View reports",
    type = TenantPermissionType.ACTION,
)
val CUSTOM_REPORT_EXPORT = TenantPermissionDeclaration(
    name = "custom.report.export",
    description = "Export reports",
    type = TenantPermissionType.ACTION,
)
```

The built-in `TenantBuiltinRbacConfigurer` uses `TenantPermission.allPermissions()` to register all permissions — new ones are included automatically.

### Naming

`name` uses `module.operation.resource` format, globally unique.

## Step 2: Add a Role (optional)

Add to `TenantRole.kt`. Supports hierarchy via `parentRoleName`:

```kotlin
val CUSTOM_REPORTER = TenantRoleDeclaration(
    name = "custom_reporter",
    description = "Report viewer",
    parentRoleName = null,
)
```

## Step 3: Bind Permissions to Role

Add to the `mapping` in `TenantRolePermissionRelation.kt`:

```kotlin
mapping = mapOf(
    TenantRole.ADMIN to listOf(
        // ... existing permissions ...
        TenantPermission.CUSTOM_REPORT_VIEW,
    ),
    TenantRole.CUSTOM_REPORTER to listOf(
        TenantPermission.CUSTOM_REPORT_VIEW,
        TenantPermission.CUSTOM_REPORT_EXPORT,
    ),
)
```

## Step 4: Set Default Roles (optional)

Configure in `TenantBuiltinRbacConfigurer`:

```kotlin
registry.defaultOwnerRole(TenantRole.ROOT.name)
registry.defaultMemberRole(TenantRole.MEMBER.name)
```

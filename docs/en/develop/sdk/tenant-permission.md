# Tenant Permission

Plugins can register tenant-level permissions, roles, and role-permission bindings by implementing `TenantRbacConfigurer`.

## Architecture Overview

```
Plugin provides TenantRbacConfigurer Bean
  → TenantRbacRegistryConfiguration collects them
    → Register permissions → Register roles → Bind permissions → Set default roles
```

## Registering Tenant Permissions

Implement `TenantRbacConfigurer`:

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.config.TenantRbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRolePermissionBindingDeclaration
import org.springframework.stereotype.Component

@Component
class MyPluginTenantRbacConfigurer : TenantRbacConfigurer {
    override fun configure(registry: TenantRbacRegistry) {
        // Register permissions
        registry.permissions(
            TenantPermissionDeclaration(
                name = "myplugin.report.view",
                description = "View reports",
                type = com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType.ACTION,
            ),
            TenantPermissionDeclaration(
                name = "myplugin.report.export",
                description = "Export reports",
                type = com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType.ACTION,
            ),
        )

        // Register roles (supports hierarchy)
        registry.roles(
            TenantRoleDeclaration(
                name = "myplugin_reporter",
                description = "Can view and export reports",
                parentRoleName = null,
            ),
        )

        // Bind permissions to role
        registry.bind(
            roleName = "myplugin_reporter",
            permissionNames = setOf("myplugin.report.view", "myplugin.report.export"),
        )

        // Set as default member role (optional)
        registry.defaultMemberRole("myplugin_reporter")
    }
}
```

### Permission Declaration

`TenantPermissionDeclaration` parameters:

| Parameter | Description |
|-----------|-------------|
| `name` | Permission name, format `module.operation.resource`, globally unique |
| `description` | Description |
| `type` | `TenantPermissionType.ACTION` or `.MENU` |

### Role Hierarchy

`TenantRoleDeclaration` supports permission inheritance via `parentRoleName`:

```kotlin
// super_admin inherits all permissions from admin
TenantRoleDeclaration(name = "tenant_admin", ...)
TenantRoleDeclaration(name = "tenant_super_admin", parentRoleName = "tenant_admin", ...)
```

### Default Roles

Automatically assigned when a tenant is created:

- `registry.defaultOwnerRole(roleName)` — default role for tenant owner
- `registry.defaultMemberRole(roleName)` — default role for tenant member

# System Permission

Plugins can register system-level permissions, roles, and role-permission bindings by implementing `SystemRbacConfigurer`.

## Architecture Overview

```
Plugin provides SystemRbacConfigurer Bean
  → RbacRegistryConfiguration collects them
    → Register permissions → Register roles → Bind permissions to roles
      → RbacTableDataCheckRunner syncs to database on startup
```

## Registering Permissions

Implement `SystemRbacConfigurer`:

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemPermissionType
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRoleDeclaration
import org.springframework.core.annotation.Order
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MyPluginRbacConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        // Register permissions
        registry.permissions(
            SystemRbacPermissionDeclaration.action(
                name = "myplugin.data.read",
                description = "Read my plugin data",
                module = "myplugin"
            ),
            SystemRbacPermissionDeclaration.action(
                name = "myplugin.data.write",
                description = "Write my plugin data",
                module = "myplugin"
            ),
            SystemRbacPermissionDeclaration.menu(
                name = "/manager/myplugin",
                description = "My Plugin Page",
                path = "/manager/myplugin",
                module = "myplugin"
            ),
        )

        // Register roles
        registry.roles(
            SystemRoleDeclaration(
                name = "myplugin_manager",
                description = "My Plugin Manager",
                module = "myplugin"
            )
        )

        // Bind permissions to role
        registry.bind(
            roleName = "myplugin_manager",
            permissionNames = setOf(
                "myplugin.data.read",
                "myplugin.data.write",
                "/manager/myplugin",
            )
        )

        // Grant all permissions to a role
        registry.grantAll("myplugin_manager")
    }
}
```

### Permission Declaration Types

| Factory Method | Usage | Example |
|---------------|-------|---------|
| `action(name, description, module)` | API operation permission | `myplugin.data.read` |
| `menu(name, description, path, module)` | Menu visibility | `/manager/myplugin` |
| `component(name, description, path, module)` | UI component permission | `myplugin:dashboard` |

### Naming Conventions

- Action: `module.operation.resource`, e.g. `myplugin.data.read`
- Menu: `module:page-path`, e.g. `/manager/myplugin`
- Each permission `name` must be globally unique

### Role and grantAll

- `registry.grantAll(roleName)` marks the role as having all permissions (including future ones)
- Suitable for super roles like `root`, `admin`
- Regular roles should use `registry.bind()` to specify explicit permission sets

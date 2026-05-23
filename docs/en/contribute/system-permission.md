# System Permission

When contributing to the framework source code, add system permissions by modifying the constants in the `crystal-shared` module.

## Files Involved

| File | Purpose |
|------|---------|
| `SystemPermission.kt` | Permission string constants |
| `SystemRole.kt` | Role name constants |
| `SystemRolePermissionRelation.kt` | Role-permission bindings |

## Step 1: Add Permission Constants

Add to `crystal-shared/src/main/kotlin/.../constants/SystemPermission.kt`:

```kotlin
const val ACTION_MYPLUGIN_DATA_READ = "myplugin.data.read"
const val ACTION_MYPLUGIN_DATA_WRITE = "myplugin.data.write"
const val MENU_MYPLUGIN_PAGE = "/manager/myplugin"
```

### Naming Rules

Constants are UPPER_SNAKE_CASE. The value format determines the permission type:

| Type | Value Format | Example |
|------|-------------|---------|
| action | `module.operation.resource` | `myplugin.data.read` |
| menu | Path | `/manager/myplugin` |
| component | `module:component` | `myplugin:dashboard` |

The built-in configurer reads all `const val` properties via reflection and auto-detects the type:
- Contains `:` → split as `name:path`, registered as `menu()`
- Contains `@` → split as `name@path`, registered as `component()`
- Otherwise → registered as `action()`

## Step 2: Add a Role (optional)

Add to `SystemRole.kt`:

```kotlin
const val ROLE_MYPLUGIN_MANAGER = "myplugin_manager"
```

## Step 3: Bind Permissions to Role

Add to the `mapping` in `SystemRolePermissionRelation.kt`:

```kotlin
mapping = mapOf(
    SystemRole.ROLE_ADMIN to listOf(
        // ... existing permissions ...
        SystemPermission.ACTION_MYPLUGIN_DATA_READ,
    ),
    SystemRole.ROLE_MYPLUGIN_MANAGER to listOf(
        SystemPermission.ACTION_MYPLUGIN_DATA_READ,
        SystemPermission.ACTION_MYPLUGIN_DATA_WRITE,
        SystemPermission.MENU_MYPLUGIN_PAGE,
    ),
)
```

New roles must be added to both `SystemRole.kt` and the `mapping`.

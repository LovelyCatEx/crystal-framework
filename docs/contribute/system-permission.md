# 系统权限

在框架源码内添加系统权限，需要修改 `crystal-shared` 模块中的常量定义。

## 涉及文件

| 文件 | 用途 |
|------|------|
| `SystemPermission.kt` | 定义权限字符串常量 |
| `SystemRole.kt` | 定义角色名称 |
| `SystemRolePermissionRelation.kt` | 定义角色-权限绑定关系 |

## 步骤 1：添加权限常量

在 `crystal-shared/src/main/kotlin/.../constants/SystemPermission.kt` 中添加：

```kotlin
const val ACTION_MYPLUGIN_DATA_READ = "myplugin.data.read"
const val ACTION_MYPLUGIN_DATA_WRITE = "myplugin.data.write"
const val MENU_MYPLUGIN_PAGE = "/manager/myplugin"
```

### 命名规则

常量名全大写，下划线分隔。权限值格式：

| 权限类型 | 值格式 | 示例 |
|---------|--------|------|
| action | `模块.操作.资源` | `myplugin.data.read` |
| menu | 路径 | `/manager/myplugin` |
| component | `模块:组件名` | `myplugin:dashboard` |

内置 Configurer 通过反射读取所有 `const val` 属性，根据值格式自动判断类型：
- 包含 `:` → 按 `name:path` 分割，注册为 `menu()`
- 包含 `@` → 按 `name@path` 分割，注册为 `component()`
- 其他 → 注册为 `action()`

## 步骤 2：添加角色（可选）

在 `SystemRole.kt` 中添加：

```kotlin
const val ROLE_MYPLUGIN_MANAGER = "myplugin_manager"
```

## 步骤 3：绑定权限到角色

在 `SystemRolePermissionRelation.kt` 的 `mapping` 中添加：

```kotlin
mapping = mapOf(
    SystemRole.ROLE_ADMIN to listOf(
        // ... 已有权限 ...
        SystemPermission.ACTION_MYPLUGIN_DATA_READ,
    ),
    SystemRole.ROLE_MYPLUGIN_MANAGER to listOf(
        SystemPermission.ACTION_MYPLUGIN_DATA_READ,
        SystemPermission.ACTION_MYPLUGIN_DATA_WRITE,
        SystemPermission.MENU_MYPLUGIN_PAGE,
    ),
)
```

新角色需要同时添加到 `SystemRole.kt` 和 `mapping` 中。

---
name: add-system-permission
description: 为系统添加新的系统级权限（action/menu/component），包括常量定义、角色绑定和注册。
---

# 添加系统权限

## 触发条件

当用户需要为某个功能添加访问控制时使用，例如：
- 新增一个 manager 页面，需要菜单权限
- 新增一个 API，需要 action 权限
- 新增一个前端组件，需要 component 权限

## 输入格式

用户需要提供：
1. 权限名称（如 `monitor.cleanup.read`）
2. 权限类型（action / menu / component）
3. 如果是 menu 或 component，还需要提供 path
4. 需要绑定到哪些角色（root 自动拥有所有权限，无需手动绑定）
5. 所属模块（主项目 or 某个子模块如 crystal-monitor）

## 前提信息

### 权限类型说明

| 类型 | 用途 | name 格式 | path |
|------|------|-----------|------|
| `action` | API 操作权限，用于 `@PreAuthorize` | `模块.资源.操作`，如 `monitor.cleanup.read` | 无 |
| `menu` | 前端菜单可见权限 | `模块:/manager/路径`，如 `monitor:/manager/monitor` | 前端路由路径 |
| `component` | 前端页面组件权限 | `模块.组件名@模块.组件名`，如 `dashboard.business.statistics@dashboard.business.statistics` | 同 name 的 `@` 后半段 |

### 注册方式：两条路径

#### 路径 A：主项目内置权限（推荐用于 crystal-starter 内的功能）

**第一步**：在 `SystemPermission.kt` 添加常量

```kotlin
// crystal-shared/.../constants/SystemPermission.kt
const val MENU_MY_FEATURE = "my.feature:/manager/my-feature"
const val ACTION_MY_FEATURE_READ = "my.feature.read"
const val ACTION_MY_FEATURE_UPDATE = "my.feature.update"
```

**第二步**：在 `SystemRolePermissionRelation.kt` 绑定到角色

```kotlin
// crystal-shared/.../constants/SystemRolePermissionRelation.kt
SystemRole.ROLE_ADMIN to listOf(
    // ... 已有权限 ...
    SystemPermission.MENU_MY_FEATURE,
    SystemPermission.ACTION_MY_FEATURE_READ,
)
```

`SystemSystemRbacConfigurer` 会在启动时自动扫描 `SystemPermission` 的所有属性并注册，无需其他操作。

#### 路径 B：子模块/插件权限（推荐用于 crystal-monitor 等独立模块）

**第一步**：在模块内定义常量文件

```kotlin
// crystal-mymodule/.../constants/MyModulePermission.kt
object MyModulePermission {
    const val MENU_MY_MODULE = "my.module:/manager/my-module"
    const val ACTION_MY_MODULE_READ = "my.module.read"
}
```

**第二步**：实现 `SystemRbacConfigurer`

```kotlin
// crystal-mymodule/.../config/MyModulePermissionConfigurer.kt
@Component
class MyModulePermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.permission(
            SystemRbacPermissionDeclaration.menu(
                name = MyModulePermission.MENU_MY_MODULE,
                path = "/manager/my-module",
            )
        )
        registry.permission(
            SystemRbacPermissionDeclaration.action(
                name = MyModulePermission.ACTION_MY_MODULE_READ,
            )
        )
        // 绑定到已有角色（admin 自动获得这些权限）
        registry.grantAll(SystemRole.ROLE_ADMIN)
    }
}
```

### 冲突检测

`SystemRbacRegistry` 在注册时会检测重复 name，遇到重复立即抛出 `IllegalStateException` 导致启动失败。添加前必须确认 name 全局唯一。

### 在 Controller 中使用

```kotlin
@PreAuthorize("hasAuthority('${MyModulePermission.ACTION_MY_MODULE_READ}')")
@GetMapping("/data")
suspend fun getData(): ApiResponse<...> { ... }
```

menu 权限由前端路由守卫自动校验，不需要在 controller 上标注。

### 角色说明

| 角色 | 说明 |
|------|------|
| `root` | 超级管理员，通过 `grantAll` 自动拥有所有权限，无需手动绑定 |
| `admin` | 管理员，需要在 `SystemRolePermissionRelation` 或 `Configurer` 中显式绑定 |
| `user` | 普通用户，通常只绑定少量前端组件权限 |

## 执行步骤

### 路径 A（主项目内置）

1. 阅读 `SystemPermission.kt`，确认新 name 不重复
2. 在 `SystemPermission.kt` 添加常量，按模块分组注释
3. 在 `SystemRolePermissionRelation.kt` 的对应角色列表中添加绑定

### 路径 B（子模块）

1. 确认模块内是否已有 Permission 常量文件，有则追加，无则新建
2. 确认模块内是否已有 `SystemRbacConfigurer` 实现，有则追加，无则新建
3. 在 Configurer 中注册权限并绑定角色

## 输出格式

完成后说明：
1. 新增的权限列表（name、类型、path）
2. 绑定到哪些角色
3. 修改/新建的文件路径
4. 如果是 action 权限，给出 `@PreAuthorize` 的使用示例

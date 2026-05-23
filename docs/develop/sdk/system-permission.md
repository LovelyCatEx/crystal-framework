# 系统权限

插件可以通过实现 `SystemRbacConfigurer` 注册系统级权限、角色和角色-权限绑定。

## 架构概览

```
插件提供 SystemRbacConfigurer Bean
  → RbacRegistryConfiguration 自动收集
    → 注册权限 → 注册角色 → 绑定角色权限
      → RbacTableDataCheckRunner 启动时同步到数据库
```

## 注册权限

实现 `SystemRbacConfigurer` 接口：

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
        // 注册权限
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

        // 注册角色
        registry.roles(
            SystemRoleDeclaration(
                name = "myplugin_manager",
                description = "My Plugin Manager",
                module = "myplugin"
            )
        )

        // 绑定权限到角色
        registry.bind(
            roleName = "myplugin_manager",
            permissionNames = setOf(
                "myplugin.data.read",
                "myplugin.data.write",
                "/manager/myplugin",
            )
        )

        // 将角色授权给已有角色（该角色的所有成员自动获得 myplugin_manager 的所有权限）
        registry.grantAll("myplugin_manager")
    }
}
```

### 权限声明类型

| 工厂方法 | 用途 | 示例 |
|---------|------|------|
| `action(name, description, module)` | API 操作权限 | `myplugin.data.read` |
| `menu(name, description, path, module)` | 菜单可见权限 | `/manager/myplugin` |
| `component(name, description, path, module)` | 页面组件权限 | `myplugin:dashboard` |

### 权限命名建议

- action 权限：`模块名.操作.资源`，如 `myplugin.data.read`
- menu 权限：`模块名:页面路径`，如 `/manager/myplugin`
- 每个权限的 `name` 必须全局唯一

### Role 与 grantAll

- `registry.grantAll(roleName)` 标记该角色拥有所有权限（包括未来新增的）
- 适用于 root、admin 等超级角色
- 普通角色应通过 `registry.bind()` 指定具体权限集合

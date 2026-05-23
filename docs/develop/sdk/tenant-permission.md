# 租户权限

插件可以通过实现 `TenantRbacConfigurer` 注册租户级权限、角色和角色-权限绑定。

## 架构概览

```
插件提供 TenantRbacConfigurer Bean
  → TenantRbacRegistryConfiguration 自动收集
    → 注册权限 → 注册角色 → 绑定角色权限 → 设置默认角色
```

## 注册租户权限

实现 `TenantRbacConfigurer` 接口：

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
        // 注册权限
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

        // 注册角色（支持层级）
        registry.roles(
            TenantRoleDeclaration(
                name = "myplugin_reporter",
                description = "Can view and export reports",
                parentRoleName = null,
            ),
        )

        // 绑定权限到角色
        registry.bind(
            roleName = "myplugin_reporter",
            permissionNames = setOf("myplugin.report.view", "myplugin.report.export"),
        )

        // 将角色设为租户默认成员角色（可选）
        registry.defaultMemberRole("myplugin_reporter")
    }
}
```

### 权限声明

`TenantPermissionDeclaration` 参数：

| 参数 | 说明 |
|------|------|
| `name` | 权限名称，建议 `模块.操作.资源`，全局唯一 |
| `description` | 描述 |
| `type` | `TenantPermissionType.ACTION` 或 `.MENU` |

### 角色层级

`TenantRoleDeclaration` 支持通过 `parentRoleName` 继承权限：

```kotlin
// super_admin 继承 admin 的所有权限
TenantRoleDeclaration(name = "tenant_admin", ...)
TenantRoleDeclaration(name = "tenant_super_admin", parentRoleName = "tenant_admin", ...)
```

### 默认角色

租户创建时会自动分配默认角色：

- `registry.defaultOwnerRole(roleName)` — 租户拥有者默认角色
- `registry.defaultMemberRole(roleName)` — 租户成员默认角色

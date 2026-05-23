# 租户权限

在框架源码内添加租户权限，需要修改 `crystal-starter` 中的常量定义。

## 涉及文件

| 文件 | 用途 |
|------|------|
| `TenantPermission.kt` | 定义 `TenantPermissionDeclaration` 对象 |
| `TenantRole.kt` | 定义 `TenantRoleDeclaration` 对象 |
| `TenantRolePermissionRelation.kt` | 定义角色-权限绑定关系 |

## 步骤 1：添加权限

在 `crystal-starter/src/main/kotlin/.../tenant/constants/TenantPermission.kt` 中添加：

```kotlin
val CUSTOM_REPORT_VIEW = TenantPermissionDeclaration(
    name = "custom.report.view",
    description = "查看报表",
    type = TenantPermissionType.ACTION,
)
val CUSTOM_REPORT_EXPORT = TenantPermissionDeclaration(
    name = "custom.report.export",
    description = "导出报表",
    type = TenantPermissionType.ACTION,
)
```

内置的 `TenantBuiltinRbacConfigurer` 通过 `TenantPermission.allPermissions()` 注册所有权限，新增会自动被包含。

### 权限命名

`name` 使用 `模块.操作.资源` 格式，全局唯一。

## 步骤 2：添加角色（可选）

在 `TenantRole.kt` 中添加，支持层级继承：

```kotlin
val CUSTOM_REPORTER = TenantRoleDeclaration(
    name = "custom_reporter",
    description = "报表查看者",
    parentRoleName = null,
)
```

`parentRoleName` 指定父角色，子角色继承父角色的所有权限。

## 步骤 3：绑定权限到角色

在 `TenantRolePermissionRelation.kt` 的 `mapping` 中添加：

```kotlin
mapping = mapOf(
    TenantRole.ADMIN to listOf(
        // ... 已有权限 ...
        TenantPermission.CUSTOM_REPORT_VIEW,
    ),
    TenantRole.CUSTOM_REPORTER to listOf(
        TenantPermission.CUSTOM_REPORT_VIEW,
        TenantPermission.CUSTOM_REPORT_EXPORT,
    ),
)
```

## 步骤 4：设置默认角色（可选）

在 `TenantBuiltinRbacConfigurer` 中设置：

```kotlin
registry.defaultOwnerRole(TenantRole.ROOT.name)
registry.defaultMemberRole(TenantRole.MEMBER.name)
```

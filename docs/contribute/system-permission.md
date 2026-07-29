# 系统权限

在框架源码内添加系统权限，需要修改 `crystal-shared` 模块中的常量定义。

## 涉及文件

| 文件 | 用途 |
|------|------|
| `SystemPermission.kt` | 定义 `SystemRbacPermissionDeclaration` 常量 |
| `SystemRole.kt` | 定义角色名称常量 |
| `SystemRolePermissionRelation.kt` | 定义角色-权限绑定关系 |

## 4 层前缀规范

`SystemPermission` 覆盖三层：`super` / `system` / `tenantAdmin`。权限的字符串 `name` 必须按下表选一个前缀，常量名段必须与前缀严格对应。

| 层 | authority 前缀 | 常量名段 | 语义 | 示例 name |
|---|---|---|---|---|
| `super` | `x.` | `ACTION_X_XXX` / `MENU_X_XXX` / `COMPONENT_X_XXX` | 跨 SYSTEM + TENANT 的超级管理员 | `x.dict.type.create` |
| `system` | `system.` | `ACTION_SYSTEM_XXX` / `MENU_SYSTEM_XXX` / `COMPONENT_SYSTEM_XXX` | 仅 SYSTEM scope | `system.user.create` |
| `tenantAdmin` | `tenant.` | `ACTION_TENANT_XXX` / `MENU_TENANT_XXX` | TENANT scope 且跨租户（运维层） | `tenant.member.create` |

第 4 层 `tenantPem`（`i.tenant.` 前缀）由 `TenantPermission.kt` 承载，见 [租户权限](./tenant-permission.md)。

前缀规范由 `PermissionMatrix.init` **在构造时 hard-error**（`throw IllegalStateException`），由 `PermissionNameConventionTest` 静态扫描 `allPermissions()` 结果验证。

## 步骤 1：添加权限 Declaration

在 `crystal-shared/src/main/kotlin/.../shared/constants/SystemPermission.kt` 中，按业务域分区追加 `val`：

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

要点：

- 每一条权限都是 `val SystemRbacPermissionDeclaration`（不再是 `const val String`）
- 用三个工厂：`.action(name, description)` / `.menu(name, path, description)` / `.component(name, path, description)`
- 若声明所在的业务域跨多层（如 `x + system + tenantAdmin`），把三层的 `val` 放在同一分区块中，便于对照
- 需要字符串的地方一律用 `.name`
- 不需要在任何 `Map<String, String>` 中同步 description（description 内嵌到 Declaration 中）
- `SystemSystemRbacConfigurer` 通过 `SystemPermission.allPermissions()` 自动注册，新增无需改配置

## 步骤 2：添加角色（可选）

在 `SystemRole.kt` 中添加：

```kotlin
const val ROLE_MYPLUGIN_MANAGER = "myplugin_manager"
```

## 步骤 3：绑定权限到角色

在 `SystemRolePermissionRelation.kt` 的 `mapping` 中添加。引用的是 Declaration 而不是字符串：

```kotlin
mapping = mapOf(
    SystemRole.ROLE_ADMIN to listOf(
        // ... 已有权限 ...
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ,
    ),
    SystemRole.ROLE_MYPLUGIN_MANAGER to listOf(
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ,
        SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_UPDATE,
        SystemPermission.MENU_SYSTEM_MYPLUGIN_MANAGER,
    ),
)
```

新角色需要同时添加到 `SystemRole.kt` 和 `mapping` 中；`SystemSystemRbacConfigurer` 会在启动时把绑定关系写回数据库。

## 在 `@PreAuthorize` 中引用

Spring `@PreAuthorize` 只接受编译期常量字符串，不能用 `SystemPermission.ACTION_SYSTEM_MYPLUGIN_DATA_READ.name`（非 const）。写字面量即可：

```kotlin
@PreAuthorize("hasAuthority('system.myplugin.data.read')")
@GetMapping("/my-endpoint")
fun myEndpoint(): ApiResponse<Data> { ... }
```

`PreAuthorizeCoverageTest` 会扫描全项目所有 `@PreAuthorize` 字面量，若字符串在 `SystemPermission.allPermissions()` 或 `TenantPermission.allPermissions()` 里找不到匹配的 `.name`，测试立即失败——因此拼错也会被拦截。

## 数据库迁移

本次命名重构随附 Flyway `V20260729.01__reset_permissions_for_naming_redesign.sql`，会 hard-delete 所有旧权限行和角色-权限绑定。启动时 Configurer 会按新常量重新注册全量权限并按 `SystemRolePermissionRelation.mapping` 恢复内置绑定。用户在 UI 上自定义的角色-权限绑定会丢失，部署时必须公告，由每个 admin 重新绑定。

设计背景与旧命名对照见 [权限模型迁移指南](../develop/controller/permission-migration.md) 与 `.claude/research/permission-naming-redesign.md`。

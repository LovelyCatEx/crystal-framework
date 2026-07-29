# 租户权限

在框架源码内添加租户权限（`tenantPem` 层），需要修改 `crystal-rbac` 模块中的常量定义。

## 涉及文件

| 文件 | 用途 |
|------|------|
| `TenantPermission.kt` | 定义 `TenantPermissionDeclaration` 常量 |
| `TenantRole.kt` | 定义 `TenantRoleDeclaration` 常量 |
| `TenantRolePermissionRelation.kt` | 定义角色-权限绑定关系 |

## 前缀规范

`TenantPermission` 只承载一层 `tenantPem`。权限的字符串 `name` 必须以 `i.tenant.` 开头（含义：in-tenant / internal-tenant，即"用户在自己所属租户内"），常量名不再带 `TENANT` 段（整个类都是 tenant，冗余），也不再带 `_PEM` 后缀（不再双写字符串 + Declaration）。

| 层 | authority 前缀 | 常量名段 | 语义 | 示例 name |
|---|---|---|---|---|
| `tenantPem` | `i.tenant.` | `ACTION_XXX` / `MENU_XXX` | TENANT scope 且严格匹配 tenantId | `i.tenant.role.create` |

跨 scope（`x.`）、SYSTEM scope（`system.`）、跨租户（`tenant.`）三层放在 `SystemPermission.kt`，见 [系统权限](./system-permission.md)。

前缀规范由 `PermissionMatrix.init` **在构造时 hard-error**（`throw IllegalStateException`），由 `PermissionNameConventionTest` 静态扫描 `allPermissions()` 结果验证。

## 步骤 1：添加权限 Declaration

在 `crystal-rbac/src/main/kotlin/.../rbac/tenant/constants/TenantPermission.kt` 中，按业务域分区追加 `val`：

```kotlin
// ============================================================
//   Report
// ============================================================
val MENU_REPORT = TenantPermissionDeclaration(
    name = "i.tenant.report",
    description = "My tenant reports menu",
    type = TenantPermissionType.MENU,
    path = "/manager/tenant/reports",
)

val ACTION_REPORT_READ = TenantPermissionDeclaration(
    name = "i.tenant.report.read",
    description = "Read reports within own tenant",
    type = TenantPermissionType.ACTION,
)

val ACTION_REPORT_EXPORT = TenantPermissionDeclaration(
    name = "i.tenant.report.export",
    description = "Export reports within own tenant",
    type = TenantPermissionType.ACTION,
)
```

要点：

- 每一条权限都是 `val TenantPermissionDeclaration`（**已消灭 `_PEM` 双写**：过去每条权限要写一个 `const val String` + 一个 `val Declaration`，现在只有 Declaration，需要字符串一律 `.name`）
- 直接构造 `TenantPermissionDeclaration(...)`，用 `type = TenantPermissionType.ACTION / MENU`（MENU 需要 `path`）
- 常量名 `ACTION_XXX` / `MENU_XXX`，不带 `TENANT` 段
- `TenantBuiltinRbacConfigurer` 通过 `TenantPermission.allPermissions()` 自动注册，新增无需改配置

## 步骤 2：添加角色（可选）

在 `TenantRole.kt` 中添加，支持层级继承：

```kotlin
val CUSTOM_REPORTER = TenantRoleDeclaration(
    name = "custom_reporter",
    description = "Report viewer",
    parentRoleName = null,
)
```

`parentRoleName` 指定父角色，子角色继承父角色的所有权限。

## 步骤 3：绑定权限到角色

在 `TenantRolePermissionRelation.kt` 的 `mapping` 中添加。引用的是 Declaration 而不是字符串：

```kotlin
mapping = mapOf(
    TenantRole.ADMIN to listOf(
        // ... 已有权限 ...
        TenantPermission.ACTION_REPORT_READ,
    ),
    TenantRole.CUSTOM_REPORTER to listOf(
        TenantPermission.MENU_REPORT,
        TenantPermission.ACTION_REPORT_READ,
        TenantPermission.ACTION_REPORT_EXPORT,
    ),
)
```

## 步骤 4：设置默认角色（可选）

在 `TenantBuiltinRbacConfigurer` 中设置：

```kotlin
registry.defaultOwnerRole(TenantRole.ROOT.name)
registry.defaultMemberRole(TenantRole.MEMBER.name)
```

## 在 `@PreAuthorize` 中引用

Spring `@PreAuthorize` 只接受编译期常量字符串，不能用 `TenantPermission.ACTION_REPORT_READ.name`（非 const）。写字面量即可：

```kotlin
@PreAuthorize("hasAuthority('i.tenant.report.read')")
@GetMapping("/my-endpoint")
fun myEndpoint(): ApiResponse<Data> { ... }
```

`PreAuthorizeCoverageTest` 会扫描全项目所有 `@PreAuthorize` 字面量，若字符串在 `SystemPermission.allPermissions()` 或 `TenantPermission.allPermissions()` 里找不到匹配的 `.name`，测试立即失败——因此拼错也会被拦截。

## 数据库迁移

本次命名重构随附 Flyway `V20260729.01__reset_permissions_for_naming_redesign.sql`，会 hard-delete 所有旧权限行和角色-权限绑定。启动时 Configurer 会按新常量重新注册全量权限并按 `TenantRolePermissionRelation.mapping` 恢复内置绑定。租户 admin 在 UI 上自定义的角色-权限绑定会丢失，部署时必须公告，由每个租户 admin 重新绑定。

设计背景与旧命名对照见 [权限模型迁移指南](../develop/controller/permission-migration.md) 与 `.claude/research/permission-naming-redesign.md`。

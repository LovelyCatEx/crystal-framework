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

1. 权限所属业务域（如 `user`、`dict.type`、`monitor.sessions` 等）
2. 授权层：`super`（x）/ `system` / `tenantAdmin`（tenant）/ `tenantPem`（i.tenant）
3. 权限类型（action / menu / component）
4. 如果是 menu 或 component，还需要提供 path
5. 需要绑定到哪些角色（root 自动拥有所有权限，无需手动绑定）
6. 所属模块（主项目 or 某个子模块如 crystal-monitor）

## 前提信息

### 授权层与前缀严格对应（强制）

系统采用 4 层授权模型，每层的字符串前缀和 Kotlin 常量名的层段必须严格对应。这是 `PermissionMatrix.init` 的 hard-error 校验项，也是 `PermissionNameConventionTest` 的测试项。

| 授权层 | 字符串前缀 | Kotlin 常量名段 | 承载类 | 语义 |
|---|---|---|---|---|
| super | `x.` | `ACTION_X_XXX` / `MENU_X_XXX` / `COMPONENT_X_XXX` | `SystemPermission` | 跨 SYSTEM + TENANT，只有真正跨 scope 才用 |
| system | `system.` | `ACTION_SYSTEM_XXX` / `MENU_SYSTEM_XXX` / `COMPONENT_SYSTEM_XXX` | `SystemPermission` | 仅 SYSTEM scope（含"管理租户档案"这类系统级操作） |
| tenantAdmin | `tenant.` | `ACTION_TENANT_XXX` / `MENU_TENANT_XXX` | `SystemPermission` | TENANT scope 且跨租户（无 tenantId 匹配） |
| tenantPem | `i.tenant.` | `ACTION_XXX` / `MENU_XXX`（无 TENANT 段） | `TenantPermission` | TENANT scope 且严格匹配 tenantId |

**⛔ 常见错误：**

- `tenant.create` 想表达"创建租户"→ 错，该操作是系统级 → 用 `system.tenant.create`
- `permission.create` 无前缀 → 错，SYSTEM scope → 用 `system.permission.create`
- 常量名 `ACTION_USER_CREATE` name `system.user.create` → 错，常量名缺 SYSTEM 段 → 改成 `ACTION_SYSTEM_USER_CREATE`

### 权限类型说明

| 类型 | 用途 | 工厂方法 | 是否需要 path |
|---|---|---|---|
| ACTION | API 操作权限 | `SystemRbacPermissionDeclaration.action(name, description)` | 否 |
| MENU | 前端菜单可见权限 | `SystemRbacPermissionDeclaration.menu(name, path, description)` | 是（前端路由路径，如 `/manager/users`） |
| COMPONENT | 前端页面组件权限 | `SystemRbacPermissionDeclaration.component(name, path, description)` | 是（组件 id，如 `dashboard.business.statistics`） |

Tenant 侧同理：`TenantPermissionDeclaration(name, description, type, path)`。

### 注册方式：两条路径

#### 路径 A：主项目内置权限（`SystemPermission` / `TenantPermission`）

**第一步**：在对应文件中按业务域找到分区（或新建分区），添加 Declaration val

```kotlin
// crystal-shared/.../shared/constants/SystemPermission.kt

// ============================================================
//   Cleanup  (system)     <- 新增分区示例
// ============================================================
val ACTION_SYSTEM_CLEANUP_READ = SystemRbacPermissionDeclaration.action(
    name = "system.cleanup.read",
    description = "Read cleanup status"
)
val ACTION_SYSTEM_CLEANUP_UPDATE = SystemRbacPermissionDeclaration.action(
    name = "system.cleanup.update",
    description = "Trigger cleanup"
)
val MENU_SYSTEM_CLEANUP_MANAGER = SystemRbacPermissionDeclaration.menu(
    name = "system.cleanup",
    path = "/manager/cleanup",
    description = "Cleanup management menu"
)
```

租户 pem 层权限写入 `TenantPermission.kt`（同样按业务域分区，常量名**不带** TENANT 段）：

```kotlin
// crystal-rbac/.../rbac/tenant/constants/TenantPermission.kt

// ============================================================
//   Cleanup
// ============================================================
val ACTION_CLEANUP_READ = TenantPermissionDeclaration(
    name = "i.tenant.cleanup.read",
    description = "Read own tenant cleanup status",
    type = TenantPermissionType.ACTION,
)
```

**第二步**：在 `SystemRolePermissionRelation.kt`（系统角色）或 `TenantRolePermissionRelation.kt`（租户内置角色）中绑定到角色

```kotlin
// crystal-shared/.../shared/constants/SystemRolePermissionRelation.kt
SystemRole.ROLE_ADMIN to listOf(
    // ... 已有权限 ...
    // Cleanup
    SystemPermission.MENU_SYSTEM_CLEANUP_MANAGER,
    SystemPermission.ACTION_SYSTEM_CLEANUP_READ,
),
```

`ROLE_ROOT` 通过 `SystemPermission.allPermissions()` 自动获得**全部** SystemPermission，无需手动补。同理 `SystemSystemRbacConfigurer` 会在启动时扫描并注册全部 Declaration，无需其他操作。

#### 路径 B：子模块 / 插件权限

**推荐做法（新模块）：** 直接使用 `SystemRbacPermissionDeclaration` 工厂，跟 `SystemPermission` 保持结构一致。

```kotlin
// crystal-mymodule/.../constants/MyModulePermission.kt
object MyModulePermission {
    val ACTION_SYSTEM_MYMODULE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.mymodule.read",
        description = "Read mymodule data"
    )
    val MENU_SYSTEM_MYMODULE = SystemRbacPermissionDeclaration.menu(
        name = "system.mymodule",
        path = "/manager/mymodule",
        description = "MyModule management menu"
    )
}
```

**遗留做法（兼容存量模块，如 `crystal-monitor`）：** `const val` 字符串 + 在 Configurer 里用工厂重新包一层。仅在存量代码里保留，新模块不推荐。

```kotlin
// crystal-monitor/.../constants/MonitorPermission.kt（遗留）
object MonitorPermission {
    const val MENU_SYSTEM_MONITOR = "system.monitor:/manager/monitor/system-metrics"
    const val ACTION_SYSTEM_MONITOR_READ = "system.monitor.read"
}
```

**注册**：实现 `SystemRbacConfigurer`

```kotlin
// crystal-mymodule/.../config/MyModulePermissionConfigurer.kt
@Component
class MyModulePermissionConfigurer : SystemRbacConfigurer {
    override fun configure(registry: SystemRbacRegistry) {
        registry.permission(MyModulePermission.ACTION_SYSTEM_MYMODULE_READ)
        registry.permission(MyModulePermission.MENU_SYSTEM_MYMODULE)
        // 绑定到已有角色（root 通过 grantAll 自动获得）
        registry.grantAll(SystemRole.ROLE_ADMIN)
    }
}
```

若模块用的是遗留 `const val` 字符串常量，Configurer 里必须手动 `SystemRbacPermissionDeclaration.menu(name = ..., path = ...)` 包一层（见 `MonitorPermissionConfigurer`）——但这样比 Declaration 方式多一次映射，容易漏字段。

### 冲突检测

`SystemRbacRegistry` 在注册时会检测重复 name，遇到重复立即抛出 `IllegalStateException` 导致启动失败。添加前必须确认 name 全局唯一（跨 `SystemPermission` + `TenantPermission` + 各模块 `XxxPermission` 都不能重）。

### 在 Controller 中使用

#### 用 `PermissionMatrix`（推荐，绝大多数场景）

Manager Controller 家族统一走 `PermissionMatrix`（不同基类对应不同便捷工厂），构造参数用 `.name` 取字符串：

```kotlin
class ManagerCleanupController(...) : StandardManagerController<...>(
    ...,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_CLEANUP_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE.name,
    ),
)
```

`PermissionMatrix.init` 会在构造时对每个槽做前缀校验，前缀跟层不匹配直接 `throw IllegalStateException` 导致启动失败——没有 warn 兜底。

#### 用 `@PreAuthorize`（少量非 CRUD 端点）

Spring Security 的 `@PreAuthorize` 只接受编译期字符串常量，`SystemPermission.ACTION_XXX` 是 `val`（Declaration 对象）不是 `const val String`，**无法插值**。必须**手写字面量**：

```kotlin
@PreAuthorize("hasAuthority('system.cleanup.read')")
@GetMapping("/status")
suspend fun getStatus(): ApiResponse<...> { ... }
```

`PreAuthorizeCoverageTest` 会扫描项目里所有 `@PreAuthorize("hasAuthority('xxx')")` 字面量，`xxx` 必须能在 `SystemPermission.allPermissions()` 或 `TenantPermission.allPermissions()` 里找到匹配的 `.name`——**打错字直接测试失败**。

menu / component 权限由前端路由守卫和菜单渲染自动校验，不需要在 controller 上标注。

### 角色说明

| 角色 | 说明 |
|---|---|
| `root` | 超级管理员，通过 `grantAll` 自动拥有所有权限，无需手动绑定 |
| `admin` | 系统管理员，需在 `SystemRolePermissionRelation` 或模块 `Configurer` 中显式绑定 |
| `user` | 普通用户，通常只绑定少量前端组件权限 |
| 租户内置角色 | 参见 `TenantRolePermissionRelation` |

## 执行步骤

### 路径 A（主项目 SystemPermission / TenantPermission）

1. 阅读 `SystemPermission.kt`（或 `TenantPermission.kt`）当前所有 name，确认新 name 不重复
2. 根据权限所属**授权层**选出前缀：`x.` / `system.` / `tenant.` / `i.tenant.`
3. 根据权限**业务域**找到现有的 `// ==== 分区注释 ====` 分区；若无则新建（分区注释格式：`// ============================================================\n//   <Domain>  (<layers>)\n// ============================================================`）
4. 添加 Declaration val：常量名段（`X` / `SYSTEM` / `TENANT` / 无）必须跟 name 前缀严格对应
5. 用工厂方法 `SystemRbacPermissionDeclaration.action(...)` / `.menu(...)` / `.component(...)`（或 `TenantPermissionDeclaration(...)`），description 内嵌到 Declaration 里，禁止外部维护 DESCRIPTIONS map
6. 在 `SystemRolePermissionRelation.kt` / `TenantRolePermissionRelation.kt` 对应角色列表中添加绑定（引用 Declaration val 本体，不是字符串）
7. 若涉及 Controller，`PermissionMatrix` 槽用 `SystemPermission.XXX.name` 传入
8. 若涉及 `@PreAuthorize`，手写字面量必须跟 Declaration.name 完全一致

### 路径 B（子模块 Configurer）

1. 确认模块内是否已有 `XxxPermission` 常量文件；有则追加，无则新建
2. **新模块**：直接用 `SystemRbacPermissionDeclaration` 工厂声明（跟 `SystemPermission` 结构一致，常量名段必须跟 name 前缀对应）
3. **存量模块（如 `crystal-monitor`）**：可继续用 `const val String`，但需要在 Configurer 里手动 `SystemRbacPermissionDeclaration.menu/action(name = ..., path = ...)` 包一层
4. 确认模块内是否已有 `SystemRbacConfigurer` 实现（`@Component` 标注）；有则追加，无则新建
5. 在 Configurer 中 `registry.permission(...)` 注册每一个权限，`registry.grantAll(SystemRole.ROLE_XXX)` 绑定角色
6. Controller / `@PreAuthorize` 使用规则同路径 A

## 校验清单（4 道保险）

添加/修改权限后，必须同时通过以下所有校验：

1. **`PermissionMatrix.init` hard error**：任一 Controller 的 `PermissionMatrix` 槽中的 name 前缀跟其所属层不匹配（如 `system` 槽塞了 `x.` 前缀的常量），**应用启动直接 `throw IllegalStateException` 崩溃**
2. **`PermissionNameConventionTest`**：`SystemPermission` name 必须以 `x.` / `system.` / `tenant.` 之一开头；`TenantPermission` name 必须以 `i.tenant.` 开头；Kotlin 常量名的层段（`ACTION_X_` / `ACTION_SYSTEM_` / `ACTION_TENANT_` / 无）必须跟 name 前缀严格对应；MENU / COMPONENT 必须带 path，ACTION 不能带 path；name 全局唯一
3. **`PreAuthorizeCoverageTest`**：项目里所有 `@PreAuthorize("hasAuthority('xxx')")` 的 `xxx` 必须能在 `SystemPermission` / `TenantPermission` / 模块 `XxxPermission` 的 `.name` 集合里找到匹配
4. **`SystemRbacRegistry` 冲突检测**：启动时若发现重复 name，抛 `IllegalStateException` 拒绝启动

**违反前缀规则将同时触发校验 1 和 2；打错 `@PreAuthorize` 字面量将触发校验 3；name 冲突将触发校验 4。任意一条不通过都无法上线。**

## 输出格式

完成后必须说明：

1. 新增/修改的权限列表（Kotlin 常量名、字符串 name、类型、path、所属层）
2. 绑定到哪些角色（哪个文件的哪个列表）
3. 修改/新建的文件路径
4. 若是 action 权限，给出对应的 `PermissionMatrix` 或 `@PreAuthorize` 使用示例
5. 逐条确认已通过 4 道校验（`PermissionMatrix.init` / `PermissionNameConventionTest` / `PreAuthorizeCoverageTest` / `SystemRbacRegistry` 冲突检测）

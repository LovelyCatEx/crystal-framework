---
name: add-system-permission
description: 为系统添加新的系统级权限（action/menu/component），包括常量定义、角色绑定、注册和前端 i18n 同步。
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
6. 所属模块（主项目 or 某个子模块如 crystal-schedule）

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

**⚠️ 权限常量命名规则（强制）：每个权限必须先定义 `const val XXX_NAME = "string"`，再定义 `val XXX = Declaration(XXX_NAME, ...)`，两者必须紧挨着，禁止分开放。**

```kotlin
// crystal-shared/.../shared/constants/SystemPermission.kt

// ============================================================
//   Cleanup  (system)     <- 新增分区示例
// ============================================================
const val ACTION_SYSTEM_CLEANUP_READ_NAME = "system.cleanup.read"
val ACTION_SYSTEM_CLEANUP_READ = SystemRbacPermissionDeclaration.action(
    name = ACTION_SYSTEM_CLEANUP_READ_NAME,
    description = "Read cleanup status"
)

const val ACTION_SYSTEM_CLEANUP_UPDATE_NAME = "system.cleanup.update"
val ACTION_SYSTEM_CLEANUP_UPDATE = SystemRbacPermissionDeclaration.action(
    name = ACTION_SYSTEM_CLEANUP_UPDATE_NAME,
    description = "Trigger cleanup"
)

const val MENU_SYSTEM_CLEANUP_MANAGER_NAME = "system.cleanup"
val MENU_SYSTEM_CLEANUP_MANAGER = SystemRbacPermissionDeclaration.menu(
    name = MENU_SYSTEM_CLEANUP_MANAGER_NAME,
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
const val ACTION_CLEANUP_READ_NAME = "i.tenant.cleanup.read"
val ACTION_CLEANUP_READ = TenantPermissionDeclaration(
    name = ACTION_CLEANUP_READ_NAME,
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

#### 路径 B：子模块权限

**推荐做法（新模块）：** 直接使用 `SystemRbacPermissionDeclaration` 工厂，跟 `SystemPermission` 保持结构一致。

```kotlin
// crystal-mymodule/.../constants/MyModulePermission.kt
object MyModulePermission {
    const val ACTION_SYSTEM_MYMODULE_READ_NAME = "system.mymodule.read"
    val ACTION_SYSTEM_MYMODULE_READ = SystemRbacPermissionDeclaration.action(
        name = ACTION_SYSTEM_MYMODULE_READ_NAME,
        description = "Read mymodule data"
    )
    
    const val MENU_SYSTEM_MYMODULE_NAME = "system.mymodule"
    val MENU_SYSTEM_MYMODULE = SystemRbacPermissionDeclaration.menu(
        name = MENU_SYSTEM_MYMODULE_NAME,
        path = "/manager/mymodule",
        description = "MyModule management menu"
    )
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

### 冲突检测

`SystemRbacRegistry` 在注册时会检测重复 name，遇到重复立即抛出 `IllegalStateException` 导致启动失败。添加前必须确认 name 全局唯一（跨 `SystemPermission` + `TenantPermission` + 各模块 `XxxPermission` 都不能重）。

### 在 Controller 中使用

#### 用 `PermissionMatrix`（推荐，绝大多数场景）

Manager Controller 家族统一走 `PermissionMatrix`（不同基类对应不同便捷工厂），构造参数用 NAME 常量：

```kotlin
class ManagerCleanupController(...) : StandardManagerController<...>(
    ...,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE_NAME,
        systemRead   = SystemPermission.ACTION_SYSTEM_CLEANUP_READ_NAME,
        systemUpdate = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE_NAME,
        systemDelete = SystemPermission.ACTION_SYSTEM_CLEANUP_UPDATE_NAME,
    ),
)
```

`PermissionMatrix.init` 会在构造时对每个槽做前缀校验，前缀跟层不匹配直接 `throw IllegalStateException` 导致启动失败——没有 warn 兜底。

#### 用 `@RequiresAuthority`（少量非 CRUD 端点）

非 Manager Controller 家族的端点用 `@RequiresAuthority`。该注解只接受编译期字符串常量，`SystemPermission.ACTION_XXX` 是 `val`（Declaration 对象）不是 `const val String`，**无法插值**，必须**手写字面量**：

```kotlin
@GetMapping("/query/batch")
@RequiresAuthority(anyOf = ["system.cleanup.read"], scope = ResourceScope.SYSTEM)
fun batchQuery(...): Mono<ApiResponse<...>> { ... }
```

`RequiresAuthorityCoverageTest` 会扫描项目里所有 `@RequiresAuthority` 的 `anyOf`/`allOf` 字面量，必须能在 `SystemPermission.allPermissions()` 或 `TenantPermission.allPermissions()` 里找到匹配——**打错字直接测试失败**。

**⚠️ `@PreAuthorize` 已被 `SpringSecurityAnnotationBanBeanFactoryPostProcessor` 在启动时硬禁用，禁止新代码使用。**

menu / component 权限由前端路由守卫和菜单渲染自动校验，不需要在 controller 上标注。

### 角色说明

| 角色 | 说明 |
|---|---|
| `root` | 超级管理员，通过 `grantAll` 自动拥有所有权限，无需手动绑定 |
| `admin` | 系统管理员，需在 `SystemRolePermissionRelation` 或模块 `Configurer` 中显式绑定 |
| `user` | 普通用户，通常只绑定少量前端组件权限 |
| 租户内置角色 | 参见 `TenantRolePermissionRelation` |

### 前端 i18n 同步

每新增一个权限，**必须**同步在前端两个 locale 文件的 `pages.permissionCatalog.byName` 对象里添加对应的中英文描述：

```
web/src/i18n/locales/en-US.ts
web/src/i18n/locales/zh-CN.ts
```

key 就是权限的 `name` 字符串，value 是面向用户的简短描述（非技术术语）：

```typescript
// en-US.ts — pages.permissionCatalog.byName
'system.cleanup':        'Cleanup management menu',
'system.cleanup.read':   'Read cleanup status',
'system.cleanup.update': 'Trigger system cleanup',
```

```typescript
// zh-CN.ts — pages.permissionCatalog.byName
'system.cleanup':        '清理管理菜单',
'system.cleanup.read':   '查看清理状态',
'system.cleanup.update': '触发系统清理',
```

规则：
- key 与后端 Declaration 的 `name` 完全一致（一字不差）
- 两个 locale 文件条目数量必须一一对应，禁止一边多一边少
- 描述用人类可读语言，不要重复技术名词（如避免 `"system.cleanup.read"` → `"System cleanup read"`）
- 将新条目插入到同业务域的相邻行，保持文件可读性

这些翻译会被 `pages.permissionCatalog.byName` 的权限树（`PermissionTreeTable`）和权限描述切换（i18n/DB）使用。

## 执行步骤

### 路径 A（主项目 SystemPermission / TenantPermission）

1. 阅读 `SystemPermission.kt`（或 `TenantPermission.kt`）当前所有 name，确认新 name 不重复
2. 根据权限所属**授权层**选出前缀：`x.` / `system.` / `tenant.` / `i.tenant.`
3. 根据权限**业务域**找到现有的 `// ==== 分区注释 ====` 分区；若无则新建（分区注释格式：`// ============================================================\n//   <Domain>  (<layers>)\n// ============================================================`）
4. 添加 Declaration val：常量名段（`X` / `SYSTEM` / `TENANT` / 无）必须跟 name 前缀严格对应
5. 用工厂方法 `SystemRbacPermissionDeclaration.action(...)` / `.menu(...)` / `.component(...)`（或 `TenantPermissionDeclaration(...)`），description 内嵌到 Declaration 里，禁止外部维护 DESCRIPTIONS map
6. 在 `SystemRolePermissionRelation.kt` / `TenantRolePermissionRelation.kt` 对应角色列表中添加绑定（引用 Declaration val 本体，不是字符串）
7. 若涉及 Controller，`PermissionMatrix` 槽用 `SystemPermission.XXX.name` 传入；非 CRUD 端点用 `@RequiresAuthority(anyOf = ["<literal>"], scope = ...)`
8. **在 `web/src/i18n/locales/en-US.ts` 和 `zh-CN.ts` 的 `pages.permissionCatalog.byName` 中添加对应翻译**（en/zh 必须同步）

### 路径 B（子模块 Configurer）

1. 确认模块内是否已有 `XxxPermission` 常量文件；有则追加，无则新建
2. 直接用 `SystemRbacPermissionDeclaration` 工厂声明（跟 `SystemPermission` 结构一致，常量名段必须跟 name 前缀对应）
3. 确认模块内是否已有 `SystemRbacConfigurer` 实现（`@Component` 标注）；有则追加，无则新建
4. 在 Configurer 中 `registry.permission(...)` 注册每一个权限，`registry.grantAll(SystemRole.ROLE_XXX)` 绑定角色
5. Controller / `@RequiresAuthority` 使用规则同路径 A
6. **在 `web/src/i18n/locales/en-US.ts` 和 `zh-CN.ts` 的 `pages.permissionCatalog.byName` 中添加对应翻译**（en/zh 必须同步）

## 校验清单（5 道保险）

添加/修改权限后，必须同时通过以下所有校验：

1. **`PermissionMatrix.init` hard error**：任一 Controller 的 `PermissionMatrix` 槽中的 name 前缀跟其所属层不匹配（如 `system` 槽塞了 `x.` 前缀的常量），**应用启动直接 `throw IllegalStateException` 崩溃**
2. **`PermissionNameConventionTest`**：`SystemPermission` name 必须以 `x.` / `system.` / `tenant.` 之一开头；`TenantPermission` name 必须以 `i.tenant.` 开头；Kotlin 常量名的层段必须跟 name 前缀严格对应；MENU / COMPONENT 必须带 path，ACTION 不能带 path；name 全局唯一
3. **`RequiresAuthorityCoverageTest`**：项目里所有 `@RequiresAuthority(anyOf = ["xxx"])` 的字面量必须能在 `SystemPermission.allPermissions()` 或 `TenantPermission.allPermissions()` 里找到匹配的 `.name`
4. **`SystemRbacRegistry` 冲突检测**：启动时若发现重复 name，抛 `IllegalStateException` 拒绝启动
5. **前端 i18n 完整性**：`en-US.ts` 和 `zh-CN.ts` 的 `pages.permissionCatalog.byName` 新增条目数量一一对应，`tsc --noEmit` 编译通过（i18n-rules 类型校验）

**违反前缀规则将同时触发校验 1 和 2；打错 `@RequiresAuthority` 字面量将触发校验 3；name 冲突将触发校验 4；i18n 条目不对称将触发校验 5。任意一条不通过都无法上线。**

## 输出格式

完成后必须说明：

1. 新增/修改的权限列表（Kotlin 常量名、字符串 name、类型、path、所属层）
2. 绑定到哪些角色（哪个文件的哪个列表）
3. 修改/新建的文件路径（含前端 i18n 文件）
4. 若是 action 权限，给出对应的 `PermissionMatrix` 或 `@RequiresAuthority` 使用示例
5. 逐条确认已通过 5 道校验

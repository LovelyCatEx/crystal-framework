# 权限模型迁移指南（PermissionMatrix + 命名重构）

本项目的权限模型经历了两轮统一：

1. **模型统一**：`crystal-shared` 的 Manager Controller 家族过去存在三套并行的权限声明方式：
   - `@ManagerPermissions` 类注解 + AOP（Standard / Readonly）
   - `ScopedPermissionTriad`（Scoped / DerivedScoped / ReadonlyScoped）
   - 8 个 String 构造参数（Tenant）

   统一后的 `PermissionMatrix`（`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`）以 4 层 × 4 操作 = 16 个 authority 字段取代上述三种模型。所有 Manager Controller 都通过构造参数 `permissions = ...` 接收同一份数据类。

2. **命名重构**（2026-07-29）：权限常量从 `const val String` 升级为 `val Declaration`，`_PEM` 双写彻底消灭；4 层前缀（`x.` / `system.` / `tenant.` / `i.tenant.`）严格强制。

本指南覆盖两轮迁移的写法与常见坑。

## 4 层权限速查

| 层             | authority 前缀       | 常量来源                | 语义                                                    |
|---------------|-------------------|---------------------|-------------------------------------------------------|
| `super`       | `x.`             | `SystemPermission`  | 跨 SYSTEM + TENANT 的超级管理员，任意 scope 均放行                  |
| `system`      | `system.`         | `SystemPermission`  | 仅 SYSTEM scope                                        |
| `tenantAdmin` | `tenant.`         | `SystemPermission`  | TENANT scope 且跨租户 —— 无 tenantId 匹配（"运维管理员"）           |
| `tenantPem`   | `i.tenant.`       | `TenantPermission`  | TENANT scope 且严格匹配 tenantId                            |

匹配规则（由 `PermissionMatrix.layersFor(scope, op)` 决定）：

- **SYSTEM 请求** → OR-check `[super<op>, system<op>]`（跳过 `NOT_APPLICABLE`）
- **TENANT 请求** → OR-check `[super<op>, tenantAdmin<op>, tenantPem<op>]`（跳过 `NOT_APPLICABLE`）

跨租户判定（`crossTenantLayersFor(op)`，用于 `StandardScopedManagerController.checkOwnership`）：仅 `super` + `tenantAdmin` 属于跨租户层；持有它们的用户绕过 tenantId 相等检查。

## 两个哨兵值

| 常量                             | 语义                                                        | `layersFor` 行为                    |
|--------------------------------|-----------------------------------------------------------|-----------------------------------|
| `PermissionMatrix.NOT_APPLICABLE` | 该层对本资源不适用（如 SYSTEM-only 资源的两个 tenant 层）                    | **过滤掉**，不参与 OR 判定                 |
| `PermissionMatrix.NEVER_GRANTED`  | 该层在概念上存在但操作被禁止（如 Readonly 的 CUD）                          | **保留占位**，但永远不匹配任何真实 authority，恒失败 |

选用规则：**"层不该存在"用 `NOT_APPLICABLE`，"层存在但操作被封死"用 `NEVER_GRANTED`。** 便捷工厂已经把默认值填好，通常无需手写。

## 4 个便捷工厂

`PermissionMatrix.of { ... }` 是完整 DSL，覆盖任意组合。对于常见场景另有 3 个 Kotlin 扩展工厂：

| 工厂                                  | 适用                       | 未填的层            |
|-------------------------------------|--------------------------|-----------------|
| `PermissionMatrix.of { ... }`         | 完整 DSL，4 层任意组合           | 未打开的 layer 全部 `NOT_APPLICABLE` |
| `PermissionMatrix.systemOnly(...)`    | 无 scope 的全局 CRUD（Standard） | tenant 层全部 `NOT_APPLICABLE`   |
| `PermissionMatrix.tenantOnly(...)`    | 仅 TENANT 资源（Tenant）      | super + system 全部 `NOT_APPLICABLE` |
| `PermissionMatrix.systemOnlyReadonly(...)` | SYSTEM-only + 只读        | tenant 全部 `NOT_APPLICABLE`，SYSTEM CUD 全部 `NEVER_GRANTED` |
| `PermissionMatrix.readonly(...)`      | Scoped 家族的只读             | 全部 CUD `NEVER_GRANTED`，只填 4 个 read |

## 命名重构：从 String 常量迁到 Declaration

2026-07-29 的命名重构做了以下改动：

- `SystemPermission` 里的 `const val String` **全部**升级为 `val SystemRbacPermissionDeclaration`，通过 `.action(...)` / `.menu(...)` / `.component(...)` 三个工厂构造，`description` 内嵌 Declaration。老的 `DESCRIPTIONS: Map<String, String>` 彻底删除
- `TenantPermission` 里的 `const val ACTION_XXX_PEM = "i.tenant.xxx"` + `val ACTION_XXX = TenantPermissionDeclaration(name = ACTION_XXX_PEM, ...)` **双写**彻底消灭：只保留 `val Declaration`，`_PEM` 后缀去掉，常量名不再带 `TENANT` 段
- `SystemRbacPermissionDeclaration` 类型从 `crystal-sdk` 迁到 `crystal-shared-types`（包路径 `com.lovelycatv.crystalframework.shared.types.rbac.system`）
- 老 SYSTEM-only 权限**批量**加 `system.` 前缀（旧的 `user.create` / `role.create` / `tenant.create` 等全部违反前缀规则，是 hard-error 的直接触发源）
- 跨 scope 权限**批量**加 `x.` 前缀（`x.dict.type.create` / `x.message.channel.create` / `x.approval.flow.definition.create`）
- MENU / COMPONENT 权限**批量**加层前缀（老的无前缀 `permission:/manager/user-permissions` 全部改成 `system.permission:/manager/user-permissions`）
- `PermissionMatrix.init` 从 `logger.warn` 升级为 `throw IllegalStateException`——前缀违规立刻启动失败
- 引用方式：`SystemPermission.ACTION_SYSTEM_USER_READ`（返回 Declaration 对象）；需要字符串一律 `.name`
- 校验测试：`PermissionNameConventionTest`（扫描 `allPermissions()` 每条 name 的前缀 + 常量名段一致性）；`PreAuthorizeCoverageTest`（扫描全项目 `@PreAuthorize` 字面量必须能在 `allPermissions()` 的 `.name` 集合中找到）
- Flyway 迁移：`V20260729.01__reset_permissions_for_naming_redesign.sql` hard-delete 老 `user_permissions` / `tenant_permissions` 和角色-权限绑定；重启后 Configurer 按新常量重新注册全量权限并按 `SystemRolePermissionRelation` / `TenantRolePermissionRelation` 恢复内置绑定

用户自定义的角色-权限绑定会全部丢失，是"抛弃"的代价，用户已明确接受。设计与决策记录详见 `.claude/research/permission-naming-redesign.md`。

### 旧 → 新命名对照

| 场景 | 旧命名 | 新命名 | 层 |
|---|---|---|---|
| 用户 CRUD | `user.create` | `system.user.create` | system |
| 角色 CRUD | `role.create` | `system.role.create` | system |
| 系统权限 CRUD | `permission.create` | `system.permission.create` | system |
| 管理租户列表 | `tenant.create`（歧义） | `system.tenant.create` | system |
| 管理租户档位 | `tenant.tire.type.create`（歧义） | `system.tenant.tire.type.create` | system |
| 系统菜单（老） | `permission:/manager/user-permissions` | `system.permission:/manager/user-permissions` | system |
| 跨 scope 管字典 | `dict.type.create`（无前缀） | `x.dict.type.create` | super |
| 跨租户管字典 | `tenant.dict.type.create` | `tenant.dict.type.create`（保持） | tenantAdmin |
| 租户内字典 | `i.tenant.dict.type.create` | `i.tenant.dict.type.create`（保持） | tenantPem |

Tenant 侧常量名同步简化：`ACTION_TENANT_ROLE_CREATE_PEM` → `ACTION_ROLE_CREATE`（去 `TENANT` 段、去 `_PEM` 后缀）。

## 迁移前后对照

### 样本 1：Standard（@ManagerPermissions → systemOnly）

**迁移前**：

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    readAll = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    create  = [SystemPermission.ACTION_STORAGE_PROVIDER_CREATE],
    update  = [SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE],
    delete  = [SystemPermission.ACTION_STORAGE_PROVIDER_DELETE],
)
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<...>(managerService)
```

**迁移后**：

```kotlin
@Validated
@RestController
@RequestMapping("\${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<...>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE.name,
        systemRead   = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE.name,
    ),
)
```

要点：
- 移除类顶部的 `@ManagerPermissions` 注解
- `permissions` 通过构造参数传入
- `SystemPermission.ACTION_SYSTEM_XXX` 现在是 `SystemRbacPermissionDeclaration` 对象，`PermissionMatrix` 要的是字符串，所以要 `.name`
- `system<op>` authority 必须带 `system.` 前缀，前缀违规会直接 `throw IllegalStateException` 阻断启动
- 需要跨 scope 超级管理员时，`systemOnly` 还接受 `superCreate = ..., superRead = ..., ...` 4 个可选参数（默认 `NOT_APPLICABLE`）

### 样本 2：Scoped（ScopedPermissionTriad → PermissionMatrix.of DSL）

**迁移前**：

```kotlin
) : StandardScopedManagerController<...>(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate     = SystemPermission.ACTION_DICT_TYPE_CREATE,
        superRead       = SystemPermission.ACTION_DICT_TYPE_READ,
        superUpdate     = SystemPermission.ACTION_DICT_TYPE_UPDATE,
        superDelete     = SystemPermission.ACTION_DICT_TYPE_DELETE,
        systemCreate    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE,
        systemRead      = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ,
        systemUpdate    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE,
        systemDelete    = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM,
        tenantPemRead   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM,
    ),
)
```

**迁移后**：

```kotlin
) : StandardScopedManagerController<...>(
    managerService,
    permissions = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_X_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_X_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_X_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_X_DICT_TYPE_DELETE.name
        }
        system {
            create = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE.name
        }
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE.name
            read   = SystemPermission.ACTION_TENANT_DICT_TYPE_READ.name
            update = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE.name
            delete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE.name
        }
        tenantPem {
            create = TenantPermission.ACTION_DICT_TYPE_CREATE.name
            read   = TenantPermission.ACTION_DICT_TYPE_READ.name
            update = TenantPermission.ACTION_DICT_TYPE_UPDATE.name
            delete = TenantPermission.ACTION_DICT_TYPE_DELETE.name
        }
    },
)
```

要点：
- 从 12 权限升级为 16 权限（新增 `tenantAdmin` 层）—— 旧的 Triad 没有跨租户运维层，导致 SYSTEM 管理员必须持有 `super*` 才能改租户数据；新的 `tenantAdmin` 层用 `tenant.` 前缀专司此责
- **`` `super` `` 是 Kotlin 关键字，DSL 里必须反引号包裹**
- 未打开的 layer 默认为全部 `NOT_APPLICABLE`（不参与决策），无需显式声明
- 前缀约定：`super*` 必须以 `x.` 开头；`system*` 必须以 `system.` 开头；`tenantAdmin*` 必须以 `tenant.` 开头；`tenantPem*` 必须以 `i.tenant.` 开头
- `TenantPermission` 常量名去掉了 `TENANT` 段和 `_PEM` 后缀

### 样本 3：Tenant（8 String → tenantOnly）

**迁移前**：

```kotlin
) : StandardTenantManagerController<...>(
    managerService,
    createPermission        = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    scopedCreatePermission  = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
    readPermission          = SystemPermission.ACTION_TENANT_ROLE_READ,
    scopedReadPermission    = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
    updatePermission        = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    scopedUpdatePermission  = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
    deletePermission        = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    scopedDeletePermission  = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
)
```

**迁移后**：

```kotlin
) : StandardTenantManagerController<...>(
    managerService,
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE.name,
        tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE.name,
        tenantPemCreate   = TenantPermission.ACTION_ROLE_CREATE.name,
        tenantPemRead     = TenantPermission.ACTION_ROLE_READ.name,
        tenantPemUpdate   = TenantPermission.ACTION_ROLE_UPDATE.name,
        tenantPemDelete   = TenantPermission.ACTION_ROLE_DELETE.name,
    ),
)
```

要点：
- 旧的 `xxxPermission` 语义映射到 `tenantAdmin*`（跨租户）
- 旧的 `scopedXxxPermission` 语义映射到 `tenantPem*`（本租户）
- super / system 层保持 `NOT_APPLICABLE`（工厂默认）—— Tenant 资源无 SYSTEM scope 概念
- 旧的 8-string 构造函数已 `@Deprecated`，但仍会转发到新 API 一并保留一版；新代码只用主构造函数

### 样本 4：Readonly（@ManagerPermissions → systemOnlyReadonly）

**迁移前**：

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    update  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
class ManagerMailSendLogController(...) : ReadonlyManagerController<...>(managerService)
```

**迁移后**：

```kotlin
class ManagerMailSendLogController(
    managerService: MailSendLogManagerService
) : ReadonlyManagerController<...>(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ.name,
    ),
)
```

要点：
- 只填 `systemRead` 即可 —— 工厂自动把 CUD 4 位填成 `NEVER_GRANTED`，把 tenant 层 8 位填成 `NOT_APPLICABLE`
- 双重防护仍在：即使权限层配错，`Mutability.READ_ONLY` 也会挡下 CUD

### 样本 5：ReadonlyScoped（Triad.readonly → PermissionMatrix.readonly）

**迁移前**：

```kotlin
permissions = ScopedPermissionTriad.readonly(
    superRead      = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead     = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead  = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

**迁移后**：

```kotlin
permissions = PermissionMatrix.readonly(
    superRead       = SystemPermission.ACTION_X_APPROVAL_FLOW_INSTANCE_READ.name,
    systemRead      = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ.name,
    tenantPemRead   = TenantPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ.name,
)
```

要点：
- 新工厂多了 `tenantAdminRead`（跨租户读）
- 12 个 CUD 位自动填 `NEVER_GRANTED`
- 若某层对本资源不适用（如审批实例无 SYSTEM scope），显式填 `PermissionMatrix.NOT_APPLICABLE`

## 迁移步骤

1. **搜索类顶部** `@ManagerPermissions` —— 删掉注解，把 5 个字段的 authority 挪到 `PermissionMatrix.systemOnly(...)` 里，作为 `permissions = ...` 传给父类构造
2. **搜索类型引用** `ScopedPermissionTriad` / `ScopedPermissionMatrix` —— 直接替换为 `PermissionMatrix`；`ScopedPermissionMatrix` 是 typealias，编译期已重定向但会 emit `@Deprecated` 警告
3. **搜索工厂调用** `ScopedPermissionTriad.readonly(...)` —— 替换为 `PermissionMatrix.readonly(...)`，并补上 `tenantAdminRead` 参数
4. **搜索构造参数** `createPermission = ...` / `scopedCreatePermission = ...` —— 替换为 `PermissionMatrix.tenantOnly(...)`，将旧 `xxxPermission` 映射到 `tenantAdmin*`、`scopedXxxPermission` 映射到 `tenantPem*`
5. **补上前缀** —— `super*` 加 `x.`、`system*` 加 `system.`、`tenantAdmin*` 加 `tenant.`、`tenantPem*` 加 `i.tenant.`
6. **常量引用改 Declaration** —— `SystemPermission.ACTION_XXX` 现在是 Declaration，`PermissionMatrix` 要字符串，全部加 `.name`；`TenantPermission` 的 `_PEM` 后缀 + `TENANT` 段全部去掉
7. **启动验证** —— 若前缀违规，`PermissionMatrix.init` 直接 `throw IllegalStateException`，启动失败并列出所有违规

## 常见坑

### 1. `super` 需要反引号

Kotlin 中 `super` 是保留字，DSL builder 用反引号声明方法名 `` `super` ``，调用方也必须反引号：

```kotlin
PermissionMatrix.of {
    `super` { create = "..."; ... }  // 正确
    // super { ... }                  // 语法错误
}
```

### 2. super 层不是"权限继承"

新加的 `tenantAdmin` 层容易被误解为"super 的子类"。实际上 4 个层是**互相独立的授权维度**：

- 用户持有 `super*` → SYSTEM + TENANT 都放行、跨租户
- 用户持有 `tenantAdmin*` → 仅 TENANT 放行、跨租户
- 用户持有 `system*` → 仅 SYSTEM 放行
- 用户持有 `tenantPem*` → 仅 TENANT + 本租户

`layersFor` 返回的是 OR 数组，用户持有任一即可通过。super 层不"包含"其他层的能力 —— 只是碰巧覆盖面更广。

### 3. `NOT_APPLICABLE` vs `NEVER_GRANTED` 别混用

- 用错方向：把 SYSTEM-only 资源的 tenant 层填 `NEVER_GRANTED`（应填 `NOT_APPLICABLE`）—— 无实际影响，但暴露 layer 数组时多出一个永远不匹配的元素，日志/审计上误导
- 用错方向：把 Readonly 的 CUD 填 `NOT_APPLICABLE`（应填 `NEVER_GRANTED`）—— `layersFor` 过滤后返回空数组，`hasAnyAuthority()` 就永远为 false，看似效果一致但语义完全不同；后续若有人误调 `layersFor(SYSTEM, CREATE)` 期望拿到"永远拒绝"的哨兵，会拿到空数组反而以为是"无需权限"

**规则**：`readonly(...)` / `systemOnlyReadonly(...)` 工厂已经把 `NEVER_GRANTED` 填对，不要手工构造 Matrix。

### 4. 别再给 Standard / Readonly 加 `@ManagerPermissions`

新的 `StandardManagerController.authorize` 优先使用 `permissions` 字段；只有 `permissions == null` 时才回退到旧的 `@ManagerPermissions` + AOP 路径（该路径已带 `@Deprecated` 警告）。新代码只用 `permissions`。

### 5. 前缀违规现在是 hard error

`PermissionMatrix.init` 遇到前缀违规会直接 `throw IllegalStateException("PermissionMatrix prefix violations: ...")`，启动失败。老代码里所有 `permission.create` / `user.create` / `tenant.create` 等无前缀名都不再合法，必须改成 `system.permission.create` / `system.user.create` / `system.tenant.create`。

## FAQ

**Q：老代码里的 `ScopedPermissionMatrix.readonly(...)` 能保留吗？**
A：能编译（`ScopedPermissionMatrix` 是 typealias，`readonly` 通过别名继承），但会 emit 弃用警告。逐步替换即可，别写新代码继续用它。

**Q：`StandardTenantManagerController` 的 8-string 构造函数被删了吗？**
A：没删，标了 `@Deprecated` 并保留一版，内部转发到 `PermissionMatrix.tenantOnly(...)`。老 protected 属性 `createPermission` 之类也有兼容 getter。新代码只用主构造函数。

**Q：`DISABLED_SCOPED_PERMISSION = ""` 空字符串还能用吗？**
A：能，等价于 `PermissionMatrix.NOT_APPLICABLE`（Tenant Controller 的 `hasScopedAuthority` 对空串和 `NOT_APPLICABLE` 都短路返回 false）。但新代码用 `PermissionMatrix.NOT_APPLICABLE`，语义更清晰。

**Q：为什么要拆出 `tenantAdmin` 层？**
A：旧 Scoped 家族没有跨租户运维层 —— SYSTEM 管理员想改租户数据必须持有 `super*`，权限模型上把"跨 scope"和"跨租户运维"揉在一起。新的 `tenantAdmin`（`tenant.` 前缀）专司"TENANT scope 但跨租户"，让运维职责与超级管理员职责分离。

**Q：可以只填 super 层不填其他吗？**
A：技术上可以（其他层默认 `NOT_APPLICABLE`），但意味着系统里只有超级管理员能操作该资源。除非确认是"仅 super 可用"的诊断/运维接口，正常业务都应该拆到具体层。

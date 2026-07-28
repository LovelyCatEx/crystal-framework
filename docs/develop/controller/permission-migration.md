# 权限模型迁移指南（PermissionMatrix）

`crystal-shared` 的 Manager Controller 家族过去存在三套并行的权限声明方式：

- `@ManagerPermissions` 类注解 + AOP（Standard / Readonly）
- `ScopedPermissionTriad`（Scoped / DerivedScoped / ReadonlyScoped）
- 8 个 String 构造参数（Tenant）

统一后的 `PermissionMatrix`（`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`）以 4 层 × 4 操作 = 16 个 authority 字段取代上述三种模型。所有 Manager Controller 都通过构造参数 `permissions = ...` 接收同一份数据类。

本指南对照旧代码给出迁移写法，并列出常见坑。

## 4 层权限速查

| 层             | authority 前缀       | 常量来源                | 语义                                                    |
|---------------|-------------------|---------------------|-------------------------------------------------------|
| `super`       | 无前缀               | `SystemPermission`  | 跨 SYSTEM + TENANT 的超级管理员，任意 scope 均放行                  |
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
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE,
        systemRead   = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE,
    ),
)
```

要点：
- 移除类顶部的 `@ManagerPermissions` 注解
- `permissions` 通过构造参数传入
- `system<op>` authority 必须带 `system.` 前缀，前缀违规会 emit warn 日志
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
            create = SystemPermission.ACTION_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_DICT_TYPE_READ
            update = SystemPermission.ACTION_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_DICT_TYPE_DELETE
        }
        system {
            create = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ
            update = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE
        }
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE
            read   = SystemPermission.ACTION_TENANT_DICT_TYPE_READ
            update = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE
            delete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE
        }
        tenantPem {
            create = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM
            read   = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM
            update = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM
            delete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM
        }
    },
)
```

要点：
- 从 12 权限升级为 16 权限（新增 `tenantAdmin` 层）—— 旧的 Triad 没有跨租户运维层，导致 SYSTEM 管理员必须持有 `super*` 才能改租户数据；新的 `tenantAdmin` 层用 `tenant.` 前缀专司此责
- **`` `super` `` 是 Kotlin 关键字，DSL 里必须反引号包裹**
- 未打开的 layer 默认为全部 `NOT_APPLICABLE`（不参与决策），无需显式声明
- 前缀约定：`super*` 不能带 `system.` / `tenant.` / `i.tenant.` 前缀；`system*` 必须以 `system.` 开头；`tenantAdmin*` 必须以 `tenant.` 开头；`tenantPem*` 必须以 `i.tenant.` 开头

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
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
        tenantAdminRead   = SystemPermission.ACTION_TENANT_ROLE_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
        tenantPemCreate   = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
        tenantPemRead     = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
        tenantPemUpdate   = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
        tenantPemDelete   = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
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
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
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
    superRead       = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
    systemRead      = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_INSTANCE_READ,
    tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
    tenantPemRead   = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
)
```

要点：
- 新工厂多了 `tenantAdminRead`（跨租户读）
- 12 个 CUD 位自动填 `NEVER_GRANTED`

## 迁移步骤

1. **搜索类顶部** `@ManagerPermissions` —— 删掉注解，把 5 个字段的 authority 挪到 `PermissionMatrix.systemOnly(...)` 里，作为 `permissions = ...` 传给父类构造
2. **搜索类型引用** `ScopedPermissionTriad` / `ScopedPermissionMatrix` —— 直接替换为 `PermissionMatrix`；`ScopedPermissionMatrix` 是 typealias，编译期已重定向但会 emit `@Deprecated` 警告
3. **搜索工厂调用** `ScopedPermissionTriad.readonly(...)` —— 替换为 `PermissionMatrix.readonly(...)`，并补上 `tenantAdminRead` 参数
4. **搜索构造参数** `createPermission = ...` / `scopedCreatePermission = ...` —— 替换为 `PermissionMatrix.tenantOnly(...)`，将旧 `xxxPermission` 映射到 `tenantAdmin*`、`scopedXxxPermission` 映射到 `tenantPem*`
5. **补上新增的 `tenantAdmin` 层权限常量** —— 见 `add-system-permission` skill 或对应模块的 `Permission` 常量类，命名一般为 `ACTION_TENANT_<RES>_<OP>`（`tenant.` 前缀）
6. **补上前缀** —— `system*` 加 `system.`、`tenantAdmin*` 加 `tenant.`、`tenantPem*` 加 `i.tenant.`、`super*` 无前缀
7. **启动验证** —— 若前缀违规，`PermissionMatrix.init` 会 emit `PermissionMatrix: super* permission '...' violates prefix convention` 之类的 warn 日志（当前是软约束，未来会升级为 hard error）

## 常见坑

### 1. `super` 需要反引号

Kotlin 中 `super` 是保留字，DSL builder 用反引号声明方法名 `` `super` ``，调用方也必须反引号：

```kotlin
PermissionMatrix.of {
    `super` { create = "..."; ... }  // ✅ 正确
    // super { ... }                  // ❌ 语法错误
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

### 5. 前缀违规是软警告，别忽略

`PermissionMatrix.init` 遇到前缀违规会 emit `logger.warn`，不会阻断启动。忽略掉的话未来升级为 hard error 时会集中爆雷。任何 `PermissionMatrix: ... violates prefix convention` 都应视作待修的技术债。

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

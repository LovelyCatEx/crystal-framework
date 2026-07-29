# 控制器家族总览

`crystal-shared` 提供 6 个管理端 CRUD Controller 基类，覆盖不同的资源模型。在写 Controller 前先根据资源特征选定基类。

## 基类速查

所有基类的权限声明**统一使用 `PermissionMatrix`**（构造参数 `permissions = ...`）。旧的 `@ManagerPermissions` 类注解、`ScopedPermissionTriad`、8 String 构造参数已弃用，迁移说明见 [权限模型迁移指南](./permission-migration)。

| 基类 | Matrix 便捷工厂 | 端点 | 适用资源 |
|---|---|---|---|
| `StandardManagerController` | `PermissionMatrix.systemOnly(...)` | `list` / `create` / `query` / `update` / `delete` | 全局 CRUD，无租户/系统之分 |
| `ReadonlyManagerController` | `PermissionMatrix.systemOnlyReadonly(...)` | `list` / `query`（写操作返回 403） | 系统生成、不可修改的日志类 |
| `StandardScopedManagerController` | `PermissionMatrix.of { ... }` | `list?scope&scopeId` / `create` / `query` / `update` / `delete` | 资源自带 `scope` + `scopeId` 列 |
| `ReadonlyScopedManagerController` | `PermissionMatrix.readonly(...)` | `list` / `query`（写操作返回 403） | Scoped 家族的只读版本 |
| `StandardDerivedScopedManagerController` | `PermissionMatrix.of { ... }` + 三个抽象钩子 | `create` / `query` / `update` / `delete`（无 `list`） | 自身无 scope 列，需从父实体推导 |
| `StandardTenantManagerController` | `PermissionMatrix.tenantOnly(...)` | `list?tenantId` / `create` / `query` / `update` / `delete` | 强制以 tenantId 为 scope |

## 选型决策

按下表从上至下判断，第一个匹配的行即为目标基类：

| 判断 | 选择 |
|---|---|
| 不是 CRUD 场景（登录、上传、动作触发） | [普通 Controller](./generic-controller) |
| 系统生成且不允许用户修改（日志、审计） | 全局资源用 [ReadonlyManagerController](./readonly-manager-controller)，带 scope 用 [ReadonlyScopedManagerController](./readonly-scoped-manager-controller) |
| 只属于租户（如租户角色、租户成员） | [StandardTenantManagerController](./tenant-manager-controller) |
| 支持 SYSTEM 与 TENANT 双 scope 挂载 | 自带 scope 列用 [StandardScopedManagerController](./scoped-manager-controller)，靠父实体推 scope 用 [StandardDerivedScopedManagerController](./derived-scoped-manager-controller) |
| 普通的全局 CRUD 资源 | [StandardManagerController](./standard-manager-controller) |

## 权限统一模型（PermissionMatrix）

`PermissionMatrix`（`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`）是数据类，4 层 × 4 操作 = 16 个 authority 字段。构造参数 `permissions = ...` 传入基类，`layersFor(scope, op)` 返回一组待 OR 匹配的 authority。

### 4 层语义

| 层             | authority 前缀     | 常量来源                | 语义                                |
|---------------|-----------------|---------------------|-----------------------------------|
| `super`       | 无前缀             | `SystemPermission`  | 跨 SYSTEM + TENANT 的超级管理员          |
| `system`      | `system.`       | `SystemPermission`  | 仅 SYSTEM scope                    |
| `tenantAdmin` | `tenant.`       | `SystemPermission`  | TENANT scope 且跨租户（"运维管理员"）      |
| `tenantPem`   | `i.tenant.`     | `TenantPermission`  | TENANT scope 且严格匹配 tenantId       |

匹配规则：SYSTEM 请求 OR-check `[super, system]`；TENANT 请求 OR-check `[super, tenantAdmin, tenantPem]`。

### 两个哨兵值

- `PermissionMatrix.NOT_APPLICABLE` —— 该层对本资源不适用（如 SYSTEM-only 资源的 tenant 层），`layersFor` 过滤掉，不参与决策
- `PermissionMatrix.NEVER_GRANTED` —— 该层存在但操作被封死（如 Readonly 的 CUD），`layersFor` 保留占位但永不匹配任何真实 authority

### 快速入门

```kotlin
// SYSTEM-only 资源
permissions = PermissionMatrix.systemOnly(
    systemCreate = SystemPermission.ACTION_SYSTEM_XXX_CREATE,
    systemRead   = SystemPermission.ACTION_SYSTEM_XXX_READ,
    systemUpdate = SystemPermission.ACTION_SYSTEM_XXX_UPDATE,
    systemDelete = SystemPermission.ACTION_SYSTEM_XXX_DELETE,
)

// SYSTEM + TENANT 双 scope（Scoped / DerivedScoped 家族）
permissions = PermissionMatrix.of {
    `super`     { create = ...; read = ...; update = ...; delete = ... }
    system      { create = ...; read = ...; update = ...; delete = ... }
    tenantAdmin { create = ...; read = ...; update = ...; delete = ... }
    tenantPem   { create = ...; read = ...; update = ...; delete = ... }
}
```

`` `super` `` 是 Kotlin 关键字，DSL 中必须反引号包裹。未打开的 layer 默认全部 `NOT_APPLICABLE`。

### 便捷工厂速查

| 工厂                                       | 适用                       |
|------------------------------------------|--------------------------|
| `PermissionMatrix.of { ... }`             | 完整 DSL，任意组合             |
| `PermissionMatrix.systemOnly(...)`        | 无 scope 的全局 CRUD（Standard） |
| `PermissionMatrix.tenantOnly(...)`        | 仅 TENANT 资源（Tenant）      |
| `PermissionMatrix.systemOnlyReadonly(...)` | SYSTEM-only + 只读        |
| `PermissionMatrix.readonly(...)`          | Scoped 家族的只读             |

完整迁移步骤、常见坑与 FAQ 见 [权限模型迁移指南](./permission-migration)。

## 必备组件

| 类型 | 要求 |
|---|---|
| Entity | 继承 `BaseEntity`；Scoped 家族继承 `BaseScopedEntity`，DerivedScoped / Tenant 实现 `ScopedEntity<Long>` |
| Repository | 继承 `BaseRepository` |
| Service | Standard / Readonly 用 `CachedBaseManagerService`；Scoped 家族用 `BaseScopedManagerService`；Tenant 用 `BaseTenantResourceManagerService` |
| DTO | 4 个（Create / Read / Update / Delete），基类因家族而异，见各基类文档 |

## 包结构

Manager 相关代码统一放入 `controller/manager/` 与 `service/manager/`：

```
your-module/
├── controller/
│   ├── manager/
│   │   ├── ManagerXxxController.kt
│   │   ├── dto/
│   │   │   ├── ManagerCreateXxxDTO.kt
│   │   │   ├── ManagerReadXxxDTO.kt
│   │   │   ├── ManagerUpdateXxxDTO.kt
│   │   │   └── ManagerDeleteXxxDTO.kt
│   │   └── vo/
│   └── xxx/
├── service/
│   └── manager/
│       ├── XxxManagerService.kt
│       └── impl/
│           └── XxxManagerServiceImpl.kt
└── entity/
    └── XxxEntity.kt
```

## 通用规则

- Controller 头部必须携带 `@Validated`、`@RestController`、`@RequestMapping`
- 方法返回值必须显式声明为 `ApiResponse<*>`（详见 [ApiResponse](./api-response)）
- Controller 内禁止注入 Repository，数据库操作走 Service 层
- Manager Controller 只能注入 Manager Service，普通 Controller 只能注入普通 Service
- 请求参数命名为 `XxxDTO` 放入 `dto/`，响应参数命名为 `XxxVO` 放入 `vo/`
- 所有 `Long` 字段在 DTO / VO 中加 `@get:JsonSerialize(using = ToStringSerializer::class)`，前端接为 `string`
- 无需授权的端点用 `@Unauthorized` 显式标注

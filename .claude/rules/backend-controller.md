---
paths:
  - "**/controller/**/*.kt"
  - "**/controller/**/*.java"
---

# Controller 规范

## 必需注解

```kotlin
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/dashboard")
class DashboardController
```

## 返回值规则

**所有 Controller 方法必须显式返回 `ApiResponse<>` 类型**，使用 `ApiResponse.success()` / `ApiResponse.failed()` 包装。

禁止返回裸实体或 List。前端 `doGet` / `doPost` 依赖 `ApiResponse` 结构解析。

## DTO/VO 分包规则

**前端→后端的类命名为 DTO，后端→前端的类命名为 VO，必须分包存放。**

- **DTO**: 放 `controller/dto/`（Manager 相关放 `controller/manager/dto/`）
- **VO**: 放 `controller/vo/`（Manager 相关放 `controller/manager/vo/`）

### Long 类型序列化规则

所有落在 `Long` 范围的字段（主键 `id`、外键 `xxxId`、时间戳 `xxxTime` 等）必须在 DTO/VO 中以**字符串（`String`）**传递。

后端序列化时通过 `ToStringSerializer` 将 `Long` → `String`，前端对应类型为 `string`。

## 标准化 ManagerController

对于标准化 Controller，必须继承对应基类：
- `StandardManagerController` - 系统级 CRUD
- `StandardScopedManagerController` - 跨 SYSTEM/TENANT scope
- `StandardTenantManagerController` - 租户级 CRUD
- `ReadonlyManagerController` - 系统级只读
- `ReadonlyScopedManagerController` - scope 只读

示例参考 `ManagerOAuthAccountController` 类。

### DTO 使用规则

`BaseManagerReadDTO`、`BaseManagerCreateDTO`、`BaseManagerUpdateDTO`、`BaseManagerDeleteDTO` **仅限标准化 Controller（`StandardManagerController` 子类）使用**。

自定义 Controller（非标准化、普通、自定义端点）禁止混用这些 DTO，应使用更轻量的基类如 `PageQuery`。

## 权限声明规则（强制）

**Manager Controller 家族的权限声明必须使用统一的 `PermissionMatrix`**（`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`）。

禁止使用旧的 `@ManagerPermissions` 类注解、`ScopedPermissionTriad`、`ScopedPermissionMatrix` 别名，或 `StandardTenantManagerController` 的 8 字符串构造参数。

### PermissionMatrix 四个授权层

| 层             | authority 前缀 | 常量来源            | 语义                          |
| -------------- | -------------- | ------------------- | ----------------------------- |
| `super`        | 无前缀         | `SystemPermission`  | 跨 SYSTEM + TENANT 的超级管理 |
| `system`       | `system.`      | `SystemPermission`  | 仅 SYSTEM scope               |
| `tenantAdmin`  | `tenant.`      | `SystemPermission`  | TENANT scope 且跨租户         |
| `tenantPem`    | `i.tenant.`    | `TenantPermission`  | TENANT scope 且严格匹配 tenantId |

### 按基类选择构造方式

```kotlin
// StandardManagerController 子类
permissions = PermissionMatrix.systemOnly(
    systemCreate = ..., 
    systemRead = ..., 
    systemUpdate = ..., 
    systemDelete = ...
)

// StandardScopedManagerController 子类
permissions = PermissionMatrix.of { 
    `super` { ... }
    system { ... }
    tenantAdmin { ... }
    tenantPem { ... }
}
// 注意：`super` 是 Kotlin 关键字，必须用反引号包裹

// StandardTenantManagerController 子类
permissions = PermissionMatrix.tenantOnly(
    tenantAdminCreate = ..., 
    tenantAdminRead = ...,
    ..., 
    tenantPemCreate = ...,
    ...
)

// ReadonlyManagerController 子类
permissions = PermissionMatrix.systemOnlyReadonly(systemRead = ...)

// ReadonlyScopedManagerController 子类
permissions = PermissionMatrix.readonly(
    superRead = ..., 
    systemRead = ..., 
    tenantAdminRead = ..., 
    tenantPemRead = ...
)
```

**哨兵值**：
- 不适用的层用 `PermissionMatrix.NOT_APPLICABLE`（`layersFor` 会过滤掉）
- 显式禁用的层用 `PermissionMatrix.NEVER_GRANTED`（参与决策但无角色能匹配，用于禁止访问特定操作）

前缀约定由 `PermissionMatrix.init` 校验，详见 `docs/develop/controller/permission-migration.md`。

### 禁止接口访问

当需要禁止某个接口被任何角色访问时，使用 `PermissionMatrix.NEVER_GRANTED` 常量。

```kotlin
// 禁止所有人创建
permissions = PermissionMatrix.systemOnly(
    systemCreate = PermissionMatrix.NEVER_GRANTED,
    systemRead = SystemPermission.USER_READ,
    systemUpdate = SystemPermission.USER_UPDATE,
    systemDelete = SystemPermission.USER_DELETE
)
```

`NEVER_GRANTED` 是一个特殊的权限字符串（`"!!never_granted!!"`），没有任何角色能匹配它，因此该操作会被拒绝。

## 非标准化 Controller

默认情况下，所有接口都要求访问时携带合法 Token。

使用 `@Unauthorized` 注解表示无需任何授权即可访问的接口。

### 唯一鉴权注解：@RequiresAuthority

**本项目禁止使用 Spring Security 自带的鉴权注解。**

禁止使用：
- `@PreAuthorize`
- `@PostAuthorize`
- `@PreFilter`
- `@PostFilter`
- `@Secured`
- `@RolesAllowed`

**唯一允许的鉴权注解是 `@RequiresAuthority`**（`com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority`）。

使用方式：

```kotlin
@RequiresAuthority(
    anyOf = ["permission.read", "permission.admin"],  // 满足任一权限
    scope = ResourceScope.SYSTEM
)
// 或
@RequiresAuthority(
    allOf = ["permission.read", "permission.write"],  // 必须同时拥有
    scope = ResourceScope.TENANT
)
```

- `anyOf` 和 `allOf` 必须二选一（不能同时为空，不能同时有值）
- `scope` 必填（SYSTEM 或 TENANT）
- 失败时抛出结构化的 `ForbiddenException`，前端可渲染实际所需权限

## 请求参数绑定规则

Controller 方法接收前端参数时只允许以下三种方式：

| 后端注解          | 适用场景                     | 前端调用方式                                              |
| ----------------- | ---------------------------- | --------------------------------------------------------- |
| `@RequestBody`    | JSON 请求体（POST/PUT）      | `doPost(url, body, {'Content-Type': 'application/json'})` |
| `@ModelAttribute` | form-urlencoded / 查询参数绑定 | `doPost(url, body)` 默认行为                              |
| `@RequestParam`   | 单个查询参数（GET 为主）     | `doGet(url, { param: value })`                            |

## URL 命名规范

Manager Controller 的 URL 结构为 `${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/<resource>/<action>`，三段一律 **kebab-case**（小写字母、数字、`-`）：

- **资源段用单数**：`user` / `oauth-account` / `mail-send-log`
  - 禁止复数形式：`users` / `mail-send-logs`
- **动作段**：沿用 base 类的 `list` / `create` / `query` / `update` / `delete`
  - 自定义端点也必须 kebab-case：`/update-graph` / `/details-by-id`
  - 单个单词直接使用：`/my` / `/tree` / `/start` / `/handle`
- **禁止**：大写字母、下划线、camelCase、PascalCase

前端 `.api.ts` 的 `BaseManagerController` 构造参数、`doGet` / `doPost` 硬编码路径必须与后端一致。

详见 `docs/develop/controller/url-naming.md`，`ControllerUrlConventionTest` 会自动扫描全部 `@RequestMapping` / `@XxxMapping` 拦截违规。

## 禁止行为

- **Controller 禁止直接注入 Repository**，所有数据库操作必须通过 Service 层进行
- 禁止编造不存在的方法名

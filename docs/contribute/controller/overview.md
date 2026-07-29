# 控制器家族总览

`crystal-shared` 里的 6 个 Controller 基类是随框架处理多租户 / 系统级资源的需求分化出来的。本文梳理它们的设计动机与演化路径，用于在改动 `shared` 里的 Controller 基础设施时判断影响面。

## 演化路径

```
StandardManagerController                          v1.0 起点，全局 CRUD 基线
  ├─ ReadonlyManagerController                     系统生成不可改（日志类）
  ├─ StandardTenantManagerController               v1.4 前，租户资源专用
  │                                                8 个 String 权限 + isXxxInScope 钩子
  │                                                硬编码 tenantId 为唯一 scope
  └─ StandardScopedManagerController               v1.10，抽象出 ResourceScope
      │                                            引入 ScopedPermissionTriad（12 权限）
      │                                            资源可挂 SYSTEM 或 TENANT
      ├─ ReadonlyScopedManagerController           Scoped 的只读变体
      │                                            用 Triad.readonly() + NEVER_GRANTED 兜底
      └─ StandardDerivedScopedManagerController    资源无自身 scope 列
                                                   scope 从父实体链推导
                                                   无 /list（无父上下文无法列举）

统一到 PermissionMatrix（当前）：三套权限模型（@ManagerPermissions / ScopedPermissionTriad
/ 8 String）合并为一个 4 层 × 4 操作 = 16 权限的 `PermissionMatrix` 数据类，所有基类
统一通过构造参数 `permissions = ...` 接收。`ScopedPermissionMatrix` 保留为 typealias、
`StandardTenantManagerController` 保留兼容构造函数一版，标 @Deprecated 引导迁移。
```

`StandardTenantManagerController` 早于 `StandardScopedManagerController`。新代码在 SYSTEM/TENANT 双 scope 场景下优先选 Scoped 家族。

## 六个基类的定位差异

| 基类 | 关键抽象 | 数据模型 | 权限声明（PermissionMatrix 工厂） |
|---|---|---|---|
| `StandardManagerController` | 泛型链 + `authorize` 方法 | 全局资源，无 scope | `PermissionMatrix.systemOnly(...)` |
| `ReadonlyManagerController` | 继承 Standard + `Mutability.READ_ONLY` | 同上，读特化 | `PermissionMatrix.systemOnlyReadonly(...)` |
| `StandardScopedManagerController` | `BaseScopedEntity` 显式 scope 列 | 资源自带 `scope` + `scopeId` | `PermissionMatrix.of { ... }` |
| `ReadonlyScopedManagerController` | 继承 Scoped + `Mutability.READ_ONLY` | 同 Scoped，读特化 | `PermissionMatrix.readonly(...)`（16 位 CUD 填 `NEVER_GRANTED`） |
| `StandardDerivedScopedManagerController` | `resolveScopeFromXXX` 三抽象方法 | 靠父实体推 scope | `PermissionMatrix.of { ... }` |
| `StandardTenantManagerController` | `isXxxInScope` 钩子 + `preflight` | 强制 tenantId 为 scope | `PermissionMatrix.tenantOnly(...)` |

## 权限体系的四代演化

### 第一代：@ManagerPermissions（AOP，已弃用）

`StandardManagerController` 初期采用类注解 + AOP 拦截：

```kotlin
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? { ... }
}
```

要点：

- 切入点写死在 `StandardManagerController.*(..)`，覆盖 Standard 与 Readonly（子类）
- 按方法名反射匹配（`readAll` / `read` / `create` / `update` / `delete`）
- `AopUtils.getTargetClass` 穿透 CGLIB 代理，`AnnotationUtils.findAnnotation` 支持注解继承查找

局限：数组只能声明静态权限清单，无法表达"根据 scope 动态选权限"。这是后续 Scoped 家族改走 Triad、并最终统一到 `PermissionMatrix` 的动因之一。当前 AOP 路径仅在 Controller 未设 `permissions` 时作为兼容回退，且带 `@Deprecated`。

### 第二代：ScopedPermissionTriad（构造参数，已弃用）

Scoped 家族曾用 `ScopedPermissionTriad` 数据类装 12 个权限：

```
super × CRUD      跨 scope 的管理员权限
system × CRUD     仅 SYSTEM 域内
tenantPem × CRUD  仅 TENANT 域内
```

匹配规则：

```kotlin
SYSTEM scope → hasAnyAuthority(super<op>, system<op>)
TENANT scope → hasAnyAuthority(super<op>, tenantPem<op>)
```

局限：缺少"TENANT 但跨租户"这一层 —— 让 SYSTEM 管理员编辑租户数据必须持有 `super*`，把"跨 scope"和"跨租户运维"揉在一起。`ScopedPermissionTriad` 类型已合并进 `PermissionMatrix`，`ScopedPermissionMatrix` typealias 保留一版兼容，携带 `@Deprecated`。

### 第三代：8 个 String 参数（TenantManager 老设计，已弃用）

`StandardTenantManagerController` 早于 Triad，权限模型更扁平：

```kotlin
createPermission,      scopedCreatePermission,     // system + tenant
readPermission,        scopedReadPermission,
updatePermission,      scopedUpdatePermission,
deletePermission,      scopedDeletePermission,
```

匹配规则：先查 system 级 → 再查 scoped 级 → 都无则 403。同样缺少"跨 scope 管理员"（super 层）。老 8-String 构造函数带 `@Deprecated`、内部转发到 `PermissionMatrix.tenantOnly(...)`，保留一版兼容。

### 第四代：PermissionMatrix（当前）

`PermissionMatrix`（`com.lovelycatv.crystalframework.shared.controller.PermissionMatrix`）用 4 层 × 4 操作 = 16 权限统一以上三种声明方式：

| 层             | authority 前缀     | 常量来源               | 语义                                |
|---------------|-----------------|--------------------|-----------------------------------|
| `super`       | 无前缀             | `SystemPermission` | 跨 SYSTEM + TENANT 的超级管理员          |
| `system`      | `system.`       | `SystemPermission` | 仅 SYSTEM scope                    |
| `tenantAdmin` | `tenant.`       | `SystemPermission` | TENANT scope 且跨租户                 |
| `tenantPem`   | `i.tenant.`     | `TenantPermission` | TENANT scope 且严格匹配 tenantId       |

匹配规则（`layersFor(scope, op)`）：

```
SYSTEM scope → hasAnyAuthority(super, system)
TENANT scope → hasAnyAuthority(super, tenantAdmin, tenantPem)
```

关键设计：

- **两个哨兵值**：`NOT_APPLICABLE`（层不适用，`layersFor` 过滤掉，不参与决策）+ `NEVER_GRANTED`（层存在但操作禁止，`layersFor` 保留占位但永不匹配任何真实 authority）。设计动机同旧 `Triad.NEVER_GRANTED` —— 让错误路径 fail-safe 而非 fail-open
- **前缀约定**：`super*` 不能有前缀；`system*` 必须以 `system.` 开头；`tenantAdmin*` 必须以 `tenant.` 开头；`tenantPem*` 必须以 `i.tenant.` 开头。`PermissionMatrix.init` 遇违规 emit warn（未来会升级为 hard error），`collectPrefixViolations(matrix)` 用于测试/gating 严格校验
- **DSL + 4 个便捷工厂**：`of { ... }` / `systemOnly(...)` / `tenantOnly(...)` / `readonly(...)` / `systemOnlyReadonly(...)`，未打开层默认 `NOT_APPLICABLE`
- **兼容层**：老 `ScopedPermissionMatrix` typealias、Tenant 老构造函数、`@ManagerPermissions` AOP 回退路径都保留一版并标 `@Deprecated`，无破坏性迁移

## 权限切面与显式校验的区别

| 家族 | 权限检查方式 | 检查时机 |
|---|---|---|
| Standard / Readonly | `StandardManagerController.authorize` 里显式 OR-check `matrix.layersFor(SYSTEM, op)` | 每个端点方法开头 |
| Scoped / DerivedScoped / ReadonlyScoped | 方法内显式 `assertAccess` | 端点第一行 |
| Tenant | 方法内显式 `RbacUtils.hasAuthority`，用 `PermissionMatrix.tenantAdminFor` / `tenantPemFor` 分派 | 端点内部 |

Scoped 家族原本就走内联校验，Standard / Readonly 在 `PermissionMatrix` 引入后也从 AOP 迁到 `authorize` 方法（保留 AOP 回退是为了兼容仍带 `@ManagerPermissions` 的旧 Controller）。这样所有家族的权限决策都统一在 typed DTO + typed Matrix 上，AOP 只留兼容用途。

## 泛型链条

以 Standard 为例：

```
StandardManagerController
  <SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
   REPOSITORY : BaseRepository<ENTITY>,
   ENTITY : BaseEntity,
   CREATE_DTO : Any,
   READ_DTO : BaseManagerReadDTO,
   UPDATE_DTO : BaseManagerUpdateDTO,
   DELETE_DTO : BaseManagerDeleteDTO>
```

7 个类型参数建立完整的类型链：Service 约束 Repository / Entity / 4 DTO，Controller 再对同一组类型做二次约束。这带来三个效果：

- 子类 Controller 声明 `<..., ..., ManagerXxxCreateDTO, ...>` 时，编译器强制其与 Service 的 CREATE 参数一致
- IDE 补全能推出完整的字段与方法
- `managerService.create(dto)` 里的 dto 类型是 `CREATE_DTO`，无需 unsafe cast

Scoped 家族多一个 `ENTITY : BaseScopedEntity` 约束；DerivedScoped 用 `where ENTITY : BaseEntity, ENTITY : ScopedEntity<*>` 联合约束；Tenant 用 `where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long>`。

## 与其他基础设施的耦合

- `GlobalExceptionHandler` — Controller 抛异常后由它统一转 `ApiResponse`
- `ManagerControllerAuditAspect`（在 `crystal-audit`）— 同一切入点，`@Order` 排在权限切面之后，记录 CRUD 审计
- `ReactiveSecurityContextHolder` — 通过 WebFlux reactor context 传递 `Authentication`，`UserAuthentication` 参数由 `ArgumentResolver` 提取
- `UnauthorizedPathScanner` — 启动时扫描 `@Unauthorized`，把路径塞进 `permitAll()`

## 现有真实使用位置

| 家族 | 模块 | Controller |
|---|---|---|
| Standard | `crystal-resource` | `ManagerStorageProviderController`、`ManagerFileResourceController` |
| Standard | `crystal-rbac` | `ManagerUserRoleController`、`ManagerUserPermissionController` |
| Readonly | `crystal-audit` | `ManagerAuditLogController` |
| Readonly | `crystal-mail` | `ManagerMailSendLogController` |
| Readonly | `crystal-auth` | `ManagerUserLoginLogController` |
| Scoped | `crystal-tenant` | `ManagerTenantDictTypeController` |
| Scoped | `crystal-approval` | `ManagerApprovalFlowDefinitionController` |
| DerivedScoped | `crystal-tenant` | `ManagerTenantDictItemController`（顺 `typeId` 找父类型的 scope） |
| ReadonlyScoped | `crystal-approval` | `ManagerApprovalFlowInstanceController`、`ManagerApprovalFlowTaskController` |
| Tenant | `crystal-rbac` | `ManagerTenantRoleController` |
| Tenant | `crystal-tenant` | `ManagerTenantMemberController`、`ManagerTenantDepartmentController`、`ManagerTenantDepartmentMemberController`、`ManagerTenantMessageChannelController` |

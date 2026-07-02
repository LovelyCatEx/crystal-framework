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
```

`StandardTenantManagerController` 早于 `StandardScopedManagerController`。新代码在 SYSTEM/TENANT 双 scope 场景下优先选 Scoped 家族。

## 六个基类的定位差异

| 基类 | 关键抽象 | 数据模型 | 权限模型 |
|---|---|---|---|
| `StandardManagerController` | 泛型链 + AOP 权限 | 全局资源，无 scope | `@ManagerPermissions` 5 字段数组，OR 语义 |
| `ReadonlyManagerController` | 继承 Standard 三方法重写 | 同上，读特化 | 同上 |
| `StandardScopedManagerController` | `BaseScopedEntity` 显式 scope 列 | 资源自带 `scope` + `scopeId` | `ScopedPermissionTriad` 三层 × 四操作 |
| `ReadonlyScopedManagerController` | 继承 Scoped 三方法重写 | 同 Scoped，读特化 | `Triad.readonly(...)` + `NEVER_GRANTED` 兜底 |
| `StandardDerivedScopedManagerController` | `resolveScopeFromXXX` 三抽象方法 | 靠父实体推 scope | 同 Scoped |
| `StandardTenantManagerController` | 8 个 String 权限 + `isXxxInScope` 钩子 | 强制 tenantId 为 scope | 双层权限（system + scoped） |

## 权限体系的三次演化

### 第一层：@ManagerPermissions（AOP）

`StandardManagerController` 采用类注解 + AOP 拦截。

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

局限：数组只能声明静态权限清单，无法表达"根据 scope 动态选权限"。这是后续 Scoped 家族改走 Triad 的原因。

### 第二层：ScopedPermissionTriad（构造参数）

Scoped 家族用 `ScopedPermissionTriad` 数据类装 12 个权限：

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

`NEVER_GRANTED` 常量用于兜底。`Triad.readonly(...)` 只填读权限，其他 CRUD 位塞 `"!!never_granted!!"`——不是任何真实权限，也不属于 `SystemPermission` 常量集合，`root` 角色的全权自动授权也不包含它。即使有人绕开 `ReadonlyScopedManagerController` 直接查 `triad.superFor(CREATE)`，返回值也拒绝匹配，避免只读权限被误用为写权限的静默 bypass。

### 第三层：8 个 String 参数（TenantManager 老设计）

`StandardTenantManagerController` 早于 Triad，权限模型较扁平：

```kotlin
createPermission,      scopedCreatePermission,     // system + tenant
readPermission,        scopedReadPermission,
updatePermission,      scopedUpdatePermission,
deletePermission,      scopedDeletePermission,
```

匹配规则：先查 system 级 → 再查 scoped 级 → 都无则 403。此模型缺少"跨 scope 管理员"（super 层）的概念——`super` 被 system 位吞并，导致跨租户运维必须依赖系统级权限。此为 Scoped 家族引入 Triad 的动机之一。

`DISABLED_SCOPED_PERMISSION = ""` 用于禁用 tenant-scoped 访问，只允许 system 级调用者。

## 权限切面与显式校验的区别

| 家族 | 权限检查方式 | 检查时机 |
|---|---|---|
| Standard / Readonly | AOP 切面（`ManagerControllerPermissionAspect`） | 方法调用前 |
| Scoped / DerivedScoped / ReadonlyScoped | 方法内显式 `assertAccess` | 端点第一行 |
| Tenant | 方法内显式 `RbacUtils.hasAuthority` | 端点内部 |

Scoped 家族不走 AOP 的原因：权限决策依赖 DTO 里的 `scope` 和 `scopeId`，AOP 拦截时 DTO 尚未反序列化为 typed 对象；`update` / `delete` 场景还需要数据库查询结果参与决策。方法内显式校验 + `checkPermission` / `checkOwnership` 钩子更适合。

Standard 家族相反——权限清单在类注解上静态可读，AOP 处理最省事。

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

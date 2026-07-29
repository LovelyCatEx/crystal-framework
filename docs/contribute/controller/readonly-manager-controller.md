# ReadonlyManagerController

## 设计意图

`ReadonlyManagerController` 不是重复实现一个只读 CRUD，而是通过继承 + 三方法重写，以最小代价复用 `StandardManagerController` 的查询端点，同时禁止变更操作。

## 源码

`crystal-shared/controller/ReadonlyManagerController.kt`：

```kotlin
@Validated
abstract class ReadonlyManagerController<
    SERVICE : CachedBaseManagerService<...>,
    ...
>(
    managerService: SERVICE,
    permissions: PermissionMatrix? = null,
) : StandardManagerController<SERVICE, ...>(
    managerService,
    permissions = permissions,
    mutability = Mutability.READ_ONLY,   // 关键：让 CUD 端点直接抛 ForbiddenException
)
```

结构要点：

- 只加了 `Mutability.READ_ONLY` 一件事，由 `AbstractManagerController` 在 CUD 端点入口检查该标志并抛 `ForbiddenException`
- `list` / `query` 未被触碰，完全继承自 `StandardManagerController`
- 类型参数与父类完全一致——泛型继承强制上下界传递
- `permissions` 通过构造参数原样传给父类，推荐用 `PermissionMatrix.systemOnlyReadonly(...)` 工厂

## 双重防护链路

CUD 端点即使调用也会被 `Mutability.READ_ONLY` 挡下；在此之前，`StandardManagerController.authorize`（走 `PermissionMatrix`）已先做一轮权限校验。完整链路：

```
POST /create
  → StandardManagerController.authorize
      ├─ 校验 permissions.layersFor(SYSTEM, CREATE)
      ├─ 无权限 → AuthorizationDeniedException（GlobalExceptionHandler 转 403）
      └─ 有权限 → 继续
  → AbstractManagerController 入口
      └─ Mutability.READ_ONLY → 抛 ForbiddenException（403）
```

两层拦截的动机：权限层拦是通用防线（配置错误或角色错发），业务层拦是结构性约束——此类型的 Controller 就是不允许写入，代码即声明。即使权限层配置错误让请求穿过，业务层也会兜底。

## 权限配置的实际做法

推荐 `PermissionMatrix.systemOnlyReadonly(...)`，只填 `systemRead`（可选 `superRead`）：

```kotlin
permissions = PermissionMatrix.systemOnlyReadonly(
    systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
)
```

工厂自动把 CUD 4 位填成 `NEVER_GRANTED`、tenant 层 8 位填成 `NOT_APPLICABLE` —— 即使 `Mutability` 保护被绕过，权限层的 `NEVER_GRANTED` 也保证 CUD 恒被拒绝。此为双重防护的关键。

若把 CUD 权限手工填成真实的写权限，`Mutability.READ_ONLY` 仍会挡下，但权限层的 fail-safe 就丢了；应始终使用工厂而非手工构造。

## 类型参数约束

与 `StandardManagerController` 完全一致。Kotlin 的泛型继承规则要求：

```kotlin
abstract class ReadonlyManagerController<
    SERVICE : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
    ...
> : StandardManagerController<SERVICE, ...>(managerService)
```

上下界必须完全传递，Readonly 不能修改约束。

## 与 AOP 拦截链的关系

只读控制器仍被 `ManagerControllerPermissionAspect` 拦截（切入点是 `StandardManagerController.*(..)`，覆盖所有子类），但只在 `permissions == null` 且带 `@ManagerPermissions` 的老代码路径上真正生效；带 `PermissionMatrix` 的新代码由父类 `authorize` 直接放行 AOP：

```
StandardManagerController.* (pointcut)
    → ManagerControllerPermissionAspect (@Order 较高)
        → permissions != null → proceed（authorize 已处理）
        → permissions == null + @ManagerPermissions → 老校验路径
    → AbstractManagerController → Mutability.READ_ONLY 挡 CUD
```

审计切面 `ManagerControllerAuditAspect` 同样覆盖此类，被拒的调用也会被记录，用于事后分析异常访问模式。

## 命名规范

以下两种命名风格任选：

- `Manager{Xxx}Controller`（如 `ManagerMailSendLogController`）——与 Standard 一致
- `Manager{Xxx}ReadonlyController`——在类名中显式标注"该资源不可改"

同一模块内保持命名风格一致。

## 现有真实使用位置

| 模块 | Controller | 承载的资源 |
|---|---|---|
| `crystal-audit` | `ManagerAuditLogController` | 审计日志 |
| `crystal-mail` | `ManagerMailSendLogController` | 邮件发送记录 |
| `crystal-auth` | `ManagerUserLoginLogController` | 用户登录日志 |

新增只读资源时需确认：

1. 数据完全由系统生成（触发器、事件监听、切面等），无用户输入
2. 修改会破坏业务不变量（如审计日志被改会毁掉审计的意义）

两条同时成立才使用 Readonly 家族；否则考虑普通 Standard + 精细化权限控制。

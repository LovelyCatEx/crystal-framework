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
    managerService: SERVICE
) : StandardManagerController<SERVICE, ...>(managerService) {

    override suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: CREATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be created")
    }

    override suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: UPDATE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be updated")
    }

    override suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute dto: DELETE_DTO
    ): ApiResponse<*> {
        return ApiResponse.forbidden<Nothing>("This resource is read-only and cannot be deleted")
    }
}
```

结构要点：

- 仅重写 3 个写方法，直接返回 `ApiResponse.forbidden`
- `list` 与 `query` 未被触碰，完全继承自 `StandardManagerController`
- 类型参数与父类完全一致——泛型继承强制上下界传递

## 双重防护链路

三个写方法即使被调用也不会真正执行，但在此之前 `ManagerControllerPermissionAspect` 已先做一轮权限校验。完整链路：

```
POST /create
  → ManagerControllerPermissionAspect（AOP）
      ├─ 校验 @ManagerPermissions.create 里的权限
      ├─ 无权限 → AuthorizationDeniedException（GlobalExceptionHandler 转 403）
      └─ 有权限 → 继续
  → ReadonlyManagerController.create（业务层重写）
      └─ 恒定返回 ApiResponse.forbidden（403）
```

两层拦截的动机：权限层拦是通用防线（配置错误或角色错发），业务层拦是结构性约束——此类型的 Controller 就是不允许写入，代码即声明。即使权限层配置错误让请求穿过，业务层也会兜底。

## 权限配置的实际做法

一般把 `@ManagerPermissions` 中所有 5 个字段都填同一个读权限：

```kotlin
@ManagerPermissions(
    read    = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],  // 即使过了 AOP，业务层仍拒绝
    update  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete  = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
```

若把 `create` 权限配为不存在的字符串，AOP 会先拦；填读权限有一个好处：读权限持有者调 `create` 会直接拿到业务层的语义化 403（"cannot be created"），而不是权限层的通用 "Access denied"，前端更容易分辨"不能改这类资源"与"没权限"。

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

只读控制器仍被 `ManagerControllerPermissionAspect` 拦截（切入点是 `StandardManagerController.*(..)`，覆盖所有子类）：

```
StandardManagerController.* (pointcut)
    → ManagerControllerPermissionAspect (@Order 较高)
        → @ManagerPermissions 校验
            → create / update / delete → 业务方法（重写后返回 403）
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

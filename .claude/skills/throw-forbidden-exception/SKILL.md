---
name: throw-forbidden-exception
description: 抛出结构化 403 ForbiddenException 的规范,覆盖 ForbiddenContext 三字段(reason / requiredPermissions / scope)填法、ForbiddenReason 枚举选择、requiredPermissions 从 PermissionMatrix.layersFor 派生并过滤 NEVER_GRANTED/NOT_APPLICABLE 的写法、scope 强制必填、以及"没有合适 reason 时怎么新增枚举值"的后端 + 前端四步同步流程。
---

# 抛出结构化 403 异常 (ForbiddenException)

## 触发条件

当业务代码需要拒绝访问 (403) 时使用。典型场景:

- Controller / Service 中检测到调用者缺少权限
- 资源属于其他租户 / 其他用户 (scope mismatch)
- 尝试删改被系统保护的资源 (protected role / super_admin / built-in permission 等)
- 尝试触碰会导致权限提升的操作
- Manager Controller 家族中 tenant 分支要求 `userAuthentication.tenantId` 但未提供

## 前提: 后端返回结构

`ForbiddenException` 携带的 `ForbiddenContext` 会被 `GlobalExceptionHandler` 挂到 `ApiResponse.data`,前端 `system-request.ts` 会 `isForbiddenContext(data)` 判断:结构化则弹 `ForbiddenModal`,否则降级到 `message.warning(response.message)`。**因此填 context 不是可选装饰,而是让前端获得可读弹窗的唯一路径。**

`ForbiddenException` 签名:

```kotlin
class ForbiddenException(
    message: String = "",
    cause: Exception? = null,
    val context: ForbiddenContext? = null,
) : RuntimeException(message, cause)
```

`ForbiddenContext` (位于 `crystal-shared/src/main/kotlin/com/lovelycatv/crystalframework/shared/exception/ForbiddenContext.kt`):

```kotlin
data class ForbiddenContext(
    val requiredPermissions: List<String> = emptyList(),
    val reason: ForbiddenReason = ForbiddenReason.MISSING_PERMISSION,
    val scope: ResourceScope,        // 无默认值,必填
)
```

## 三字段填法

### 1. `scope: ResourceScope` — 必填

**禁止用字符串。** 只能使用 `com.lovelycatv.crystalframework.shared.types.common.ResourceScope` 枚举。

| 语义 | 值 |
|---|---|
| 资源属于系统级 (跨租户共享) | `ResourceScope.SYSTEM` |
| 资源属于某个租户 (租户隔离) | `ResourceScope.TENANT` |

**如何选择**:

- **Manager Controller 家族**: 看基类
  - `StandardManagerController` / `ReadonlyManagerController` (system-only) → `SYSTEM`
  - `StandardTenantManagerController` / `ReadonlyTenantManagerController` (tenant-only) → `TENANT`
  - `StandardScopedManagerController` / `ReadonlyScopedManagerController` (双域) → 用当前分支的 `resolvedScope` 变量,不要写死
- **Service 层**: 看操作对象
  - `OAuthAccountServiceImpl.unbindUser` (系统级绑定) → `SYSTEM`
  - `OAuthAccountServiceImpl.unbindTenant` (租户级绑定) → `TENANT`
  - 角色 / 权限 / 用户等系统级资源 → `SYSTEM`
- **自定义端点**: 看资源本身的 `BaseScopedEntity.scope` 或调用方 `userAuthentication.tenantId` 的存在性
  - 已经 resolve 出 scope 变量 → 直接传变量
  - 分支内确知 scope → 传对应字面量

### 2. `reason: ForbiddenReason` — 语义化原因

**必须使用枚举,禁止裸字符串。** 定义在 `crystal-shared/src/main/kotlin/com/lovelycatv/crystalframework/shared/exception/ForbiddenReason.kt`:

| 枚举值 | 语义 | 典型使用位置 |
|---|---|---|
| `MISSING_PERMISSION` | 调用者缺少所需权限 (默认原因) | RBAC 权限矩阵校验失败、`else` 分支兜底 |
| `SCOPE_MISMATCH` | 资源不属于当前 scope / 租户 / 用户 | 跨租户访问、跨用户操作、任务不属于当前接收人 |
| `PROTECTED_RESOURCE` | 资源受保护 (Mutability / immutable 标记) | `AbstractManagerController` 的 `assertXxxAllowed` |
| `NOT_TENANT_MEMBER` | 调用者不是当前租户的成员 | `userAuthentication.tenantMemberId` 为空的 TENANT 分支 |
| `ROLE_PROTECTED` | 角色本身受保护 (如 super_admin 系统角色) | `UserRoleRelationServiceImpl` 尝试修改保护角色 |
| `PERMISSION_ESCALATION` | 该操作会导致权限提升 | `UserRolePermissionRelationServiceImpl` 授予调用者自己没有的权限 |

**选择顺序**:

1. 先在上表找语义最贴的
2. 找不到 → 停下来,按下文 **"新增 ForbiddenReason 枚举值"** 全链路走一遍,不允许把不匹配的原因硬套上去
3. 也不允许把新原因塞进 `message` 字符串 —— 前端弹窗只展示枚举翻译,`message` 只是 fallback 提示

### 3. `requiredPermissions: List<String>` — 权限清单

**默认可省略 (`emptyList()`),但只要能列出所需 authority 就应当填。** 前端 Modal 会渲染成 Tag 列表,是最直观的诊断信息。

**Manager Controller 家族中标准派生方式** —— 从 `PermissionMatrix` 直接派生:

```kotlin
// 场景 A: StandardScopedManagerController / ReadonlyScopedManagerController
// resolvedScope 和 operation 已在上下文中,直接查矩阵
throw ForbiddenException(
    context = ForbiddenContext(
        reason = ForbiddenReason.MISSING_PERMISSION,
        requiredPermissions = permissions?.layersFor(resolvedScope, ScopedOperation.READ)
            ?.filter { it != PermissionMatrix.NEVER_GRANTED }
            ?.toList() ?: emptyList(),
        scope = resolvedScope,
    )
)
```

```kotlin
// 场景 B: StandardTenantManagerController 里显式列 admin + pem 两层
throw ForbiddenException(
    context = ForbiddenContext(
        reason = ForbiddenReason.MISSING_PERMISSION,
        requiredPermissions = listOf(permissions.tenantAdminCreate, permissions.tenantPemCreate)
            .filter { it != PermissionMatrix.NOT_APPLICABLE && it != PermissionMatrix.NEVER_GRANTED },
        scope = ResourceScope.TENANT,
    )
)
```

**为什么必须过滤两个哨兵**:

- `PermissionMatrix.NOT_APPLICABLE` (`"!!not_applicable!!"`): 这一层对当前资源没有语义 (如 SYSTEM-only 资源的 `tenantAdmin`/`tenantPem` 层)。`layersFor()` 内部已过滤,但**手工组合 `listOf(admin, pem)` 时不会**,必须手动过滤。
- `PermissionMatrix.NEVER_GRANTED` (`"!!never_granted!!"`): 这一层"存在但显式禁用" (如 Readonly variants 的 CREATE / UPDATE / DELETE)。`layersFor()` **保留**该值以便 OR-check 失败,但暴露给前端的 requiredPermissions 是无意义字符串 —— 必须过滤掉。

**Service 层 / 自定义端点** — 无 `PermissionMatrix` 时直接列 SystemPermission / TenantPermission 常量:

```kotlin
throw ForbiddenException(
    context = ForbiddenContext(
        reason = ForbiddenReason.MISSING_PERMISSION,
        requiredPermissions = listOf(
            SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_READ.name,
            TenantPermission.ACTION_MEMBER_ROLE_READ.name,
        ),
        scope = ResourceScope.TENANT,
    )
)
```

**禁止**:

- ❌ `requiredPermissions = listOf("tenant.role.create")` —— 违反禁止魔法值规则,必须走 `TenantPermission.ACTION_XXX.name`
- ❌ 把权限拼进 `message` 字符串 —— 前端只读结构化字段
- ❌ 忘了过滤 `NEVER_GRANTED` —— 前端 Tag 会显示 `!!never_granted!!`

## 完整写法示例

### 示例 1: Scoped Manager Controller 缺权限

```kotlin
val matrix = permissions
    ?: error("ManagerXxxController requires a PermissionMatrix")
if (!checkPermission(resolvedScope, scopeId, ScopedOperation.READ, userAuthentication)) {
    throw ForbiddenException(
        context = ForbiddenContext(
            reason = ForbiddenReason.MISSING_PERMISSION,
            requiredPermissions = matrix.layersFor(resolvedScope, ScopedOperation.READ)
                .filter { it != PermissionMatrix.NEVER_GRANTED }
                .toList(),
            scope = resolvedScope,
        )
    )
}
```

### 示例 2: 租户资源不属于当前租户

```kotlin
if (!checkOwnership(resolvedScope, scopeId, ScopedOperation.READ, userAuthentication)) {
    throw ForbiddenException(
        context = ForbiddenContext(
            reason = ForbiddenReason.SCOPE_MISMATCH,
            scope = resolvedScope,
        )
    )
}
```

### 示例 3: TENANT 分支需要 tenantMemberId

```kotlin
val callerId = when (resolvedScope) {
    ResourceScope.SYSTEM -> userAuthentication.userId
    ResourceScope.TENANT -> userAuthentication.tenantMemberId
        ?: throw ForbiddenException(
            "Current user is not a member of this tenant",
            context = ForbiddenContext(
                reason = ForbiddenReason.NOT_TENANT_MEMBER,
                scope = ResourceScope.TENANT,
            )
        )
}
```

### 示例 4: 保护资源

```kotlin
// 通常由 AbstractManagerController.assertXxxAllowed(scope) 内部抛
throw ForbiddenException(
    "This resource is immutable",
    context = ForbiddenContext(
        reason = ForbiddenReason.PROTECTED_RESOURCE,
        scope = scope,   // 由外层 assertXxxAllowed 传入
    )
)
```

### 示例 5: 权限提升

```kotlin
// UserRolePermissionRelationServiceImpl: 授予的权限里存在调用者自身不具备的
if (grantedPermissions.any { !currentUserAuthorities.contains(it) }) {
    throw ForbiddenException(
        "Cannot grant permissions you do not hold",
        context = ForbiddenContext(
            reason = ForbiddenReason.PERMISSION_ESCALATION,
            scope = ResourceScope.SYSTEM,
        )
    )
}
```

## 禁止清单

- ❌ **只写 message 不写 context**: 前端拿不到结构化数据,只会弹 `message.warning`。除非当前分支确实无法给出 reason/scope,否则必须补齐。
- ❌ **`scope` 用字符串**: 只允许 `ResourceScope.SYSTEM` / `ResourceScope.TENANT` 枚举值。
- ❌ **`reason` 用不贴切的枚举硬凑**: 找不到贴切枚举 → 先加枚举再回来抛。
- ❌ **`requiredPermissions` 出现 `NEVER_GRANTED` / `NOT_APPLICABLE` 哨兵**: 必须过滤。
- ❌ **`requiredPermissions` 写魔法值**: 必须引用 `SystemPermission.ACTION_XXX.name` / `TenantPermission.ACTION_XXX.name` 常量。
- ❌ **在 `catch` 块把 `AuthorizationDeniedException` 或其他 Spring Security 异常直接翻译成裸 `ForbiddenException` 不带 context**: 转换时应尽量补齐 scope / reason。
- ❌ **重复检查已经由框架切面负责的场景**: `PermissionMatrix` 自身的检查由 `StandardScopedManagerController.assertAccess` 完成,不要在每个 override 前重复抛。

## 新增 ForbiddenReason 枚举值 (原有 6 个不够用时)

**触发条件**: 上文的 6 个枚举 (`MISSING_PERMISSION` / `SCOPE_MISMATCH` / `PROTECTED_RESOURCE` / `NOT_TENANT_MEMBER` / `ROLE_PROTECTED` / `PERMISSION_ESCALATION`) 都无法准确描述当前 403 语义时,必须走以下四步同步流程 —— **禁止**只加后端不加前端 (前端会显示成 `enums.unknown (VALUE)`)。

### 前置判断

先确认新原因的**独立性**:

- 能否被现有枚举涵盖 (如"跨部门操作" 归 `SCOPE_MISMATCH`,不用新加)
- 是否属于 `BusinessException` 而非 `ForbiddenException` (业务规则错误 ≠ 授权失败,前者不算 403)
- 只有确定是全新的授权失败类别,才继续

### 步骤 1 — 后端: 新增 `ForbiddenReason` 枚举值

文件: `crystal-shared/src/main/kotlin/com/lovelycatv/crystalframework/shared/exception/ForbiddenReason.kt`

```kotlin
enum class ForbiddenReason {
    MISSING_PERMISSION,
    SCOPE_MISMATCH,
    PROTECTED_RESOURCE,
    NOT_TENANT_MEMBER,
    ROLE_PROTECTED,
    PERMISSION_ESCALATION,
    YOUR_NEW_REASON,     // ← 新增,SCREAMING_SNAKE_CASE
}
```

**命名约定**: 大写下划线,与其他值风格一致,不带前缀。

### 步骤 2 — 前端: 同步 TS 枚举

文件: `web/src/types/common/forbidden.types.ts`

```typescript
export enum ForbiddenReason {
    MISSING_PERMISSION = "MISSING_PERMISSION",
    SCOPE_MISMATCH = "SCOPE_MISMATCH",
    PROTECTED_RESOURCE = "PROTECTED_RESOURCE",
    NOT_TENANT_MEMBER = "NOT_TENANT_MEMBER",
    ROLE_PROTECTED = "ROLE_PROTECTED",
    PERMISSION_ESCALATION = "PERMISSION_ESCALATION",
    YOUR_NEW_REASON = "YOUR_NEW_REASON",     // ← 与后端 name 完全一致
}
```

**注意**:

- 值必须是**字符串枚举**,和后端 Jackson 默认序列化的 enum name 保持一致 (后端 `ResourceScope` / `ForbiddenReason` 均按 name 序列化,无 `@JsonValue` 覆盖)
- 名字和值都要写 (不能用 `= 6` 这类数字)

### 步骤 3 — 前端: 添加 i18n 翻译

文件: `web/src/i18n/locales/en-US.ts` 和 `web/src/i18n/locales/zh-CN.ts` **必须同时改**,不允许只加一种语言 (违反"文档修改必须跨语言同步"规则)。

`en-US.ts` 的 `enums.forbiddenReason` 对象:

```typescript
forbiddenReason: {
  MISSING_PERMISSION: 'Missing permission',
  SCOPE_MISMATCH: 'Resource does not belong to your scope',
  PROTECTED_RESOURCE: 'This resource is protected',
  NOT_TENANT_MEMBER: 'Not a member of this tenant',
  ROLE_PROTECTED: 'Role is protected from this operation',
  PERMISSION_ESCALATION: 'Operation would escalate privileges',
  YOUR_NEW_REASON: 'Human-readable description',    // ← 新增
},
```

`zh-CN.ts` 的 `enums.forbiddenReason` 对象:

```typescript
forbiddenReason: {
  MISSING_PERMISSION: '缺少所需权限',
  SCOPE_MISMATCH: '资源不属于当前范围',
  PROTECTED_RESOURCE: '该资源受保护',
  NOT_TENANT_MEMBER: '你不是该租户的成员',
  ROLE_PROTECTED: '该角色受保护，禁止执行此操作',
  PERMISSION_ESCALATION: '该操作会导致权限提升',
  YOUR_NEW_REASON: '可读的中文说明',              // ← 新增
},
```

**注意**: 两个语言的条目数量必须严格一致,不允许中文写得更细,也不允许英文额外加原文没有的解释。

### 步骤 4 — 无需改 `enum-helpers.ts` 或 `ForbiddenModal.tsx`

`getForbiddenReason(reason: string)` 已经通过 `translateEnum('forbiddenReason', reason)` 自动读取 `enums.forbiddenReason.<VALUE>`。新增值只要走完步骤 1-3 就自动生效,**不需要**再动 helper 或 Modal 组件。

### 验证 (必须做)

1. **后端**: `./mvnw clean install -pl crystal-shared -am -DskipTests` 通过
2. **前端**: `cd web && npx tsc --noEmit` 通过 —— 若 TS 枚举漏加值,任何 `context.reason === ForbiddenReason.YOUR_NEW_REASON` 的比较都会编译报错
3. **本地手测**: 触发对应 403,前端 Modal "原因" 字段显示的是**新翻译文案而非 `enums.unknown (YOUR_NEW_REASON)`**。若显示 unknown,说明 locales 没同步或键名拼错。

### 反面例子

**❌ 只加后端不加前端**:

```
# 后端加了 CIRCULAR_DEPENDENCY
# 前端 forbidden.types.ts 没加、locales 没加
# 结果: 前端 Modal 显示 "enums.unknown (CIRCULAR_DEPENDENCY)"
# DEV 环境 console.warn: [i18n] missing enum translation: enums.forbiddenReason.CIRCULAR_DEPENDENCY
```

**❌ 后端加了但前端 TS 枚举值写错大小写**:

```typescript
// 前端写成 CircularDependency = "CircularDependency"
// 后端序列化的字符串是 "CIRCULAR_DEPENDENCY"
// 前端 isForbiddenContext 依旧通过,但 getForbiddenReason("CIRCULAR_DEPENDENCY") 匹配不到翻译键,fallback 到 unknown
```

**❌ 只加了中文没加英文**:

```
# 违反跨语言同步规则,英文用户看到 enums.unknown
```

## 相关引用

- `crystal-shared/exception/ForbiddenException.kt`
- `crystal-shared/exception/ForbiddenContext.kt`
- `crystal-shared/exception/ForbiddenReason.kt`
- `crystal-shared/controller/PermissionMatrix.kt` (`layersFor` / `NEVER_GRANTED` / `NOT_APPLICABLE`)
- `crystal-shared/controller/StandardScopedManagerController.kt` (`assertAccess` 中的标准写法)
- `crystal-shared/controller/StandardTenantManagerController.kt` (`tenantMissingPermission` / `tenantScopeMismatch` 辅助方法)
- `crystal-shared/controller/Mutability.kt` (`assertXxxAllowed(scope)` `PROTECTED_RESOURCE`)
- `web/src/types/common/forbidden.types.ts` (前端类型)
- `web/src/components/ForbiddenModal.tsx` (前端弹窗)
- `web/src/api/system-request.ts` (403 分流入口,`isForbiddenContext` 判定)
- `web/src/i18n/enum-helpers.ts` (`getForbiddenReason` / `getForbiddenScope`)

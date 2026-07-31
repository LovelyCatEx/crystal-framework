---
name: add-audit-annotation
description: 给自定义 controller 端点加 @Audit 审计注解,覆盖 pom 依赖、AuditAction 枚举、resourceType 走 TableConstants、resourceIds SpEL 表达式、UserAuthentication 参数要求、以及 Manager Controller 家族的标准 CRUD 端点已被自动审计无需再加的场景。
---

# 添加 @Audit 审计注解

## 触发条件

当用户要求给一个 controller 端点加**操作审计日志**(记录谁、何时、对哪个资源做了 CRUD)时使用。典型场景:

- 自定义业务端点(register / bind / unbind / handle / start 等)需要留痕
- OAuth 绑定 / 解绑 / 切租户
- 系统设置读写 / 敏感操作
- 审批流程实例的 handle / start 等触发型端点

## 判断标准 —— 加还是不加,由 controller 基类决定

**不用手动加 `@Audit`**(自动审计):

- 端点是 **Manager Controller 家族的标准 CRUD 5 端点** —— `readAll` / `create` / `read` / `update` / `delete`
- 只要 controller 继承 `AbstractManagerController` 及其子类(`StandardManagerController` / `StandardScopedManagerController` / `StandardTenantManagerController` / `ReadonlyManagerController` / `ReadonlyScopedManagerController`)
- 泛型第 3 位 ENTITY 上带了 `@Table("xxx")`
- 方法参数含 `UserAuthentication`

上述条件满足时,`ManagerControllerAuditAspect` 会自动切入并记录审计日志。**手动再加 `@Audit` 是重复,会被两个切面都命中**(order 同级),要避免。

**需要手动加 `@Audit`**(显式审计):

- 自定义业务端点(非标准 CRUD 5 个方法名)
- Manager Controller 里自定义的额外端点(如 `/reorder` `/tree` `/handle`)
- Generic Controller 里所有需要留痕的端点

**不要加的场景**(硬做也无价值):

- **方法签名没有 `UserAuthentication` 参数** —— 切面直接静默放行,注解形同虚设。除非能安全补入参(见"给方法补 UserAuthentication 入参"节)
- 只读列表 / 无副作用的 GET 聚合查询,且不涉及敏感数据(如公告 list、readiness 探针)
- 一次性系统初始化端点(`@Unauthorized` + 无登录态)
- 找不到合适的 `TableConstants.TABLE_*` 常量,且不属于表数据(如系统维护开关这类运行时状态)—— 需先与用户对齐是新增 `AuditResourceConstants` 还是复用现有常量,不允许硬套

## 输入格式

用户需要提供:
1. 目标 controller 类 / 方法列表
2. 每个方法的 action 语义(CREATE / READ / UPDATE / DELETE)
3. 资源对应的表(用于选 `TableConstants.TABLE_XXX`)

如果方法签名缺 `UserAuthentication`,先与用户确认能否补入参再动手。

## 前提信息

### 注解定义

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Audit(
    val action: AuditAction,           // 必填枚举
    val resourceType: String,          // 必填,走 TableConstants.TABLE_XXX
    val resourceIds: String = "",      // 可选 SpEL,默认空串→null
)
```

### AuditAction 枚举

```
UNKNOWN(0) / CREATE(1) / READ(2) / UPDATE(3) / DELETE(4)
```

**禁止使用 `UNKNOWN`**(仅用作解析失败的兜底)。CREATE/READ/UPDATE/DELETE 按业务语义精确对齐:

| 动作 | 语义 |
|---|---|
| `CREATE` | 新增资源(bind / register / 创建审批) |
| `READ` | 只读查询、敏感数据聚合(getProfile / getSettings / testSendEmail 等触发但不改数据的动作也归 READ) |
| `UPDATE` | 修改资源(unbind / accept / handle / 状态切换等改变资源状态的都归 UPDATE) |
| `DELETE` | 删除资源(物理或软删) |

### resourceType —— 走 TableConstants,禁止裸字符串

**必须**使用 `TableConstants.TABLE_XXX` 常量,与实体的 `@Table("xxx")` 值一致:

```kotlin
// ✓
@Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_OAUTH_ACCOUNTS)

// ✗ 违反"禁止魔法值"规则
@Audit(action = AuditAction.UPDATE, resourceType = "oauth_accounts")
```

若 `TableConstants` 里没有对应常量,**先补常量**(参照现有 TABLE_XXX 命名),再回来加注解。**禁止**只为审计而临时新增"资源类型字符串常量"—— 如果目标不是表数据(如系统维护开关),停下向用户确认是否新增 `AuditResourceConstants` 或复用现有 `TABLE_SYSTEM_SETTINGS`。

### resourceIds SpEL 语法

- **变量以方法参数名进上下文**,前缀 `#`,如 `#userAuthentication.tenantId` `#dto.oauthAccountId` `#id`
- 表达式结果必须是 `Long` 或 `Collection<Long>`,否则运行时 `error()` 抛
- 空串(默认值)→ 不记录 resourceIds,数据库字段为 null
- 参数名以**方法实际参数名**为准,不是想当然,写前必须 Read 方法签名验证

```kotlin
// 单参数 id
@Audit(action = AuditAction.READ, resourceType = TableConstants.TABLE_FILE_RESOURCES, resourceIds = "#id")
suspend fun getFileDownloadUrl(@RequestParam id: Long): ApiResponse<*> { ... }

// DTO 里的字段
@Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_OAUTH_ACCOUNTS, resourceIds = "#dto.oauthAccountId")
suspend fun bindOAuthAccount(userAuthentication: UserAuthentication, @ModelAttribute dto: BindOAuthAccountDTO): ApiResponse<*> { ... }

// UserAuthentication 里的字段
@Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_TENANTS, resourceIds = "#userAuthentication.tenantId")
suspend fun updateTenantProfile(userAuthentication: UserAuthentication, @ModelAttribute dto: UpdateTenantProfileDTO): ApiResponse<*> { ... }

// 找不到合适的 id 就不写
@Audit(action = AuditAction.READ, resourceType = TableConstants.TABLE_TENANT_SETTINGS)
suspend fun getTenantSettings(userAuthentication: UserAuthentication): ApiResponse<*> { ... }
```

**参数名不匹配的坑**:如果方法参数叫 `authentication` 而 SpEL 写 `#userAuthentication.xxx`,会解析失败;宁可省略 `resourceIds` 也不能盲写。

### UserAuthentication 参数是**硬性触发条件**

`AnnotatedControllerAuditAspect` 检查方法参数中是否含 `UserAuthentication`:

- **没有**:切面**静默放行**,`@Audit` 完全不生效
- **有可空版 `UserAuthentication?`**:参数是 `null` 时仍会跳过记录(`filterIsInstance` 会命中类型,但 null 场景下 `firstOrNull()` 拿到实例才继续)

若方法本来没有 `UserAuthentication`(如通过 `@PreAuthorize("hasAuthority(...)")` 保证已登录的端点):

- **能补就补**:在参数列表加 `userAuthentication: UserAuthentication`,Spring 自动注入
- **不能补**(如 `@Unauthorized` 匿名端点):加了也不生效,别加

### pom 依赖

**任何模块的 controller 使用 `@Audit` 之前**,该模块的 `pom.xml` 必须显式声明 `crystal-audit` 依赖:

```xml
<dependency>
    <groupId>com.lovelycatv.crystalframework</groupId>
    <artifactId>crystal-audit</artifactId>
</dependency>
```

- 无需 `<version>`,继承父 pom 的 `${revision}`
- 位置紧跟 `crystal-shared` 后,与 `crystal-rbac/pom.xml` 保持一致
- **禁止依赖 transitive**(靠间接依赖引进来),中间模块调整就断链

## 执行步骤

1. **前置核查**:
   - 模块 `pom.xml` 是否有 `crystal-audit`?没有 → 先补依赖
   - 目标方法是否属于 Manager Controller 家族的标准 CRUD 5 端点?是 → 已自动审计,**不加**
   - 目标方法是否含 `UserAuthentication`?没有 → 与用户对齐能否补入参
   - `TableConstants` 里有对应 `TABLE_XXX`?没有 → 停下问用户

2. **加 imports**(每个 controller 文件顶部):
   ```kotlin
   import com.lovelycatv.crystalframework.audit.annotations.Audit
   import com.lovelycatv.crystalframework.audit.types.AuditAction
   import com.lovelycatv.crystalframework.shared.constants.TableConstants
   ```

3. **给方法加注解**(紧贴其他方法级注解,通常放在最上面):
   ```kotlin
   @Audit(action = AuditAction.UPDATE, resourceType = TableConstants.TABLE_XXX, resourceIds = "#dto.xxxId")
   @PreAuthorize("hasAnyAuthority('...')")
   @PostMapping("/xxx")
   suspend fun xxx(
       userAuthentication: UserAuthentication,
       @ModelAttribute dto: XxxDTO,
   ): ApiResponse<*> { ... }
   ```

4. **SpEL 参数名验证**:每写一个 `#xxx.yyy`,Read 一次方法签名确认参数名对得上、字段类型是 `Long`(或 `Collection<Long>`)

5. **编译验证**:
   ```bash
   ./mvnw -q -pl <module> -am compile -DskipTests
   ```

## 常见错误

| 错误 | 修正 |
|---|---|
| 给标准 CRUD 5 端点(`create`/`read`/`readAll`/`update`/`delete`)加 `@Audit` | 已被 `ManagerControllerAuditAspect` 自动审计,别重复加 |
| 方法签名没 `UserAuthentication` 却加了注解 | 补入参(Spring 自动注入)或跳过该端点;硬加无效 |
| `resourceType = "oauth_accounts"` 裸字符串 | 走 `TableConstants.TABLE_OAUTH_ACCOUNTS` |
| SpEL 参数名与方法签名不一致(如 `#userAuthentication` vs `authentication`) | Read 方法签名确认;宁可省略 `resourceIds` |
| SpEL 表达式结果不是 `Long` / `Collection<Long>`(如 `String`) | 换字段;或改用 `.toLong()` 强转前提是字段能安全转 |
| 模块 pom 缺 `crystal-audit` 依赖,靠 transitive 拿到 | 显式声明,避免间接依赖断链 |
| 用 `AuditAction.UNKNOWN` | 精确对齐 CREATE/READ/UPDATE/DELETE 之一 |
| 给 `@Unauthorized` 匿名端点加 `@Audit` | 无 `UserAuthentication` 时切面静默跳过,别加 |
| 找不到 `TableConstants.TABLE_XXX` 就临时新增一个非表名常量 | 停下问用户,可能是新增 `AuditResourceConstants` 的信号 |

## 快速参考

**触发路径**(哪个切面生效):

```
方法上有 @Audit 注解?
├── 是 → AnnotatedControllerAuditAspect(要求方法参数含 UserAuthentication)
└── 否 → Controller 是 AbstractManagerController 子类,且方法名是 5 个标准 CRUD 之一?
        ├── 是 → ManagerControllerAuditAspect(自动,需 ENTITY 有 @Table + 方法参数含 UserAuthentication)
        └── 否 → 不审计
```

**resourceIds SpEL 常见形式**:

| 场景 | 表达式 |
|---|---|
| 简单参数 id | `#id` |
| DTO 里的字段 | `#dto.xxxId` |
| UserAuthentication 里 | `#userAuthentication.tenantId` |
| 不写 / 无合适 id | 省略参数 |

## 输出格式

完成后说明:
1. 修改的 controller 文件路径 + 加注解的方法列表
2. 每个方法:action + resourceType + resourceIds(或"无")
3. 跳过的方法及原因(如"无 UserAuthentication")
4. 是否修改了模块 pom(补 `crystal-audit` 依赖)
5. 编译验证结果

---
name: write-integration-test
description: 为 crystal-starter 模块编写集成测试（@SpringBootTest + 真实 DB + 事务回滚），严格遵循 Service 1:1 映射、职责分离和现有测试模式。
---

# 编写集成测试

> **适用范围：** 集成测试 — 启动完整 Spring Boot 上下文、连接真实数据库（R2DBC + PostgreSQL）、通过事务回滚清理数据。
> **不适用于：** 纯单元测试（无 Spring 上下文、mock 替代真实依赖），纯单元测试应使用 Mockito + JUnit 5。

## 触发条件

当用户要求为已有功能编写集成测试、要求验证 Service 层的正确性、或要求提供数据 mock 方法时使用。

---

## 核心规则

### 规则一：测试类 1:1 映射 Service

每个测试类有且仅对应一个 Service 接口，类名 = Service 类名 + `Test`，放在同包路径下。

| 所在包 | Service | 测试类 |
|--------|---------|--------|
| `tenant.service` | `TenantTireTypeService` | `TenantTireTypeServiceTest` |
| `tenant.service` | `TenantService` | `TenantServiceTest` |
| `tenant.service` | `TenantBenefitService` | `TenantBenefitServiceTest` |
| `tenant.service.impl` | `TenantBenefitServiceImpl` | `TenantBenefitServiceImplTest` |
| `tenant.service.manager.impl` | `TenantMemberManagerServiceImpl` | `TenantMemberManagerServiceImplTest` |

### 规则二：Mock 方法必须映射到真实 Service 方法

测试类中公开的 `suspend fun mockXxx()` 方法必须调用其对应 Service 的真实方法（走完整事务 + DB 读写），禁止直接调 Repository。

**正确（通过 Service 调用）：**
```kotlin
// TenantMemberManagerServiceImplTest — 对应 TenantMemberManagerService
suspend fun mockMember(tenantId: Long, userId: Long): TenantMemberEntity {
    return memberManagerService.create(
        ManagerCreateTenantMemberDTO(tenantId = tenantId, memberUserId = userId)
    )
}
```

**禁止（跨职责）：**
```kotlin
// TenantServiceTest — 这是 TenantService 的测试类
suspend fun mockMember(...)      // ❌ mockMember 是 TenantMemberManagerService 的职责
suspend fun mockTireType(...)    // ❌ mockTireType 是 TenantTireTypeService 的职责
```

**例外 — Service 无公开 create 方法时：**
当 Service 仅继承 `CachedBaseService` 而无自定义 `create` 方法（如 `TenantTireTypeService`、`TenantService`），可直接调用 `repository.save()`。

```kotlin
// TenantTireTypeServiceTest — 对应 TenantTireTypeService（无 create 方法）
suspend fun mockTireType(name: String = "TestTire"): TenantTireTypeEntity {
    return tenantTireTypeRepository.save(
        TenantTireTypeEntity(id = snowIdGenerator.nextId(), name = name)
            .apply { newEntity() }
    ).awaitFirstOrNull() ?: error("Failed to create tire type")
}
```

### 规则三：组合方法归主体 Service

当一个方法需要组合多个实体的创建，以**主体实体**的 Service 决定归属：

```kotlin
// TenantServiceTest — 主体是 tenant
suspend fun mockTenantWithMembers(n: Int): Pair<TenantEntity, List<TenantMemberEntity>> {
    val tireTypeServiceTest = getTestClassInstance<TenantTireTypeServiceTest>(applicationContext)
    val memberServiceTest = getTestClassInstance<TenantMemberManagerServiceImplTest>(applicationContext)
    val tireType = tireTypeServiceTest.mockTireType()
    val owner = mockUser()
    val tenant = mockTenant(owner.id, tireType.id)
    val members = (1..n).map {
        val user = mockUser()
        memberServiceTest.mockMember(tenant.id, user.id)
    }
    return tenant to members
}
```

组合方法内部必须调用各领域的 Test 类的 mock 方法，禁止直接调 Repository。

### 规则四：跨测试类复用通过 getTestClassInstance

引用其他 Service 的 mock 方法必须通过 `getTestClassInstance` 获取实例：

```kotlin
class TenantInvitationManagerServiceImplTest(
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val memberServiceTest: TenantMemberManagerServiceImplTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }
}
```

### 规则五：禁止魔法值，优先使用项目中的常量

Feature key、permission name、table name、setting key 等所有可枚举的字符串值必须引用 `constants` 包或对应常量类中的常量，禁止硬编码。

```kotlin
// ❌ 禁止
setBenefitValue(tireType.id, "member.max_count", "10")
allBenefits["member.max_count"]

// ✅ 正确
setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "10")
allBenefits[TenantBenefit.MEMBER_MAX_COUNT.featureKey]
```

**例外：** 测试中用于限值的数值（如 `"10"`、`"2"`、`"0"`）是测试逻辑自有的值，不属于业务常量，无需提取为常量。

### 规则六：私有辅助方法可在 Impl 测试类中存在

在 `XxxServiceImplTest` 中可以定义 `private suspend fun` 减少代码重复，前提是内部调用真实 Service 方法：

```kotlin
// TenantBenefitServiceImplTest — private 辅助方法，内部调用 TenantTireBenefitValueManagerService
private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
    val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
        ?: error("Feature $featureKey not found")
    benefitValueManagerService.create(ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value))
}
```

### 规则七：只读校验方法可在 Service 测试类中存在

在对应 Service 的测试类中可提供只读校验方法，不违反 1:1 规则：

```kotlin
// TenantBenefitServiceTest — 只读校验，对应 TenantBenefitService（读服务）
suspend fun ensureBenefitFeaturesExist() {
    tenantBenefitRegistry.benefitDeclarations().forEach { decl ->
        val existing = benefitFeatureRepository.findByFeatureKey(decl.featureKey).awaitFirstOrNull()
        kotlin.test.assertNotNull(existing) { "Feature '${decl.featureKey}' missing from database" }
    }
}
```

---

## 集成测试基础结构

### 基类

所有集成测试类继承 `CrystalFrameworkApplicationTests`：

```kotlin
@SpringBootTest
@Import(ReactiveTestConfig::class, TestMockConfig::class, TestMockInitializer::class)
abstract class CrystalFrameworkApplicationTests {
    @Autowired protected lateinit var transactionalOperator: TransactionalOperator
    protected fun withTransactionalRollback(actionName: String, action: suspend () -> Unit)
    protected final inline fun <reified T> getTestClassInstance(applicationContext: ApplicationContext): T
}
```

### 构造函数注入

所有依赖通过 `@Autowired` 构造注入：

```kotlin
class XxxServiceImplTest(
    @Autowired private val xxxService: XxxService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {
    // ...
}
```

### 事务回滚

每个 `@Test` 方法包裹在 `withTransactionalRollback` 中，测试数据自动回滚：

```kotlin
@Test
fun testSomething() {
    withTransactionalRollback("test-name") {
        // 测试逻辑
    }
}
```

### 异常测试

Service 方法抛 `BusinessException` 时在 `withTransactionalRollback` 内用 `runCatching` 捕获：

```kotlin
val result = runCatching {
    service.create(dto)
}
val ex = result.exceptionOrNull()
assertNotNull(ex)
assertTrue(ex is BusinessException)
assertTrue((ex as BusinessException).message!!.contains("limit"))
```

---

## 新建集成测试的步骤

1. **确认要测试的 Service**，找到其接口路径
2. **创建测试类**，放在 `src/test/kotlin/` 下与 Service 同包路径，类名 = Service 名 + `Test`
3. **通过构造注入** 所需的 Service 和 `ApplicationContext`
4. **编写 mock 方法**（遵循规则二），供自己和其他测试类复用
5. **编写 `@Test` 方法**，使用 `withTransactionalRollback` 包裹
6. **编译验证**：`mvnw compile test-compile -DskipTests`
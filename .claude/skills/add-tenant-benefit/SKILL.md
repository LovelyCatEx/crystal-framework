---
name: add-tenant-benefit
description: 在 crystal-starter 中新增租户套餐权益（声明 → 注册 → 业务落地 → 测试），自动落库无需改 Runner/Registry。
---

# 新增租户权益

> **适用范围：** 在 `crystal-starter` 模块中添加一项新的租户套餐权益（如 `member.max_count`、`invitation.enabled`），包括声明、注册、业务侧使用和集成测试。
> **不适用的场景：** 仅修改前端展示、只改已有权益的默认值（仅改 `TenantBenefit.kt` 对应常量即可）。

---

## 触发条件

用户明确要求新增一项租户权益（tenant benefit/feature），或要求为某个功能增加可配置的限制/开关值，且该值需要按套餐类型区分。

---

## 步骤

### 第一步：在 `TenantBenefit.kt` 声明权益

```kotlin
// crystal-starter/src/main/.../tenant/constants/TenantBenefit.kt

val MY_FEATURE = TenantBenefitDeclaration(
    featureKey = "my.feature_key",               // 点号分隔，全局唯一
    name = "My Feature Display Name",            // 显示名称（英文，前端可过 i18n）
    description = "What this feature controls",  // 说明文字
    featureType = TenantBenefitType.LIMIT,       // BOOLEAN | LIMIT | ENUM
    defaultValue = "10",                         // 默认值
)
```

三个 `TenantBenefitType` 的用法：

| 类型 | 适用场景 | `defaultValue` 示例 | 业务代码取值 |
|------|----------|-------------------|-------------|
| `BOOLEAN` | 开关（是否启用） | `"true"` / `"false"` | `hasBenefit(tireTypeId, featureKey)` → `Boolean` |
| `LIMIT` | 数值限制 | `"100"` | `getBenefitLimit(tireTypeId, featureKey)` → `Long` |
| `ENUM` | 有限选项 | `"option1,option2,option3"` | `getBenefitValue(tireTypeId, featureKey)` → `String?` |

`extractAllValProperties` 会自动扫描新的 val，无需人工注册到任何列表。

### 第二步：业务侧使用

在需要限制的地方注入 `TenantBenefitService`，调用对应方法：

```kotlin
// BOOLEAN — 判断是否启用
if (!tenantBenefitService.hasBenefit(tireTypeId, TenantBenefit.MY_FEATURE.featureKey)) {
    throw BusinessException("My feature is not enabled")
}

// LIMIT — 获取限制值
val limit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.MY_FEATURE.featureKey)
if (currentCount >= limit) {
    throw BusinessException("Limit reached ($limit)")
}

// 通用取值
val value = tenantBenefitService.getBenefitValue(tireTypeId, TenantBenefit.MY_FEATURE.featureKey)
```

### 第三步：编写集成测试

参照 [write-integration-test](../write-integration-test/SKILL.md) 规则：

```kotlin
// 在业务对应的 ImplTest 中加（如 TenantInvitationManagerServiceImplTest）
@Test
fun myFeatureIsEnforced() {
    withTransactionalRollback("my-feature-enforced") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)
        val member = memberServiceTest.mockMember(tenant.id, owner.id)

        // 设为 false/0 测试限制被触发
        setBenefitValue(tireType.id, TenantBenefit.MY_FEATURE.featureKey, "false")
        // 设其他必要限制为高值避免干扰
        // ...

        val result = runCatching {
            targetService.create(dto)
        }
        assertTrue(result.exceptionOrNull() is BusinessException)
    }
}

@Test
fun myFeatureRespectsDefaultValue() {
    withTransactionalRollback("my-feature-default") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

        // 不设值，测默认值生效
        val result = tenantBenefitService.getBenefitValue(tireType.id, TenantBenefit.MY_FEATURE.featureKey)
        assertEquals(TenantBenefit.MY_FEATURE.defaultValue, result)
    }
}
```

## 不变的模块

| 模块/文件 | 原因 |
|-----------|------|
| `TenantBenefitBuiltinConfigurer` | 通过 `extractAllValProperties` 自动扫描，改 `TenantBenefit.kt` 即可 |
| `TenantBenefitRegistry` | 同上 |
| `TenantBenefitTableDataCheckRunner` | 启动时自动遍历 registry 写入 DB |
| Flyway migration | 表结构不变，只增记录 |
| Controller / DTO / VO | 权益管理和概览查询是通用的 |

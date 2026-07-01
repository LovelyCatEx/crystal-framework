---
name: add-tenant-benefit
description: 新增租户套餐权益（tenant benefit / feature）。在 crystal-tenant 声明并按需在 crystal-tenant/i18n 补齐前端翻译，其余基础设施（Registry / Configurer / TableDataCheckRunner / Controller / DTO / VO / Page）无需改动。用于开关型 / 数量限制型 / 枚举型三种权益。
---

# 新增租户套餐权益

## 触发条件

当用户明确要求为某个功能新增一项**按套餐类型区分**的权益开关、数量限制或有限选项时使用，例如：
- "允许某套餐禁用邀请码功能" → BOOLEAN 权益
- "限制某套餐最多创建 30 个部门" → LIMIT 权益
- "某套餐能选择的存储后端限定几种" → ENUM 权益

> **不适用的场景：** 仅修改前端展示、只想调整已有权益的默认值（直接改 `TenantBenefit.kt` 对应常量即可，无需走本 Skill 的完整流程）、新增系统级配置（改用 `add-system-settings`）、新增租户级配置（改用 `add-tenant-settings`）。租户权益（本 Skill）与租户设置（`add-tenant-settings`）是两套完全独立的机制，禁止混用。

---

## 输入格式

用户需要提供：
1. **featureKey**：点号分隔，格式 `group.name`（如 `invitation.max_count`、`member.max_count`）。首段是分组（前端会作为二级表格的一级）。**全局唯一。**
2. **featureType**：BOOLEAN / LIMIT / ENUM 三选一。
3. **defaultValue**：**必须是字符串**（`String`），按类型严格约束（见下表）。
4. **中英文 name / description**：用于前端 i18n。
5. **业务侧使用点**：哪个 Service 的哪个方法应当读取并强制这项权益。

---

## 前提信息

### 1. 声明结构 `TenantBenefitDeclaration`

位于 `crystal-sdk` 模块 `com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitDeclaration`：

```kotlin
data class TenantBenefitDeclaration(
    val featureKey: String,
    val name: String,
    val description: String = "",
    val featureType: TenantBenefitType,
    val defaultValue: String = "",
)
```

### 2. 支持的权益类型 `TenantBenefitType`

位于 `crystal-sdk` 模块 `com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitType`：

| Kotlin 枚举 | typeId | 前端展示 | `defaultValue` 严格格式 | 业务侧读取方法 | 返回类型 |
|--------------|--------|-----------|-----------|-----------|-----------|
| `BOOLEAN` | 0 | 开关 (Switch) | 字面量 `"true"` 或 `"false"`，其他值抛 BusinessException | `hasBenefit(tireTypeId, featureKey)` | `Boolean` |
| `LIMIT` | 1 | 整数输入 | 非负整数字符串（`"0"` 允许，`"-1"` / `"3.5"` 不允许） | `getBenefitLimit(tireTypeId, featureKey, defaultLimit = 0)` | `Long` |
| `ENUM` | 2 | 单选 (Select) | 逗号分隔的选项字符串，如 `"option1,option2,option3"`。运行时 featureValue 必须落在此集合内 | `getBenefitValue(tireTypeId, featureKey)` | `String?`（`null` 表示 feature 不存在） |

上述格式由 `crystal-tenant/utils/TenantBenefitValidator.kt` 强制校验：`validateByType` 校验值类型合法性，`validateEnumAllowedValue` 校验 ENUM 值在选项集内。**违反格式启动会失败，禁止绕过。**

### 3. 声明扫描机制（重要 — 与 Settings 不同）

`TenantBenefit.kt` 是一个 `object`，其中通过 `fun allBenefits(): List<TenantBenefitDeclaration>` 调用 `KotlinObjectClassUtils.extractAllValProperties(TenantBenefit, false)` **自动扫描所有 `val` 属性**。

因此新增权益**只需要**在 `TenantBenefit.kt` 添加一个 `val` 声明。**禁止**手动把它添加到任何列表中，也**禁止**改 `TenantBenefitBuiltinConfigurer`、`TenantBenefitRegistry`、`TenantBenefitRegistryConfiguration`、`TenantBenefitTableDataCheckRunner`。

启动时 `TenantBenefitTableDataCheckRunner`（`@Order(4)`）自动比对 registry 与 `tenant_tire_benefit_feature` 表，缺失的会插入。**前提**：`system.bootstrap.autoCheckRbacTableData` 系统设置为 `true`（默认 true）；若被关闭，需提示用户手动开启或人工执行数据初始化。

### 4. 业务侧读取入口 `TenantBenefitService`

位于 `crystal-tenant` 模块 `com.lovelycatv.crystalframework.tenant.service.TenantBenefitService`：

```kotlin
interface TenantBenefitService {
    suspend fun getBenefitValue(tireTypeId: Long, featureKey: String): String?
    suspend fun hasBenefit(tireTypeId: Long, featureKey: String): Boolean
    suspend fun getBenefitLimit(tireTypeId: Long, featureKey: String, defaultLimit: Long = 0): Long
    suspend fun getAllBenefitsForTireType(tireTypeId: Long): Map<String, String>
}
```

**必须**通过这些方法读取，**禁止**直接注入 `TenantTireBenefitFeatureRepository` / `TenantTireBenefitValueRepository` 读值。

**读取时 featureKey 必须使用 `TenantBenefit.XXX.featureKey` 常量**，禁止硬编码字符串（对应 CLAUDE.md 的"禁止魔法值"规则）。

### 5. 前端翻译映射文件

`web/src/i18n/tenant-benefit.tsx`：
- `useTenantBenefitKeyToTranslationMap()` 内的 `featureKeys: string[]` 数组维护所有 featureKey → 翻译对照。**新增权益必须把新 featureKey 加入该数组**，否则前端页面显示原始 key 而非 i18n 名称。
- `useTenantBenefitGroupToTranslationMap()` 内维护 group → `{label, icon}` 的 Map。**新增 group 必须在 Map 中添加条目**（选一个 `@ant-design/icons` 图标作为可选 icon）。

### 6. 前端 i18n 位置

`web/src/i18n/locales/zh-CN.ts` 与 `en-US.ts` 的 `pages.tenantTireBenefitValueManager` 节点下：
- `keys.{featureKey}.name`：权益名称
- `keys.{featureKey}.description`：权益说明
- `groups.{groupName}`：group 分组名（仅一级 group，如 `invitation` / `member`）

`enums.tenantBenefitType` 已完整覆盖 BOOLEAN / LIMIT / ENUM 三种，**新增权益不需要动**（除非引入新的 `TenantBenefitType`，那属于扩改枚举本身，不在本 Skill 范围）。

### 7. 不需要改的模块（明确列出，避免多余改动）

| 层级 | 文件 | 不需要改的原因 |
|------|------|----------------|
| SDK | `TenantBenefitRegistry.kt` | 通用注册容器，fail-fast 冲突检测已具备 |
| SDK | `TenantBenefitConfigurer.kt` | 是 fun interface，实现方在 tenant 模块 |
| tenant | `TenantBenefitBuiltinConfigurer.kt` | 已调用 `TenantBenefit.allBenefits()`，自动扫描 |
| tenant | `TenantBenefitRegistryConfiguration.kt` | Bean 装配层，与业务无关 |
| starter | `TenantBenefitTableDataCheckRunner.kt` | 启动时自动落库，与业务无关 |
| SQL / Flyway | 所有迁移脚本 | 表结构固定，只在运行时增记录 |
| tenant | `TenantBenefitController.kt` | 权益读取接口是通用的 map 返回 |
| tenant | `TenantTireBenefitFeatureManagerServiceImpl` / `TenantTireBenefitValueManagerServiceImpl` | 通用 CRUD Service，与具体权益无关 |
| web | `web/src/api/tenant/tenant-benefit.api.ts` | 通用 API 定义 |
| web | `web/src/types/tenant/tenant-benefit.types.ts` | 通用类型定义（TenantBenefitType 枚举已覆盖三种） |
| web | `TenantTireBenefitFeatureManagerPage.tsx` | 通用管理页 |
| web | `TenantTireBenefitValueCrossOverviewPage.tsx` / `TenantTireBenefitValueOverviewPage.tsx` / `TenantTireBenefitValueManagementPage.tsx` | 通用取值页 |
| web | `web/src/i18n/enum-helpers.ts` | `getTenantBenefitType` 已注册 |

---

## 执行步骤

### 后端

#### 步骤 1：在 `TenantBenefit.kt` 添加声明

文件路径：`crystal-tenant/src/main/kotlin/com/lovelycatv/crystalframework/tenant/constants/TenantBenefit.kt`

在 `object TenantBenefit` 内添加 `val` 声明。**必须按 featureKey 的 group 前缀顺序邻近现有同 group 的声明**，方便阅读。

```kotlin
// BOOLEAN 示例
val MY_FEATURE_ENABLED = TenantBenefitDeclaration(
    featureKey = "my_group.enabled",
    name = "My Feature",
    description = "Whether the tenant can use my feature",
    featureType = TenantBenefitType.BOOLEAN,
    defaultValue = "true",
)

// LIMIT 示例
val MY_FEATURE_MAX_COUNT = TenantBenefitDeclaration(
    featureKey = "my_group.max_count",
    name = "My Feature Limit",
    description = "Maximum number of my_feature entities per tenant",
    featureType = TenantBenefitType.LIMIT,
    defaultValue = "100",
)

// ENUM 示例
val MY_FEATURE_MODE = TenantBenefitDeclaration(
    featureKey = "my_group.mode",
    name = "My Feature Mode",
    description = "The operation mode for my_feature",
    featureType = TenantBenefitType.ENUM,
    defaultValue = "auto,manual,disabled",  // 逗号分隔的候选项，第一个作为初始默认
)
```

约束（违反即视为违规）：
1. featureKey 全局唯一。先 grep 整个仓库 `featureKey = "` 检查是否重复；重复立即停止并通知用户。
2. `name` 是英文兜底展示（前端有 i18n 时不显示）；`description` 同理。
3. `defaultValue` 类型必须匹配 `featureType`（见前提信息 2 节）；`TenantBenefitValidator` 会在业务操作时校验，启动落库不校验，但**不允许**留下会被校验拒绝的默认值。

#### 步骤 2：在业务 Service 中强制该权益

在真正需要限制的 Manager Service 或普通 Service 中，通过构造注入 `TenantBenefitService`（若尚未注入）：

```kotlin
@Service
class MyManagerServiceImpl(
    // ... 已有依赖
    private val tenantBenefitService: TenantBenefitService,
    private val tenantService: TenantService,  // 用于把 tenantId 转成 tireTypeId
) : ... {

    override suspend fun create(dto: MyCreateDTO) {
        // 1. 得到 tireTypeId
        val tenant = tenantService.getByIdOrNull(dto.tenantId)
            ?: throw BusinessException("Tenant not found")
        val tireTypeId = tenant.tireTypeId

        // 2. BOOLEAN 用法
        if (!tenantBenefitService.hasBenefit(tireTypeId, TenantBenefit.MY_FEATURE_ENABLED.featureKey)) {
            throw BusinessException("My feature is not enabled for this tenant plan")
        }

        // 3. LIMIT 用法（先取限值，再对当前实际数量校验）
        val limit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.MY_FEATURE_MAX_COUNT.featureKey)
        val currentCount = myRepository.countByTenantId(dto.tenantId).awaitSingle()
        if (currentCount >= limit) {
            throw BusinessException("Reached the maximum ($limit) allowed by the current plan")
        }

        // 4. ENUM 用法（通用取值）
        val mode = tenantBenefitService.getBenefitValue(tireTypeId, TenantBenefit.MY_FEATURE_MODE.featureKey)
        // ... 根据 mode 走不同逻辑
    }
}
```

强制约束：
1. **必须**使用 `TenantBenefit.XXX.featureKey` 常量，禁止字符串硬编码。
2. 拿到 `Boolean` / `Long` / `String?` 后自己抛 `BusinessException`，异常文案应能让最终用户理解（"该套餐未开启 X 功能" / "已达套餐上限 N"）。
3. 若同一次业务操作需要读多项权益，逐条调用即可（`getBenefitValue` / `hasBenefit` / `getBenefitLimit` 是轻量数据库查询），**禁止**为了"聚合"而绕过 Service 直接访问 Repository。

#### 步骤 3：集成测试

严格遵循 `write-integration-test` skill 的全部规则。测试类必须是**业务对应 Manager Service 的 ImplTest**（如 `MyManagerServiceImplTest`），因为权益是"入口点约束"，测试的是业务方法而非权益本身。

**a) 私有辅助方法：设置权益值**（写在 `MyManagerServiceImplTest` 内部）：

```kotlin
private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
    val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
        ?: error("Feature $featureKey not found")
    benefitValueManagerService.create(
        ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value)
    )
}
```

内部**必须**走 `TenantTireBenefitValueManagerService.create()`（真实 Service 方法），**禁止**直接 `benefitValueRepository.save()`。

**b) 依赖注入**（构造函数）：

```kotlin
class MyManagerServiceImplTest(
    @Autowired private val myManagerService: MyManagerService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val tenantBenefitService: TenantBenefitService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }
    // ...其他需要复用 mock 的 XxxServiceTest
}
```

**c) BOOLEAN 权益测试模板**：

```kotlin
@Test
fun myFeatureBlockedWhenDisabled() {
    withTransactionalRollback("my-feature-blocked-when-disabled") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

        // 关闭该权益
        setBenefitValue(tireType.id, TenantBenefit.MY_FEATURE_ENABLED.featureKey, "false")

        val result = runCatching { myManagerService.create(mockDto(tenant.id)) }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is BusinessException)
        assertTrue(ex.message!!.contains("not enabled"))
    }
}

@Test
fun myFeatureAllowedByDefault() {
    withTransactionalRollback("my-feature-allowed-by-default") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

        // 不设值 — 走默认值 "true"
        val created = myManagerService.create(mockDto(tenant.id))
        assertNotNull(created)
    }
}
```

**d) LIMIT 权益测试模板**：

```kotlin
@Test
fun myFeatureRespectsLimit() {
    withTransactionalRollback("my-feature-respects-limit") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

        // 限值改为 2
        setBenefitValue(tireType.id, TenantBenefit.MY_FEATURE_MAX_COUNT.featureKey, "2")

        // 前 2 次成功
        myManagerService.create(mockDto(tenant.id))
        myManagerService.create(mockDto(tenant.id))

        // 第 3 次被限
        val result = runCatching { myManagerService.create(mockDto(tenant.id)) }
        val ex = result.exceptionOrNull()
        assertNotNull(ex)
        assertTrue(ex is BusinessException)
        assertTrue(ex.message!!.contains("maximum"))
    }
}
```

**e) ENUM 权益测试模板**：

```kotlin
@Test
fun myFeatureModeSwitchesBehavior() {
    withTransactionalRollback("my-feature-mode-switches-behavior") {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

        setBenefitValue(tireType.id, TenantBenefit.MY_FEATURE_MODE.featureKey, "manual")

        val actual = tenantBenefitService.getBenefitValue(tireType.id, TenantBenefit.MY_FEATURE_MODE.featureKey)
        assertEquals("manual", actual)
    }
}
```

强制约束（对应 CLAUDE.md 与 write-integration-test skill）：
1. featureKey 必须用 `TenantBenefit.XXX.featureKey` 常量，禁止硬编码。
2. 每个 `@Test` 必须由 `withTransactionalRollback("test-name") { ... }` 包裹。
3. 异常测试用 `runCatching { ... }` 再 `assertNotNull(ex)` + `assertTrue(ex is BusinessException)`。
4. mock 方法必须落到主体 Service 归属的 Test 类（`tireTypeServiceTest.mockTireType`、`tenantServiceTest.mockTenant/mockUser`），禁止跨职责在本 Test 类里写 `mockTenant()` / `mockUser()`。

### 前端

新增权益前端**只需要修改两个文件**（`tenant-benefit.tsx` 映射 + 两份 locales i18n）；不涉及组件、页面、api、types、columns。

#### 步骤 1：`web/src/i18n/tenant-benefit.tsx`

在 `useTenantBenefitKeyToTranslationMap` 的 `featureKeys: string[]` 数组末尾追加新 featureKey：

```typescript
const featureKeys: string[] = [
    "invitation.enabled",
    // ... 已有
    "my_group.enabled",         // ← 新增
    "my_group.max_count",       // ← 新增
    "my_group.mode",            // ← 新增
];
```

若引入了**新的 group**（首段之前不存在），必须在 `useTenantBenefitGroupToTranslationMap` 的 Map 里追加：

```typescript
const map = new Map<string, TenantBenefitGroupTranslation>([
    // ... 已有
    ["my_group", {label: t(`${I18N_NAMESPACE}.groups.my_group`), icon: <YourAntIcon/>}],
]);
```

`icon` 可选（从 `@ant-design/icons` 选一个语义贴切的图标 import 到文件顶部）；不选也允许，此时 group 只显示 label。

#### 步骤 2：i18n 文本（`zh-CN.ts` 与 `en-US.ts` 双文件必须同步）

节点：`pages.tenantTireBenefitValueManager`。

**keys 节点**（每个 featureKey 一段 `{ name, description }`）：

```typescript
// zh-CN.ts
'my_group.enabled': {
    name: '我的功能',
    description: '租户是否可以使用我的功能',
},
'my_group.max_count': {
    name: '我的功能数量上限',
    description: '租户可创建的功能实体数量上限',
},
'my_group.mode': {
    name: '我的功能模式',
    description: '我的功能的运行模式',
},

// en-US.ts
'my_group.enabled': {
    name: 'My Feature',
    description: 'Whether the tenant can use my feature',
},
'my_group.max_count': {
    name: 'My Feature Limit',
    description: 'Maximum number of my_feature entities per tenant',
},
'my_group.mode': {
    name: 'My Feature Mode',
    description: 'The operation mode for my_feature',
},
```

**groups 节点**（只有新引入 group 时才需要加）：

```typescript
// zh-CN.ts
groups: {
    // ... 已有
    'my_group': '我的功能',
},

// en-US.ts
groups: {
    // ... 已有
    'my_group': 'My Feature',
},
```

强制约束（对应 CLAUDE.md 的"文档跨语言同步"）：
1. `zh-CN.ts` 和 `en-US.ts` 条目数量必须一一对应，禁止只加一边。
2. 禁止在翻译中加入原声明未包含的额外细节。
3. 键名 `'my_group.enabled'` **必须**用引号包裹（含点号），保持与已有条目一致的对象字面量写法。

---

## 编译与验证

修改完成后按以下顺序验证（无 pre-commit hook，需手动跑）：

1. 后端编译：
   ```
   ./mvnw compile -pl crystal-tenant,crystal-starter -am -DskipTests
   ```
2. 前端类型检查：
   ```
   cd web && npx tsc --noEmit
   ```
3. 相关集成测试：
   ```
   ./mvnw test -pl crystal-starter -Dtest=MyManagerServiceImplTest
   ```
4. 运行一次应用，观察启动日志中的 `TenantBenefitTableDataCheckRunner` 输出：
   - `* my_group.enabled (name: My Feature)` 表示已成功写入表；
   - `√ my_group.enabled` 表示表中已有该 featureKey（非首次启动）。

**若步骤 1 / 2 报错，禁止提交**。

---

## 输出格式

完成后必须向用户汇报以下清单，缺一项视为未完成：

1. 新增的每个权益：`featureKey` / `featureType` / `defaultValue`。
2. 修改的后端文件（路径 + 修改要点）：
   - `TenantBenefit.kt`
   - 业务侧 Service（哪个 Service 的哪个方法、加了什么类型的校验）
   - 集成测试类（新增了哪几个 `@Test`）
3. 修改的前端文件（路径 + 修改要点）：
   - `web/src/i18n/tenant-benefit.tsx`（新增 key / 新增 group）
   - `web/src/i18n/locales/zh-CN.ts` + `en-US.ts`（新增的 keys / groups 节点）
4. 逐条对照本 Skill 的合规检查（每条给出 ✓ 或指出违反位置）：
   - featureKey 全局唯一（已 grep 检查）
   - `defaultValue` 格式匹配 `featureType`
   - 业务代码使用了 `TenantBenefit.XXX.featureKey` 常量，无字符串硬编码
   - `TenantBenefitService` 是唯一读取入口，未直接注入 Repository
   - 集成测试覆盖了"默认值行为"与"设值触发/生效"两类场景
   - 集成测试内 mock 方法归属正确（用 `getTestClassInstance` 拉主体 Service 的 Test 类）
   - 前端两份 locales 条目数量一一对应
   - `useTenantBenefitKeyToTranslationMap` 的 `featureKeys` 数组已追加
   - 若引入新 group，`useTenantBenefitGroupToTranslationMap` 的 Map 已追加

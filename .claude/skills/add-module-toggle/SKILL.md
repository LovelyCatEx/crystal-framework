# Skill: add-module-toggle

添加可以通过系统设置开关的功能模块，并在前端根据开关状态调整 UI

## 适用场景

- 需要为某个大功能模块添加全局开关（如禁用租户功能、禁用消息功能）
- 需要在前端根据模块状态隐藏相关 UI
- 需要在后端拦截对应功能的调用

## 关联 Skills

- `add-system-settings` - 必须先添加模块开关的系统设置
- `modify-system-integrated-info` - 必须通过 SystemIntegratedInfo 向前端暴露模块状态

---

## 强制步骤（必须执行）

### 第 0 步：确认拦截策略（必须，先思考后动手）

**必须回答的问题：**

**1. 是否需要 API 路径拦截？**
- ✅ **需要**：整个模块的所有 API 都要禁止访问 → 需要 `apiPathPatterns`
  - 示例：租户模块禁用时，所有 `/api/*/tenant/**` 和 `/api/*/manager/tenant/**` 都不可访问
- ❌ **不需要**：只在业务逻辑层判断或只在前端隐藏 → 不需要 `apiPathPatterns`
  - 示例：Message 模块已在 Service 层拦截，不需要 API 路径拦截

**2. 是否需要业务逻辑层拦截？**
- ✅ **需要**：某些 Service 方法需要根据模块状态拦截 → 在 Service 中读取 `SystemSettings.module` 判断
  - 示例：`MessageServiceImpl.checkMessageFeatureEnabled()` 在发送消息前检查模块是否启用
- ❌ **不需要**：只是隐藏前端入口 → 只做前端 UI 隐藏

**3. 是否需要前端菜单隐藏？**
- ✅ **需要**：模块有对应的侧边栏菜单项 → 需要在 `SYSTEM_MODULE_MENU_PATH_PREFIXES` 中配置
  - 示例：租户模块有 `/manager/tenant/` 等菜单项需要隐藏
- ❌ **不需要**：模块没有独立菜单（如嵌入在其他页面的功能） → 不需要菜单路径映射
  - 示例：Message 模块的"发起会话"按钮嵌入在消息中心，不是独立菜单

**示例对比：**

| 模块 | API 路径拦截 | 业务逻辑层拦截 | 前端菜单隐藏 |
|------|------------|--------------|------------|
| Tenant | ✅ 需要 | ✅ 需要 | ✅ 需要 |
| Message | ❌ 不需要 | ✅ 需要 | ❌ 不需要 |

---

### 第 1 步：添加系统设置项（必须）

**关联 Skill：** `add-system-settings`

**必须执行：** 按照 `add-system-settings` skill 的**完整流程**添加模块开关

**必须添加的位置：** `SystemSettings.Module` 数据类

**文件：** `crystal-shared-types/src/main/kotlin/.../shared/types/system/SystemSettings.kt`

**示例：**
```kotlin
data class SystemSettings(
    val basic: Basic,
    val bootstrap: Bootstrap,
    val mail: Mail,
    val messageChannel: MessageChannel,
    val security: Security,
    val oauth: OAuth,
    val resource: Resource,
    val module: Module,  // ← Module 数据类
) {
    // ... 其他数据类
    
    data class Module(
        val tenantEnabled: Boolean,
        val approvalEnabled: Boolean,
        val yourModuleEnabled: Boolean,  // ← 新增模块开关
    )
}
```

**必须完成的子步骤（参考 add-system-settings）：**
1. 在 `SystemSettingsConstants.Module` 中声明 `SettingsItemDeclaration`
2. 在 `SystemSettingsServiceImpl.getSystemModuleSettings()` 中实现读取
3. 在 `SystemSettingsServiceImpl.updateSystemSettings()` 中实现写入
4. 在 `SystemSettingsBuiltinConfigurer` 中注册声明
5. 前端在 `zh-CN.ts` 和 `en-US.ts` 的 `pages.systemSettingsManager.keys` 下添加翻译
6. 前端在 `web/src/i18n/system-settings.tsx` 的 key 映射 Map 中加上对应条目

---

### 第 2 步：后端定义模块 KEY（必须）

**文件：** `crystal-shared/src/main/kotlin/.../shared/constants/SystemModulePathConstants.kt`

**必须操作：** 添加新的模块对象定义

**模板：**
```kotlin
/**
 * Single source of truth for toggleable system module identifiers and the backend URL patterns
 * they own. Consumed by:
 *   - SystemModuleGuardFilter (crystal-system) — blocks backend APIs when a module is disabled.
 *   - SystemIntegratedInfoController (crystal-system) — reports the disabled module keys back
 *     to the frontend so the UI can hide menus locally.
 */
object SystemModulePathConstants {
    // ... 其他模块
    
    object YourModule {
        const val KEY = "yourModule"  // ← 前后端必须一致
        
        // ⚠️ 只有第 0 步确认需要 API 路径拦截时才添加这部分
        val apiPathPatterns = listOf(
            "/api/*/your-module/**",
            "/api/*/manager/your-module/**",
        )
    }
}
```

**关键注意：**
- `KEY` 是**必须的**，用于前后端识别模块
- `apiPathPatterns` **只在需要 API 路径拦截时才添加**
- 如果只做业务逻辑层或 UI 层控制，**只需要 `KEY`，不需要 `apiPathPatterns`**

**示例对比：**

```kotlin
// ✅ 需要 API 路径拦截（Tenant 模块）
object Tenant {
    const val KEY = "tenant"
    val apiPathPatterns = listOf(
        "/api/*/tenant/**",
        "/api/*/manager/tenant/**",
    )
}

// ✅ 不需要 API 路径拦截（Message 模块）
object MessageSystemPeer {
    const val KEY = "message.systemPeer"
    // 不需要 apiPathPatterns，Service 层已拦截
}
```

---

### 第 3 步：后端在 SystemIntegratedInfo 中暴露模块状态（必须）

**文件：** `crystal-system/src/main/kotlin/.../controller/SystemIntegratedInfoController.kt`

**必须操作：** 在 `disabledModules` 构建逻辑中添加判断

**示例：**
```kotlin
@Unauthorized
@GetMapping("/integratedInfo")
suspend fun getSystemIntegratedInfo(): ApiResponse<*> {
    val systemSettings = systemSettingsService.getSystemSettings()
    val oauthSettings = systemSettings.oauth
    val enabledPlatforms = buildList {
        if (oauthSettings.github.enabled) add(OAuthPlatform.GITHUB.typeId)
        if (oauthSettings.google.enabled) add(OAuthPlatform.GOOGLE.typeId)
        if (oauthSettings.oicq.enabled) add(OAuthPlatform.OICQ.typeId)
    }
    val disabledModules = buildList {
        if (!systemSettings.module.tenantEnabled) add(SystemModulePathConstants.Tenant.KEY)
        if (!systemSettings.module.approvalEnabled) add(SystemModulePathConstants.Approval.KEY)
        if (!systemSettings.module.yourModuleEnabled) add(SystemModulePathConstants.YourModule.KEY)  // ← 新增
    }
    return ApiResponse.success(
        SystemIntegratedInfoVO(
            maintenance = readinessController.getSystemMaintenance().data!!,
            waterMark = SystemIntegratedInfoVO.WaterMark(
                enabled = systemSettings.basic.waterMark.enabled,
                type = systemSettings.basic.waterMark.type,
                customValue = systemSettings.basic.waterMark.customValue,
                fontColor = systemSettings.basic.waterMark.fontColor
            ),
            enabledOAuthPlatforms = enabledPlatforms,
            disabledModules = disabledModules,
        )
    )
}
```

---

### 第 4 步：前端添加模块 KEY（必须）

**文件：** `web/src/router/system-module-menu-paths.ts`

**必须操作：** 在 `SystemModuleKey` 中添加常量

**示例：**
```typescript
export const SystemModuleKey = {
    TENANT: 'tenant',
    APPROVAL: 'approval',
    YOUR_MODULE: 'yourModule',  // ← 新增，值必须与后端 KEY 完全一致
} as const;

export type SystemModuleKeyValue = (typeof SystemModuleKey)[keyof typeof SystemModuleKey];
```

**⚠️ 只有第 0 步确认需要菜单隐藏时才执行以下操作：**

```typescript
export const SYSTEM_MODULE_MENU_PATH_PREFIXES: Record<string, string[]> = {
    [SystemModuleKey.TENANT]: [
        '/manager/tenant/',
        '/manager/tenants',
        '/manager/tenant-',
    ],
    [SystemModuleKey.APPROVAL]: [
        '/manager/approval/',
        '/manager/approval-flow-',
        '/manager/tenant/approval-flow-',
        '/manager/tenant-approval-flow-',
    ],
    [SystemModuleKey.YOUR_MODULE]: [  // ← 新增菜单路径映射
        '/manager/your-module/',
    ],
};
```

**关键注意：**
- `SystemModuleKey` 的值必须与后端 `KEY` **完全一致**
- `SYSTEM_MODULE_MENU_PATH_PREFIXES` **只在需要菜单隐藏时才添加**
- 路径前缀支持通配符，会被 `resolveDisabledMenuPrefixes()` 用于过滤菜单

---

### 第 5 步：前端组件中根据模块状态调整 UI（必须）

**必须操作：**

**1. 在需要条件渲染的组件中引入依赖**
```typescript
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {SystemModuleKey} from "@/router/system-module-menu-paths.ts";
```

**2. 获取模块状态**
```typescript
export function YourComponent() {
    const {isModuleEnabled} = useSystemIntegrated();
    const yourModuleEnabled = isModuleEnabled(SystemModuleKey.YOUR_MODULE);
    
    // ...
}
```

**3. 条件渲染 UI**
```typescript
// 简单的条件渲染
{yourModuleEnabled && (
    <Button onClick={...}>Your Feature</Button>
)}

// 带禁用提示的条件渲染
{yourModuleEnabled ? (
    <YourComponent />
) : (
    <div style={{padding: 16, textAlign: 'center', color: '#999'}}>
        {t('yourFeature.disabled')}
    </div>
)}

// 动态构建列表（如 Tabs）
const tabs = [
    yourModuleEnabled && {
        key: 'yourFeature',
        label: t('yourFeature.title'),
        children: <YourFeaturePanel />,
    },
    otherFeatureEnabled && {
        key: 'other',
        label: t('other.title'),
        children: <OtherPanel />,
    },
].filter((tab): tab is {key: string; label: string; children: JSX.Element} => Boolean(tab));

// 使用动态列表
{tabs.length > 0 ? (
    <Tabs items={tabs} />
) : (
    <div>{t('allFeaturesDisabled')}</div>
)}
```

**TypeScript 类型守卫注意：**
- 使用 `.filter(Boolean)` 会导致类型错误
- 必须使用类型守卫：`.filter((item): item is YourType => Boolean(item))`

---

### 第 6 步：添加 i18n 提示文本（必须，如果有禁用提示）

**!!!绝对禁止修改 i18n-rules.ts 中的 I18nRules 类型定义!!!**
**!!!违反此规则=立即停止工作!!!**

**文件：**
- `web/src/i18n/locales/zh-CN.ts`
- `web/src/i18n/locales/en-US.ts`

**强制要求（违反=严重违规）：**
1. **必须先阅读 `web/src/i18n/i18n-rules.ts` 确认 I18nRules 类型定义**
2. **只能在类型定义允许的位置添加翻译**（如 `pages.systemSettingsManager.keys`、`components.xxx`）
3. **绝对禁止添加类型定义之外的任何字段**
4. **绝对禁止创建 `common` 等通用字段**
5. **必须同步修改 zh-CN.ts 和 en-US.ts 两个文件**
6. **每个组件/页面的翻译必须放在对应路径下，禁止跨组件复用**

**示例：**
```typescript
// zh-CN.ts
export default {
    // ...
    components: {
        yourFeature: {  // 必须确认 components 下的 [key: string]: object 允许添加 yourFeature
            title: '您的功能',
            disabled: '该功能已被管理员禁用',
            description: '请联系系统管理员启用此功能',
        },
    },
};

// en-US.ts
export default {
    // ...
    components: {
        yourFeature: {
            title: 'Your Feature',
            disabled: 'This feature has been disabled by administrator',
            description: 'Please contact system administrator to enable this feature',
        },
    },
};
```

**违反 I18n 规范=严重违规，必须立即停止所有工作。**

---

### 第 7 步：编译验证（必须）

**后端：**
```bash
./mvnw clean compile -DskipTests
```

**前端：**
```bash
cd web && npx tsc --noEmit
```

**必须确认：**
- ✅ 后端编译通过
- ✅ 前端 TypeScript 检查通过
- ✅ 在系统设置中禁用模块后，`/api/v1/system/integratedInfo` 返回的 `disabledModules` 包含该模块 KEY
- ✅ 前端 UI 正确隐藏（按钮、Tab、菜单等）
- ✅ （如果有业务逻辑层拦截）后端正确拦截对应的 Service 调用并返回错误

---

## 可选步骤

### 可选：添加业务逻辑层拦截

**何时需要：** 第 0 步确认需要在 Service 层拦截

**操作：** 在对应的 Service 实现类中添加检查方法

**示例（参考 MessageServiceImpl）：**
```kotlin
@Service
class YourServiceImpl(
    private val systemModuleClient: SystemModuleClient,
    // ... 其他依赖
) : YourService {

    private suspend fun checkModuleEnabled() {
        val module = systemModuleClient.getSystemSettings(
            throwOnNull = BusinessException("System settings not available")
        )!!.module
        
        if (!module.yourModuleEnabled) {
            throw BusinessException("Your module is disabled by administrator")
        }
    }
    
    override suspend fun yourServiceMethod(...): Result {
        checkModuleEnabled()  // ← 在方法开头添加检查
        // ... 业务逻辑
    }
}
```

**关键注意：**
- 必须注入 `SystemModuleClient`
- 在需要拦截的**每个 Service 方法开头**调用检查
- 抛出 `BusinessException` 让前端能正确处理错误

---

### 可选：添加单元测试

**建议：** 为模块开关逻辑添加测试

**测试点：**
- 模块禁用时，前端 `isModuleEnabled()` 返回 `false`
- 模块禁用时，后端拦截逻辑正确抛出异常
- 模块禁用时，前端 UI 正确隐藏
- 模块禁用时，对应的菜单项不出现在侧边栏

---

## 关键原则

1. **先调查后动手** - 必须先读取相关文件理解现有实现
2. **明确拦截层级** - API 层 / 业务逻辑层 / UI 层各有用途，不要重复
3. **前后端 KEY 必须一致** - `SystemModulePathConstants.KEY` = `SystemModuleKey.YOUR_MODULE` 的值
4. **按需添加 apiPathPatterns** - 不是所有模块都需要 API 路径拦截
5. **按需添加菜单路径映射** - 不是所有模块都需要菜单隐藏
6. **前端优先 UI 隐藏** - 避免用户看到无权限错误
7. **后端必须有兜底** - 防止绕过前端直接调用 API

---

## 完整示例：Message 模块

### 第 0 步分析

- API 路径拦截：❌ 不需要（Service 层已拦截）
- 业务逻辑层拦截：✅ 需要（`MessageServiceImpl.checkMessageFeatureEnabled`）
- 前端菜单隐藏：❌ 不需要（嵌入在消息中心内部）

### 后端系统设置

```kotlin
// SystemSettings.kt
data class Module(
    val tenantEnabled: Boolean,
    val approvalEnabled: Boolean,
    val messageSystemPeerEnabled: Boolean,
    val messageTenantScopeEnabled: Boolean,
    val messageTenantDeskEnabled: Boolean,
)
```

### 后端模块 KEY（无 apiPathPatterns）

```kotlin
// SystemModulePathConstants.kt
object MessageSystemPeer {
    const val KEY = "message.systemPeer"
    // 不需要 apiPathPatterns
}

object MessageTenantScope {
    const val KEY = "message.tenantScope"
}

object MessageTenantDesk {
    const val KEY = "message.tenantDesk"
}
```

### 后端 SystemIntegratedInfo

```kotlin
// SystemIntegratedInfoController.kt
val disabledModules = buildList {
    if (!systemSettings.module.tenantEnabled) add(SystemModulePathConstants.Tenant.KEY)
    if (!systemSettings.module.approvalEnabled) add(SystemModulePathConstants.Approval.KEY)
    if (!systemSettings.module.messageSystemPeerEnabled) add(SystemModulePathConstants.MessageSystemPeer.KEY)
    if (!systemSettings.module.messageTenantScopeEnabled) add(SystemModulePathConstants.MessageTenantScope.KEY)
    if (!systemSettings.module.messageTenantDeskEnabled) add(SystemModulePathConstants.MessageTenantDesk.KEY)
}
```

### 后端 Service 层拦截

```kotlin
// MessageServiceImpl.kt
private suspend fun checkMessageFeatureEnabled(scope: Scope, target: Party) {
    val module = systemModuleClient.getSystemSettings(
        throwOnNull = BusinessException("System settings not available")
    )!!.module
    when (scope.type) {
        ScopeType.SYSTEM if target.type == PartyType.USER && !module.messageSystemPeerEnabled ->
            throw BusinessException("System peer messaging is disabled by administrator")

        ScopeType.TENANT if target.type == PartyType.USER && !module.messageTenantScopeEnabled ->
            throw BusinessException("Tenant-scope messaging is disabled by administrator")

        ScopeType.TENANT if target.type == PartyType.TENANT && !module.messageTenantDeskEnabled ->
            throw BusinessException("Tenant service desk is disabled by administrator")

        else -> {}
    }
}

override suspend fun send(...): MsgMessageEntity {
    checkMessageFeatureEnabled(scope, target)  // ← 拦截
    // ... 业务逻辑
}
```

### 前端模块 KEY（无菜单路径映射）

```typescript
// system-module-menu-paths.ts
export const SystemModuleKey = {
    TENANT: 'tenant',
    APPROVAL: 'approval',
    MESSAGE_SYSTEM_PEER: 'message.systemPeer',
    MESSAGE_TENANT_SCOPE: 'message.tenantScope',
    MESSAGE_TENANT_DESK: 'message.tenantDesk',
} as const;

// 不需要在 SYSTEM_MODULE_MENU_PATH_PREFIXES 中添加
```

### 前端组件 UI 隐藏

```typescript
// StartConversationModal.tsx
export function StartConversationModal(props: {...}) {
    const {isModuleEnabled} = useSystemIntegrated();
    const systemPeerEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_SYSTEM_PEER);
    const tenantScopeEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_SCOPE);
    const tenantDeskEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_DESK);

    // 动态构建 Tabs
    const systemTabs = [
        systemPeerEnabled && {
            key: 'user',
            label: t('components.notification.startConversation.userTab'),
            children: <ContactUserPanel onSelect={onUserSelect}/>,
        },
        tenantDeskEnabled && {
            key: 'tenant',
            label: t('components.notification.startConversation.tenantTab'),
            children: <ContactTenantPanel onSelect={onTenantSelect}/>,
        },
    ].filter((tab): tab is {key: string; label: string; children: JSX.Element} => Boolean(tab));

    return (
        <Modal open={open} onCancel={onClose} footer={null} title={...}>
            {isOrg ? (
                tenantScopeEnabled ? (
                    <TenantMateSelectorPanel {...} />
                ) : (
                    <div style={{padding: 16, textAlign: 'center', color: '#999'}}>
                        {t('components.notification.startConversation.tenantScopeDisabled')}
                    </div>
                )
            ) : (
                systemTabs.length > 0 ? (
                    <Tabs items={systemTabs} />
                ) : (
                    <div style={{padding: 16, textAlign: 'center', color: '#999'}}>
                        {t('components.notification.startConversation.allFeaturesDisabled')}
                    </div>
                )
            )}
        </Modal>
    );
}
```

```typescript
// NotificationCenter.tsx
export function NotificationCenter(props: {...}) {
    const {isModuleEnabled} = useSystemIntegrated();
    const systemPeerEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_SYSTEM_PEER);
    const tenantScopeEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_SCOPE);
    const tenantDeskEnabled = isModuleEnabled(SystemModuleKey.MESSAGE_TENANT_DESK);

    // 所有功能都禁用时隐藏"发起会话"按钮
    const canStartConversation = systemPeerEnabled || tenantScopeEnabled || tenantDeskEnabled;
    // 租户功能禁用时隐藏 Segment 切换器
    const showIdentitySwitcher = currentTenant != null && tenantScopeEnabled;

    return (
        <div>
            <div>
                <Text strong>{t('components.notification.conversations.title')}</Text>
                {canStartConversation && (
                    <Button icon={<PlusOutlined/>} onClick={() => setStartConversationOpen(true)}>
                        {t('components.notification.startConversation.start')}
                    </Button>
                )}
            </div>
            {showIdentitySwitcher && (
                <Segmented
                    value={identityTab}
                    onChange={(v) => setIdentityTab(v as IdentityTab)}
                    options={[
                        {label: t('components.notification.conversations.systemTab'), value: 'system'},
                        {label: currentTenant.tenantName, value: 'tenant'},
                    ]}
                />
            )}
            {/* ... */}
        </div>
    );
}
```

### 前端 i18n

```typescript
// zh-CN.ts & en-US.ts
startConversation: {
    start: 'Start conversation',
    title: 'Start conversation',
    userTab: 'User',
    tenantTab: 'Tenant',
    tenantMemberTab: 'Organization Members',
    tenantScopeDisabled: 'Tenant conversation feature has been disabled by administrator',
    allFeaturesDisabled: 'Conversation feature has been disabled by administrator',
},
```

---
name: add-system-settings
description: 为系统添加设置项，当用户提到添加某项系统配置时使用。
---

# 添加系统设置项

## 触发条件
当用户明确表明需要添加某项系统配置，例如 `security.encrypt.api.xxx` 时，请参考如下步骤实现。

> 添加**租户设置项**请改用 `add-tenant-settings` skill，两者所在模块、包路径、前端文件均不同，禁止混用。

## 输入格式
用户需要提供配置项的具体 key、值类型、默认值，例如 数字 `myPlugin.timeout` 默认值 `30` 或 字符串 `mail.smtp.fromEmail` 默认值 `example@crystalframework.com`。

## 前提信息

### SettingsItemDeclaration

声明类型为 `SettingsItemDeclaration`（位于 `crystal-sdk` 的 `com.lovelycatv.crystalframework.sdk.common.settings.types` 包，系统设置与租户设置共用同一套声明结构）。

参数说明：
1. `key`: 设置项 path，点分命名法 `tab.group.settingName`。第一段是 tab，第一段+中间段是 group，完整 key 是具体设置项。
2. `valueType`: 值类型 `SettingsItemValueType`，见下表
3. `defaultValue`: 默认值（`String?`，必须是字符串）
4. `enumValues`: 当值类型为 `ENUM_*` 时必填，否则不填
5. `sort`: 前端展示顺序，数字越小越靠前

### 支持的值类型 SettingsItemValueType

| 类型 | 前端组件 | 说明 |
|------|---------|------|
| `STRING` | Input | 文本输入 |
| `NUMBER` | InputNumber | 整数值（Long） |
| `DECIMAL` | InputNumber | 小数值（Double） |
| `BOOLEAN` | Switch | 开关 |
| `ENUM_SINGLE` | Radio.Group | 单选枚举，直接存字符串 |
| `ENUM_MULTIPLE` | Checkbox.Group | 多选枚举，存 JSONArray 字符串如 `["a","b","c"]` |
| `STRING_ARRAY` | Select (tags) | 字符串数组，JSON 字符串数组传输 |
| `NUMBER_ARRAY` | Select (tags) | 整数数组，JSON 字符串数组传输 |
| `DECIMAL_ARRAY` | Select (tags) | 小数数组，JSON 字符串数组传输 |
| `BOOLEAN_ARRAY` | Select (tags) | 布尔值数组，JSON 字符串数组传输 |

补充：`ENUM_*` 必须填 `enumValues`；数组类型（`*_ARRAY`）以 JSON 数组字符串存储/传输，无需 `enumValues`。

### 内置声明文件

声明定义在 `crystal-system` 模块的 `system/types/SystemSettingsConstants.kt`（包 `com.lovelycatv.crystalframework.system.types`）的 nested object 中，按 tab 分类嵌套。新声明加在对应分类下。

### 注册文件

`crystal-system` 模块的 `system/config/SystemSettingsBuiltinConfigurer.kt` 通过 `registry.settings(listOf(...))` 手动注册所有内置设置项。新增的声明必须添加到这个列表里。

### 前端 i18n

i18n 翻译位于 `web/src/i18n/locales/zh-CN.ts` 和 `en-US.ts`，格式严格，禁止漏写：
1. key: `pages.systemSettingsManager.keys.${keyPath}`，例如 `pages.systemSettingsManager.keys.mail.smtp.host`
2. group: `pages.systemSettingsManager.groups.${groupName}`，例如 `pages.systemSettingsManager.groups.mail.smtp`
3. tab: `pages.systemSettingsManager.tabs.${tabName}`，例如 `pages.systemSettingsManager.tabs.mail`
4. ENUM 枚举值: `pages.systemSettingsManager.enums.${key}.${enumName}`

### 前端翻译映射文件

`web/src/i18n/system-settings.tsx` 维护 key / group / tab 的翻译映射 Map，新增设置项必须在对应的 `useSettingsKeyToTranslationMap` / `useSettingsGroupToTranslationMap` / `useSettingsTabToTranslationMap` 中补齐条目，否则前端页面无法展示正确文本。

## 执行步骤

### 后端
1. 阅读 `SystemSettingsConstants.kt`，判断新增 key 是否重复。如果重复则立即停止并通知用户。
2. 如果新增 key 属于已有分类，在对应 nested object 内添加 `val` 声明。如果需要新的 tab/group 分类，按嵌套 object 规则创建。
3. 阅读 `SystemSettingsBuiltinConfigurer.kt`，将上一步的声明添加到 `registry.settings()` 列表中。
4. 更新 `SystemSettings` 数据类（`crystal-shared-types` 模块的 `com.lovelycatv.crystalframework.shared.types.system.SystemSettings`）：
   - 如果属于已有 tab/group，在对应嵌套 data class 中添加新字段。
   - 如果需要新的 tab/group，新增对应嵌套 data class 并在顶层 `SystemSettings` 构造参数中引用。
   - **禁止给新增字段添加默认值（如 `= ""`, `= false`）。** 数据类必须严格反映实际结构，反序列化兼容性问题由缓存失效机制处理，不允许用默认值掩盖 schema 不一致。
5. 更新 `SystemSettingsService` 接口（`crystal-system` 模块）：
   - 如果新增了 tab 级别的分类，添加对应的 `suspend fun getSystemXxxSettings(): SystemSettings.Xxx` 方法。
6. 更新 `SystemSettingsServiceImpl`（`crystal-system` 模块）：
   - 在对应的 `getSystemXxxSettings()` 方法中添加新字段的读取。
   - 如果新增了 tab，实现新的 `getSystemXxxSettings()` 方法，并在 `getSystemSettings()` 聚合方法中引用。
   - 在 `updateSystemSettings(settings: SystemSettings)` 中添加新字段的写入。

**禁止在业务代码中逐条调用 `getSettings(declaration)` 读取多个设置项。必须通过 `getSystemXxxSettings()` 一次性获取已缓存的 `SystemSettings` 对象，再从中取值。**

### 前端
1. 在 `zh-CN.ts` 和 `en-US.ts` 的 `pages.systemSettingsManager.keys` 下添加 key 翻译。
2. 如果是新 group，在 `pages.systemSettingsManager.groups` 下添加 group 翻译。
3. 如果是新 tab，在 `pages.systemSettingsManager.tabs` 下添加 tab 翻译。
4. 如果是 ENUM 类型，在 `pages.systemSettingsManager.enums` 下添加枚举值翻译。
5. 在 `web/src/i18n/system-settings.tsx` 的 key / group / tab 映射 Map 中加上对应条目（group 可附 icon），否则前端页面无法展示正确的值。

## 输出格式
1. 列出新增的配置项：tab（是否存在）、group（是否存在）、key、值类型、默认值。
2. 指出前端添加的 i18n 文本（中文和英文）以及 `system-settings.tsx` 的映射改动。

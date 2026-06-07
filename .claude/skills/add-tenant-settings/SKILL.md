---
name: add-tenant-settings
description: 为租户添加设置项，当用户提到添加某项租户/组织级配置时使用。
---

# 添加租户设置项

## 触发条件
当用户明确表明需要添加某项租户（组织）级配置，例如 `notification.memberJoin.email` 时，请参考如下步骤实现。

> 添加**系统设置项**请改用 `add-system-settings` skill。两者共用同一套声明结构（`SettingsItemDeclaration` / `SettingsItemValueType`），但分别由 `TenantSettingsRegistry` 和 `SystemSettingsRegistry` 管理，所在模块、包路径、前端文件均不同，互不干扰，禁止混用。

## 输入格式
用户需要提供配置项的具体 key、值类型、默认值，例如 布尔 `notification.dailyDigest.enabled` 默认值 `true` 或 数字 `notification.digest.intervalHours` 默认值 `24`。

## 前提信息

### SettingsItemDeclaration

声明类型为 `SettingsItemDeclaration`（位于 `crystal-sdk` 的 `com.lovelycatv.crystalframework.sdk.common.settings.types` 包，与系统设置共用）。

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

声明定义在 `crystal-tenant` 模块的 `tenant/settings/constants/TenantSettingsConstants.kt`（包 `com.lovelycatv.crystalframework.tenant.settings.constants`）的 nested object 中，按分类嵌套。新声明加在对应分类下。

### 注册文件

`crystal-tenant` 模块的 `tenant/settings/config/TenantSettingsBuiltinConfigurer.kt` 通过 `registry.settings(listOf(...))` 手动注册所有内置租户设置项。新增的声明必须添加到这个列表里。

### Key 命名规则与布局差异

key 命名规则（`tab.group.settingName`）与系统设置一致。但**租户设置目前仅支持单标签页布局**，Tab 主要用于日志/调试用途，前端最终以 Group 为单位展示。

### 前端 i18n

i18n 翻译位于 `web/src/i18n/locales/zh-CN.ts` 和 `en-US.ts`，节点为 `pages.tenantSettingsManager`（注意与系统设置的 `systemSettingsManager` 区分）：
1. key: `pages.tenantSettingsManager.keys.${keyPath}`
2. group: `pages.tenantSettingsManager.groups.${groupName}`
3. tab: `pages.tenantSettingsManager.tabs.${tabName}`
4. ENUM 枚举值: `pages.tenantSettingsManager.enums.${key}.${enumName}`

### 前端翻译映射文件

`web/src/i18n/tenant-settings.tsx`（注意：是 `tenant-settings.tsx`，不是 `system-settings.tsx`）维护 key / group / tab 的翻译映射 Map，新增设置项必须在对应的 `useTenantSettingsKeyToTranslationMap` / `useTenantSettingsGroupToTranslationMap` / `useTenantSettingsTabToTranslationMap` 中补齐条目，否则前端页面无法展示正确文本。

## 执行步骤

### 后端
1. 阅读 `TenantSettingsConstants.kt`，判断新增 key 是否重复。如果重复则立即停止并通知用户。
2. 如果新增 key 属于已有分类，在对应 nested object 内添加 `val` 声明。如果需要新的 group/tab 分类，按嵌套 object 规则创建。
3. 阅读 `TenantSettingsBuiltinConfigurer.kt`，将上一步的声明添加到 `registry.settings()` 列表中。

### 前端
1. 在 `zh-CN.ts` 和 `en-US.ts` 的 `pages.tenantSettingsManager.keys` 下添加 key 翻译。
2. 如果是新 group，在 `pages.tenantSettingsManager.groups` 下添加 group 翻译。
3. 如果是新 tab，在 `pages.tenantSettingsManager.tabs` 下添加 tab 翻译。
4. 如果是 ENUM 类型，在 `pages.tenantSettingsManager.enums` 下添加枚举值翻译。
5. 在 `web/src/i18n/tenant-settings.tsx` 的 key / group / tab 映射 Map 中加上对应条目（group 可附 icon），否则前端页面无法展示正确的值。

## 输出格式
1. 列出新增的配置项：tab（是否存在）、group（是否存在）、key、值类型、默认值。
2. 指出前端添加的 i18n 文本（中文和英文）以及 `tenant-settings.tsx` 的映射改动。

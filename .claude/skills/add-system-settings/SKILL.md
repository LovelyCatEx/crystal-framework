---
name: add-system-settings
description: 为系统添加设置项，当用户提到添加某项系统配置时使用。
---

# 添加系统设置项

## 触发条件
当用户明确表明需要添加某项配置，例如 `security.encrypt.api.xxx` 时，请参考如下步骤实现。

## 输入格式
用户需要提供配置项的具体 key、值类型、默认值，例如 数字 `myPlugin.timeout` 默认值 `30` 或 字符串 `mail.smtp.fromEmail` 默认值 `example@crystalframework.com`。

## 前提信息

### SystemSettingsItemDeclaration

参数说明：
1. `key`: 设置项 path，点分命名法 `tab.group.settingName`。第一段是 tab，第一段+中间段是 group，完整 key 是具体设置项。
2. `valueType`: 值类型 — `STRING`、`NUMBER`、`DECIMAL`、`BOOLEAN`、`ENUM_SINGLE`、`ENUM_MULTIPLE`
3. `defaultValue`: 默认值（必须是字符串）
4. `enumValues`: 当值类型为 `ENUM_*` 时必填，否则不填
5. `sort`: 前端展示顺序，数字越小越靠前

补充：ENUM_SINGLE 直接存字符串，ENUM_MULTIPLE 存 JSONArray 字符串如 `["a","b","c"]`。

### 内置声明文件

声明定义在 `SystemSettingsConstants.kt` 的 nested object 中，按 tab 分类嵌套。新声明加在对应分类下。

### 注册文件

`SystemSettingsBuiltinConfigurer.kt` 通过 `registry.settings(listOf(...))` 手动注册所有内置设置项。新增的声明必须添加到这个列表里。

### 前端 i18n

i18n 文件位于 `web/src/i18n/locales/zh-CN.ts` 和 `en-US.ts`，格式严格，禁止漏写：
1. key: `pages.systemSettingsManager.keys.${keyPath}`，例如 `pages.systemSettingsManager.keys.mail.smtp.host`
2. group: `pages.systemSettingsManager.groups.${groupName}`，例如 `pages.systemSettingsManager.groups.mail.smtp`
3. tab: `pages.systemSettingsManager.tabs.${tabName}`，例如 `pages.systemSettingsManager.tabs.mail`
4. ENUM 枚举值: `pages.systemSettingsManager.enums.${key}.${enumName}`

## 执行步骤

### 后端
1. 阅读 `SystemSettingsConstants.kt`，判断新增 key 是否重复。如果重复则立即停止并通知用户。
2. 如果新增 key 属于已有分类，在对应 nested object 内添加 `val` 声明。如果需要新的 tab/group 分类，按嵌套 object 规则创建。
3. 阅读 `SystemSettingsBuiltinConfigurer.kt`，将上一步的声明添加到 `registry.settings()` 列表中。

### 前端
1. 在 `zh-CN.ts` 和 `en-US.ts` 的 `pages.systemSettingsManager.keys` 下添加 key 翻译。
2. 如果是新 group，在 `pages.systemSettingsManager.groups` 下添加 group 翻译。
3. 如果是新 tab，在 `pages.systemSettingsManager.tabs` 下添加 tab 翻译。
4. 如果是 ENUM 类型，在 `pages.systemSettingsManager.enums` 下添加枚举值翻译。
5. 必须在 i18n/system-settings.tsx 内加上对应的 i18n key 否则前端页面无法展示正确的值。

## 输出格式
1. 列出新增的配置项：tab（是否存在）、group（是否存在）、key、值类型、默认值。
2. 指出前端添加的 i18n 文本（中文和英文）。

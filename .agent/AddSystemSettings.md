---
name: add-system-settings
description: 为系统添加设置项，当用户提到添加某项系统配置时使用。
---

# 添加系统设置项

## 触发条件
当用户明确表明需要添加某项配置，例如 'security.encrypt.api.xxx' 时，请参考如下步骤实现。

## 输入格式
用户需要提供配置项的具体设置项 path、参数类型、默认值，例如 数字 'security.encrypt.api.xxx' 默认值 0 或 字符串 'mail.smtp.fromEmail' 默认值 'example@crystalframework.com'。

## 前提信息

### SystemSettingsItemDeclaration

关于 SystemSettingsItemDeclaration 的参数如下：
1. key: 设置项 path，第一项被认为是 tab、除最后一项外的全体被认为是 group。以 'mail.smtp.host' 为例，其中 'mail' 是一个 tab 类型，而 'mail.smtp' 是 group 类型。
2. valueType: 值类型
3. defaultValue: 默认值（必须是字符串）
4. enumValues: 当值类型为 ENUM_* 类型时，此项为必填项，具体值列表必须符合用户的描述，如果不是，此项默认为 null，可不填写。 
5. sort: 设置项在前端展示的顺序，数字越小越靠前

补充：当值类型为枚举（ENUM_*）时，如果是单值枚举则直接将值保存为字符串，若是多值枚举则必须保存为 JSONArray 字符串，例如 ["a", "b", "c"]。

### 前端 i18n 定义

i18n 文件位于 web/i18n/locales 中，格式严格，禁止出现漏写的情况，包括但不限于：
1. 不按原有规则定义 i18n 文本项。
2. 仅在部分语言的 i18n 有定义，遗漏其他语言文件。
3. 不按下列规则定义 i18n 文本项。

对于本 skill 需要的添加设置项相关 i18n 的描述如下：
1. key 必须定义为 'pages.systemSettingsManager.keys.${keyPath}'，例如 key 'mail.smtp.host' 对应 i18n 中的 'pages.systemSettingsManager.keys.mail.smtp.host'。
2. group 必须定义为 'pages.systemSettingsManager.groups.${groupName}'，例如 group 'mail.smtp' 对应 i18n 中的 'pages.systemSettingsManager.groups.mail.smtp'。
3. tab 必须定义为 'pages.systemSettingsManager.tabs.${tabName}'，例如 tab 'mail' 对应 i18n 中的 'pages.systemSettingsManager.tabs.mail'。
4. 如果值类型是 ENUM_*，必须与后端定义的在 i18n 中对应 'pages.systemSettingsManager.enums.${key}.${enumName}'。

## 执行步骤

### 后端
1. 首先阅读 SystemSettingsConstants 内的配置定义，判断是否出现重复 key。
2. 如果出现重复的 key 定义，立刻停止操作，然后通知用户，禁止覆盖已有的定义。
3. 对于配置项的 path 必须具象化为 object 类，例如 'mail.smtp.host' 对应 'SystemSettingsConstants$Mail$SMTP$HOST'，最后一项必须以全大写字母+驼峰转下划线分割的形式体现。
4. 按照第二步的规则添加 SystemSettingsItemDeclaration。
5. 阅读 SystemSettings 内的定义，应当与 SystemSettingsConstants 的结构保持一致，参数类型必须具体化，然后添加用户要求的配置。
6. 阅读 SystemSettingsServiceImpl 的内容，了解 updateSystemSettings(settings: SystemSettings) 函数。
7. 在 updateSystemSettings 内添加 setSettings 逻辑，以确保设置项可以被正常更新，保证值是字符串形式（必须满足上述前提中的描述）。
8. 根据第二步和第四步的规则，你可以判断出是否需要新增一种配置类型。修改 getSystemSettings 函数，如果有新增的顶级类型（设置项 path 的第一项，例如 'mail.smtp.host' 中的 'mail' 是顶级类型，而其他两项不是），必须写 getSystem[Name]Settings() 函数，再应用到 getSystemSettings() 中。

### 前端
1. 首先阅读 system-settings.tsx 文件，了解设置项的 i18n 定义。
2. 在 useSettingsTabToTranslationMap 添加 tab 定义。
3. 在 useSettingsGroupToTranslationMap 添加 group 定义，其中的 icon 你可以使用任意有效的 antd/icons 但必须以用户的意图为准。
4. 在 useSettingsKeyToTranslationMap 添加 key 定义。

## 输出格式
1. 列出你在后端新增的配置项，必须描述 tab（是否已存在）、group（是否已存在）、key 分别是什么以及值类型和默认值。
2. 指出你在前端添加的 i18n 定义的文本（若有多语言，只需要指出中文/英文即可）
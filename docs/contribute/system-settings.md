# 系统设置项

在框架源码内添加系统设置项，需要修改两个文件。

## 步骤 1：添加声明

打开 `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/system/types/SystemSettingsConstants.kt`，在对应分类的 nested object 中添加声明：

```kotlin
val CUSTOM_LOGO = SystemSettingsItemDeclaration(
    key = "basic.customLogo.url",
    valueType = SystemSettingsItemValueType.STRING,
    defaultValue = "",
    sort = 10
)
```

## 步骤 2：注册到 Registry

打开 `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/system/config/SystemSettingsBuiltinConfigurer.kt`，将上一步声明的常量添加到 `registry.settings()` 列表中：

```kotlin
registry.settings(
    listOf(
        SystemSettingsConstants.Basic.BASE_URL,
        SystemSettingsConstants.Basic.WaterMark.ENABLED,
        // ... 其他已有项 ...
        SystemSettingsConstants.Basic.CUSTOM_LOGO,  // <-- 新增
    )
)
```

## 支持的值类型

| 类型 | 前端组件 | 说明 |
|------|---------|------|
| `STRING` | Input | 文本输入 |
| `NUMBER` | InputNumber | 整数值 |
| `DECIMAL` | InputNumber | 小数值 |
| `BOOLEAN` | Switch | 开关 |
| `ENUM_SINGLE` | Radio.Group | 单选枚举 |
| `ENUM_MULTIPLE` | Checkbox.Group | 多选枚举 |

## Key 命名规则

key 采用点分命名法：`tab.group.settingName`。

- **第一段**：标签页（Tab），如 `basic`、`mail`、`security`
- **第一段 + 中间段**：分组（Group），如 `basic.waterMark`、`mail.smtp`
- **完整 key**：具体设置项，如 `basic.waterMark.enabled`

例如声明 `myPlugin.timeout`：

- Tab = `myPlugin`
- Group = `myPlugin`
- Key = `myPlugin.timeout`

如果需要分组嵌套，可以声明 `myPlugin.advanced.maxRetries`：

- Tab = `myPlugin`
- Group = `myPlugin.advanced`
- Key = `myPlugin.advanced.maxRetries`

## 添加前端翻译

在 `web/src/i18n/locales/zh-CN.ts` 和 `en-US.ts` 的 `pages.systemSettingsManager` 下添加对应翻译：

```typescript
// zh-CN.ts
pages: {
    systemSettingsManager: {
        keys: {
            'basic.customLogo.url': '自定义 Logo URL',
        },
    },
}
```

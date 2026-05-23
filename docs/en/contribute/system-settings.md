# System Settings

When contributing to the framework source code, you need to modify two files.

## Step 1: Add a Declaration

Open `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/system/types/SystemSettingsConstants.kt` and add the declaration in the appropriate nested object:

```kotlin
val CUSTOM_LOGO = SystemSettingsItemDeclaration(
    key = "basic.customLogo.url",
    valueType = SystemSettingsItemValueType.STRING,
    defaultValue = "",
    sort = 10
)
```

## Step 2: Register in SystemSettingsBuiltinConfigurer

Open `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/system/config/SystemSettingsBuiltinConfigurer.kt` and add the new constant to the `registry.settings()` list:

```kotlin
registry.settings(
    listOf(
        SystemSettingsConstants.Basic.BASE_URL,
        SystemSettingsConstants.Basic.WaterMark.ENABLED,
        // ... other existing items ...
        SystemSettingsConstants.Basic.CUSTOM_LOGO,  // <-- add this
    )
)
```

## Value Types

| Type | Frontend Component | Description |
|------|-------------------|-------------|
| `STRING` | Input | Text input |
| `NUMBER` | InputNumber | Integer value |
| `DECIMAL` | InputNumber | Decimal value |
| `BOOLEAN` | Switch | Toggle |
| `ENUM_SINGLE` | Radio.Group | Single selection |
| `ENUM_MULTIPLE` | Checkbox.Group | Multiple selection |

## Key Naming Convention

Keys use dot notation: `tab.group.settingName`.

- **First segment**: Tab, e.g. `basic`, `mail`, `security`
- **First + middle segments**: Group, e.g. `basic.waterMark`, `mail.smtp`
- **Full key**: The specific setting, e.g. `basic.waterMark.enabled`

For example, declaring `myPlugin.timeout`:

- Tab = `myPlugin`
- Group = `myPlugin`
- Key = `myPlugin.timeout`

For nested groups, declare `myPlugin.advanced.maxRetries`:

- Tab = `myPlugin`
- Group = `myPlugin.advanced`
- Key = `myPlugin.advanced.maxRetries`

## Add Frontend Translations

Add entries in `web/src/i18n/locales/zh-CN.ts` and `en-US.ts` under `pages.systemSettingsManager`:

```typescript
// en-US.ts
pages: {
    systemSettingsManager: {
        keys: {
            'basic.customLogo.url': 'Custom Logo URL',
        },
    },
}
```

# Tenant Settings

When contributing tenant settings to the framework source code, you need to modify two files.

Tenant settings share the same declaration shape (`SettingsItemDeclaration` / `SettingsItemValueType`) as system settings, but are managed by `TenantSettingsRegistry` (versus `SystemSettingsRegistry`) — the two registries are independent.

## Step 1: Add a Declaration

Open `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/tenant/settings/constants/TenantSettingsConstants.kt` and add the declaration in the appropriate nested object:

```kotlin
object TenantSettingsConstants {
    object Notification {
        val DAILY_DIGEST_ENABLED = SettingsItemDeclaration(
            key = "notification.dailyDigestEnabled",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 3,
        )
    }
}
```

## Step 2: Register in TenantSettingsBuiltinConfigurer

Open `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/tenant/settings/config/TenantSettingsBuiltinConfigurer.kt` and add the new constant to the `registry.settings()` list:

```kotlin
registry.settings(
    listOf(
        TenantSettingsConstants.Notification.MEMBER_JOIN_NOTIFY_EMAIL,
        TenantSettingsConstants.Notification.MEMBER_JOIN_REVIEW_NOTIFY_EMAIL,
        TenantSettingsConstants.Notification.DAILY_DIGEST_ENABLED,  // <-- add this
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

Keys use dot notation: `tab.group.settingName`, identical to system settings.

- **First segment**: Tab
- **First + middle segments**: Group
- **Full key**: The specific setting

> **The tenant settings UI currently uses a single-tab layout**, so the Tab segment is mainly used for diagnostics and the page is rendered group by group.

## Add Frontend Translations

Add entries in `web/src/i18n/locales/zh-CN.ts` and `en-US.ts` under `pages.tenantSettingsManager`:

```typescript
// en-US.ts
pages: {
    tenantSettingsManager: {
        keys: {
            'notification.dailyDigestEnabled': 'Daily Digest',
        },
    },
}
```

If you introduce the first key in a new group / tab, also add the corresponding translation under `groups` / `tabs`.

# Tenant Settings

You can add per-tenant configurable settings when developing a plugin. Tenant settings share the same declaration shape and rendering pipeline as system settings, with the following difference:

- **System settings**: globally unique, maintained by system administrators
- **Tenant settings**: each tenant stores its own values, maintained by tenant administrators

This section covers the standalone plugin approach.

## Declare Settings

Implement `TenantSettingsConfigurer` in your plugin:

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.sdk.tenant.settings.config.TenantSettingsConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MyPluginTenantSettingsConfigurer : TenantSettingsConfigurer {
    override fun configure(registry: TenantSettingsRegistry) {
        registry.settings(
            listOf(
                SettingsItemDeclaration(
                    key = "myPlugin.dailyDigestEnabled",
                    valueType = SettingsItemValueType.BOOLEAN,
                    defaultValue = "true",
                    sort = 10
                ),
                SettingsItemDeclaration(
                    key = "myPlugin.notifyChannel",
                    valueType = SettingsItemValueType.ENUM_SINGLE,
                    defaultValue = "email",
                    enumValues = listOf("email", "lark"),
                    sort = 20
                ),
            )
        )
    }
}
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
| `STRING_ARRAY` | Select (tags) | String array (transported as a JSON string array) |
| `NUMBER_ARRAY` | Select (tags) | Integer array (transported as a JSON string array) |
| `DECIMAL_ARRAY` | Select (tags) | Decimal array (transported as a JSON string array) |
| `BOOLEAN_ARRAY` | Select (tags) | Boolean array (transported as a JSON string array) |

## Key Naming Convention

Keys use dot notation: `tab.group.settingName`, identical to system settings.

- **First segment**: Tab
- **First + middle segments**: Group
- **Full key**: The specific setting

For example, declaring `notification.memberJoinNotifyEmail`:

- Tab = `notification`
- Group = `notification`
- Key = `notification.memberJoinNotifyEmail`

> **The tenant settings UI currently uses a single-tab layout**, so the Tab segment is mainly used for diagnostics and the page is rendered group by group.

## Frontend i18n

In your frontend plugin, provide translations via `i18nResources` and register keys/groups/tabs in `configure`. The first parameter to each `addSettings*` method is the scope; pass `'tenant'` for tenant settings:

```typescript
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addSettingsKey('tenant', 'myPlugin.dailyDigestEnabled');
        registry.addSettingsKey('tenant', 'myPlugin.notifyChannel');
        registry.addSettingsGroup('tenant', 'myPlugin');
        registry.addSettingsTab('tenant', 'myPlugin');
    },

    i18nResources: {
        'zh-CN': {
            pages: {
                tenantSettingsManager: {
                    keys: {
                        'myPlugin.dailyDigestEnabled': '每日摘要',
                        'myPlugin.notifyChannel': '通知渠道',
                    },
                    groups: {
                        'myPlugin': '我的插件',
                    },
                    tabs: {
                        'myPlugin': '我的插件',
                    },
                },
            },
        },
        'en-US': {
            pages: {
                tenantSettingsManager: {
                    keys: {
                        'myPlugin.dailyDigestEnabled': 'Daily Digest',
                        'myPlugin.notifyChannel': 'Notify Channel',
                    },
                    groups: {
                        'myPlugin': 'My Plugin',
                    },
                    tabs: {
                        'myPlugin': 'My Plugin',
                    },
                },
            },
        },
    },
};

export default myPlugin;
```

For ENUM types, add enum translations under `pages.tenantSettingsManager.enums`:

```typescript
i18nResources: {
    'zh-CN': {
        pages: {
            tenantSettingsManager: {
                enums: {
                    'myPlugin.notifyChannel': {
                        email: '邮件',
                        lark: '飞书',
                    },
                },
            },
        },
    },
}
```

Enum translations do not require `registry.addSettingsKey/Group/Tab` calls — the page reads them directly from i18n resources.

## Custom Renderers and Group Extra Renderers

Tenant settings share the rendering pipeline of system settings; the registration API is identical, just pass `'tenant'` as the first argument:

```typescript
import {Input} from "antd";
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addSettingsItemRenderer(
            'tenant',
            'myPlugin.notifyChannel',
            ({schema, loading}) => (
                <Input
                    className="rounded-lg h-10"
                    placeholder={schema.defaultValue ?? ''}
                    disabled={loading}
                />
            ),
        );

        registry.addSettingsGroupExtraRenderer(
            'tenant',
            'myPlugin',
            ({form}) => (
                <div className="flex justify-end">
                    <button onClick={() => console.log(form.getFieldsValue())}>
                        Test Notification
                    </button>
                </div>
            ),
        );
    },
};
```

The `SettingsItemRenderer` / `SettingsGroupExtraRenderer` context fields, `Form.Item` wrapping, and override priority match system settings exactly. See [System Settings](./system-settings.md) for details.

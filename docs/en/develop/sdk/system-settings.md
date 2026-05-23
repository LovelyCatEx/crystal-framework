# System Settings

You can add system settings when developing a plugin. This section covers the standalone plugin approach.

## Declare Settings

Implement `SystemSettingsConfigurer` in your plugin:

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.system.settings.config.SystemSettingsConfigurer
import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType
import org.springframework.core.annotation.Order
import org.springframework.core.Ordered
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MyPluginSettingsConfigurer : SystemSettingsConfigurer {
    override fun configure(registry: SystemSettingsConfigurerContext) {
        registry.register(
            SystemSettingsItemDeclaration(
                key = "myPlugin.apiKey",
                valueType = SystemSettingsItemValueType.STRING,
                defaultValue = "",
                sort = 10
            )
        )
        registry.register(
            SystemSettingsItemDeclaration(
                key = "myPlugin.retryCount",
                valueType = SystemSettingsItemValueType.NUMBER,
                defaultValue = "3",
                sort = 20
            )
        )
        registry.register(
            SystemSettingsItemDeclaration(
                key = "myPlugin.mode",
                valueType = SystemSettingsItemValueType.ENUM_SINGLE,
                defaultValue = "auto",
                enumValues = listOf("auto", "manual"),
                sort = 30
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

## Frontend i18n

In your frontend plugin, provide translations via `i18nResources` and register keys/groups/tabs in `configure`:

```typescript
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addSystemSettingsKey('myPlugin.apiKey');
        registry.addSystemSettingsKey('myPlugin.retryCount');
        registry.addSystemSettingsKey('myPlugin.mode');
        registry.addSystemSettingsGroup('myPlugin');
        registry.addSystemSettingsTab('myPlugin');
    },

    i18nResources: {
        'zh-CN': {
            pages: {
                systemSettingsManager: {
                    keys: {
                        'myPlugin.apiKey': 'API 密钥',
                        'myPlugin.retryCount': '重试次数',
                        'myPlugin.mode': '运行模式',
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
                systemSettingsManager: {
                    keys: {
                        'myPlugin.apiKey': 'API Key',
                        'myPlugin.retryCount': 'Retry Count',
                        'myPlugin.mode': 'Mode',
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

For ENUM types, add enum translations under `pages.systemSettingsManager.enums`:

```typescript
i18nResources: {
    'zh-CN': {
        pages: {
            systemSettingsManager: {
                enums: {
                    'myPlugin.mode': {
                        auto: '自动',
                        manual: '手动',
                    },
                },
            },
        },
    },
}
```

Note: enum translations do not require `registry.addSystemSettingsKey/Group/Tab` calls — the page reads them directly from i18n resources.

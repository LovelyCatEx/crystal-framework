# System Settings

You can add system settings when developing a plugin. This section covers the standalone plugin approach.

## Declare Settings

Implement `SystemSettingsConfigurer` in your plugin:

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.system.settings.config.SystemSettingsConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class MyPluginSettingsConfigurer : SystemSettingsConfigurer {
    override fun configure(registry: SystemSettingsRegistry) {
        registry.settings(
            listOf(
                SettingsItemDeclaration(
                    key = "myPlugin.apiKey",
                    valueType = SettingsItemValueType.STRING,
                    defaultValue = "",
                    sort = 10
                ),
                SettingsItemDeclaration(
                    key = "myPlugin.retryCount",
                    valueType = SettingsItemValueType.NUMBER,
                    defaultValue = "3",
                    sort = 20
                ),
                SettingsItemDeclaration(
                    key = "myPlugin.mode",
                    valueType = SettingsItemValueType.ENUM_SINGLE,
                    defaultValue = "auto",
                    enumValues = listOf("auto", "manual"),
                    sort = 30
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

In your frontend plugin, provide translations via `i18nResources` and register keys/groups/tabs in `configure`. The first parameter to each `addSettings*` method is the scope; pass `'system'` for system settings:

```typescript
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        registry.addSettingsKey('system', 'myPlugin.apiKey');
        registry.addSettingsKey('system', 'myPlugin.retryCount');
        registry.addSettingsKey('system', 'myPlugin.mode');
        registry.addSettingsGroup('system', 'myPlugin');
        registry.addSettingsTab('system', 'myPlugin');
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

Enum translations do not require `registry.addSettingsKey/Group/Tab` calls — the page reads them directly from i18n resources.

## Custom Renderers and Group Extra Renderers

By default each setting renders as Input/InputNumber/Switch/Radio.Group/Checkbox.Group based on its `valueType`. To use a richer control (color picker, password input, JSON editor), or to append extra UI under a group (such as a "Test Connection" button), register renderers at `configure` time.

```typescript
import {Input} from "antd";
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        // Custom input control for a single setting
        registry.addSettingsItemRenderer(
            'system',
            'myPlugin.apiKey',
            ({schema, loading}) => (
                <Input.Password
                    className="rounded-lg h-10"
                    placeholder={schema.defaultValue ?? ''}
                    disabled={loading}
                    autoComplete="new-password"
                />
            ),
        );

        // Extra area under a group (buttons, hints, etc.)
        registry.addSettingsGroupExtraRenderer(
            'system',
            'myPlugin',
            ({form}) => (
                <div className="flex justify-end">
                    <button onClick={() => console.log(form.getFieldsValue())}>
                        Test Connection
                    </button>
                </div>
            ),
        );
    },
};
```

`SettingsItemRenderer` context:

| Field | Type | Description |
|-------|------|-------------|
| `settingsKey` | `string` | The settings key |
| `schema` | `SystemSettingsSchema` | Contains `valueType`, `defaultValue`, `enumValues`, etc. |
| `loading` | `boolean` | Whether the page is currently loading or saving |

`SettingsGroupExtraRenderer` context:

| Field | Type | Description |
|-------|------|-------------|
| `group` | `string` | The current group key |
| `form` | `FormInstance` | The current form instance, useful for reading or setting field values |

> **Note**: A custom item renderer is wrapped by `Form.Item` automatically — return the raw control (`Input`, `Select`, …) directly; the framework wires `value` / `onChange` for you.

> **Override priority**: Built-in renderers (such as the framework's own `mail.smtp.password`) take precedence over plugin renderers. If two plugins register a renderer for the same key, the first one wins and a console warning is emitted for the later one.

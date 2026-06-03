# 系统设置项

插件开发时可以为系统添加设置项。这里只涉及独立插件的方式。

## 声明设置项

实现 `SystemSettingsConfigurer` 接口：

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

## 前端 i18n

在插件中通过 `i18nResources` 提供翻译，并在 `configure` 中注册 key/group/tab。注册方法的第一个参数固定为 `'system'`，表示作用域是系统设置：

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

ENUM 类型的枚举值翻译加在 `pages.systemSettingsManager.enums` 下：

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

枚举翻译不需要调用 `registry.addSettingsKey/Group/Tab`，前端直接从 i18n 资源中读取。

## 自定义渲染与分组扩展渲染

默认情况下，系统设置项根据 `valueType` 渲染为 Input/InputNumber/Switch/Radio.Group/Checkbox.Group 等基础控件。如果需要更精细的控件（如颜色选择器、密码框、JSON 编辑器），或在某个分组下追加额外操作（如"测试连接"按钮），可以在 `configure` 阶段注册渲染器。

```typescript
import {Input} from "antd";
import type {CrystalWebPlugin} from "@/plugin/types.ts";

const myPlugin: CrystalWebPlugin = {
    configure(registry) {
        // 自定义某个 setting 的输入控件
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

        // 在分组下追加额外区域（按钮、提示等）
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

`SettingsItemRenderer` 上下文：

| 字段 | 类型 | 说明 |
|------|------|------|
| `settingsKey` | `string` | 设置项 key |
| `schema` | `SystemSettingsSchema` | 包含 `valueType`、`defaultValue`、`enumValues` 等 |
| `loading` | `boolean` | 当前是否处于加载/保存中 |

`SettingsGroupExtraRenderer` 上下文：

| 字段 | 类型 | 说明 |
|------|------|------|
| `group` | `string` | 当前分组 key |
| `form` | `FormInstance` | 当前表单实例，可用于读取/设置字段值 |

> **注意**：自定义渲染器返回的内容会被 `Form.Item` 包裹，因此渲染器内部不需要再写 `Form.Item`，直接返回 `Input`、`Select` 等控件即可。框架会自动处理 `value` / `onChange`。

> **覆盖优先级**：内置渲染器（框架自带的 `mail.smtp.password` 等）优先于插件渲染器。如果两个插件注册了同 key 的渲染器，先注册的生效，后者会输出告警。

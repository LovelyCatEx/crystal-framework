# 系统设置项

插件开发时可以为系统添加设置项。这里只涉及独立插件的方式。

## 声明设置项

实现 `SystemSettingsConfigurer` 接口：

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

在插件中通过 `i18nResources` 提供翻译，并在 `configure` 中注册 key/group/tab：

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

枚举翻译不需要调用 `registry.addSystemSettingsKey/Group/Tab`。

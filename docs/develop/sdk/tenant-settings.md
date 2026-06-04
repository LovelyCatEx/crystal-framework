# 租户设置项

插件开发时可以为每个租户添加可独立配置的设置项。租户设置与系统设置共享同一套声明结构和渲染管线，区别在于：

- **系统设置**：全局唯一、由系统管理员维护
- **租户设置**：每个租户独立保存自己的值，由租户管理员维护

这里只涉及独立插件的方式。

## 声明设置项

实现 `TenantSettingsConfigurer` 接口：

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

## 支持的值类型

| 类型 | 前端组件 | 说明 |
|------|---------|------|
| `STRING` | Input | 文本输入 |
| `NUMBER` | InputNumber | 整数值 |
| `DECIMAL` | InputNumber | 小数值 |
| `BOOLEAN` | Switch | 开关 |
| `ENUM_SINGLE` | Radio.Group | 单选枚举 |
| `ENUM_MULTIPLE` | Checkbox.Group | 多选枚举 |
| `STRING_ARRAY` | Select (tags) | 字符串数组（JSON 字符串数组传输） |
| `NUMBER_ARRAY` | Select (tags) | 整数数组（JSON 字符串数组传输） |
| `DECIMAL_ARRAY` | Select (tags) | 小数数组（JSON 字符串数组传输） |
| `BOOLEAN_ARRAY` | Select (tags) | 布尔值数组（JSON 字符串数组传输） |

## Key 命名规则

key 采用点分命名法：`tab.group.settingName`，规则与系统设置一致。

- **第一段**：标签页（Tab）
- **第一段 + 中间段**：分组（Group）
- **完整 key**：具体设置项

例如声明 `notification.memberJoinNotifyEmail`：

- Tab = `notification`
- Group = `notification`
- Key = `notification.memberJoinNotifyEmail`

> **租户设置目前仅支持单标签页布局**，因此 Tab 主要用于日志/调试用途，最终以 Group 为单位展示。

## 前端 i18n

在插件中通过 `i18nResources` 提供翻译，并在 `configure` 中注册 key/group/tab。注册方法的第一个参数固定为 `'tenant'`，表示作用域是租户设置：

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

ENUM 类型的枚举值翻译加在 `pages.tenantSettingsManager.enums` 下：

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

枚举翻译不需要调用 `registry.addSettingsKey/Group/Tab`，前端直接从 i18n 资源中读取。

## 自定义渲染与分组扩展渲染

租户设置共享系统设置的渲染管线，注册方式相同，只需要把第一个参数改为 `'tenant'`：

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

`SettingsItemRenderer` 与 `SettingsGroupExtraRenderer` 的上下文字段、`Form.Item` 包裹、覆盖优先级与系统设置完全一致，详见[系统设置项](./system-settings.md)。

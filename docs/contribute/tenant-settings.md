# 租户设置项

在框架源码内添加租户设置项，需要修改两个文件。

租户设置与系统设置使用同一套声明结构（`SettingsItemDeclaration` / `SettingsItemValueType`），但分别由 `TenantSettingsRegistry` 和 `SystemSettingsRegistry` 管理，互不干扰。

## 步骤 1：添加声明

打开 `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/tenant/settings/constants/TenantSettingsConstants.kt`，在对应分类的 nested object 中添加声明：

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

## 步骤 2：注册到 Registry

打开 `crystal-starter/src/main/kotlin/com/lovelycatv/crystalframework/tenant/settings/config/TenantSettingsBuiltinConfigurer.kt`，将上一步声明的常量添加到 `registry.settings()` 列表中：

```kotlin
registry.settings(
    listOf(
        TenantSettingsConstants.Notification.MEMBER_JOIN_NOTIFY_EMAIL,
        TenantSettingsConstants.Notification.MEMBER_JOIN_REVIEW_NOTIFY_EMAIL,
        TenantSettingsConstants.Notification.DAILY_DIGEST_ENABLED,  // <-- 新增
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
| `STRING_ARRAY` | Select (tags) | 字符串数组（JSON 字符串数组传输） |
| `NUMBER_ARRAY` | Select (tags) | 整数数组（JSON 字符串数组传输） |
| `DECIMAL_ARRAY` | Select (tags) | 小数数组（JSON 字符串数组传输） |
| `BOOLEAN_ARRAY` | Select (tags) | 布尔值数组（JSON 字符串数组传输） |

## Key 命名规则

key 采用点分命名法：`tab.group.settingName`，规则与系统设置一致。

- **第一段**：标签页（Tab）
- **第一段 + 中间段**：分组（Group）
- **完整 key**：具体设置项

> **租户设置目前仅支持单标签页布局**，因此 Tab 主要用于日志/调试用途，最终以 Group 为单位展示。

## 添加前端翻译

在 `web/src/i18n/locales/zh-CN.ts` 和 `en-US.ts` 的 `pages.tenantSettingsManager` 下添加对应翻译：

```typescript
// zh-CN.ts
pages: {
    tenantSettingsManager: {
        keys: {
            'notification.dailyDigestEnabled': '每日摘要',
        },
    },
}
```

如新增的是首个分组 / 首个标签页，需要在 `groups` / `tabs` 节点下补对应翻译。

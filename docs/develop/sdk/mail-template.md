# 邮件模板

开发插件时可以注册自定义邮件模板。mail 模块通过 Spring Bean 自动发现所有 `MailModuleConfigure` 实现，在启动时将模板同步到数据库。

## 架构概览

```
插件提供 MailModuleConfigure Bean
  → MailModuleAutoConfigure 自动发现
    → 按三个阶段注册：分类 → 模板类型 → 模板
      → 不存在则创建，存在则跳过
```

## 声明模板组件

### 分类声明

```kotlin
val orderCategory = MailTemplateCategoryDeclaration(
    name = "order",
    description = "Order mail templates",
)
```

### 模板类型声明

```kotlin
val orderPaidNoticeTemplateType = MailTemplateTypeDeclaration(
    name = "order_paid_notice",
    description = "Notification after an order is paid",
    variables = arrayOf("order_no", "amount", "paid_time"),
    categoryDeclaration = orderCategory,
    allowMultiple = false,
)
```

### 默认模板声明

```kotlin
val defaultOrderPaidNoticeTemplate = MailTemplateDeclaration(
    name = "default_order_paid_notice",
    description = "Default notification after an order is paid",
    title = "Order {{order_no}} Paid Successfully",
    content = """
        <!DOCTYPE html>
        <html lang="en">
        <body>
            <p>Your order {{order_no}} has been paid successfully.</p>
            <p>Amount: {{amount}}</p>
            <p>Paid time: {{paid_time}}</p>
        </body>
        </html>
    """.trimIndent(),
    active = true,
    type = orderPaidNoticeTemplateType,
)
```

### 变量占位符规则

- 模板标题和内容中的占位符格式：<code v-pre>{{变量名}}</code>
- 变量名大小写和下划线敏感，必须与 `variables` 中声明的一致
- 发送时传入的 `placeholders` Map 中不存在的 key，占位符会原样保留

## 注册模板

实现 `MailModuleConfigure` 接口，三个方法分别注册分类、模板类型、模板：

```kotlin
package com.example.myplugin

import com.lovelycatv.crystalframework.sdk.mail.config.MailModuleConfigure
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateTypeDeclaration
import org.springframework.context.annotation.Configuration

@Configuration
class OrderMailModuleConfigure : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.add(OrderMailDeclaration.orderCategory)
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>,
    ) {
        templateTypes.add(OrderMailDeclaration.orderPaidNoticeTemplateType)
    }

    override fun configureTemplate(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: List<MailTemplateTypeDeclaration>,
        templates: MutableList<MailTemplateDeclaration>,
    ) {
        templates.add(OrderMailDeclaration.defaultOrderPaidNoticeTemplate)
    }
}
```

### 要求

- `MailModuleConfigure` 实现类必须加 `@Configuration`，确保 Spring 能发现
- `configureTemplateCategory` 注册分类
- `configureTemplateType` 注册类型，`categoryDeclaration` 必须引用已经注册的分类
- `configureTemplate` 注册模板
- 模板 `name` 必须全局唯一

## 发送邮件

按模板类型发送（推荐，管理员替换模板时业务代码无需变更）：

```kotlin
mailService.sendMailByType(
    to = email,
    templateTypeName = OrderMailDeclaration.orderPaidNoticeTemplateType.name,
    placeholders = mapOf(
        "order_no" to orderNo,
        "amount" to amount.toString(),
        "paid_time" to paidTime.toString(),
    ),
)
```

或按模板名称发送：

```kotlin
mailService.sendMail(
    to = email,
    templateName = OrderMailDeclaration.defaultOrderPaidNoticeTemplate.name,
    placeholders = mapOf(...),
)
```

## 验证

启动后观察日志：

```
Checking mail template categories...
Checking mail template types...
Checking mail templates...
```

新增项显示 `* name`，已存在项显示 `√ name`。

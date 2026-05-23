# Mail Template

When developing a plugin, you can register custom mail templates. The mail module automatically discovers all `MailModuleConfigure` beans via Spring and syncs templates to the database on startup.

## Architecture Overview

```
Plugin provides MailModuleConfigure Bean
  → MailModuleAutoConfigure discovers it
    → Three-phase registration: Category → Type → Template
      → Creates if not exists, skips if exists
```

## Declaring Template Components

### Category Declaration

```kotlin
val orderCategory = MailTemplateCategoryDeclaration(
    name = "order",
    description = "Order mail templates",
)
```

### Template Type Declaration

```kotlin
val orderPaidNoticeTemplateType = MailTemplateTypeDeclaration(
    name = "order_paid_notice",
    description = "Notification after an order is paid",
    variables = arrayOf("order_no", "amount", "paid_time"),
    categoryDeclaration = orderCategory,
    allowMultiple = false,
)
```

### Default Template Declaration

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

### Placeholder Rules

- Placeholder format in title and content: <code v-pre>{{variable_name}}</code>
- Variable names are case-sensitive and underscore-sensitive, must match `variables` declaration
- Missing keys in the `placeholders` Map will leave placeholders un-replaced

## Registering Templates

Implement `MailModuleConfigure` with three methods for categories, types, and templates:

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

### Requirements

- `MailModuleConfigure` implementation must be annotated with `@Configuration`
- `configureTemplateCategory` registers categories
- `configureTemplateType` registers types, `categoryDeclaration` must reference a registered category
- `configureTemplate` registers templates
- Template `name` must be globally unique

## Sending Email

Send by template type (recommended — admin can swap templates without code changes):

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

Or send by template name:

```kotlin
mailService.sendMail(
    to = email,
    templateName = OrderMailDeclaration.defaultOrderPaidNoticeTemplate.name,
    placeholders = mapOf(...),
)
```

## Verification

Check the startup logs:

```
Checking mail template categories...
Checking mail template types...
Checking mail templates...
```

New items show `* name`, existing items show `√ name`.

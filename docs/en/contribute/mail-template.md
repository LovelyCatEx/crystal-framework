# Mail Template

When contributing to the framework source code, modify the templates directly in the `crystal-mail` or `crystal-starter` module.

## Customizing System Templates

System templates are defined in `SystemMailDeclaration.kt` in `crystal-mail`. To customize the subject and content, provide a `SystemMailTemplateConfigure` bean to override the default:

```kotlin
package com.lovelycatv.crystalframework.mail.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MySystemMailTemplateConfigure {
    @Bean
    fun systemMailTemplateConfigure(): SystemMailTemplateConfigure {
        return object : SystemMailTemplateConfigure {
            override fun configureUserRegistration(): MailTemplateDeclaration {
                return SystemMailDeclaration.defaultUserRegisterTemplate.copy(
                    title = "Welcome to our platform!",
                    content = "<html>...</html>"
                )
            }
            override fun configureUserResetPassword(): MailTemplateDeclaration {
                return SystemMailDeclaration.defaultResetPasswordTemplate.copy(
                    title = "Reset Your Password",
                    content = "<html>...</html>"
                )
            }
            override fun configureUserResetEmail(): MailTemplateDeclaration {
                return SystemMailDeclaration.defaultResetEmailTemplate.copy(
                    title = "Reset Your Email",
                    content = "<html>...</html>"
                )
            }
        }
    }
}
```

The framework uses `@ConditionalOnMissingBean(SystemMailTemplateConfigure::class)`, so your bean takes precedence.

## Adding Business Templates

Follow the Tenant module pattern in `crystal-starter`:

### 1. Define Declarations

```kotlin
// crystal-starter/src/main/kotlin/.../order/constants/OrderMailDeclaration.kt
object OrderMailDeclaration {
    const val VARIABLE_ORDER_NO = "order_no"
    const val VARIABLE_AMOUNT = "amount"

    val orderCategory = MailTemplateCategoryDeclaration(
        name = "order",
        description = "Order mail templates",
    )

    val orderPaidNoticeTemplateType = MailTemplateTypeDeclaration(
        name = "order_paid_notice",
        description = "Notification after an order is paid",
        variables = arrayOf(VARIABLE_ORDER_NO, VARIABLE_AMOUNT),
        categoryDeclaration = orderCategory,
        allowMultiple = false,
    )

    val defaultOrderPaidNoticeTemplate = MailTemplateDeclaration(
        name = "default_order_paid_notice",
        description = "Default notification after an order is paid",
        title = "Order {{order_no}} Paid Successfully",
        content = """<html>...</html>""".trimIndent(),
        active = true,
        type = orderPaidNoticeTemplateType,
    )
}
```

### 2. Register via MailModuleConfigure

```kotlin
// crystal-starter/src/main/kotlin/.../order/config/OrderMailModuleConfigure.kt
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

### Placeholder Rules

- Use <code v-pre>{{variable_name}}</code> format in title and content
- Variable names are case-sensitive and underscore-sensitive
- Missing keys in the placeholders Map will leave placeholders un-replaced

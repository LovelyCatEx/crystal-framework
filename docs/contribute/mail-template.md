# 邮件模板

在框架源码内修改邮件模板，直接在 `crystal-mail` 或 `crystal-starter` 模块中操作。

## 修改系统默认模板

系统模板定义在 `crystal-mail` 模块的 `SystemMailDeclaration.kt` 中。如果你需要自定义标题和内容，提供 `SystemMailTemplateConfigure` 的 Bean 覆盖默认实现：

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
                    title = "欢迎注册！",
                    content = "<html>...</html>"
                )
            }
            override fun configureUserResetPassword(): MailTemplateDeclaration {
                return SystemMailDeclaration.defaultResetPasswordTemplate.copy(
                    title = "重置密码",
                    content = "<html>...</html>"
                )
            }
            override fun configureUserResetEmail(): MailTemplateDeclaration {
                return SystemMailDeclaration.defaultResetEmailTemplate.copy(
                    title = "重置邮箱",
                    content = "<html>...</html>"
                )
            }
        }
    }
}
```

框架自带 `@ConditionalOnMissingBean(SystemMailTemplateConfigure::class)`，你的 Bean 会优先使用。

## 新增业务模板

参考 `crystal-starter` 中的 Tenant 模块模式：

### 1. 定义声明

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

### 2. 注册到 MailModuleConfigure

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

### 变量占位符规则

- 模板标题和内容中使用 <code v-pre>{{变量名}}</code> 格式
- 变量名大小写和下划线敏感
- 发送时 placeholders 中不存在的 key，占位符会原样保留

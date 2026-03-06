package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultMailModuleConfigure : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.add(
            MailTemplateCategoryDeclaration(
                name = "system",
                description = "System mail templates",
            )
        )
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templates: MutableList<MailTemplateTypeDeclaration>
    ) {
        val systemCategory = categories["system"]!!

        templates.add(
            MailTemplateTypeDeclaration(
                name = "system_user_register",
                description = "User registration mail templates",
                variables = arrayOf("code"),
                categoryDeclaration = systemCategory,
                allowMultiple = false
            )
        )

        templates.add(
            MailTemplateTypeDeclaration(
                name = "system_reset_password",
                description = "User reset password mail templates",
                variables = arrayOf("code"),
                categoryDeclaration = systemCategory,
                allowMultiple = false
            )
        )

        templates.add(
            MailTemplateTypeDeclaration(
                name = "system_reset_email_address",
                description = "User reset email address mail templates",
                variables = arrayOf("code"),
                categoryDeclaration = systemCategory,
                allowMultiple = false
            )
        )
    }

}
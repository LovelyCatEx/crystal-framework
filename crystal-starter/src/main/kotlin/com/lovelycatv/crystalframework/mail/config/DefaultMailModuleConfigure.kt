package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.constants.SystemMailDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultMailModuleConfigure(
    private val systemMailTemplateConfigure: SystemMailTemplateConfigure
) : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.addAll(SystemMailDeclaration.categories)
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>
    ) {
        templateTypes.addAll(SystemMailDeclaration.types)
    }

    override fun configureTemplate(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: List<MailTemplateTypeDeclaration>,
        templates: MutableList<MailTemplateDeclaration>
    ) {
        val preProcessMailTemplateDeclaration = fun (
            declaration: MailTemplateDeclaration,
            name: String,
            type: MailTemplateTypeDeclaration,
        ): MailTemplateDeclaration {
            return declaration.copy(
                name = name,
                type = type
            )
        }

        templates.add(
            preProcessMailTemplateDeclaration.invoke(
                this.systemMailTemplateConfigure.configureUserRegistration(),
                SystemMailDeclaration.defaultSystemUserRegisterTemplate.name,
                SystemMailDeclaration.defaultSystemUserRegisterTemplate.type,
            )
        )

        templates.add(
            preProcessMailTemplateDeclaration.invoke(
                this.systemMailTemplateConfigure.configureUserResetPassword(),
                SystemMailDeclaration.defaultSystemResetPasswordTemplate.name,
                SystemMailDeclaration.defaultSystemResetPasswordTemplate.type,
            )
        )

        templates.add(
            preProcessMailTemplateDeclaration.invoke(
                this.systemMailTemplateConfigure.configureUserResetEmail(),
                SystemMailDeclaration.defaultSystemResetEmailAddressTemplate.name,
                SystemMailDeclaration.defaultSystemResetEmailAddressTemplate.type,
            )
        )

    }
}
package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.mail.config.MailModuleConfigure
import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration
import com.lovelycatv.crystalframework.tenant.constants.TenantMailDeclaration
import org.springframework.context.annotation.Configuration

@Configuration
class TenantMailModuleConfigure(
    private val tenantMailTemplateConfigure: TenantMailTemplateConfigure
) : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.add(TenantMailDeclaration.tenantCategory)
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>
    ) {
        templateTypes.add(TenantMailDeclaration.tenantMemberJoinReviewTemplateType)
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
                TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate,
                TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate.name,
                TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate.type
            )
        )
    }
}
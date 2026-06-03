package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.mail.config.MailModuleConfigure
import com.lovelycatv.crystalframework.sdk.mail.config.TenantMailTemplateConfigure
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateTypeDeclaration
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
        templateTypes.add(TenantMailDeclaration.tenantMemberJoinNotifyTemplateType)
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
                tenantMailTemplateConfigure.configureUserJoinReview(),
                TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate.name,
                TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate.type
            )
        )

        templates.add(
            preProcessMailTemplateDeclaration.invoke(
                tenantMailTemplateConfigure.configureMemberJoinNotify(),
                TenantMailDeclaration.defaultTenantMemberJoinNotifyTemplate.name,
                TenantMailDeclaration.defaultTenantMemberJoinNotifyTemplate.type
            )
        )
    }
}
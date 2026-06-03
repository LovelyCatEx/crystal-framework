package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.mail.config.TenantMailTemplateConfigure
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.tenant.constants.TenantMailDeclaration

class DefaultTenantMailTemplateConfigure : TenantMailTemplateConfigure {
    override fun configureUserJoinReview(): MailTemplateDeclaration {
        return TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate
    }

    override fun configureMemberJoinNotify(): MailTemplateDeclaration {
        return TenantMailDeclaration.defaultTenantMemberJoinNotifyTemplate
    }
}
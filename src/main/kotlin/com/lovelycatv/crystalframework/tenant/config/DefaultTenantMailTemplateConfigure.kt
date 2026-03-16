package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.tenant.constants.TenantMailDeclaration

class DefaultTenantMailTemplateConfigure : TenantMailTemplateConfigure {
    override fun configureUserJoinReview(): MailTemplateDeclaration {
        return TenantMailDeclaration.defaultTenantMemberJoinReviewTemplate
    }
}
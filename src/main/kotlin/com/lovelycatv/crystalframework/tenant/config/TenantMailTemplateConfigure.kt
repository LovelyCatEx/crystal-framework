package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration

interface TenantMailTemplateConfigure {
    fun configureUserJoinReview(): MailTemplateDeclaration
}
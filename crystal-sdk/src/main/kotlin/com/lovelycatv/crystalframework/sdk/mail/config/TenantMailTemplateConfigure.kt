package com.lovelycatv.crystalframework.sdk.mail.config

import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration

interface TenantMailTemplateConfigure {
    fun configureUserJoinReview(): MailTemplateDeclaration

    fun configureMemberJoinNotify(): MailTemplateDeclaration
}
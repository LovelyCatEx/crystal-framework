package com.lovelycatv.crystalframework.sdk.mail.config

import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration

fun interface TenantMailTemplateConfigure {
    fun configureUserJoinReview(): MailTemplateDeclaration
}
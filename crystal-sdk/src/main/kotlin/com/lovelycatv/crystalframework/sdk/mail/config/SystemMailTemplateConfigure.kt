package com.lovelycatv.crystalframework.sdk.mail.config

import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration

interface SystemMailTemplateConfigure {
    fun configureUserRegistration(): MailTemplateDeclaration

    fun configureUserResetPassword(): MailTemplateDeclaration

    fun configureUserResetEmail(): MailTemplateDeclaration
}
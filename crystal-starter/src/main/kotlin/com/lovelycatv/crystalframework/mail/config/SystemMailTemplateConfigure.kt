package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration

interface SystemMailTemplateConfigure {
    fun configureUserRegistration(): MailTemplateDeclaration

    fun configureUserResetPassword(): MailTemplateDeclaration

    fun configureUserResetEmail(): MailTemplateDeclaration
}
package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.constants.SystemMailDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration

class DefaultSystemMailTemplateConfigure : SystemMailTemplateConfigure {
    override fun configureUserRegistration(): MailTemplateDeclaration {
        return SystemMailDeclaration.defaultSystemUserRegisterTemplate
    }

    override fun configureUserResetPassword(): MailTemplateDeclaration {
        return SystemMailDeclaration.defaultSystemResetPasswordTemplate
    }

    override fun configureUserResetEmail(): MailTemplateDeclaration {
        return SystemMailDeclaration.defaultSystemResetEmailAddressTemplate
    }
}
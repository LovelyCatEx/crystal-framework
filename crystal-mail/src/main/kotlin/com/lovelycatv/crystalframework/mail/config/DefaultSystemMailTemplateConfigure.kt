/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.constants.SystemMailDeclaration
import com.lovelycatv.crystalframework.sdk.mail.config.SystemMailTemplateConfigure
import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration

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
/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.mail.config

import com.lovelycatv.crystalframework.sdk.mail.types.MailTemplateDeclaration

interface SystemMailTemplateConfigure {
    fun configureUserRegistration(): MailTemplateDeclaration

    fun configureUserResetPassword(): MailTemplateDeclaration

    fun configureUserResetEmail(): MailTemplateDeclaration
}
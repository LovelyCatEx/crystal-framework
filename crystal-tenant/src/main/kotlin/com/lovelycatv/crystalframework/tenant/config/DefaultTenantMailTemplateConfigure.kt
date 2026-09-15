/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
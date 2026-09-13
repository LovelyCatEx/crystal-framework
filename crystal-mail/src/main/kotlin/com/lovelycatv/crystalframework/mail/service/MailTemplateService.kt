/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface MailTemplateService : CachedBaseService<MailTemplateRepository, MailTemplateEntity> {
    suspend fun getAvailableTemplateByTypeName(templateTypeName: String): MailTemplateEntity
}

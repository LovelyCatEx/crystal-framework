/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface MailTemplateTypeService : CachedBaseService<MailTemplateTypeRepository, MailTemplateTypeEntity>

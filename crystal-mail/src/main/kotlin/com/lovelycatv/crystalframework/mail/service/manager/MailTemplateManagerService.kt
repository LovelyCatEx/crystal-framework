/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerDeleteMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerReadMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface MailTemplateManagerService : CachedBaseManagerService<
        MailTemplateRepository,
        MailTemplateEntity,
        ManagerCreateMailTemplateDTO,
        ManagerReadMailTemplateDTO,
        ManagerUpdateMailTemplateDTO,
        ManagerDeleteMailTemplateDTO
>

/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerDeleteMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerReadMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface MailTemplateCategoryManagerService : CachedBaseManagerService<
        MailTemplateCategoryRepository,
        MailTemplateCategoryEntity,
        ManagerCreateMailTemplateCategoryDTO,
        ManagerReadMailTemplateCategoryDTO,
        ManagerUpdateMailTemplateCategoryDTO,
        ManagerDeleteMailTemplateCategoryDTO
>

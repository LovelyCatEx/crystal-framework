/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository

@Repository
interface MailTemplateCategoryRepository : BaseRepository<MailTemplateCategoryEntity>
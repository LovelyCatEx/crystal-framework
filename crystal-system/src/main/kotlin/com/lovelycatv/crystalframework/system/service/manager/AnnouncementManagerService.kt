/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerCreateAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerDeleteAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerReadAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerUpdateAnnouncementDTO
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import com.lovelycatv.crystalframework.system.repository.AnnouncementRepository

interface AnnouncementManagerService : CachedBaseManagerService<
        AnnouncementRepository,
        AnnouncementEntity,
        ManagerCreateAnnouncementDTO,
        ManagerReadAnnouncementDTO,
        ManagerUpdateAnnouncementDTO,
        ManagerDeleteAnnouncementDTO
>

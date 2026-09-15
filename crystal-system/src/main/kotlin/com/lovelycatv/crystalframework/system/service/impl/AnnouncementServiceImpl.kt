/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import com.lovelycatv.crystalframework.system.repository.AnnouncementRepository
import com.lovelycatv.crystalframework.system.service.AnnouncementService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AnnouncementServiceImpl(
    private val announcementRepository: AnnouncementRepository,
) : AnnouncementService {

    override fun getPublished(): Flux<AnnouncementEntity> =
        announcementRepository.findPublished()
}

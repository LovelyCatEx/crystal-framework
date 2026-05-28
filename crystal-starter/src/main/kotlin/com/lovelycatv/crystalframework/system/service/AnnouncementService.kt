package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import reactor.core.publisher.Flux

interface AnnouncementService {
    fun getPublished(): Flux<AnnouncementEntity>
}

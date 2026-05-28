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

    override fun getPublishedForUser(): Flux<AnnouncementEntity> =
        announcementRepository.findPublishedForUser()

    override fun getPublishedForManager(): Flux<AnnouncementEntity> =
        announcementRepository.findPublishedForManager()
}

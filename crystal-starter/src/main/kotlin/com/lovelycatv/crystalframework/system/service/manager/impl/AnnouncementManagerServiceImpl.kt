package com.lovelycatv.crystalframework.system.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerCreateAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerUpdateAnnouncementDTO
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import com.lovelycatv.crystalframework.system.repository.AnnouncementRepository
import com.lovelycatv.crystalframework.system.service.manager.AnnouncementManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AnnouncementManagerServiceImpl(
    private val announcementRepository: AnnouncementRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AnnouncementManagerService {

    override val cacheStore: ExpiringKVStore<String, AnnouncementEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<AnnouncementEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<AnnouncementEntity> = AnnouncementEntity::class

    override fun getRepository(): AnnouncementRepository = announcementRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAnnouncementDTO): AnnouncementEntity {
        val entity = AnnouncementEntity(
            id = snowIdGenerator.nextId(),
            title = dto.title,
            content = dto.content,
            status = dto.status,
            target = dto.target,
            priority = dto.priority,
        ).apply { newEntity() }
        return announcementRepository.save(entity).awaitFirstOrNull()!!
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAnnouncementDTO,
        original: AnnouncementEntity,
    ): AnnouncementEntity {
        dto.title?.let { original.title = it }
        dto.content?.let { original.content = it }
        dto.status?.let { original.status = it }
        dto.target?.let { original.target = it }
        dto.priority?.let { original.priority = it }
        return original
    }
}

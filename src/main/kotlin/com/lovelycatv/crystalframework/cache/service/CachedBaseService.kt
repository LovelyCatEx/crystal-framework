package com.lovelycatv.crystalframework.cache.service

import com.lovelycatv.crystalframework.cache.event.EntityCacheCreatedEvent
import com.lovelycatv.crystalframework.cache.event.EntityCacheDeletedEvent
import com.lovelycatv.crystalframework.cache.event.EntityCacheUpdatedEvent
import com.lovelycatv.crystalframework.cache.event.EntityListCacheCreatedEvent
import com.lovelycatv.crystalframework.cache.event.EntityListCacheDeletedEvent
import com.lovelycatv.crystalframework.cache.event.EntityListCacheUpdatedEvent
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.service.BaseService
import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.repository.R2dbcRepository
import kotlin.reflect.KClass

interface CachedBaseService<REPOSITORY: R2dbcRepository<ENTITY, Long>, ENTITY: BaseEntity> :
    BaseService<REPOSITORY, ENTITY> {
    val cacheStore: ExpiringKVStore<String, ENTITY>

    val listCacheStore: ExpiringKVStore<String, List<ENTITY>>

    val eventPublisher: ApplicationEventPublisher

    val entityClass: KClass<ENTITY>

    fun buildCacheKey(entityId: Long): String {
        return "${RedisConstants.ENTITY_CACHE_BY_ID}$entityId"
    }

    fun buildCacheKey(entity: ENTITY): String {
        return this.buildCacheKey(entity.id)
    }

    fun updateCache(entityId: Long, entity: ENTITY, expirationInMs: Long = randomHourExpirationInMs(8, 12)) {
        val cacheKey = buildCacheKey(entityId)

        this.cacheStore.set(cacheKey, entity, expirationInMs)

        this.eventPublisher.publishEvent(
            EntityCacheUpdatedEvent(
                entityId = entityId,
                entityClass = this.entityClass,
                cacheKey = cacheKey
            )
        )
    }

    fun updateCache(entity: ENTITY, expirationInMs: Long = randomHourExpirationInMs(8, 12)) {
        this.updateCache(entity.id, entity, expirationInMs)
    }

    fun removeCache(entityId: Long) {
        val cacheKey = buildCacheKey(entityId)

        this.cacheStore.remove(cacheKey)

        this.eventPublisher.publishEvent(
            EntityCacheDeletedEvent(
                entityId = entityId,
                entityClass = this.entityClass,
                cacheKey = cacheKey
            )
        )
    }

    fun removeCache(entity: ENTITY) {
        val cacheKey = buildCacheKey(entity.id)

        this.cacheStore.remove(cacheKey)

        this.eventPublisher.publishEvent(
            EntityCacheDeletedEvent(
                entityId = entity.id,
                entityClass = this.entityClass,
                cacheKey = cacheKey
            )
        )
    }

    fun buildListCacheKey(identifier: String): String {
        return "${RedisConstants.ENTITY_CACHE_BY_ID}$identifier"
    }

    fun getListCache(identifier: String): List<ENTITY>? {
        return this.listCacheStore[buildListCacheKey(identifier)]
    }

    fun updateListCache(
        identifier: String,
        entities: List<ENTITY>,
        expirationInMs: Long = randomHourExpirationInMs(8, 12)
    ) {
        val cacheKey = buildListCacheKey(identifier)
        val exists = this.listCacheStore.containsKey(cacheKey)

        this.listCacheStore.set(cacheKey, entities, expirationInMs)


        this.eventPublisher.publishEvent(
            if (exists) {
                EntityListCacheCreatedEvent(
                    entityClass = this.entityClass,
                    cacheKey = cacheKey
                )
            } else {
                EntityListCacheUpdatedEvent(
                    entityClass = this.entityClass,
                    cacheKey = cacheKey
                )
            }
        )
    }

    fun removeListCache(identifier: String) {
        val cacheKey = buildListCacheKey(identifier)

        this.listCacheStore.remove(cacheKey)

        this.eventPublisher.publishEvent(
            EntityListCacheDeletedEvent(
                entityClass = this.entityClass,
                cacheKey = cacheKey
            )
        )
    }

    fun randomHourExpirationInMs(startHour: Int, endHour: Int): Long {
        return ((startHour * 3600000L)..(endHour * 3600000L)).random()
    }

    override suspend fun getByIdOrNull(
        id: Long?
    ): ENTITY? {
        if (id == null) {
            return null
        }

        val cacheKey = buildCacheKey(id)

        return this.cacheStore[cacheKey]
            ?: super.getByIdOrNull(id).also { entity ->
                entity?.let {
                    this.cacheStore.set(
                        cacheKey,
                        it,
                        randomHourExpirationInMs(8, 12)
                    )

                    this.eventPublisher.publishEvent(
                        EntityCacheCreatedEvent(
                            entityId = it.id,
                            entityClass = it::class,
                            cacheKey = cacheKey
                        )
                    )
                }
            }
    }
}
package com.lovelycatv.crystalframework.cache.service

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.service.BaseService
import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface CachedBaseService<REPOSITORY: R2dbcRepository<ENTITY, Long>, ENTITY: BaseEntity> :
    BaseService<REPOSITORY, ENTITY> {
    val cacheStore: ExpiringKVStore<String, ENTITY>
    val listCacheStore: ExpiringKVStore<String, List<ENTITY>>

    fun buildCacheKey(entityId: Long): String {
        return "${RedisConstants.ENTITY_CACHE_BY_ID}$entityId"
    }

    fun buildCacheKey(entity: ENTITY): String {
        return this.buildCacheKey(entity.id)
    }

    fun updateCache(entityId: Long, entity: ENTITY, expirationInMs: Long = randomHourExpirationInMs(8, 12)) {
        this.cacheStore.set(buildCacheKey(entityId), entity, expirationInMs)
    }

    fun updateCache(entity: ENTITY, expirationInMs: Long = randomHourExpirationInMs(8, 12)) {
        this.updateCache(entity.id, entity, expirationInMs)
    }

    fun removeCache(entityId: Long) {
        this.cacheStore.remove(buildCacheKey(entityId))
    }

    fun removeCache(entity: ENTITY) {
        this.cacheStore.remove(buildCacheKey(entity.id))
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
        this.listCacheStore.set(buildListCacheKey(identifier), entities, expirationInMs)
    }

    fun removeListCache(identifier: String) {
        this.cacheStore.remove(buildListCacheKey(identifier))
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
            ?.also {
                println("cache hit: $cacheKey")
            }
            ?: super.getByIdOrNull(id).also { entity ->
                entity?.let {
                    this.cacheStore.set(
                        cacheKey,
                        it,
                        randomHourExpirationInMs(8, 12)
                    )
                    println("cache ready for: $cacheKey")
                }
            }
    }
}
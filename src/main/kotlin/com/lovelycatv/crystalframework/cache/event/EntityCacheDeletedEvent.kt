package com.lovelycatv.crystalframework.cache.event

import com.lovelycatv.crystalframework.cache.types.EntityCacheEventType
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import kotlin.reflect.KClass

data class EntityCacheDeletedEvent(
    override val entityId: Long,
    override val entityClass: KClass<out BaseEntity>,
    override val cacheKey: String,
) : SingleEntityCacheEvent {
    override val eventType: EntityCacheEventType get() = EntityCacheEventType.DELETED
}

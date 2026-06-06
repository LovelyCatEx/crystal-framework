package com.lovelycatv.crystalframework.shared.event

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.EntityCacheEventType
import kotlin.reflect.KClass

data class EntityListCacheCreatedEvent(
    override val entityClass: KClass<out BaseEntity>,
    override val cacheKey: String,
) : EntityListCacheEvent {
    override val eventType: EntityCacheEventType get() = EntityCacheEventType.UPDATED
}

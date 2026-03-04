package com.lovelycatv.crystalframework.cache.event

import com.lovelycatv.crystalframework.cache.types.EntityCacheEventType
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import kotlin.reflect.KClass

interface EntityCacheEvent {
    val entityClass: KClass<out BaseEntity>
    val cacheKey: String
    val eventType: EntityCacheEventType
}
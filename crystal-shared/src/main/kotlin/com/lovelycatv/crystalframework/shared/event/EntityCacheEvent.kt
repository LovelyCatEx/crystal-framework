package com.lovelycatv.crystalframework.shared.event

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.EntityCacheEventType
import kotlin.reflect.KClass

interface EntityCacheEvent {
    val entityClass: KClass<out BaseEntity>
    val cacheKey: String
    val eventType: EntityCacheEventType
}
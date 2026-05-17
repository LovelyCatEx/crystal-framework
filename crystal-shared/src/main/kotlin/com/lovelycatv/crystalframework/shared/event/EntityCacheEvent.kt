package com.lovelycatv.crystalframework.shared.event

import com.lovelycatv.crystalframework.shared.types.EntityCacheEventType
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import kotlin.reflect.KClass

interface EntityCacheEvent {
    val entityClass: KClass<out BaseEntity>
    val cacheKey: String
    val eventType: EntityCacheEventType
}
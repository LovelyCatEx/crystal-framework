package com.lovelycatv.crystalframework.cache.event

interface SingleEntityCacheEvent : EntityCacheEvent {
    val entityId: Long
}
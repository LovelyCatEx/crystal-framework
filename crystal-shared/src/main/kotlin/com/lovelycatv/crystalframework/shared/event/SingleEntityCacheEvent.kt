package com.lovelycatv.crystalframework.shared.event

interface SingleEntityCacheEvent : EntityCacheEvent {
    val entityId: Long
}
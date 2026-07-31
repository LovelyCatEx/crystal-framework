package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity

interface StorageProviderRouter {
    suspend fun get(context: RoutingContext): StorageProviderEntity

    fun invalidateCache()
}
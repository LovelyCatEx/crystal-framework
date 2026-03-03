package com.lovelycatv.crystalframework.cache.store

import com.lovelycatv.vertex.cache.store.ExpiringKVStore

class ServiceCacheStore(
    val delegate: ExpiringKVStore<String, Any>
) : ExpiringKVStore<String, Any> by delegate
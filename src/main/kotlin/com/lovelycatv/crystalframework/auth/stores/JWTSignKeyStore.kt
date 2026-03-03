package com.lovelycatv.crystalframework.auth.stores

import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.vertex.cache.store.ExpiringKVStore

class JWTSignKeyStore(
    val store: ExpiringKVStore<String, String>,
    val keyGenerator: () -> String,
) {
    fun getSignKey(): String {
        return store[RedisConstants.JWT_SIGN_KEY]
            ?: keyGenerator.invoke()
                .also { setSignKey(it) }
    }

    fun setSignKey(signKey: String) {
        store[RedisConstants.JWT_SIGN_KEY] = signKey
    }
}
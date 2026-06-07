package com.lovelycatv.crystalframework.auth.stores

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.runBlocking

class JWTSignKeyStore(
    val store: ReactiveExpiringKVStore<String, String>,
    val keyGenerator: () -> String,
) {
    /**
     * Called from non-suspend Spring Security / WebFlux SPIs, so the reactive cache read is
     * bridged with [runBlocking].
     */
    fun getSignKey(): String = runBlocking {
        store.get(RedisConstants.JWT_SIGN_KEY)
            ?: keyGenerator.invoke().also { setSignKey(it) }
    }

    fun setSignKey(signKey: String): Unit = runBlocking {
        store.set(RedisConstants.JWT_SIGN_KEY, signKey)
    }
}

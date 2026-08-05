package com.lovelycatv.crystalframework.shared.service.redis

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReactiveRedisServiceTest(
    @Autowired private val reactiveRedisService: ReactiveRedisService,
) : CrystalFrameworkApplicationTests() {
    @Test
    fun compareAndDeleteRemovesMatchingValue() = runBlocking {
        val key = testKey()
        try {
            reactiveRedisService.set(key, "expected").awaitFirstOrNull()

            assertTrue(reactiveRedisService.compareAndDelete(key, "expected").awaitFirstOrNull() == true)
            assertNull(reactiveRedisService.get<String>(key).awaitFirstOrNull())
        } finally {
            reactiveRedisService.removeKey(key).awaitFirstOrNull()
        }
    }

    @Test
    fun compareAndDeleteRetainsDifferentValue() = runBlocking {
        val key = testKey()
        try {
            reactiveRedisService.set(key, "current").awaitFirstOrNull()

            assertFalse(reactiveRedisService.compareAndDelete(key, "stale").awaitFirstOrNull() == true)
            assertEquals("current", reactiveRedisService.get<String>(key).awaitFirstOrNull())
        } finally {
            reactiveRedisService.removeKey(key).awaitFirstOrNull()
        }
    }

    @Test
    fun compareAndExpireRefreshesMatchingValue() = runBlocking {
        val key = testKey()
        try {
            reactiveRedisService.set(key, "expected").awaitFirstOrNull()

            assertTrue(
                reactiveRedisService.compareAndExpire(key, "expected", Duration.ofSeconds(30)).awaitFirstOrNull() == true,
            )
            assertEquals("expected", reactiveRedisService.get<String>(key).awaitFirstOrNull())
        } finally {
            reactiveRedisService.removeKey(key).awaitFirstOrNull()
        }
    }

    @Test
    fun compareAndExpireRejectsDifferentValue() = runBlocking {
        val key = testKey()
        try {
            reactiveRedisService.set(key, "current").awaitFirstOrNull()

            assertFalse(
                reactiveRedisService.compareAndExpire(key, "stale", Duration.ofSeconds(30)).awaitFirstOrNull() == true,
            )
            assertEquals("current", reactiveRedisService.get<String>(key).awaitFirstOrNull())
        } finally {
            reactiveRedisService.removeKey(key).awaitFirstOrNull()
        }
    }

    @Test
    fun compareAndDeleteReturnsFalseForMissingKey() = runBlocking {
        val key = testKey()
        try {
            assertFalse(reactiveRedisService.compareAndDelete(key, "missing").awaitFirstOrNull() == true)
        } finally {
            reactiveRedisService.removeKey(key).awaitFirstOrNull()
        }
    }

    private fun testKey(): String = "test:redis:compare-and-delete:${UUID.randomUUID()}"
}

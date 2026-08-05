package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.time.Duration
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

/**
 * Acquire a distributed lock on [lockKey] via SETNX, renew it while [block] runs, then release.
 * Uses a per-call token to prevent renewing or releasing someone else's lock.
 *
 * If the lock is currently held, throws [BusinessException] with [busyMessage] (no wait/retry —
 * this is designed for "one operation at a time" hot spots, not queue-serialization). If renewal
 * fails, the current coroutine scope is cancelled so the operation cannot continue without the lock.
 */
suspend fun <T> ReactiveRedisService.withDistributedLock(
    lockKey: String,
    ttl: Duration = Duration.ofSeconds(10),
    busyMessage: String = "operation is being processed, please retry later",
    block: suspend () -> T,
): T = coroutineScope {
    val token = UUID.randomUUID().toString()
    val acquired = this@withDistributedLock.setIfAbsent(lockKey, token, ttl).awaitFirstOrNull() ?: false

    if (!acquired) {
        throw BusinessException(busyMessage)
    }

    val renewalInterval = ttl.dividedBy(3).coerceAtLeast(Duration.ofMillis(1))
    val renewalJob = launch {
        while (isActive) {
            delay(renewalInterval.toMillis().milliseconds)
            val renewed = runCatching {
                this@withDistributedLock.compareAndExpire(lockKey, token, ttl).awaitFirstOrNull() == true
            }.getOrDefault(false)
            if (!renewed) {
                this@coroutineScope.cancel()
            }
        }
    }

    try {
        block()
    } finally {
        renewalJob.cancelAndJoin()
        runCatching {
            this@withDistributedLock.compareAndDelete(lockKey, token).awaitFirstOrNull()
        }
    }
}

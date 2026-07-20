package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.time.Duration
import java.util.UUID

/**
 * Acquire a distributed lock on [lockKey] via SETNX, run [block], then release.
 * Uses a per-call token to prevent releasing someone else's lock if [block] exceeds [ttl].
 *
 * If the lock is currently held, throws [BusinessException] with [busyMessage] (no wait/retry —
 * this is designed for "one operation at a time" hot spots, not queue-serialization).
 */
suspend fun <T> ReactiveRedisService.withDistributedLock(
    lockKey: String,
    ttl: Duration = Duration.ofSeconds(10),
    busyMessage: String = "operation is being processed, please retry later",
    block: suspend () -> T,
): T {
    val token = UUID.randomUUID().toString()
    val acquired = this.setIfAbsent(lockKey, token, ttl).awaitFirstOrNull() ?: false

    if (!acquired) {
        throw BusinessException(busyMessage)
    }

    return try {
        block()
    } finally {
        // Best-effort release with token verification: don't remove another owner's lock
        // if [ttl] elapsed and someone else acquired [lockKey] before we ran `finally`.
        val current = this.get<String>(lockKey).awaitFirstOrNull()
        if (current == token) {
            this.removeKey(lockKey).awaitFirstOrNull()
        }
    }
}

/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.store

/**
 * Fully non-blocking key-value cache contract used across the framework.
 *
 * Replaces the synchronous `com.lovelycatv.vertex.cache.store.ExpiringKVStore`: every operation
 * is a `suspend` function backed by reactive Redis, so callers never block a reactor event-loop
 * thread. Non-suspend boundaries (Spring SPIs, @Async listeners) must bridge with `runBlocking`.
 */
interface ReactiveExpiringKVStore<K : Any, V> {
    suspend fun get(key: K): V?

    suspend fun set(key: K, value: V)

    suspend fun set(key: K, value: V, expirationInMs: Long)

    suspend fun remove(key: K): V?

    suspend fun containsKey(key: K): Boolean
}
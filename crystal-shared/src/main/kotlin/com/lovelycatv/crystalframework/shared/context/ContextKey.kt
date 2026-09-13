/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.context

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import reactor.util.context.Context
import reactor.util.context.ContextView

abstract class ContextKey<T : Any> {
    protected abstract val key: Any

    fun install(value: T): (Context) -> Context = { ctx -> ctx.put(key, value) }

    fun from(ctx: ContextView): T? {
        @Suppress("UNCHECKED_CAST")
        return if (ctx.hasKey(key)) ctx.get(key) else null
    }

    suspend fun current(): T? {
        val reactor = currentCoroutineContext()[ReactorContext]?.context ?: return null
        @Suppress("UNCHECKED_CAST")
        return if (reactor.hasKey(key)) reactor.get(key) else null
    }
}
/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
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
/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.utils.reactor

import reactor.core.publisher.Mono
import reactor.util.context.Context

class ReactorExtensions private constructor()

/**
 * A safe version of [Mono.contextWrite] that merges the provided context entries
 * into the existing context instead of replacing it.
 *
 * Use this instead of the raw `contextWrite { newContext }` pattern to avoid
 * accidentally discarding entries written by upstream filters.
 */
fun <T : Any> Mono<T>.contextMerge(contextToMerge: Context): Mono<T> {
    return this.contextWrite { existing ->
        existing.putAll(contextToMerge.readOnly())
    }
}

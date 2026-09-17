/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.utils.reactor

import co.elastic.apm.api.Transaction
import com.lovelycatv.crystalframework.shared.config.observability.ApmParentSpan
import com.lovelycatv.crystalframework.shared.config.observability.ApmParentTransaction
import com.lovelycatv.crystalframework.shared.config.observability.ApmSpanConstants
import com.lovelycatv.crystalframework.shared.config.observability.DistributedTransactionLabel
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import reactor.util.context.ContextView
import kotlin.coroutines.coroutineContext

class DistributedTransactionExtensions private constructor()

private fun ContextView.currentDistributedTransaction(): Transaction? {
    if (!hasKey(ApmParentTransaction::class.java)) return null
    return get<ApmParentTransaction>(ApmParentTransaction::class.java).transaction
}

fun <T : Any> Mono<T>.withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
): Mono<T> {
    val source = this
    return Mono.deferContextual { context ->
        val parentTransaction = context.currentDistributedTransaction()
        if (parentTransaction != null) {
            val span = parentTransaction
                .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, name)
                .setName(name)
            source
                .contextWrite { it.put(ApmParentSpan::class.java, ApmParentSpan(span)) }
                .doFinally { span.end() }
        } else {
            source.contextWrite { c ->
                c.put(DistributedTransactionLabel::class.java, DistributedTransactionLabel(name, labels))
                    .put(ApmParentTransaction::class.java, ApmParentTransaction())
            }
        }
    }
}

fun <T : Any> Flux<T>.withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
): Flux<T> {
    val source = this
    return Flux.deferContextual { context ->
        val parentTransaction = context.currentDistributedTransaction()
        if (parentTransaction != null) {
            val span = parentTransaction
                .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, name)
                .setName(name)
            source
                .contextWrite { it.put(ApmParentSpan::class.java, ApmParentSpan(span)) }
                .doFinally { span.end() }
        } else {
            source.contextWrite { c ->
                c.put(DistributedTransactionLabel::class.java, DistributedTransactionLabel(name, labels))
                    .put(ApmParentTransaction::class.java, ApmParentTransaction())
            }
        }
    }
}

suspend fun <T> withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
    block: suspend () -> T,
): T {
    val current = coroutineContext[ReactorContext]?.context ?: Context.empty()

    val parentTransaction = current.currentDistributedTransaction()
    if (parentTransaction != null) {
        val span = parentTransaction
            .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, name)
            .setName(name)
        val next = current.put(ApmParentSpan::class.java, ApmParentSpan(span))
        return try {
            withContext(ReactorContext(next)) { block() }
        } finally {
            span.end()
        }
    }

    val next = current
        .put(DistributedTransactionLabel::class.java, DistributedTransactionLabel(name, labels))
        .put(ApmParentTransaction::class.java, ApmParentTransaction())
    return withContext(ReactorContext(next)) { block() }
}

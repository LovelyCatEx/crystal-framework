package com.lovelycatv.crystalframework.shared.utils.reactor

import com.lovelycatv.crystalframework.shared.config.observability.DistributedTransactionLabel
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.withContext
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import kotlin.coroutines.coroutineContext

class DistributedTransactionExtensions private constructor()

fun <T : Any> Mono<T>.withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
): Mono<T> = this.contextWrite { context ->
    context.put(DistributedTransactionLabel::class.java, DistributedTransactionLabel(name, labels))
}

fun <T : Any> Flux<T>.withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
): Flux<T> = this.contextWrite { context ->
    context.put(DistributedTransactionLabel::class.java, DistributedTransactionLabel(name, labels))
}

suspend fun <T> withDistributedTransactionName(
    name: String,
    labels: Map<String, String> = emptyMap(),
    block: suspend () -> T,
): T {
    val current = coroutineContext[ReactorContext]?.context ?: Context.empty()
    val next = current.put(
        DistributedTransactionLabel::class.java,
        DistributedTransactionLabel(name, labels),
    )
    return withContext(ReactorContext(next)) { block() }
}

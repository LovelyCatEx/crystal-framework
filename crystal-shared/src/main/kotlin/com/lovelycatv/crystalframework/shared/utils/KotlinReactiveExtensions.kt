package com.lovelycatv.crystalframework.shared.utils

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withTimeoutOrNull
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class KotlinReactiveExtensions private constructor()

suspend fun <T : Any> Flux<T>.awaitListWithTimeout(
    timeout: Duration = Duration.ofMillis(10000)
): List<T> {
    val disposable = subscribe()
    return try {
        val result = withTimeoutOrNull(timeout.toMillis().milliseconds) {
            subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .collectList()
                .awaitSingle()
        }
        result ?: emptyList()
    } finally {
        disposable.dispose()
    }
}

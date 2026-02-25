package com.lovelycatv.template.springboot.shared.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withTimeoutOrNull
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.TimeoutException

class KotlinReactiveExtensions private constructor()

suspend fun <T: Any> Flux<T>.awaitListWithTimeout(
    timeout: Duration = Duration.ofMillis(1000)
): List<T> {
    return try {
        // !!! IMPORTANT !!!
        val disposable = this.subscribe()

        val result: List<T>? = withTimeoutOrNull(timeout.toMillis()) {
            this@awaitListWithTimeout
                .subscribeOn(Schedulers.boundedElastic())
                .collectList()
                .awaitSingleOrNull()
        }

        return (result ?: emptyList()).also {
            disposable.dispose()
        }
    } catch (_: TimeoutException) {
        println("awaitListWithTimeout() function timeout: $timeout ms")
        emptyList()
    }
}

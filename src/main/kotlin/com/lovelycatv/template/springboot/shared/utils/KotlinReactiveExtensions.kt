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
        emptyList()
    }
}

suspend fun <T: Any> Flux<T>.collectListSafe(): List<T> = coroutineScope {
    val results = mutableListOf<T>()
    val done = CompletableDeferred<Unit>()

    this@collectListSafe.subscribe(
        { results.add(it) },             // onNext
        { e -> done.completeExceptionally(e) },  // onError
        { done.complete(Unit) }          // onComplete
    )

    done.await()  // 协程挂起，等待 Flux 完成，不阻塞线程
    results
}


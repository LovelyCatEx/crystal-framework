package com.lovelycatv.template.springboot.shared.utils

import kotlinx.coroutines.reactive.awaitFirstOrNull
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.TimeoutException

class KotlinReactiveExtensions private constructor()

suspend fun <T: Any> Flux<T>.awaitListWithTimeout(
    timeout: Duration = Duration.ofSeconds(10)
): List<T> {
    return try {
        this.collectList()
            .timeout(timeout)
            .awaitFirstOrNull() ?: listOf()
    } catch (_: TimeoutException) {
        emptyList()
    }
}

package com.lovelycatv.crystalframework.shared.utils

import kotlinx.coroutines.reactive.awaitSingle
import reactor.core.publisher.Flux
import java.time.Duration

class KotlinReactiveExtensions private constructor()

suspend fun <T : Any> Flux<T>.awaitListWithTimeout(
    timeout: Duration = Duration.ofMillis(1000)
): List<T> {
    return collectList()
        .timeout(timeout)
        .awaitSingle()
}

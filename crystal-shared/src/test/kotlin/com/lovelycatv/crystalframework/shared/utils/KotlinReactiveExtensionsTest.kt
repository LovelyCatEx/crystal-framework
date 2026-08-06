package com.lovelycatv.crystalframework.shared.utils

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureNanoTime
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

class KotlinReactiveExtensionsTest {

    @Test
    fun `collects values with one subscription`() = runBlocking {
        val subscriptions = AtomicInteger()
        val result = Flux.defer {
            subscriptions.incrementAndGet()
            Flux.just(1, 2, 3)
        }.awaitListWithTimeout()

        assertEquals(listOf(1, 2, 3), result)
        assertEquals(1, subscriptions.get())
    }

    @Test
    fun `returns an empty list for an empty flux`() = runBlocking {
        assertEquals(emptyList<Int>(), Flux.empty<Int>().awaitListWithTimeout())
    }

    @Test
    fun `propagates timeout`() {
        assertFailsWith<TimeoutException> {
            runBlocking {
                Flux.never<Int>().awaitListWithTimeout(Duration.ofMillis(100))
            }
        }
    }

    @Test
    fun `propagates source failure`() {
        val failure = IllegalStateException("source failed")

        val thrown = assertFailsWith<IllegalStateException> {
            runBlocking {
                Flux.error<Int>(failure).awaitListWithTimeout()
            }
        }

        assertEquals(IllegalStateException::class.java, thrown::class.java)
        assertEquals("source failed", thrown.message)
    }

    @Test
    fun `original implementation subscribes twice`() = runBlocking {
        val subscriptions = AtomicInteger()
        val result = Flux.defer {
            subscriptions.incrementAndGet()
            Flux.just(1, 2, 3)
        }.originalAwaitListWithTimeout()

        assertEquals(listOf(1, 2, 3), result)
        assertEquals(2, subscriptions.get())
    }

    @Test
    fun `compares fixed implementation with original implementation`() = runBlocking {
        val warmupIterations = 100
        val measuredIterations = 1_000
        val expectedValues = listOf(1, 2, 3)

        repeat(warmupIterations) {
            Flux.just(1, 2, 3).awaitListWithTimeout()
            Flux.just(1, 2, 3).originalAwaitListWithTimeout()
        }

        var fixedResult: List<Int> = emptyList()
        val fixedElapsedNanos = measureNanoTime {
            repeat(measuredIterations) {
                fixedResult = Flux.just(1, 2, 3).awaitListWithTimeout()
            }
        }

        var originalResult: List<Int> = emptyList()
        val originalElapsedNanos = measureNanoTime {
            repeat(measuredIterations) {
                originalResult = Flux.just(1, 2, 3).originalAwaitListWithTimeout()
            }
        }

        assertEquals(expectedValues, fixedResult)
        assertEquals(expectedValues, originalResult)
        assertTrue(fixedElapsedNanos > 0)
        assertTrue(originalElapsedNanos > 0)

        val fixedAverageNanos = fixedElapsedNanos / measuredIterations
        val originalAverageNanos = originalElapsedNanos / measuredIterations
        val fixedToOriginalRatio = fixedElapsedNanos.toDouble() / originalElapsedNanos
        println(
            "awaitListWithTimeout benchmark: " +
                "fixed=${fixedAverageNanos}ns/op, " +
                "original=${originalAverageNanos}ns/op, " +
                "fixed/original=${"%.3f".format(fixedToOriginalRatio)}"
        )
    }

    private suspend fun <T : Any> Flux<T>.originalAwaitListWithTimeout(
        timeout: Duration = Duration.ofMillis(1000)
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
}

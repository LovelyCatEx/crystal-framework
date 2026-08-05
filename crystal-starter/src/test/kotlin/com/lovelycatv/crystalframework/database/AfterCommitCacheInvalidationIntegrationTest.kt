package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.utils.runAfterCommitOrNow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression guard for M-10: [CachedBaseService][com.lovelycatv.crystalframework.shared.service.CachedBaseService]'s
 * evict-around write brackets the action with an immediate pre-eviction and a deferred post-eviction.
 * The post-eviction is wired through [runAfterCommitOrNow] so it fires only after the surrounding
 * reactive transaction commits — closing the window where a concurrent read could rehydrate the
 * pre-write row before commit, and skipping entirely on rollback.
 *
 * These tests exercise the [runAfterCommitOrNow] primitive directly (no Service required) against the
 * real reactive transaction manager: they prove the callback is (a) deferred until after commit inside
 * a transaction, and (b) run inline when there is no active transaction.
 */
class AfterCommitCacheInvalidationIntegrationTest : CrystalFrameworkApplicationTests() {

    @Test
    fun callbackRunsOnlyAfterCommit() {
        val hookRan = AtomicInteger(0)
        // Observed while still inside the transactional body — must still be 0 there.
        val observedInsideTx = AtomicInteger(-1)

        runBlocking {
            transactionalOperator.execute {
                mono {
                    runAfterCommitOrNow { hookRan.incrementAndGet() }
                    // At this point the transaction has not committed yet.
                    observedInsideTx.set(hookRan.get())
                }
            }.then().awaitSingleOrNull()
        }

        assertEquals(0, observedInsideTx.get(), "post-commit hook must NOT run before the transaction commits")
        assertEquals(1, hookRan.get(), "post-commit hook must run exactly once after commit")
    }

    @Test
    fun callbackRunsInlineWithoutTransaction() {
        val hookRan = AtomicInteger(0)

        runBlocking {
            runAfterCommitOrNow { hookRan.incrementAndGet() }
            // No active transaction: the hook must have already run inline by now.
            assertEquals(1, hookRan.get(), "without a transaction the hook must run inline immediately")
        }

        assertTrue(hookRan.get() == 1, "hook must run exactly once on the non-transactional path")
    }
}

package com.lovelycatv.crystalframework.shared.utils

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.transaction.NoTransactionException
import org.springframework.transaction.reactive.TransactionSynchronization
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono

class TransactionExtensions private constructor()

/**
 * Runs [action] right after the current reactive transaction commits. When no reactive transaction
 * is active on the current Reactor context (non-transactional call path), [action] runs immediately
 * and inline instead.
 *
 * Because it is wired as an [TransactionSynchronization.afterCommit] hook, [action] never fires when
 * the surrounding transaction rolls back — the caller can therefore safely defer work (e.g. a second
 * cache invalidation) that must only take effect once the committed state is visible, and must NOT
 * run against data the rollback has reverted.
 */
suspend fun runAfterCommitOrNow(action: suspend () -> Unit) {
    val deferred = TransactionSynchronizationManager.forCurrentTransaction()
        .map { manager ->
            if (manager.isSynchronizationActive) {
                manager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCommit(): Mono<Void> = mono { action() }.then()
                })
                true
            } else {
                false
            }
        }
        .onErrorResume(NoTransactionException::class.java) {
            Mono.just(false)
        }
        .awaitSingleOrNull() ?: false

    if (!deferred) {
        action()
    }
}

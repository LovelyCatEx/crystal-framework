package com.lovelycatv.crystalframework.utils

import com.lovelycatv.crystalframework.exception.TestTransactionalRollbackException
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

class CrystalFrameworkTestExtensions private constructor()

fun testWithTransactionalRollback(
    actionName: String,
    txOperator: TransactionalOperator,
    action: suspend () -> Unit
) {
    runBlocking {
        val result = runCatching {
            txOperator.executeAndAwait {
                println()
                println(">>> [TX] ========= $actionName =========")

                action.invoke()

                println("<<< [TX] ========= $actionName =========")
                println()

                throw TestTransactionalRollbackException()
            }
        }

        val exception = result.exceptionOrNull()
        if (exception != null && result.exceptionOrNull() !is TestTransactionalRollbackException) {
            throw exception
        }
    }
}
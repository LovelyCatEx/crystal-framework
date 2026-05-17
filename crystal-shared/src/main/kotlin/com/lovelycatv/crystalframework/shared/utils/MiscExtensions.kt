package com.lovelycatv.crystalframework.shared.utils

class MiscExtensions private constructor()

fun <R> analyzeExecutionTime(name: String? = null, block: () -> R): R {
    val s = System.currentTimeMillis()
    return block.invoke().also {
        println("[$name] Execution time: ${System.currentTimeMillis() - s} ms")
    }
}

suspend fun <R> analyzeExecutionTimeSuspend(name: String? = null, block: suspend () -> R): R {
    val s = System.currentTimeMillis()
    return block.invoke().also {
        println("[$name] Execution time: ${System.currentTimeMillis() - s} ms")
    }
}
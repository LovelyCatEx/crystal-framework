/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
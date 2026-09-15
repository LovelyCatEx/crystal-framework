/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.types

/**
 * The outcome of one tool call the model asked for, surfaced to the caller so the UI can show the
 * tool's name, its arguments and what it produced.
 */
data class AiToolCallResult(
    val toolName: String,
    val arguments: Map<String, Any?>?,
    val result: String,
)

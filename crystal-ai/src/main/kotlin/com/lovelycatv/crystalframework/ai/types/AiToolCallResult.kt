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

package com.lovelycatv.crystalframework.ai.tool

import com.lovelycatv.vertex.ai.llm.tool.ToolDeclaration

/**
 * A callable tool exposed to an AI model.
 *
 * [declaration] is the JSON-schema description sent to the provider; [execute] runs the tool with
 * the arguments the model produced and returns a single text result to feed back to the model.
 */
interface AiTool {
    val declaration: ToolDeclaration

    fun execute(arguments: Map<String, Any?>): String
}

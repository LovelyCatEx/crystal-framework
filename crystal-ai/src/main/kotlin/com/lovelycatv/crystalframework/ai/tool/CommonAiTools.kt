package com.lovelycatv.crystalframework.ai.tool

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.vertex.ai.llm.tool.ToolDeclaration
import com.lovelycatv.vertex.ai.llm.tool.parameter.PrimitiveToolParameter
import com.lovelycatv.vertex.ai.llm.tool.parameter.ToolParameter
import com.lovelycatv.vertex.ai.llm.tool.parameter.ToolParameterType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * A small set of general-purpose tools an AI model can call.
 *
 * Each tool couples its [AiTool.declaration] (sent to the provider) with the executor that runs
 * the arguments the model produced. [declarations] and [execute] are the two hooks a chat loop
 * uses: the former fills `ChatRequest.tools`, the latter turns a returned tool call into a text
 * result to feed back to the model.
 */
object CommonAiTools {
    const val TOOL_GET_CURRENT_TIME = "get_current_time"
    const val TOOL_GET_CURRENT_DATE = "get_current_date"
    const val TOOL_CALCULATE = "calculate"
    const val TOOL_RANDOM_INTEGER = "random_integer"

    private const val OPERATION_ADD = "add"
    private const val OPERATION_SUBTRACT = "subtract"
    private const val OPERATION_MULTIPLY = "multiply"
    private const val OPERATION_DIVIDE = "divide"

    private const val PARAM_OPERATION = "operation"
    private const val PARAM_A = "a"
    private const val PARAM_B = "b"
    private const val PARAM_C = "c"
    private const val PARAM_D = "d"
    private const val PARAM_MIN = "min"
    private const val PARAM_MAX = "max"
    private const val MIN_OPERAND_COUNT = 2

    private const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS"
    private const val DATE_PATTERN = "yyyy-MM-dd"
    private const val NANOS_PER_SECOND = 1_000_000_000L

    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(ZoneId.systemDefault())
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN)

    private val ALL: List<AiTool> = listOf(
        tool(TOOL_GET_CURRENT_TIME, "Get the current date and time with millisecond precision and epoch timestamps", emptyList()) {
            currentTime()
        },
        tool(TOOL_GET_CURRENT_DATE, "Get the current date", emptyList()) {
            LocalDate.now().format(DATE_FORMATTER)
        },
        tool(
            TOOL_CALCULATE,
            "Perform a basic arithmetic operation on two to four numbers",
            listOf(
                PrimitiveToolParameter(ToolParameterType.STRING, PARAM_OPERATION, "One of: add, subtract, multiply, divide", true),
                PrimitiveToolParameter(ToolParameterType.NUMBER, PARAM_A, "First operand", true),
                PrimitiveToolParameter(ToolParameterType.NUMBER, PARAM_B, "Second operand", true),
                PrimitiveToolParameter(ToolParameterType.NUMBER, PARAM_C, "Third operand (optional)", false),
                PrimitiveToolParameter(ToolParameterType.NUMBER, PARAM_D, "Fourth operand (optional)", false),
            ),
        ) { calculate(it) },
        tool(
            TOOL_RANDOM_INTEGER,
            "Generate a random integer between min and max (inclusive)",
            listOf(
                PrimitiveToolParameter(ToolParameterType.INTEGER, PARAM_MIN, "Minimum value (inclusive)", true),
                PrimitiveToolParameter(ToolParameterType.INTEGER, PARAM_MAX, "Maximum value (inclusive)", true),
            ),
        ) { randomInteger(it) },
    )

    val declarations: List<ToolDeclaration> = ALL.map { it.declaration }

    fun execute(name: String, arguments: Map<String, Any?>): String {
        val tool = ALL.firstOrNull { it.declaration.name == name }
            ?: throw BusinessException("Unknown AI tool: $name")
        return tool.execute(arguments)
    }

    private fun currentTime(): String {
        val now = Instant.now()
        val epochNanos = now.epochSecond * NANOS_PER_SECOND + now.nano
        return "${DATE_TIME_FORMATTER.format(now)} | epoch_millis=${now.toEpochMilli()} | epoch_nanos=$epochNanos"
    }

    private fun calculate(arguments: Map<String, Any?>): String {
        val operation = arguments[PARAM_OPERATION] as? String
            ?: throw BusinessException("Missing 'operation' argument")

        val operands = listOf(PARAM_A, PARAM_B, PARAM_C, PARAM_D)
            .mapNotNull { key -> (arguments[key] as? Number)?.toDouble() }

        if (operands.size < MIN_OPERAND_COUNT) {
            throw BusinessException("At least $MIN_OPERAND_COUNT operands are required")
        }

        val result = when (operation) {
            OPERATION_ADD -> operands.sum()
            OPERATION_SUBTRACT -> operands.reduce { acc, value -> acc - value }
            OPERATION_MULTIPLY -> operands.reduce { acc, value -> acc * value }
            OPERATION_DIVIDE -> operands.reduce { acc, value ->
                if (value == 0.0) {
                    throw BusinessException("Division by zero")
                }
                acc / value
            }

            else -> throw BusinessException("Unsupported operation: $operation")
        }
        return result.toString()
    }

    private fun randomInteger(arguments: Map<String, Any?>): String {
        val min = arguments.asInt(PARAM_MIN)
        val max = arguments.asInt(PARAM_MAX)
        if (min > max) {
            throw BusinessException("min must not exceed max")
        }
        return Random.nextInt(min, max + 1).toString()
    }

    private fun Map<String, Any?>.asInt(key: String): Int =
        (this[key] as? Number)?.toInt()
            ?: throw BusinessException("Missing or invalid '$key' argument")

    private fun tool(
        name: String,
        description: String,
        parameters: List<ToolParameter>,
        executor: (Map<String, Any?>) -> String,
    ): AiTool = object : AiTool {
        override val declaration = ToolDeclaration(name, description, parameters)
        override fun execute(arguments: Map<String, Any?>) = executor(arguments)
    }
}

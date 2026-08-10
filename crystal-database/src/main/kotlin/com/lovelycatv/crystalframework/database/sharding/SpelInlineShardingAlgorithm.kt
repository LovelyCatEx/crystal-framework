package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.constants.ShardingConstants
import com.lovelycatv.crystalframework.database.exception.ShardingException
import org.springframework.expression.Expression
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.SimpleEvaluationContext

/**
 * Built-in [ShardingAlgorithm] that resolves the target table from a SpEL expression declared inline
 * in `crystalframework.sharding.rules[].inline-expression`, e.g. `'users_' + (#value % 8)`.
 *
 * This is the config-only counterpart to `algorithm-ref`: instead of registering a bean, the rule
 * carries the "value -> table name" arithmetic as a string, and this algorithm evaluates it. The
 * data source is fixed to the rule's own [dataSourceName]; the expression only computes the table.
 *
 * Security: the expression comes from a config file, so evaluation uses a read-only
 * [SimpleEvaluationContext] exposing only `#value` (the sharding-column value). Type references
 * (`T(...)`), constructors and bean references are unavailable by construction — only data
 * arithmetic and string composition are possible. The [Expression] is compiled once at construction
 * (stateless, thread-safe) to honor the [ShardingAlgorithm] single-shared-instance contract.
 */
class SpelInlineShardingAlgorithm(
    private val dataSourceName: String,
    private val expressionSource: String,
) : ShardingAlgorithm {
    private val expression: Expression = runCatching { PARSER.parseExpression(expressionSource) }
        .getOrElse {
            throw ShardingException("Invalid inline sharding expression '$expressionSource'", it)
        }

    override fun doSharding(logicalTable: String, shardingValue: Any?): ShardingTarget {
        if (shardingValue == null) {
            throw ShardingException(
                "Inline sharding expression '$expressionSource' for table '$logicalTable' cannot " +
                    "route a null sharding-column value",
            )
        }
        val context = SimpleEvaluationContext.forReadOnlyDataBinding().build()
        context.setVariable(ShardingConstants.INLINE_EXPRESSION_VARIABLE, shardingValue)
        val resolved = runCatching { expression.getValue(context) }
            .getOrElse {
                throw ShardingException(
                    "Inline sharding expression '$expressionSource' failed for table " +
                        "'$logicalTable' with value '$shardingValue'",
                    it,
                )
            }
            ?.toString()?.trim()?.lowercase()
            ?: throw ShardingException(
                "Inline sharding expression '$expressionSource' for table '$logicalTable' resolved " +
                    "to a blank table name",
            )
        return ShardingTarget(dataSourceName, resolved)
    }

    private companion object {
        val PARSER = SpelExpressionParser()
    }
}

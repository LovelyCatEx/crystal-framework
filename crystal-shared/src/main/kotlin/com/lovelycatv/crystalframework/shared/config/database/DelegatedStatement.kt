package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Result
import io.r2dbc.spi.Statement
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Deferred-connection delegate Statement for non-sharded queries.
 *
 * Per CLAUDE.md reactive programming rules: cannot block() to acquire connection in createStatement(),
 * must defer to execute() time for async acquisition.
 */
internal class DelegatedStatement(
    private val pool: ConnectionPool,
    private val sql: String,
) : Statement {

    private val actions = mutableListOf<(Statement) -> Unit>()

    override fun add(): Statement = apply {
        actions += { it.add() }
    }

    override fun bind(index: Int, value: Any): Statement = apply {
        actions += { it.bind(index, value) }
    }

    override fun bind(name: String, value: Any): Statement = apply {
        actions += { it.bind(name, value) }
    }

    override fun bindNull(index: Int, type: Class<*>): Statement = apply {
        actions += { it.bindNull(index, type) }
    }

    override fun bindNull(name: String, type: Class<*>): Statement = apply {
        actions += { it.bindNull(name, type) }
    }

    override fun returnGeneratedValues(vararg columns: String): Statement = apply {
        val copy = columns.clone()
        actions += { it.returnGeneratedValues(*copy) }
    }

    override fun fetchSize(rows: Int): Statement = apply {
        actions += { it.fetchSize(rows) }
    }

    override fun execute(): Publisher<out Result> {
        return Flux.usingWhen(
            pool.create(),
            { connection ->
                val realStmt = connection.createStatement(sql)
                actions.forEach { it(realStmt) }
                Flux.from(realStmt.execute())
            },
            { connection -> connection.close() },
            { connection, _ -> connection.close() },
            { connection -> connection.close() }
        )
    }
}

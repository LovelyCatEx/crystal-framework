package com.lovelycatv.crystalframework.shared.config.database

import reactor.util.context.Context
import reactor.util.context.ContextView

/**
 * Reactor Context utilities for setting and reading the data source routing key.
 *
 * Usage in reactive chains:
 * ```
 * repository.findAll()
 *     .contextWrite(R2dbcDataSourceContext.write("shard-01"))
 * ```
 *
 * Or use [writeForTable] to automatically resolve the data source via [R2dbcShardingRouter]:
 * ```
 * repository.findAll()
 *     .contextWrite(R2dbcDataSourceContext.writeForTable("sys_user", router))
 * ```
 */
object R2dbcDataSourceContext {
    const val KEY = "crystal.r2dbc.datasource"

    /**
     * Returns a context modifier that sets the data source name directly.
     */
    fun write(dataSourceName: String): (Context) -> Context = { ctx ->
        ctx.put(KEY, dataSourceName)
    }

    /**
     * Returns a context modifier that resolves the data source name from the table name
     * using the given [router].
     */
    fun writeForTable(tableName: String, router: R2dbcShardingRouter): (Context) -> Context = { ctx ->
        val decision = router.resolve(tableName)
        ctx.put(KEY, decision.dataSourceName)
    }

    /**
     * Reads the current data source name from the context, or returns null if not set.
     */
    fun current(ctx: ContextView): String? = ctx.getOrDefault(KEY, null)
}

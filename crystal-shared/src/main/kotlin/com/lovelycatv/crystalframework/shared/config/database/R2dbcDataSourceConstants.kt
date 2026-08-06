package com.lovelycatv.crystalframework.shared.config.database

object R2dbcDataSourceConstants {
    const val DEFAULT_DATA_SOURCE_NAME = "primary"

    /**
     * Prefix of PostgreSQL positional bind markers (`$1`, `$2`, ...). JSQLParser exposes the marker
     * as a [net.sf.jsqlparser.expression.JdbcParameter] whose name is [POSITIONAL_PARAMETER_PREFIX]
     * concatenated with its 1-based index; the sharding engine matches deferred binds by this name.
     */
    const val POSITIONAL_PARAMETER_PREFIX = "$"
}

package com.lovelycatv.crystalframework.shared.config.database

/**
 * Describes a logical R2DBC data source without creating a connection factory.
 */
data class R2dbcDataSourceDeclaration(
    val name: String,
    val url: String,
    val username: String,
    val password: String,
)

/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database

/**
 * Describes a logical R2DBC data source without creating a connection factory.
 */
data class R2dbcDataSourceDeclaration(
    val name: String,
    val url: String,
    val username: String,
    val password: String,
)

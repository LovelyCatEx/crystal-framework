/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database

import io.r2dbc.spi.ConnectionFactoryOptions

class R2dbcDataSourceRegistry(
    declarations: Collection<R2dbcDataSourceDeclaration>,
) {
    private val declarationsByName = linkedMapOf<String, R2dbcDataSourceDeclaration>()

    init {
        declarations.forEach(::register)
    }

    fun register(declaration: R2dbcDataSourceDeclaration) {
        val name = declaration.name.trim()
        require(name.isNotEmpty()) { "R2dbcDataSourceRegistry: data source name must not be blank" }
        require(declaration.url.isNotBlank()) {
            "R2dbcDataSourceRegistry: data source '$name' URL must not be blank"
        }
        runCatching { ConnectionFactoryOptions.parse(declaration.url) }
            .getOrElse { error ->
                throw IllegalArgumentException(
                    "R2dbcDataSourceRegistry: invalid URL for data source '$name'",
                    error,
                )
            }
        if (declarationsByName.putIfAbsent(name, declaration.copy(name = name)) != null) {
            throw IllegalStateException("R2dbcDataSourceRegistry: duplicate name '$name'")
        }
    }

    fun get(name: String): R2dbcDataSourceDeclaration? {
        return declarationsByName[name.trim()]
    }

    fun require(name: String): R2dbcDataSourceDeclaration {
        return get(name) ?: throw IllegalArgumentException(
            "R2dbcDataSourceRegistry: unknown data source '${name.trim()}'",
        )
    }

    fun declarations(): List<R2dbcDataSourceDeclaration> = declarationsByName.values.toList()
}

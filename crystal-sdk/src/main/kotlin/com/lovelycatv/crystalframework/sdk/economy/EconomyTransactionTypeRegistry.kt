/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy

import com.lovelycatv.crystalframework.sdk.economy.types.EconomyTransactionTypeDeclaration

/**
 * Central registry of all known economy transaction types (built-in enum entries + third-party
 * contributions). Populated at bean-init time from every
 * [com.lovelycatv.crystalframework.sdk.economy.config.EconomyTransactionTypeConfigurer] on the
 * classpath and read at runtime by the wallet engine and admin UIs.
 *
 * Uniqueness is enforced on **both** [EconomyTransactionTypeDeclaration.typeId] and
 * [EconomyTransactionTypeDeclaration.key]; a duplicate on either dimension is a startup-time error.
 */
class EconomyTransactionTypeRegistry {
    private val byTypeId = linkedMapOf<Int, EconomyTransactionTypeDeclaration>()
    private val byKey = linkedMapOf<String, EconomyTransactionTypeDeclaration>()

    fun register(declaration: EconomyTransactionTypeDeclaration) {
        val key = declaration.key.trim()
        if (key.isBlank()) {
            throw IllegalStateException(
                "EconomyTransactionTypeRegistry: blank key for typeId ${declaration.typeId}"
            )
        }
        if ("." in key) {
            throw IllegalStateException(
                "EconomyTransactionTypeRegistry: key '$key' must not contain '.'"
            )
        }

        if (byTypeId.putIfAbsent(declaration.typeId, declaration) != null) {
            throw IllegalStateException(
                "EconomyTransactionTypeRegistry: duplicate typeId ${declaration.typeId}"
            )
        }
        if (byKey.putIfAbsent(key, declaration) != null) {
            byTypeId.remove(declaration.typeId)
            throw IllegalStateException(
                "EconomyTransactionTypeRegistry: duplicate key '$key'"
            )
        }
    }

    fun registers(declarations: Iterable<EconomyTransactionTypeDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun getByTypeId(typeId: Int): EconomyTransactionTypeDeclaration? = byTypeId[typeId]

    fun requireByTypeId(typeId: Int): EconomyTransactionTypeDeclaration =
        byTypeId[typeId] ?: throw IllegalStateException(
            "EconomyTransactionTypeRegistry: no declaration for typeId $typeId"
        )

    fun getByKey(key: String): EconomyTransactionTypeDeclaration? = byKey[key.trim()]

    fun declarations(): List<EconomyTransactionTypeDeclaration> = byTypeId.values.toList()
}

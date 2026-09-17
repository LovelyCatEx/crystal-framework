/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy

import com.lovelycatv.crystalframework.sdk.economy.types.EconomyReferenceTypeDeclaration

/**
 * Central registry of all known economy reference types (built-in enum entries + third-party
 * contributions). Populated at bean-init time from every
 * [com.lovelycatv.crystalframework.sdk.economy.config.EconomyReferenceTypeConfigurer] on the
 * classpath and read at runtime by admin UIs.
 *
 * Uniqueness is enforced on **both** [EconomyReferenceTypeDeclaration.typeId] and
 * [EconomyReferenceTypeDeclaration.key]; a duplicate on either dimension is a startup-time error.
 */
class EconomyReferenceTypeRegistry {
    private val byTypeId = linkedMapOf<Int, EconomyReferenceTypeDeclaration>()
    private val byKey = linkedMapOf<String, EconomyReferenceTypeDeclaration>()

    fun register(declaration: EconomyReferenceTypeDeclaration) {
        val key = declaration.key.trim()
        if (key.isBlank()) {
            throw IllegalStateException(
                "EconomyReferenceTypeRegistry: blank key for typeId ${declaration.typeId}"
            )
        }
        if ("." in key) {
            throw IllegalStateException(
                "EconomyReferenceTypeRegistry: key '$key' must not contain '.'"
            )
        }

        if (byTypeId.putIfAbsent(declaration.typeId, declaration) != null) {
            throw IllegalStateException(
                "EconomyReferenceTypeRegistry: duplicate typeId ${declaration.typeId}"
            )
        }
        if (byKey.putIfAbsent(key, declaration) != null) {
            byTypeId.remove(declaration.typeId)
            throw IllegalStateException(
                "EconomyReferenceTypeRegistry: duplicate key '$key'"
            )
        }
    }

    fun registers(declarations: Iterable<EconomyReferenceTypeDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun getByTypeId(typeId: Int): EconomyReferenceTypeDeclaration? = byTypeId[typeId]

    fun getByKey(key: String): EconomyReferenceTypeDeclaration? = byKey[key.trim()]

    fun declarations(): List<EconomyReferenceTypeDeclaration> = byTypeId.values.toList()
}

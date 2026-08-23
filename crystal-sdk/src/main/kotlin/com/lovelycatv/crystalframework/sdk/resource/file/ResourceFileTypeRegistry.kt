package com.lovelycatv.crystalframework.sdk.resource.file

import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration

/**
 * Central registry of all known file-resource types (built-in enum entries + third-party
 * contributions). Populated at bean-init time from every [com.lovelycatv.crystalframework.sdk.resource.file.config.ResourceFileTypeConfigurer]
 * on the classpath and read at runtime by upload validators, download authorization, and
 * admin UIs.
 *
 * Uniqueness is enforced on **both** [ResourceFileTypeDeclaration.typeId] and
 * [ResourceFileTypeDeclaration.key] — a duplicate on either dimension is a startup-time error.
 * This is intentional: `typeId` collisions would let two contributors clobber each other's
 * records in `file_resources.type`, and `key` collisions would make admin UIs ambiguous.
 * Fail-fast avoids the silent-corruption class of bug.
 */
class ResourceFileTypeRegistry {
    private val byTypeId = linkedMapOf<Int, ResourceFileTypeDeclaration>()
    private val byKey = linkedMapOf<String, ResourceFileTypeDeclaration>()

    fun register(declaration: ResourceFileTypeDeclaration) {
        val key = declaration.key.trim()
        if (key.isBlank()) {
            throw IllegalStateException(
                "ResourceFileTypeRegistry: blank key for typeId ${declaration.typeId}"
            )
        }

        if (byTypeId.putIfAbsent(declaration.typeId, declaration) != null) {
            throw IllegalStateException(
                "ResourceFileTypeRegistry: duplicate typeId ${declaration.typeId}"
            )
        }
        if (byKey.putIfAbsent(key, declaration) != null) {
            byTypeId.remove(declaration.typeId)
            throw IllegalStateException(
                "ResourceFileTypeRegistry: duplicate key '$key'"
            )
        }
    }

    fun registers(declarations: Iterable<ResourceFileTypeDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun getByTypeId(typeId: Int): ResourceFileTypeDeclaration? = byTypeId[typeId]

    fun requireByTypeId(typeId: Int): ResourceFileTypeDeclaration =
        byTypeId[typeId] ?: throw IllegalStateException(
            "ResourceFileTypeRegistry: no declaration for typeId $typeId"
        )

    fun getByKey(key: String): ResourceFileTypeDeclaration? = byKey[key.trim()]

    fun declarations(): List<ResourceFileTypeDeclaration> = byTypeId.values.toList()
}

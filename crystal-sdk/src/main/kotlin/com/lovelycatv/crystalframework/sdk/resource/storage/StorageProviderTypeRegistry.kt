package com.lovelycatv.crystalframework.sdk.resource.storage

import com.lovelycatv.crystalframework.sdk.resource.storage.types.StorageProviderTypeDeclaration

/**
 * Central registry of all known storage-provider types (built-in enum entries + third-party
 * contributions). Populated at bean-init time from every [com.lovelycatv.crystalframework.sdk.resource.storage.config.StorageProviderTypeConfigurer]
 * on the classpath and read at runtime by admin UIs, entity readers, and the file-service manager.
 *
 * Uniqueness is enforced on **both** [StorageProviderTypeDeclaration.typeId] and
 * [StorageProviderTypeDeclaration.key] — a duplicate on either dimension is a startup-time
 * error. This is intentional: `typeId` collisions would let two contributors clobber each
 * other's records in `storage_providers.type`, and `key` collisions would make admin UIs
 * ambiguous. Fail-fast avoids the silent-corruption class of bug.
 */
class StorageProviderTypeRegistry {
    private val byTypeId = linkedMapOf<Int, StorageProviderTypeDeclaration>()
    private val byKey = linkedMapOf<String, StorageProviderTypeDeclaration>()

    fun register(declaration: StorageProviderTypeDeclaration) {
        val key = declaration.key.trim()
        if (key.isBlank()) {
            throw IllegalStateException(
                "StorageProviderTypeRegistry: blank key for typeId ${declaration.typeId}"
            )
        }

        if (byTypeId.putIfAbsent(declaration.typeId, declaration) != null) {
            throw IllegalStateException(
                "StorageProviderTypeRegistry: duplicate typeId ${declaration.typeId}"
            )
        }
        if (byKey.putIfAbsent(key, declaration) != null) {
            byTypeId.remove(declaration.typeId)
            throw IllegalStateException(
                "StorageProviderTypeRegistry: duplicate key '$key'"
            )
        }
    }

    fun registers(declarations: Iterable<StorageProviderTypeDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun getByTypeId(typeId: Int): StorageProviderTypeDeclaration? = byTypeId[typeId]

    fun requireByTypeId(typeId: Int): StorageProviderTypeDeclaration =
        byTypeId[typeId] ?: throw IllegalStateException(
            "StorageProviderTypeRegistry: no declaration for typeId $typeId"
        )

    fun getByKey(key: String): StorageProviderTypeDeclaration? = byKey[key.trim()]

    fun declarations(): List<StorageProviderTypeDeclaration> = byTypeId.values.toList()
}

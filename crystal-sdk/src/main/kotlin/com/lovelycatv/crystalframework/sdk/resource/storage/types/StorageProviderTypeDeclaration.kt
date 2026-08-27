package com.lovelycatv.crystalframework.sdk.resource.storage.types

/**
 * A pluggable storage-provider "type" contributed to [com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry].
 *
 * The framework's built-in providers (local file system, Aliyun OSS, Tencent COS, Volcengine TOS)
 * implement this interface directly on their enum values so they and third-party contributions
 * flow through the same lookup path; [typeId] is what lands in `storage_providers.type`.
 *
 * Third parties must:
 *  - pick a [typeId] outside the framework's reserved range (`>= 1000`, see the registry doc);
 *  - namespace [key] under their vendor id (`"acme.private_s3"`);
 *  - contribute an accompanying `FileResourceServiceFactory` returning the same declaration so
 *    the file-service manager can resolve the runtime service for records carrying this typeId.
 */
interface StorageProviderTypeDeclaration {
    /** Stable integer written to `storage_providers.type`. Built-in: 0..3; third-party: >= 1000. */
    val typeId: Int

    /** Globally-unique string identifier, namespaced (`"builtin.local_file_system"`, `"acme.private_s3"`). */
    val key: String

    /** Human-readable label used by admin UIs. */
    val displayName: String

    /** Long-form description; safe to be empty. */
    val description: String
        get() = ""
}

package com.lovelycatv.crystalframework.resource.types

/**
 * Common contract for every storage provider's credential/config payload (the object deserialized from
 * [com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity.properties]).
 *
 * [basePath] is a shared object-key prefix: [com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService.buildObjectKey]
 * prepends it so every uploaded object lands under it. Cloud providers default it to empty (older provider
 * records have no such field in their JSON); the local provider instead treats it as the filesystem root
 * directory, so its factory deliberately does not feed it back into the key prefix to avoid a doubled path.
 */
interface FileResourceServiceProperties {
    val basePath: String
}

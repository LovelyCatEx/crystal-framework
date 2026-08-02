package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility

/**
 * Central authority for deciding whether a viewer may read a given file resource.
 *
 * The decision is a pure function of three inputs:
 *  1. the resource's [ResourceFileType] → its configured [ResourceVisibility]
 *     (system-admin-configurable via system settings),
 *  2. the resource's own `scope` / `scopeId` ([FileResourceEntity] extends `BaseScopedEntity`),
 *  3. the viewer's [UserAuthentication] (null for anonymous requests).
 *
 * All read-authorization logic lives here so no call site re-implements it.
 */
interface ResourceAccessService {
    /**
     * Resolves the configured visibility policy for [fileType] from the cached system settings.
     */
    fun resolveVisibility(fileType: ResourceFileType): ResourceVisibility

    /**
     * @return true when [viewer] (nullable = anonymous) may read [entity] under its type's policy.
     */
    suspend fun isReadable(entity: FileResourceEntity, viewer: UserAuthentication?): Boolean

    /**
     * Throws [com.lovelycatv.crystalframework.shared.exception.ForbiddenException] when [viewer]
     * may not read [entity]; returns normally otherwise.
     */
    suspend fun assertReadable(entity: FileResourceEntity, viewer: UserAuthentication?)
}

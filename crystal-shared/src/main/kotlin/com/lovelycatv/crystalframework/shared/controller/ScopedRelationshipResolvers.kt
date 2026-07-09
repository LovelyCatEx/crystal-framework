package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity

/**
 * Helpers that walk a child entity up to its **root** `(scope, scopeId)` — used by
 * [StandardDerivedScopedManagerController] subclasses to resolve the tenant ownership of a
 * derived resource so that [ScopedPermissionMatrix]-based checks operate on the actual root
 * tenant, not on any intermediate parent.
 *
 * The tenant-side counterpart is
 * [com.lovelycatv.crystalframework.shared.service.EntityRelationshipCheckService.checkIsRelatedToRootParent],
 * which is boolean-valued (`is this entity under tenant X?`). These helpers instead return the
 * root scope pair, which is what the controller needs to pass into `checkOwnership`.
 */
object ScopedRelationshipResolvers {

    /**
     * Resolve the root `(scope, scopeId)` from a direct scoped parent — the parent itself
     * carries `scope + scopeId`, so one hop is enough.
     *
     * Example: `tenant_dict_item.type_id → tenant_dict_type.scope/scopeId`.
     *
     * Longer chains (grandparent-scoped, etc.) should compose several calls to this helper
     * inside the subclass's `resolveScopeFromEntity` — each hop turns a child id into the next
     * parent id, and the final hop returns the root scoped parent's `(scope, scopeId)`.
     */
    suspend fun <P : BaseScopedEntity> fromScopedParent(
        parentId: Long,
        parentService: CachedBaseService<*, P>,
    ): Pair<ResourceScope, Long> {
        val parent = parentService.getByIdOrNull(parentId)
            ?: throw BusinessException("Parent entity $parentId not found for scope resolution")
        val scope = ResourceScope.getById(parent.scope)
            ?: throw BusinessException("Unknown scope typeId ${parent.scope} on parent $parentId")
        return scope to parent.scopeId
    }
}

package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

/**
 * Resolves the root `(scope, scopeId)` an entity ultimately belongs to.
 *
 * This is the scoped counterpart of [EntityRelationshipCheckService.checkIsRelatedToRootParent]
 * on the tenant side: instead of returning a boolean (does this entity belong to tenant X?),
 * it returns the root scope pair so the caller can hand it to
 * [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController.checkOwnership].
 *
 * Contract:
 *  - **Directly scoped entities** (i.e. `ENTITY : BaseScopedEntity`): the default implementation
 *    on [BaseScopedManagerService] reads `entity.scope / entity.scopeId` — Service impls do
 *    not need to override.
 *  - **Derived / nested entities** (no `scope + scope_id` columns of their own): the Service
 *    impl MUST override this method to walk to the immediate scoped parent and delegate to that
 *    parent Service's [resolveRootScope]. Deep chains compose naturally by recursion.
 *
 * @return the resolved `(scope, scopeId)` pair, or `null` if the entity does not exist.
 */
interface ScopedRelationshipCheckService {
    suspend fun resolveRootScope(id: Long): Pair<ResourceScope, Long>?
}

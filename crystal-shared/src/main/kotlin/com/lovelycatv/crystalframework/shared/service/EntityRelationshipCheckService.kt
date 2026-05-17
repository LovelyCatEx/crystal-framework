package com.lovelycatv.crystalframework.shared.service

interface EntityRelationshipCheckService<CURRENT_ID, PARENT_ID> {
    /**
     * Checks if a single entity is directly related to the specified parent.
     *
     * This is a single-level check: it only verifies the entity's immediate parent matches
     * [parentId]. For checks against an outer scope (e.g. tenant) when the entity may be
     * nested arbitrarily deeply, prefer [checkIsRelatedToRootParent].
     *
     * @param id The entity ID to check
     * @param parentId The immediate parent ID to verify relationship against
     * @return true if the entity's immediate parent is [parentId], false otherwise
     */
    suspend fun checkIsRelated(id: CURRENT_ID, parentId: PARENT_ID): Boolean {
        return this.checkIsRelated(listOf(id), parentId)
    }

    /**
     * Checks if multiple entities are directly related to the specified parent.
     *
     * This is a single-level check; see [checkIsRelated] for details.
     *
     * @param ids Collection of entity IDs to check
     * @param parentId The immediate parent ID to verify relationships against
     * @return true if all entities' immediate parents are [parentId], false otherwise
     */
    suspend fun checkIsRelated(ids: Collection<CURRENT_ID>, parentId: PARENT_ID): Boolean

    /**
     * Checks if a single entity ultimately belongs to [rootParentId] by walking the parent
     * chain up to its root.
     *
     * Use this when the caller only knows the outermost scope (for example, a tenant ID)
     * and the entity may be nested arbitrarily deeply (e.g. tenant -> department ->
     * department member -> ...).
     *
     * The default implementation treats the immediate parent as the root, which is correct
     * for top-level scopes (entities whose direct parent IS the root). Services whose
     * entities are nested deeper must override [checkIsRelatedToRootParent] to delegate
     * the lookup to their parent service so the chain composes recursively.
     *
     * @param id The entity ID to check
     * @param rootParentId The root (outermost) parent ID to verify relationship against
     * @return true if the entity ultimately belongs to [rootParentId], false otherwise
     */
    suspend fun checkIsRelatedToRootParent(id: CURRENT_ID, rootParentId: PARENT_ID): Boolean {
        return this.checkIsRelatedToRootParent(listOf(id), rootParentId)
    }

    /**
     * Checks if multiple entities ultimately belong to [rootParentId] by walking the parent
     * chain up to its root.
     *
     * See [checkIsRelatedToRootParent] (single-id overload) for the semantics and override
     * contract.
     */
    suspend fun checkIsRelatedToRootParent(ids: Collection<CURRENT_ID>, rootParentId: PARENT_ID): Boolean {
        return this.checkIsRelated(ids, rootParentId)
    }
}

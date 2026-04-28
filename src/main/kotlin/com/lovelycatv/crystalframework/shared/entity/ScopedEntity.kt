package com.lovelycatv.crystalframework.shared.entity

/**
 * Marker for entities that live inside a scope (e.g. a tenant or a department).
 *
 * Implementations expose the ID of the *direct* parent in the resource hierarchy. This
 * is intentionally distinct from any self-referential `parentId` field an entity may
 * carry to model a tree structure (e.g. a department's parent department) — those are
 * unrelated concepts and should not be conflated.
 *
 * For top-level scopes (tenant) the direct parent IS the root. For deeper levels
 * (e.g. a department member, whose direct parent is the department) callers that only
 * know the outermost scope should use `checkIsRelatedToRootParent` so the chain can be
 * walked up recursively.
 */
interface ScopedEntity<PARENT_ID> {
    fun getDirectParentId(): PARENT_ID
}

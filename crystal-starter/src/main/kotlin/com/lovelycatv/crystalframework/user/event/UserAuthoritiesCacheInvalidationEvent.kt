package com.lovelycatv.crystalframework.user.event

/**
 * Marker for any change that should drop the cached authorities of one or more users.
 *
 * Listeners are expected to translate the source of the change (a user, a system role,
 * a tenant member, or a tenant role) into the actual set of affected user ids and
 * remove their entries from [com.lovelycatv.crystalframework.user.service.UserRbacQueryService].
 */
sealed interface UserAuthoritiesCacheInvalidationEvent

/**
 * The given user's role/permission set just changed; clear the cache for that user only.
 */
data class UserAuthoritiesInvalidationEvent(val userId: Long) : UserAuthoritiesCacheInvalidationEvent

/**
 * A system role mutated (permissions changed, role deleted, ...); clear the cache of
 * every user currently holding that role.
 */
data class SystemRoleAuthoritiesInvalidationEvent(val roleId: Long) : UserAuthoritiesCacheInvalidationEvent

/**
 * The role assignment of a single tenant member changed; clear the cache of the
 * underlying user of that member.
 */
data class TenantMemberAuthoritiesInvalidationEvent(val memberId: Long) : UserAuthoritiesCacheInvalidationEvent

/**
 * A tenant role mutated (permissions changed, role deleted, ...); clear the cache of
 * every member currently holding that tenant role.
 */
data class TenantRoleAuthoritiesInvalidationEvent(val roleId: Long) : UserAuthoritiesCacheInvalidationEvent

package com.lovelycatv.crystalframework.spi.message.scope

import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.config.MessageScopeResolver
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import org.springframework.stereotype.Component

/**
 * The per-tenant messaging boundary (scopeId = tenantId). A user belongs to the
 * boundary iff they are a member of that tenant, delegating to
 * [TenantMemberService]. This is the only place the messaging system learns what
 * "tenant" means — the core stays tenant-agnostic.
 */
@Component
class TenantMessageScopeResolver(
    private val tenantMemberService: TenantMemberService,
) : MessageScopeResolver {
    override val scopeType: ScopeType = ScopeType.TENANT

    override suspend fun isMember(scope: Scope, userId: Long): Boolean {
        val tenantId = scope.id ?: return false
        return tenantMemberService.getByTenantIdAndUserId(tenantId, userId) != null
    }

    /**
     * Tenant-deletion cleanup of scope-bound conversations is intentionally not
     * wired here yet (no revocation listener exists). Left as a no-op pending
     * confirmation on the desired purge semantics.
     */
    override suspend fun onScopeRevoked(scope: Scope) = Unit
}

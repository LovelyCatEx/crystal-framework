package com.lovelycatv.crystalframework.spi.message.audience

import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType
import org.springframework.stereotype.Component

/**
 * Tenant-wide audience: a recipient iff they belong to the tenant identified by
 * the broadcast's audience ref. Tenant membership is carried on the candidate, so
 * this resolver needs no tenant lookup of its own — this is what lets a tenant
 * broadcast to all its members reuse the same read-diffusion base as a system
 * announcement, differing only in the (scope, audience) tuple.
 */
@Component
class TenantMembersMessageAudienceResolver : MessageAudienceResolver {
    override val audienceType: AudienceType = AudienceType.TENANT_MEMBERS

    override suspend fun matches(audience: Audience, candidate: AudienceCandidate): Boolean =
        audience.ref != null && audience.ref in candidate.tenantIds
}

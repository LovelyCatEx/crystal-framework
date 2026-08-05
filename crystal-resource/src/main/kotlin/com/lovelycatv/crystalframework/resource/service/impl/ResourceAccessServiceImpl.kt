package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.constants.FileResourcePermissions
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.interfaces.ResourceTenantMembershipChecker
import com.lovelycatv.crystalframework.resource.service.ResourceAccessService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class ResourceAccessServiceImpl(
    private val systemModuleClient: SystemModuleClient,
    // @Lazy breaks the constructor DI cycle: the tenant-side checker transitively depends
    // (via TenantMemberService → UserService → FileResourceService → this) back on the resource
    // graph. The checker is only invoked at request time, so a lazy proxy is safe here.
    @Lazy private val membershipChecker: ResourceTenantMembershipChecker?,
) : ResourceAccessService {

    override fun resolveVisibility(fileType: ResourceFileType): ResourceVisibility {
        val visibility = systemModuleClient
            .getSystemSettings(BusinessException("System settings not initialized"))!!
            .resource
            .visibility
        return when (fileType) {
            ResourceFileType.USER_AVATAR -> visibility.userAvatar
            ResourceFileType.TENANT_ICON -> visibility.tenantIcon
            ResourceFileType.TENANT_MEMBER_AVATAR -> visibility.tenantMemberAvatar
        }
    }

    override fun resolveSignedUrlTtlSeconds(): Long {
        return systemModuleClient
            .getSystemSettings(BusinessException("System settings not initialized"))!!
            .resource
            .signedUrl
            .ttlSeconds
    }

    override suspend fun isReadable(entity: FileResourceEntity, viewer: UserAuthentication?): Boolean {
        return when (resolveVisibility(entity.getRealResourceFileType())) {
            ResourceVisibility.PUBLIC -> true
            ResourceVisibility.AUTHENTICATED -> viewer != null
            // OWNER_ONLY / SCOPE_MEMBER are the uploader-/member-facing policies; a manager who holds
            // the file-resource READ authority for this resource's own (scope, scopeId) — super,
            // system, tenantAdmin, or matching-tenant tenantPem — is allowed to read regardless.
            ResourceVisibility.OWNER_ONLY ->
                (viewer != null && viewer.userId == entity.userId) || canManageRead(entity, viewer)
            ResourceVisibility.SCOPE_MEMBER ->
                isScopeMemberReadable(entity, viewer) || canManageRead(entity, viewer)
            // SYSTEM_ADMIN is itself a privilege gate; do not widen it with the manager bypass.
            ResourceVisibility.SYSTEM_ADMIN -> viewer != null && RbacUtils.isRoot()
        }
    }

    /**
     * Whether [viewer] could read [entity] through the file-resource manager line — i.e. holds a
     * matrix READ authority eligible for the resource's own `(scope, scopeId)` and passes tenant
     * isolation. Reuses the single [FileResourcePermissions.MATRIX] so the manager-bypass set stays
     * identical to what the manager controller enforces. Anonymous viewers never qualify.
     */
    private suspend fun canManageRead(entity: FileResourceEntity, viewer: UserAuthentication?): Boolean {
        if (viewer == null) {
            return false
        }
        return FileResourcePermissions.MATRIX.grantsAccess(
            scope = entity.getRealScope(),
            scopeId = entity.scopeId,
            operation = ScopedOperation.READ,
            callerTenantId = viewer.tenantId,
        )
    }

    /**
     * SCOPE_MEMBER: system-scoped resources degrade to "any authenticated user"; tenant-scoped
     * resources require the viewer to be an active member of the owning tenant. When the tenant
     * module is absent the checker is null and tenant-scoped resources are denied (fail-closed).
     */
    private suspend fun isScopeMemberReadable(
        entity: FileResourceEntity,
        viewer: UserAuthentication?,
    ): Boolean {
        if (viewer == null) {
            return false
        }
        return when (entity.getRealScope()) {
            ResourceScope.SYSTEM -> true
            ResourceScope.TENANT -> membershipChecker?.isActiveMember(entity.scopeId, viewer.userId) == true
        }
    }

    override suspend fun assertReadable(entity: FileResourceEntity, viewer: UserAuthentication?) {
        if (isReadable(entity, viewer)) {
            return
        }
        val visibility = resolveVisibility(entity.getRealResourceFileType())
        val reason = if (visibility == ResourceVisibility.SCOPE_MEMBER) {
            ForbiddenReason.NOT_TENANT_MEMBER
        } else {
            ForbiddenReason.SCOPE_MISMATCH
        }
        throw ForbiddenException(
            message = "You are not allowed to access this resource",
            context = ForbiddenContext(
                reason = reason,
                scope = entity.getRealScope(),
            ),
        )
    }
}

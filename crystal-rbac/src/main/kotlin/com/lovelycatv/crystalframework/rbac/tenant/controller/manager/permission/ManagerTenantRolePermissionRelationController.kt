package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.SetRolePermissionsDTO
import com.lovelycatv.crystalframework.rbac.tenant.service.TenantPermissionService
import com.lovelycatv.crystalframework.rbac.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role/permission")
class ManagerTenantRolePermissionRelationController(
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantRoleManagerService: TenantRoleManagerService,
    private val tenantPermissionService: TenantPermissionService,
    private val userRbacQueryService: UserRbacQueryService
) {
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_TENANT_ROLE_PERMISSION_RELATIONS,
        resourceIds = "#roleId",
    )
    @GetMapping("/get", version = "1")
    suspend fun getRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestParam roleId: Long
    ): ApiResponse<*> {
        return if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_READ.name)) {
            ApiResponse.success(tenantRolePermissionRelationService.getRolePermissions(roleId))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_ROLE_PERMISSION_READ.name)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantRoleManagerService.checkIsRelatedToRootParent(roleId, userAuthentication.tenantId!!)) {
                ApiResponse.success(tenantRolePermissionRelationService.getRolePermissions(roleId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = listOf(
                    SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_READ.name,
                    TenantPermission.ACTION_ROLE_PERMISSION_READ.name,
                ),
                scope = ResourceScope.TENANT,
            ))
        }
    }

    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_TENANT_ROLE_PERMISSION_RELATIONS,
        resourceIds = "#dto.roleId",
    )
    @PostMapping("/set", version = "1")
    suspend fun setRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetRolePermissionsDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE.name)) {
            tenantRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_ROLE_PERMISSION_UPDATE.name)) {
            userAuthentication.assertTenantIdNotNull()
            val tenantId = userAuthentication.tenantId!!
            if (tenantRoleManagerService.checkIsRelatedToRootParent(dto.roleId, tenantId)) {
                val targetPermissionNames = tenantPermissionService
                    .getByIds(dto.permissionIds)
                    .map { it.name }
                val allowed = TenantPermission.allPermissionNames()
                val forbidden = targetPermissionNames.filterNot { it in allowed }
                if (forbidden.isNotEmpty()) {
                    throw ForbiddenException("Not tenant-scope permissions: $forbidden",
                        context = ForbiddenContext(reason = ForbiddenReason.SCOPE_MISMATCH, scope = ResourceScope.TENANT))
                }
                val operatorMemberId = userAuthentication.tenantMemberId
                    ?: throw ForbiddenException(
                        context = ForbiddenContext(
                            reason = ForbiddenReason.NOT_TENANT_MEMBER,
                            scope = ResourceScope.TENANT,
                        )
                    )
                val operatorPermissions = userRbacQueryService
                    .getTenantMemberRbacAccessInfo(operatorMemberId, tenantId)
                    .permissions
                    .map { it.name }
                    .toSet()
                if (!operatorPermissions.containsAll(targetPermissionNames.toSet())) {
                    throw ForbiddenException(
                        context = ForbiddenContext(
                            reason = ForbiddenReason.PERMISSION_ESCALATION,
                            scope = ResourceScope.TENANT,
                        )
                    )
                }
                tenantRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = listOf(
                    SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE.name,
                    TenantPermission.ACTION_ROLE_PERMISSION_UPDATE.name,
                ),
                scope = ResourceScope.TENANT,
            ))
        }
        return ApiResponse.success(null)
    }
}

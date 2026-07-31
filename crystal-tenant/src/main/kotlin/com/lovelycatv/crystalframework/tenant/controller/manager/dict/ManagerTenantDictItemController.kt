package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo.TenantDictItemTreeVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictItemRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictItemManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-item")
class ManagerTenantDictItemController(
    managerService: TenantDictItemManagerService,
) : StandardScopedManagerController<
        TenantDictItemManagerService,
        TenantDictItemRepository,
        TenantDictItemEntity,
        ManagerCreateTenantDictItemDTO,
        ManagerReadTenantDictItemDTO,
        ManagerUpdateTenantDictItemDTO,
        ManagerDeleteTenantDictItemDTO
>(
    managerService,
    permissions = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_DICT_ITEM_CREATE.name,
        superRead = SystemPermission.ACTION_X_DICT_ITEM_READ.name,
        superUpdate = SystemPermission.ACTION_X_DICT_ITEM_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_DICT_ITEM_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_DICT_ITEM_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DICT_ITEM_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DICT_ITEM_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DICT_ITEM_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DICT_ITEM_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_DICT_ITEM_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_DICT_ITEM_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_DICT_ITEM_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_DICT_ITEM_DELETE.name,
    ),
) {

    /**
     * Derived-scope DTOs carry a `typeId` (parent id) instead of `scope + scopeId`. Resolve via
     * the Service's typeId bridge — no direct dependency on the parent Service here. The entity
     * path (update / delete) uses the default hook, which already delegates to
     * `managerService.resolveRootScope` — that recursively hops via the parent Service.
     */
    override suspend fun resolveScopeFromCreateDTO(dto: ManagerCreateTenantDictItemDTO): Pair<ResourceScope, Long> {
        return managerService.resolveRootScopeFromTypeId(dto.typeId)
            ?: throw BusinessException("Dict type ${dto.typeId} not found")
    }

    override suspend fun resolveScopeFromReadDTO(dto: ManagerReadTenantDictItemDTO): Pair<ResourceScope, Long> {
        return managerService.resolveRootScopeFromTypeId(dto.typeId)
            ?: throw BusinessException("Dict type ${dto.typeId} not found")
    }

    /**
     * Tree view of dict items under a given type. Authorization mirrors the standard READ
     * pipeline: consult [PermissionMatrix.layersFor] then run [checkOwnership].
     */
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_TENANT_DICT_ITEMS,
    )
    @GetMapping("/tree")
    suspend fun tree(
        userAuthentication: UserAuthentication,
        @RequestParam typeId: Long
    ): ApiResponse<List<TenantDictItemTreeVO>> {
        val (scope, scopeId) = managerService.resolveRootScopeFromTypeId(typeId)
            ?: throw BusinessException("Dict type $typeId not found")
        if (!RbacUtils.hasAnyAuthority(*permissions!!.layersFor(scope, ScopedOperation.READ))) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = permissions!!.layersFor(scope, ScopedOperation.READ)
                    .filter { it != PermissionMatrix.NEVER_GRANTED }.toList(),
                scope = scope,
            ))
        }
        if (!checkOwnership(scope, scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.SCOPE_MISMATCH,
                scope = scope,
            ))
        }
        return ApiResponse.success(managerService.getTreeByTypeId(typeId))
    }
}

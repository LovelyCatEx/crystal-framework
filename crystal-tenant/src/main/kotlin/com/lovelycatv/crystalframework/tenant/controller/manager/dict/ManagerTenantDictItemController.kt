package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ScopedPermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
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
    permissions = ScopedPermissionMatrix(
        superCreate = SystemPermission.ACTION_DICT_ITEM_CREATE,
        superRead = SystemPermission.ACTION_DICT_ITEM_READ,
        superUpdate = SystemPermission.ACTION_DICT_ITEM_UPDATE,
        superDelete = SystemPermission.ACTION_DICT_ITEM_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_DICT_ITEM_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DICT_ITEM_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DICT_ITEM_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DICT_ITEM_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DICT_ITEM_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_ITEM_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_DICT_ITEM_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_ITEM_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_ITEM_DELETE_PEM,
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
     * pipeline: consult [ScopedPermissionMatrix.layersFor] then run [checkOwnership].
     */
    @GetMapping("/tree")
    suspend fun tree(
        userAuthentication: UserAuthentication,
        @RequestParam typeId: Long
    ): ApiResponse<List<TenantDictItemTreeVO>> {
        val (scope, scopeId) = managerService.resolveRootScopeFromTypeId(typeId)
            ?: throw BusinessException("Dict type $typeId not found")
        if (!RbacUtils.hasAnyAuthority(*permissions!!.layersFor(scope, ScopedOperation.READ))) {
            throw ForbiddenException()
        }
        if (!checkOwnership(scope, scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException()
        }
        return ApiResponse.success(managerService.getTreeByTypeId(typeId))
    }
}

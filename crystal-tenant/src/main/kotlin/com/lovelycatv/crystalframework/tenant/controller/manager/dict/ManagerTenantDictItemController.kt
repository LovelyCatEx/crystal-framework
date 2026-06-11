package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantDictConstants
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo.TenantDictItemTreeVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictItemRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictItemManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-item")
class ManagerTenantDictItemController(
    private val tenantDictItemManagerService: TenantDictItemManagerService,
    private val tenantDictTypeManagerService: TenantDictTypeManagerService,
) : StandardTenantManagerController<
        TenantDictItemManagerService,
        TenantDictItemRepository,
        TenantDictItemEntity,
        ManagerCreateTenantDictItemDTO,
        ManagerReadTenantDictItemDTO,
        ManagerUpdateTenantDictItemDTO,
        ManagerDeleteTenantDictItemDTO
>(
    tenantDictItemManagerService,
    createPermission = SystemPermission.ACTION_TENANT_DICT_ITEM_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_DICT_ITEM_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_DICT_ITEM_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_DICT_ITEM_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_DICT_ITEM_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_DICT_ITEM_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_DICT_ITEM_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_DICT_ITEM_DELETE_PEM,
) {
    // region Scope-check hooks: DictItem → DictType → Tenant

    override suspend fun isCreateInScope(
        dto: ManagerCreateTenantDictItemDTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return tenantDictTypeManagerService.checkIsRelatedToRootParent(
            dto.typeId,
            userAuthentication.tenantId!!
        )
    }

    override suspend fun isQueryInScope(
        dto: ManagerReadTenantDictItemDTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return tenantDictTypeManagerService.checkIsRelatedToRootParent(
            dto.typeId,
            userAuthentication.tenantId!!
        )
    }

    // endregion

    // region System-scope routing: typeId → type.tenantId == 0 requires system dict permissions

    private suspend fun getTypeTenantId(typeId: Long): Long {
        val type = tenantDictTypeManagerService.getByIdOrNull(typeId) ?: return -1
        return type.tenantId
    }

    override suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: ManagerCreateTenantDictItemDTO
    ): ApiResponse<*>? {
        if (getTypeTenantId(dto.typeId) != TenantDictConstants.SYSTEM_TENANT_ID) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE)) {
            throw ForbiddenException()
        }
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    /**
     * System dict item query: any authenticated user can read.
     */
    override suspend fun customQuery(
        userAuthentication: UserAuthentication,
        dto: ManagerReadTenantDictItemDTO
    ): ApiResponse<*>? {
        if (getTypeTenantId(dto.typeId) != TenantDictConstants.SYSTEM_TENANT_ID) return null
        // Read is open to all authenticated users for system dicts
        return ApiResponse.success(managerService.query(dto))
    }

    override suspend fun customUpdate(
        userAuthentication: UserAuthentication,
        dto: ManagerUpdateTenantDictItemDTO
    ): ApiResponse<*>? {
        val item = managerService.getByIdOrNull(dto.id) ?: return null
        if (getTypeTenantId(item.typeId) != TenantDictConstants.SYSTEM_TENANT_ID) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE)) {
            throw ForbiddenException()
        }
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    override suspend fun customDelete(
        userAuthentication: UserAuthentication,
        dto: ManagerDeleteTenantDictItemDTO
    ): ApiResponse<*>? {
        val items = dto.ids.map { managerService.getByIdOrNull(it) ?: return null }
        val typeIds = items.map { it.typeId }.distinct()
        if (typeIds.any { getTypeTenantId(it) != TenantDictConstants.SYSTEM_TENANT_ID }) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE)) {
            throw ForbiddenException()
        }
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }

    // endregion

    // region Additional endpoint: tree view

    @GetMapping("/tree")
    suspend fun tree(
        userAuthentication: UserAuthentication,
        @RequestParam typeId: Long
    ): ApiResponse<List<TenantDictItemTreeVO>> {
        val typeTenantId = getTypeTenantId(typeId)
        if (typeTenantId == TenantDictConstants.SYSTEM_TENANT_ID) {
            // System dict tree: any authenticated user can read
        } else {
            if (!RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DICT_ITEM_READ)) {
                val hasScopedPermission = RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DICT_ITEM_READ_PEM)
                if (!hasScopedPermission || typeTenantId != userAuthentication.tenantId) {
                    throw ForbiddenException()
                }
            }
        }
        return ApiResponse.success(tenantDictItemManagerService.getTreeByTypeId(typeId))
    }

    // endregion
}

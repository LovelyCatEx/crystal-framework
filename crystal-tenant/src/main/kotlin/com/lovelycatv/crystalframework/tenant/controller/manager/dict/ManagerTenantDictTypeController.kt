package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantDictConstants
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictTypeRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-type")
class ManagerTenantDictTypeController(
    managerService: TenantDictTypeManagerService
) : StandardTenantManagerController<
        TenantDictTypeManagerService,
        TenantDictTypeRepository,
        TenantDictTypeEntity,
        ManagerCreateTenantDictTypeDTO,
        ManagerReadTenantDictTypeDTO,
        ManagerUpdateTenantDictTypeDTO,
        ManagerDeleteTenantDictTypeDTO
>(
    managerService,
    createPermission = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_DICT_TYPE_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM
) {
    // region System-scope routing: tenantId == 0 requires separate system dict permissions

    override suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: ManagerCreateTenantDictTypeDTO
    ): ApiResponse<*>? {
        if (dto.tenantId != TenantDictConstants.SYSTEM_TENANT_ID) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE)) {
            throw ForbiddenException()
        }
        managerService.create(dto)
        return ApiResponse.success(null)
    }

    /**
     * System dict query: any authenticated user can read.
     */
    override suspend fun customQuery(
        userAuthentication: UserAuthentication,
        dto: ManagerReadTenantDictTypeDTO
    ): ApiResponse<*>? {
        if (dto.tenantId != TenantDictConstants.SYSTEM_TENANT_ID) return null
        return ApiResponse.success(buildQueryResponse(dto))
    }

    override suspend fun customReadAll(
        userAuthentication: UserAuthentication,
        tenantId: Long
    ): ApiResponse<*>? {
        if (tenantId != TenantDictConstants.SYSTEM_TENANT_ID) return null
        return ApiResponse.success(buildReadAllResponse(tenantId))
    }

    override suspend fun customUpdate(
        userAuthentication: UserAuthentication,
        dto: ManagerUpdateTenantDictTypeDTO
    ): ApiResponse<*>? {
        val entity = managerService.getByIdOrNull(dto.id) ?: return null
        if (entity.tenantId != TenantDictConstants.SYSTEM_TENANT_ID) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE)) {
            throw ForbiddenException()
        }
        managerService.update(dto)
        return ApiResponse.success(null)
    }

    override suspend fun customDelete(
        userAuthentication: UserAuthentication,
        dto: ManagerDeleteTenantDictTypeDTO
    ): ApiResponse<*>? {
        val entities = dto.ids.map { managerService.getByIdOrNull(it) ?: return null }
        if (entities.any { it.tenantId != TenantDictConstants.SYSTEM_TENANT_ID }) return null
        if (!RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE)) {
            throw ForbiddenException()
        }
        managerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }

    // endregion
}

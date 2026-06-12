package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
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
) : StandardScopedManagerController<
        TenantDictTypeManagerService,
        TenantDictTypeRepository,
        TenantDictTypeEntity,
        ManagerCreateTenantDictTypeDTO,
        ManagerReadTenantDictTypeDTO,
        ManagerUpdateTenantDictTypeDTO,
        ManagerDeleteTenantDictTypeDTO
>(managerService) {

    override suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return when (scope) {
            ResourceScope.SYSTEM -> when (operation) {
                ScopedOperation.READ -> true
                ScopedOperation.CREATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE)
                ScopedOperation.UPDATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE)
                ScopedOperation.DELETE -> RbacUtils.hasAuthority(SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE)
            }
            ResourceScope.TENANT -> when (operation) {
                ScopedOperation.CREATE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE,
                    TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM
                )
                ScopedOperation.READ -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_DICT_TYPE_READ,
                    TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM
                )
                ScopedOperation.UPDATE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE,
                    TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM
                )
                ScopedOperation.DELETE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE,
                    TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM
                )
            }
        }
    }
}

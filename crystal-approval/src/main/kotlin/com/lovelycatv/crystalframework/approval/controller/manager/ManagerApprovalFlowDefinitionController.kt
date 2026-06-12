package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerDeleteApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definitions")
class ManagerApprovalFlowDefinitionController(
    managerService: ApprovalFlowDefinitionManagerService,
) : StandardScopedManagerController<
        ApprovalFlowDefinitionManagerService,
        ApprovalFlowDefinitionRepository,
        ApprovalFlowDefinitionEntity,
        ManagerCreateApprovalFlowDefinitionDTO,
        ManagerReadApprovalFlowDefinitionDTO,
        ManagerUpdateApprovalFlowDefinitionDTO,
        ManagerDeleteApprovalFlowDefinitionDTO
>(managerService) {

    override suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return when (scope) {
            ResourceScope.SYSTEM -> when (operation) {
                ScopedOperation.CREATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE)
                ScopedOperation.READ -> RbacUtils.hasAuthority(SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ)
                ScopedOperation.UPDATE -> RbacUtils.hasAuthority(SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE)
                ScopedOperation.DELETE -> RbacUtils.hasAuthority(SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE)
            }
            ResourceScope.TENANT -> when (operation) {
                ScopedOperation.CREATE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE,
                    TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE_PEM
                )
                ScopedOperation.READ -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ,
                    TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ_PEM
                )
                ScopedOperation.UPDATE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE,
                    TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE_PEM
                )
                ScopedOperation.DELETE -> RbacUtils.hasAnyAuthority(
                    SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE,
                    TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE_PEM
                )
            }
        }
    }
}

package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerDeleteApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowDefinitionDetailsVO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowEdgeManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowNodeManagerService
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ScopedPermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definitions")
class ManagerApprovalFlowDefinitionController(
    managerService: ApprovalFlowDefinitionManagerService,
    private val approvalFlowNodeManagerService: ApprovalFlowNodeManagerService,
    private val approvalFlowEdgeManagerService: ApprovalFlowEdgeManagerService,
) : StandardScopedManagerController<
        ApprovalFlowDefinitionManagerService,
        ApprovalFlowDefinitionRepository,
        ApprovalFlowDefinitionEntity,
        ManagerCreateApprovalFlowDefinitionDTO,
        ManagerReadApprovalFlowDefinitionDTO,
        ManagerUpdateApprovalFlowDefinitionDTO,
        ManagerDeleteApprovalFlowDefinitionDTO
>(
    managerService,
    permissions = ScopedPermissionMatrix(
        superCreate = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE,
        superRead = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ,
        superUpdate = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE,
        superDelete = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE_PEM,
    ),
) {
    @PostMapping("/updateGraph")
    suspend fun updateGraph(
        userAuthentication: UserAuthentication,
        @Valid
        @RequestBody
        dto: ManagerUpdateApprovalFlowGraphDTO
    ): ApiResponse<*> {
        val definition = managerService.getByIdOrNull(dto.definitionId)
            ?: throw BusinessException("Definition not found")
        val resolvedScope = resolveScope(definition.scope)
        if (!checkPermission(resolvedScope, definition.scopeId, ScopedOperation.UPDATE, userAuthentication)) {
            throw ForbiddenException()
        }
        if (!checkOwnership(resolvedScope, definition.scopeId, ScopedOperation.UPDATE, userAuthentication)) {
            throw UnauthorizedException()
        }
        val errors = managerService.updateGraph(dto)
        return ApiResponse.success(mapOf("success" to errors.isEmpty(), "errors" to errors))
    }

    @GetMapping("/detailsById")
    suspend fun getApprovalFlowDefinitionDetails(
        @RequestParam
        definitionId: Long
    ): ApiResponse<*> {
        val definition = managerService.getByIdOrNull(definitionId)
            ?: throw BusinessException("Definition not found")

        return ApiResponse.success(
            ApprovalFlowDefinitionDetailsVO(
                definition = definition,
                nodes = approvalFlowNodeManagerService.getNodesByDefinitionsIdAndVersion(definition.id, definition.currentVersion),
                edges = approvalFlowEdgeManagerService.getEdgesByDefinitionsIdAndVersion(definition.id, definition.currentVersion)
            )
        )
    }
}

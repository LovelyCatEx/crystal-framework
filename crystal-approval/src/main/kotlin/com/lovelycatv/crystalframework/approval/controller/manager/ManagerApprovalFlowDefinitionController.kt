package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
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
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-definition")
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
    permissions = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_CREATE.name,
        superRead = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_READ.name,
        superUpdate = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE.name,
    ),
) {
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_APPROVAL_FLOW_DEFINITION,
        resourceIds = "#dto.definitionId",
    )
    @PostMapping("/update-graph")
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
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = permissions?.layersFor(resolvedScope, ScopedOperation.UPDATE)
                    ?.filter { it != PermissionMatrix.NEVER_GRANTED }?.toList() ?: emptyList(),
                scope = resolvedScope,
            ))
        }
        if (!checkOwnership(resolvedScope, definition.scopeId, ScopedOperation.UPDATE, userAuthentication)) {
            throw UnauthorizedException()
        }
        val errors = managerService.updateGraph(dto)
        return ApiResponse.success(mapOf("success" to errors.isEmpty(), "errors" to errors))
    }

    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_APPROVAL_FLOW_DEFINITION,
        resourceIds = "#definitionId",
    )
    @GetMapping("/details-by-id")
    suspend fun getApprovalFlowDefinitionDetails(
        userAuthentication: UserAuthentication,
        @RequestParam
        definitionId: Long
    ): ApiResponse<*> {
        val definition = managerService.getByIdOrNull(definitionId)
            ?: throw BusinessException("Definition not found")
        val resolvedScope = resolveScope(definition.scope)
        if (!checkPermission(resolvedScope, definition.scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = permissions?.layersFor(resolvedScope, ScopedOperation.READ)
                    ?.filter { it != PermissionMatrix.NEVER_GRANTED }?.toList() ?: emptyList(),
                scope = resolvedScope,
            ))
        }
        if (!checkOwnership(resolvedScope, definition.scopeId, ScopedOperation.READ, userAuthentication)) {
            throw UnauthorizedException()
        }

        return ApiResponse.success(
            ApprovalFlowDefinitionDetailsVO(
                definition = definition,
                nodes = approvalFlowNodeManagerService.getNodesByDefinitionsIdAndVersion(definition.id, definition.currentVersion),
                edges = approvalFlowEdgeManagerService.getEdgesByDefinitionsIdAndVersion(definition.id, definition.currentVersion)
            )
        )
    }
}

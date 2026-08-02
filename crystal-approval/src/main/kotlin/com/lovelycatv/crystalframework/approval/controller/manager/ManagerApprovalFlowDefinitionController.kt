package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerDeleteApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalDictOptionVO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowDefinitionDetailsVO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalDictResolver
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowEdgeManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowNodeManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.BusinessException
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
    private val approvalDictResolver: ApprovalDictResolver,
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
    permissions = ApprovalManagerPermissionMatrices.DEFINITION,
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
        assertAccess(resolveScope(definition.scope), definition.scopeId, ScopedOperation.UPDATE, userAuthentication)
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
        assertAccess(resolveScope(definition.scope), definition.scopeId, ScopedOperation.READ, userAuthentication)

        return ApiResponse.success(
            ApprovalFlowDefinitionDetailsVO(
                definition = definition,
                nodes = approvalFlowNodeManagerService.getNodesByDefinitionsIdAndVersion(definition.id, definition.currentVersion),
                edges = approvalFlowEdgeManagerService.getEdgesByDefinitionsIdAndVersion(definition.id, definition.currentVersion)
            )
        )
    }

    /**
     * Real-time selectable options for a DICT form field, used by the initiate-form renderer. The
     * scope is always taken from the server-side definition entity (never from the client), so it
     * cannot be tricked into reading another tenant's dictionary. Authorization reuses the base
     * controller's [assertAccess] with the definition's own resolved scope and READ operation.
     */
    @GetMapping("/dict-options")
    suspend fun dictOptions(
        userAuthentication: UserAuthentication,
        @RequestParam definitionId: Long,
        @RequestParam fieldKey: String,
    ): ApiResponse<List<ApprovalDictOptionVO>> {
        val definition = managerService.getByIdOrNull(definitionId)
            ?: throw BusinessException("Definition not found")
        val resolvedScope = resolveScope(definition.scope)
        assertAccess(resolvedScope, definition.scopeId, ScopedOperation.READ, userAuthentication)
        val options = approvalDictResolver
            .resolveItemsForField(definition.formSchema, fieldKey, resolvedScope, definition.scopeId)
            .map { ApprovalDictOptionVO(value = it.value, label = it.label) }
        return ApiResponse.success(options)
    }
}

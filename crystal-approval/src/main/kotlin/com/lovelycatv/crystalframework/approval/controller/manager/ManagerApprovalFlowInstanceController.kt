package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.StartApprovalFlowDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.service.engine.ApprovalFlowEngine
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowInstanceManagerService
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.ScopedPermissionTriad
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-instances")
class ManagerApprovalFlowInstanceController(
    managerService: ApprovalFlowInstanceManagerService,
    private val approvalFlowDefinitionManagerService: ApprovalFlowDefinitionManagerService,
    private val approvalFlowEngine: ApprovalFlowEngine,
) : ReadonlyScopedManagerController<
        ApprovalFlowInstanceManagerService,
        ApprovalFlowInstanceRepository,
        ApprovalFlowInstanceEntity,
        ManagerCreateApprovalFlowInstanceDTO,
        ManagerReadApprovalFlowInstanceDTO,
        ManagerUpdateApprovalFlowInstanceDTO,
        BaseManagerDeleteDTO
>(
    managerService,
    permissions = ScopedPermissionTriad.readonly(
        superRead = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        systemRead = SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
        tenantPemRead = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
    ),
) {

    /**
     * Read is intentionally allowed for any authenticated user — the endpoint is
     * shared between read-all admins and ordinary initiators viewing their own flows.
     * The triad declared above is only consulted by [buildQueryResponse] (and by
     * [start]) to decide whether to inject an `initiator_id` filter for callers
     * without read-all authority. Mutation endpoints are blocked by
     * [ReadonlyScopedManagerController].
     */
    override suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return operation == ScopedOperation.READ
    }

    /**
     * Inject `initiator_id` filter for users without read-all authority before delegating
     * to the manager service. Read-all authority lets the user see every instance in the scope.
     */
    override suspend fun buildQueryResponse(
        dto: ManagerReadApprovalFlowInstanceDTO,
        userAuthentication: UserAuthentication,
    ): Any {
        val resolvedScope = resolveScope(dto.scope)

        val triad = permissions
            ?: error("ManagerApprovalFlowInstanceController requires a ScopedPermissionTriad")
        val canReadAll = RbacUtils.hasAnyAuthority(*triad.forScope(resolvedScope, ScopedOperation.READ))

        val effectiveDto = if (canReadAll) {
            dto
        } else {
            val initiatorId = when (resolvedScope) {
                ResourceScope.SYSTEM -> userAuthentication.userId
                ResourceScope.TENANT -> userAuthentication.tenantMemberId
                    ?: throw ForbiddenException("Current user is not a member of this tenant")
            }
            dto.copy(query = appendInitiatorCondition(dto.query, initiatorId))
        }

        return managerService.query(effectiveDto)
    }

    /**
     * Initiate an approval flow instance from a PUBLISHED definition. Authorization mirrors
     * the standard READ logic on the definition's scope: any user who can read the definition
     * is allowed to initiate. The initiator id stored on the new instance is scope-specific
     * (userId for SYSTEM, tenantMemberId for TENANT) — see [ApprovalFlowEngine.startFlow].
     */
    @PostMapping("/start", version = "1")
    suspend fun start(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: StartApprovalFlowDTO
    ): ApiResponse<*> {
        val definitionId = dto.definitionId
            ?: throw BusinessException("definitionId is required")
        val definition = approvalFlowDefinitionManagerService.getByIdOrNull(definitionId)
            ?: throw BusinessException("Definition not found")
        val resolvedScope = resolveScope(definition.scope)

        if (!checkPermission(resolvedScope, definition.scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException()
        }
        if (!checkOwnership(resolvedScope, definition.scopeId, ScopedOperation.READ, userAuthentication)) {
            throw ForbiddenException()
        }

        val initiatorId = when (resolvedScope) {
            ResourceScope.SYSTEM -> userAuthentication.userId
            ResourceScope.TENANT -> userAuthentication.tenantMemberId
                ?: throw ForbiddenException("Current user is not a member of this tenant")
        }
        val approvalScope = ApprovalFlowScope.getById(definition.scope)
            ?: throw BusinessException("Unknown approval flow scope ${definition.scope}")

        val instance = approvalFlowEngine.startFlow(
            definitionId = definition.id,
            initiatorId = initiatorId,
            scope = approvalScope,
            scopeId = definition.scopeId,
            formData = dto.formData,
        )
        return ApiResponse.success(instance)
    }

    private fun appendInitiatorCondition(existing: QueryNode?, initiatorId: Long): QueryNode {
        val initiatorCondition = ConditionNode(
            field = COLUMN_INITIATOR_ID,
            operator = QueryOperator.EQ,
            value = initiatorId,
        )
        return if (existing == null) {
            initiatorCondition
        } else {
            GroupNode(logic = QueryLogic.AND, children = listOf(existing, initiatorCondition))
        }
    }

    companion object {
        private const val COLUMN_INITIATOR_ID = "initiator_id"
    }
}

package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowInstanceManagerService
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-instances")
class ManagerApprovalFlowInstanceController(
    managerService: ApprovalFlowInstanceManagerService,
) : ReadonlyScopedManagerController<
        ApprovalFlowInstanceManagerService,
        ApprovalFlowInstanceRepository,
        ApprovalFlowInstanceEntity,
        ManagerCreateApprovalFlowInstanceDTO,
        ManagerReadApprovalFlowInstanceDTO,
        ManagerUpdateApprovalFlowInstanceDTO,
        BaseManagerDeleteDTO
>(managerService) {

    /**
     * Read is allowed for any authenticated user.
     * Data filtering is performed in [buildQueryResponse]: callers without read-all authority
     * see only instances they initiated (in SYSTEM: initiator_id == userId;
     * in TENANT: initiator_id == tenantMemberId).
     * Mutation operations are blocked by [ReadonlyScopedManagerController].
     */
    override suspend fun checkPermission(
        scope: ResourceScope,
        scopeId: Long?,
        operation: ScopedOperation,
        userAuthentication: UserAuthentication
    ): Boolean {
        return operation == ScopedOperation.READ
    }

    override suspend fun buildQueryResponse(
        dto: ManagerReadApprovalFlowInstanceDTO,
        userAuthentication: UserAuthentication,
    ): Any {
        val resolvedScope = resolveScope(dto.scope)

        val canReadAll = when (resolvedScope) {
            ResourceScope.SYSTEM -> RbacUtils.hasAuthority(SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ)
            ResourceScope.TENANT -> RbacUtils.hasAnyAuthority(
                SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
                TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM
            )
        }

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

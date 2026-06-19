package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.HandleApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.approval.service.engine.ApprovalFlowEngine
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowTaskManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
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
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-tasks")
class ManagerApprovalFlowTaskController(
    managerService: ApprovalFlowTaskManagerService,
    private val approvalFlowEngine: ApprovalFlowEngine,
) : ReadonlyScopedManagerController<
        ApprovalFlowTaskManagerService,
        ApprovalFlowTaskRepository,
        ApprovalFlowTaskEntity,
        ManagerCreateApprovalFlowTaskDTO,
        ManagerReadApprovalFlowTaskDTO,
        ManagerUpdateApprovalFlowTaskDTO,
        BaseManagerDeleteDTO
>(
    managerService,
) {

    /**
     * This endpoint is a personal "my to-do" view: every authenticated user may READ, but
     * [buildQueryResponse] always restricts the result set to tasks assigned to the caller.
     * There is no read-all concept here. Mutations are blocked by [ReadonlyScopedManagerController].
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
     * Always inject `assignee_id == self` so a user only ever sees their own tasks. The assignee id
     * is scope-specific (userId for SYSTEM, tenantMemberId for TENANT) — mirrors the initiator id
     * stored on the instance (see [ApprovalFlowEngine.resolveApprovers]).
     */
    override suspend fun buildQueryResponse(
        dto: ManagerReadApprovalFlowTaskDTO,
        userAuthentication: UserAuthentication,
    ): Any {
        val resolvedScope = resolveScope(dto.scope)
        val assigneeId = resolveAssigneeId(resolvedScope, userAuthentication)
        return managerService.query(dto.copy(query = appendAssigneeCondition(dto.query, assigneeId)))
    }

    /**
     * Approve or reject a task assigned to the current user. Ownership is enforced by comparing the
     * task's scope-specific assignee id against the caller; the actual state transition (records,
     * token/instance advancement) is delegated to [ApprovalFlowEngine.handleTask].
     */
    @PostMapping("/handle", version = "1")
    suspend fun handle(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: HandleApprovalFlowTaskDTO
    ): ApiResponse<*> {
        val taskId = dto.taskId ?: throw BusinessException("taskId is required")
        val approved = dto.approved ?: throw BusinessException("approved is required")

        val task = managerService.getByIdOrThrow(taskId)
        val resolvedScope = resolveScope(task.scope)
        val operatorId = resolveAssigneeId(resolvedScope, userAuthentication)
        if (task.assigneeId != operatorId) {
            throw ForbiddenException("This task is not assigned to the current user")
        }

        approvalFlowEngine.handleTask(
            taskId = task.id,
            operatorId = operatorId,
            approved = approved,
            comment = dto.comment,
            formData = dto.formData,
        )
        return ApiResponse.success(null)
    }

    private fun resolveAssigneeId(scope: ResourceScope, userAuthentication: UserAuthentication): Long {
        return when (scope) {
            ResourceScope.SYSTEM -> userAuthentication.userId
            ResourceScope.TENANT -> userAuthentication.tenantMemberId
                ?: throw ForbiddenException("Current user is not a member of this tenant")
        }
    }

    private fun appendAssigneeCondition(existing: QueryNode?, assigneeId: Long): QueryNode {
        val assigneeCondition = ConditionNode(
            field = COLUMN_ASSIGNEE_ID,
            operator = QueryOperator.EQ,
            value = assigneeId,
        )
        return if (existing == null) {
            assigneeCondition
        } else {
            GroupNode(logic = QueryLogic.AND, children = listOf(existing, assigneeCondition))
        }
    }

    companion object {
        private const val COLUMN_ASSIGNEE_ID = "assignee_id"
    }
}

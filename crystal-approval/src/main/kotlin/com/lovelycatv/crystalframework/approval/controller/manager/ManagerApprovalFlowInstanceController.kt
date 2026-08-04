package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.StartApprovalFlowDTO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalDictOptionVO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowInstanceDetailsVO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalDictResolver
import com.lovelycatv.crystalframework.approval.service.engine.ApprovalFlowEngine
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowInstanceManagerService
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/approval-flow-instance")
class ManagerApprovalFlowInstanceController(
    managerService: ApprovalFlowInstanceManagerService,
    private val approvalFlowDefinitionManagerService: ApprovalFlowDefinitionManagerService,
    private val approvalFlowEngine: ApprovalFlowEngine,
    private val approvalDictResolver: ApprovalDictResolver,
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
    permissions = ApprovalManagerPermissionMatrices.INSTANCE,
) {

    /**
     * The inherited scope-wide `/list` neither checks read-all authority nor injects the
     * `initiator_id` filter, so it would let any tenant member enumerate every instance in
     * the scope. Full visibility for read-all admins already flows through [buildQueryResponse]
     * (`/query`); this endpoint is unused by the frontend, so return nothing here.
     */
    override suspend fun buildReadAllResponse(scopeId: Long): Any {
        throw UnsupportedOperationException("read-all is not supported for approval instances")
    }

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
     *
     * Also guards [com.lovelycatv.crystalframework.shared.service.BaseManagerService.query]'s
     * `dto.id != null` short-circuit — that path bypasses buildQueryCriteria entirely, so a
     * caller without read-all could otherwise read arbitrary instances by id. We require
     * read-all authority for the scope before allowing id lookup here; callers who only need
     * to see their own instances should go through [queryMyInstances].
     */
    override suspend fun buildQueryResponse(
        dto: ManagerReadApprovalFlowInstanceDTO,
        userAuthentication: UserAuthentication,
    ): Any {
        val resolvedScope = resolveScope(dto.scope)

        val matrix = permissions
            ?: error("ManagerApprovalFlowInstanceController requires a PermissionMatrix")
        val canReadAll = RbacUtils.hasAnyAuthority(*matrix.layersFor(resolvedScope, ScopedOperation.READ))

        if (dto.id != null && !canReadAll) {
            throw ForbiddenException("Id lookup on /query requires read-all authority for scope $resolvedScope",
                context = ForbiddenContext(
                    reason = ForbiddenReason.MISSING_PERMISSION,
                    requiredPermissions = matrix.layersFor(resolvedScope, ScopedOperation.READ)
                        .filter { it != PermissionMatrix.NEVER_GRANTED }.toList(),
                    scope = resolvedScope,
                ))
        }

        val effectiveDto = if (canReadAll) {
            dto
        } else {
            val initiatorId = when (resolvedScope) {
                ResourceScope.SYSTEM -> userAuthentication.userId
                ResourceScope.TENANT -> userAuthentication.tenantMemberId
                    ?: throw ForbiddenException("Current user is not a member of this tenant",
                        context = ForbiddenContext(reason = ForbiddenReason.NOT_TENANT_MEMBER, scope = ResourceScope.TENANT))
            }
            dto.copy(query = appendInitiatorCondition(dto.query, initiatorId))
        }

        return managerService.query(effectiveDto)
    }

    /**
     * Dedicated "my instances" endpoint. Any authenticated user may call it; the result is
     * unconditionally scoped to instances initiated by the caller (userId for SYSTEM,
     * tenantMemberId for TENANT). No RBAC triad is consulted here — this is intentional so an
     * admin viewing their personal "my flows" page does not see everyone's flows via read-all.
     *
     * [dto.id] is force-cleared so the [com.lovelycatv.crystalframework.shared.service
     * .BaseManagerService.query] id short-circuit cannot bypass the initiator filter. Callers
     * who need id lookup with cross-user visibility should go through `/query` with read-all
     * authority.
     */
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_APPROVAL_FLOW_INSTANCE,
    )
    @PostMapping("/my", version = "1")
    suspend fun queryMyInstances(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: ManagerReadApprovalFlowInstanceDTO,
    ): ApiResponse<*> {
        val resolvedScope = resolveScope(dto.scope)
        val initiatorId = when (resolvedScope) {
            ResourceScope.SYSTEM -> userAuthentication.userId
            ResourceScope.TENANT -> userAuthentication.tenantMemberId
                ?: throw ForbiddenException("Current user is not a member of this tenant",
                    context = ForbiddenContext(reason = ForbiddenReason.NOT_TENANT_MEMBER, scope = ResourceScope.TENANT))
        }
        val forcedDto = dto.copy(
            id = null,
            query = appendInitiatorCondition(dto.query, initiatorId),
        )
        return ApiResponse.success(managerService.query(forcedDto))
    }

    /**
     * Initiate an approval flow instance from a PUBLISHED definition. Authorization mirrors
     * the standard READ logic on the definition's scope: any user who can read the definition
     * is allowed to initiate. The initiator id stored on the new instance is scope-specific
     * (userId for SYSTEM, tenantMemberId for TENANT) — see [ApprovalFlowEngine.startFlow].
     */
    @Audit(
        action = AuditAction.CREATE,
        resourceType = TableConstants.TABLE_APPROVAL_FLOW_INSTANCE,
    )
    @PostMapping("/start", version = "1")
    suspend fun start(
        userAuthentication: UserAuthentication,
        @Valid @RequestBody dto: StartApprovalFlowDTO
    ): ApiResponse<*> {
        val definitionId = dto.definitionId
            ?: throw BusinessException("definitionId is required")
        val definition = approvalFlowDefinitionManagerService.getByIdOrNull(definitionId)
            ?: throw BusinessException("Definition not found")
        assertDefinitionReadable(definition, userAuthentication)
        val resolvedScope = resolveScope(definition.scope)

        val initiatorId = when (resolvedScope) {
            ResourceScope.SYSTEM -> userAuthentication.userId
            ResourceScope.TENANT -> userAuthentication.tenantMemberId
                ?: throw ForbiddenException("Current user is not a member of this tenant",
                    context = ForbiddenContext(reason = ForbiddenReason.NOT_TENANT_MEMBER, scope = ResourceScope.TENANT))
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

    /**
     * Read-only aggregation for the instance viewer: instance + pinned-version graph +
     * per-node aggregated task status + records.
     *
     * Access follows a short-circuit chain independent of the matrix's read-all triad:
     *
     *   1. read-all admin (matrix READ layers hold) — full visibility inside the scope.
     *   2. the instance initiator — always allowed to see their own flow.
     *   3. a participant assignee — anyone with at least one task assigned on this instance
     *      may view the flow (needed so approvers can inspect other approvers' remarks).
     *
     * Ownership (tenant isolation) is still enforced afterwards.
     */
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_APPROVAL_FLOW_INSTANCE,
        resourceIds = "#instanceId",
    )
    @GetMapping("/details-by-id", version = "1")
    suspend fun detailsById(
        userAuthentication: UserAuthentication,
        @RequestParam instanceId: Long,
    ): ApiResponse<ApprovalFlowInstanceDetailsVO> {
        val instance = managerService.getByIdOrNull(instanceId)
            ?: throw BusinessException("Instance not found")
        assertInstanceViewAccess(instance, userAuthentication)
        return ApiResponse.success(managerService.getInstanceDetails(instance))
    }

    /**
     * Assert the caller may READ the given flow definition, using the DEFINITION permission matrix
     * (permission layer + tenant ownership), with the same decision + exception shape as the base
     * [assertAccess]. The base primitives cannot be reused here: they are bound to this controller's
     * INSTANCE matrix, and this controller further overrides [checkPermission] to allow READ
     * unconditionally — so a definition-scoped decision must consult the DEFINITION matrix directly.
     */
    private suspend fun assertDefinitionReadable(
        definition: ApprovalFlowDefinitionEntity,
        userAuthentication: UserAuthentication,
    ) {
        val scope = resolveScope(definition.scope)
        val matrix = ApprovalManagerPermissionMatrices.DEFINITION
        if (!RbacUtils.hasAnyAuthority(*matrix.layersFor(scope, ScopedOperation.READ))) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = matrix.layersFor(scope, ScopedOperation.READ)
                    .filter { it != PermissionMatrix.NEVER_GRANTED },
                scope = scope,
            ))
        }
        val owned = when (scope) {
            ResourceScope.SYSTEM -> true
            ResourceScope.TENANT ->
                RbacUtils.hasAnyAuthority(*matrix.crossTenantLayersFor(ScopedOperation.READ))
                    || definition.scopeId == userAuthentication.tenantId
        }
        if (!owned) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.SCOPE_MISMATCH,
                scope = scope,
            ))
        }
    }

    /**
     * View authorization for a single instance: the READ authority layer (read-all admin) OR the
     * initiator OR a participating assignee passes, then tenant ownership is enforced. This is the
     * instance-specific fallback the base [assertAccess] cannot express (it only knows
     * permission + ownership), so it lives here and is shared by [detailsById] and [dictOptions].
     */
    private suspend fun assertInstanceViewAccess(
        instance: ApprovalFlowInstanceEntity,
        userAuthentication: UserAuthentication,
    ) {
        val resolvedScope = resolveScope(instance.scope)
        val callerScopedId = when (resolvedScope) {
            ResourceScope.SYSTEM -> userAuthentication.userId
            ResourceScope.TENANT -> userAuthentication.tenantMemberId
                ?: throw ForbiddenException("Current user is not a member of this tenant",
                    context = ForbiddenContext(reason = ForbiddenReason.NOT_TENANT_MEMBER, scope = ResourceScope.TENANT))
        }
        val matrix = permissions
            ?: error("ManagerApprovalFlowInstanceController requires a PermissionMatrix")
        val canReadAll = RbacUtils.hasAnyAuthority(*matrix.layersFor(resolvedScope, ScopedOperation.READ))
        val isInitiator = instance.initiatorId == callerScopedId
        val isParticipant = !canReadAll && !isInitiator
            && managerService.isAssigneeOfInstance(instance.id, callerScopedId)
        if (!(canReadAll || isInitiator || isParticipant)) {
            throw ForbiddenException(context = ForbiddenContext(
                reason = ForbiddenReason.SCOPE_MISMATCH,
                scope = resolvedScope,
            ))
        }
        if (!checkOwnership(resolvedScope, instance.scopeId, ScopedOperation.READ, userAuthentication)) {
            throw UnauthorizedException()
        }
    }

    /**
     * Real-time selectable options for DICT form fields of a running / historical instance, used by
     * the handling form and the read-only viewer. Resolution is based on the instance's snapshot
     * schema (for binding) and the instance's own scope; the client-supplied scope is never used.
     * Authorization reuses [assertInstanceViewAccess] (read-all admin / initiator / participating
     * assignee, any one passes).
     */
    @GetMapping("/dict-options", version = "1")
    suspend fun dictOptions(
        userAuthentication: UserAuthentication,
        @RequestParam instanceId: Long,
        @RequestParam fieldKey: String,
    ): ApiResponse<List<ApprovalDictOptionVO>> {
        val instance = managerService.getByIdOrNull(instanceId)
            ?: throw BusinessException("Instance not found")
        assertInstanceViewAccess(instance, userAuthentication)
        val resolvedScope = resolveScope(instance.scope)
        val options = approvalDictResolver
            .resolveItemsForField(instance.formSchemaSnapshot, fieldKey, resolvedScope, instance.scopeId)
            .map { ApprovalDictOptionVO(value = it.value, label = it.label) }
        return ApiResponse.success(options)
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

package com.lovelycatv.crystalframework.approval.service.engine

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowDefinitionService
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowEdgeService
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowInstanceService
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowNodeService
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowTaskService
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowTokenService
import com.lovelycatv.crystalframework.approval.types.*
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.service.UserServiceTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApprovalFlowEngineTest(
    @Autowired private val approvalFlowEngine: ApprovalFlowEngine,
    @Autowired private val definitionService: ApprovalFlowDefinitionService,
    @Autowired private val nodeService: ApprovalFlowNodeService,
    @Autowired private val edgeService: ApprovalFlowEdgeService,
    @Autowired private val instanceService: ApprovalFlowInstanceService,
    @Autowired private val taskService: ApprovalFlowTaskService,
    @Autowired private val tokenService: ApprovalFlowTokenService,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val userServiceTest: UserServiceTest by lazy { getTestClassInstance(applicationContext) }

    // PLACEHOLDER_HELPERS

    private suspend fun mockUserId(): Long = userServiceTest.mockRegisteredUser().id

    private suspend fun createDefinition(): ApprovalFlowDefinitionEntity {
        val entity = ApprovalFlowDefinitionEntity(
            id = snowIdGenerator.nextId(),
            scope = ApprovalFlowScope.SYSTEM.typeId,
            scopeId = 0,
            name = "TestFlow",
            currentVersion = 1,
            status = ApprovalFlowDefinitionStatus.PUBLISHED.typeId
        ).apply { newEntity() }
        return definitionService.getRepository().save(entity).awaitFirst()
    }

    private suspend fun createNode(
        definitionId: Long,
        version: Int,
        type: ApprovalFlowNodeType,
        key: String,
        config: String? = null
    ): ApprovalFlowNodeEntity {
        val entity = ApprovalFlowNodeEntity(
            id = snowIdGenerator.nextId(),
            definitionId = definitionId,
            definitionVersion = version,
            nodeKey = key,
            type = type.typeId,
            name = key,
            config = config
        ).apply { newEntity() }
        return nodeService.getRepository().save(entity).awaitFirst()
    }

    private suspend fun createEdge(
        definitionId: Long,
        version: Int,
        sourceNodeId: Long,
        targetNodeId: Long
    ): ApprovalFlowEdgeEntity {
        val entity = ApprovalFlowEdgeEntity(
            id = snowIdGenerator.nextId(),
            definitionId = definitionId,
            definitionVersion = version,
            sourceNodeId = sourceNodeId,
            targetNodeId = targetNodeId
        ).apply { newEntity() }
        return edgeService.getRepository().save(entity).awaitFirst()
    }

    private fun approvalConfig(vararg userIds: Long): String {
        return ApprovalNodeConfig(
            approveMode = ApprovalFlowApproveMode.AND.typeId,
            strategy = ApprovalFlowApproverStrategy.SPECIFIED_USER.typeId,
            strategyParams = mapOf("userIds" to userIds.map { it.toString() })
        ).toJSONString()
    }

    private suspend fun getPendingTasks(instanceId: Long): List<com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity> {
        return taskService.getRepository().findByInstanceId(instanceId).toList()
            .filter { it.getRealStatus() == ApprovalFlowTaskStatus.PENDING }
    }

    private suspend fun getActiveTokens(instanceId: Long): List<com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity> {
        return tokenService.findByInstanceIdAndStatus(instanceId, ApprovalFlowTokenStatus.ACTIVE.typeId).toList()
    }

    // PLACEHOLDER_TESTS

    /*
     * Simple linear flow:
     *
     *   START --> A(P1) --> B(P2) --> END
     *
     * Token path:
     *   T1 starts at A, P1 approves, T1 moves to B, P2 approves, T1 moves to END.
     */
    @Test
    fun simpleLinearFlow() {
        withTransactionalRollback("simple-linear-flow") {
            val p1 = mockUserId()
            val p2 = mockUserId()
            val initiator = mockUserId()

            val def = createDefinition()
            val start = createNode(def.id, 1, ApprovalFlowNodeType.START, "start")
            val nodeA = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "A", approvalConfig(p1))
            val nodeB = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "B", approvalConfig(p2))
            val end = createNode(def.id, 1, ApprovalFlowNodeType.END, "end")

            createEdge(def.id, 1, start.id, nodeA.id)
            createEdge(def.id, 1, nodeA.id, nodeB.id)
            createEdge(def.id, 1, nodeB.id, end.id)

            val instance = approvalFlowEngine.startFlow(
                def.id, initiator, ApprovalFlowScope.SYSTEM, 0, "{}"
            )
            assertEquals(ApprovalFlowInstanceStatus.IN_PROGRESS.typeId, instance.status)

            var pendingTasks = getPendingTasks(instance.id)
            assertEquals(1, pendingTasks.size)
            assertEquals(nodeA.id, pendingTasks[0].nodeId)
            assertEquals(p1, pendingTasks[0].assigneeId)

            approvalFlowEngine.handleTask(pendingTasks[0].id, p1, true, "ok", null)

            pendingTasks = getPendingTasks(instance.id)
            assertEquals(1, pendingTasks.size)
            assertEquals(nodeB.id, pendingTasks[0].nodeId)
            assertEquals(p2, pendingTasks[0].assigneeId)

            approvalFlowEngine.handleTask(pendingTasks[0].id, p2, true, "approved", null)

            val finalInstance = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.APPROVED.typeId, finalInstance.status)

            val activeTokens = getActiveTokens(instance.id)
            assertTrue(activeTokens.isEmpty())

            println("[simple] Flow completed successfully")
        }
    }

    // PLACEHOLDER_TEST_MEDIUM

    /*
     * Medium flow with one FORK/JOIN pair plus a CC:
     *
     *                              +-- B(P2) --+
     *                              |           |
     *   START --> A(P1) --> FORK --+           +--> JOIN --> CC --> END
     *                              |           |
     *                              +-- C(P3) --+
     *
     * Token path:
     *   T1 starts at A. P1 approves -> T1 reaches FORK -> T1 completes.
     *   FORK spawns T2 at B and T3 at C (forkNodeId = FORK).
     *   P2 approves -> T2 moves to JOIN -> WAITING (1/2).
     *   P3 approves -> T3 moves to JOIN -> WAITING (2/2) -> merge -> T4 (forkNodeId = null).
     *   T4 passes through CC -> END -> instance APPROVED.
     */
    @Test
    fun mediumForkJoinFlow() {
        withTransactionalRollback("medium-fork-join-flow") {
            val p1 = mockUserId()
            val p2 = mockUserId()
            val p3 = mockUserId()
            val initiator = mockUserId()

            val def = createDefinition()
            val start = createNode(def.id, 1, ApprovalFlowNodeType.START, "start")
            val nodeA = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "A", approvalConfig(p1))
            val fork = createNode(def.id, 1, ApprovalFlowNodeType.FORK, "fork")
            val nodeB = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "B", approvalConfig(p2))
            val nodeC = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "C", approvalConfig(p3))
            val join = createNode(def.id, 1, ApprovalFlowNodeType.JOIN, "join")
            val cc = createNode(def.id, 1, ApprovalFlowNodeType.CC, "cc")
            val end = createNode(def.id, 1, ApprovalFlowNodeType.END, "end")

            createEdge(def.id, 1, start.id, nodeA.id)
            createEdge(def.id, 1, nodeA.id, fork.id)
            createEdge(def.id, 1, fork.id, nodeB.id)
            createEdge(def.id, 1, fork.id, nodeC.id)
            createEdge(def.id, 1, nodeB.id, join.id)
            createEdge(def.id, 1, nodeC.id, join.id)
            createEdge(def.id, 1, join.id, cc.id)
            createEdge(def.id, 1, cc.id, end.id)

            val instance = approvalFlowEngine.startFlow(
                def.id, initiator, ApprovalFlowScope.SYSTEM, 0, "{}"
            )

            var pendingTasks = getPendingTasks(instance.id)
            assertEquals(1, pendingTasks.size)
            assertEquals(p1, pendingTasks[0].assigneeId)
            println("[medium] P1 task created at node A")

            approvalFlowEngine.handleTask(pendingTasks[0].id, p1, true, null, null)

            pendingTasks = getPendingTasks(instance.id)
            assertEquals(2, pendingTasks.size)
            val taskB = pendingTasks.first { it.nodeId == nodeB.id }
            val taskC = pendingTasks.first { it.nodeId == nodeC.id }
            assertEquals(p2, taskB.assigneeId)
            assertEquals(p3, taskC.assigneeId)
            println("[medium] FORK produced 2 parallel tasks at B and C")

            approvalFlowEngine.handleTask(taskB.id, p2, true, null, null)

            var inst = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.IN_PROGRESS.typeId, inst.status)
            println("[medium] P2 approved, flow still in progress (waiting for P3)")

            approvalFlowEngine.handleTask(taskC.id, p3, true, null, null)

            inst = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.APPROVED.typeId, inst.status)

            val activeTokens = getActiveTokens(instance.id)
            assertTrue(activeTokens.isEmpty())
            println("[medium] P3 approved, JOIN merged, CC passed, flow completed")
        }
    }

    // PLACEHOLDER_TEST_COMPLEX

    /*
     * Complex flow with nested FORK/JOIN (2 pairs), 7 approvers, 2 CC nodes:
     *
     *                                         +-- D(P3) --> E(P4) --+
     *                              +-- FORK2 -+                     +--> JOIN2 --> CC1 --+
     *                              |          +-- F(P5) --> G(P6) --+                    |
     *   START --> A(P1) --> FORK1 -+                                                    +--> JOIN1 --> I(P7) --> CC2 --> END
     *                              |                                                    |
     *                              +-- H(P2) -------------------------------------------+
     *
     * Token path:
     *   T1 starts at A. P1 approves -> FORK1 -> T1 completes.
     *   FORK1 spawns T2 (at FORK2, forkNodeId=FORK1) and T3 (at H, forkNodeId=FORK1).
     *   T2 reaches FORK2 -> T2 completes.
     *   FORK2 spawns T4 (at D, forkNodeId=FORK2) and T5 (at F, forkNodeId=FORK2).
     *   P3 approves D -> T4 moves to E. P5 approves F -> T5 moves to G.
     *   P4 approves E -> T4 arrives at JOIN2 -> WAITING (1/2).
     *   P6 approves G -> T5 arrives at JOIN2 -> WAITING (2/2) -> merge -> T6 (forkNodeId=FORK1).
     *   T6 passes through CC1 -> arrives at JOIN1 -> WAITING (1/2).
     *   P2 approves H -> T3 arrives at JOIN1 -> WAITING (2/2) -> merge -> T7 (forkNodeId=null).
     *   T7 reaches I. P7 approves -> passes CC2 -> END -> instance APPROVED.
     */
    @Test
    fun complexNestedForkJoinFlow() {
        withTransactionalRollback("complex-nested-fork-join-flow") {
            val p1 = mockUserId()
            val p2 = mockUserId()
            val p3 = mockUserId()
            val p4 = mockUserId()
            val p5 = mockUserId()
            val p6 = mockUserId()
            val p7 = mockUserId()
            val initiator = mockUserId()

            val def = createDefinition()
            val start = createNode(def.id, 1, ApprovalFlowNodeType.START, "start")
            val nodeA = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "A", approvalConfig(p1))
            val fork1 = createNode(def.id, 1, ApprovalFlowNodeType.FORK, "fork1")
            val fork2 = createNode(def.id, 1, ApprovalFlowNodeType.FORK, "fork2")
            val nodeD = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "D", approvalConfig(p3))
            val nodeE = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "E", approvalConfig(p4))
            val nodeF = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "F", approvalConfig(p5))
            val nodeG = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "G", approvalConfig(p6))
            val join2 = createNode(def.id, 1, ApprovalFlowNodeType.JOIN, "join2")
            val cc1 = createNode(def.id, 1, ApprovalFlowNodeType.CC, "cc1")
            val nodeH = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "H", approvalConfig(p2))
            val join1 = createNode(def.id, 1, ApprovalFlowNodeType.JOIN, "join1")
            val nodeI = createNode(def.id, 1, ApprovalFlowNodeType.APPROVAL, "I", approvalConfig(p7))
            val cc2 = createNode(def.id, 1, ApprovalFlowNodeType.CC, "cc2")
            val end = createNode(def.id, 1, ApprovalFlowNodeType.END, "end")

            // START → A → FORK1
            createEdge(def.id, 1, start.id, nodeA.id)
            createEdge(def.id, 1, nodeA.id, fork1.id)
            // FORK1 branch 1: FORK2 → [D→E, F→G] → JOIN2 → CC1
            createEdge(def.id, 1, fork1.id, fork2.id)
            createEdge(def.id, 1, fork2.id, nodeD.id)
            createEdge(def.id, 1, fork2.id, nodeF.id)
            createEdge(def.id, 1, nodeD.id, nodeE.id)
            createEdge(def.id, 1, nodeE.id, join2.id)
            createEdge(def.id, 1, nodeF.id, nodeG.id)
            createEdge(def.id, 1, nodeG.id, join2.id)
            createEdge(def.id, 1, join2.id, cc1.id)
            createEdge(def.id, 1, cc1.id, join1.id)
            // FORK1 branch 2: H
            createEdge(def.id, 1, fork1.id, nodeH.id)
            createEdge(def.id, 1, nodeH.id, join1.id)
            // JOIN1 → I → CC2 → END
            createEdge(def.id, 1, join1.id, nodeI.id)
            createEdge(def.id, 1, nodeI.id, cc2.id)
            createEdge(def.id, 1, cc2.id, end.id)

            val instance = approvalFlowEngine.startFlow(
                def.id, initiator, ApprovalFlowScope.SYSTEM, 0, "{}"
            )

            // Step 1: P1 approves at node A
            var pendingTasks = getPendingTasks(instance.id)
            assertEquals(1, pendingTasks.size)
            assertEquals(p1, pendingTasks[0].assigneeId)
            approvalFlowEngine.handleTask(pendingTasks[0].id, p1, true, null, null)
            println("[complex] P1 approved → FORK1 → FORK2 + H")

            // Step 2: After FORK1→FORK2, expect tasks at D, F, H
            pendingTasks = getPendingTasks(instance.id)
            assertEquals(3, pendingTasks.size)
            val taskD = pendingTasks.first { it.nodeId == nodeD.id }
            val taskF = pendingTasks.first { it.nodeId == nodeF.id }
            val taskH = pendingTasks.first { it.nodeId == nodeH.id }
            assertEquals(p3, taskD.assigneeId)
            assertEquals(p5, taskF.assigneeId)
            assertEquals(p2, taskH.assigneeId)
            println("[complex] 3 parallel tasks: D(P3), F(P5), H(P2)")

            // Step 3: P3 approves D → token moves to E
            approvalFlowEngine.handleTask(taskD.id, p3, true, null, null)
            pendingTasks = getPendingTasks(instance.id)
            val taskE = pendingTasks.first { it.nodeId == nodeE.id }
            assertEquals(p4, taskE.assigneeId)
            println("[complex] P3 approved D → E(P4) pending")

            // Step 4: P5 approves F → token moves to G
            approvalFlowEngine.handleTask(taskF.id, p5, true, null, null)
            pendingTasks = getPendingTasks(instance.id)
            val taskG = pendingTasks.first { it.nodeId == nodeG.id }
            assertEquals(p6, taskG.assigneeId)
            println("[complex] P5 approved F → G(P6) pending")

            // Step 5: P4 approves E → token arrives at JOIN2 (waiting)
            approvalFlowEngine.handleTask(taskE.id, p4, true, null, null)
            var inst = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.IN_PROGRESS.typeId, inst.status)
            println("[complex] P4 approved E → JOIN2 waiting (1/2)")

            // Step 6: P6 approves G → JOIN2 merges → CC1 → arrives at JOIN1 (waiting)
            approvalFlowEngine.handleTask(taskG.id, p6, true, null, null)
            inst = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.IN_PROGRESS.typeId, inst.status)
            println("[complex] P6 approved G → JOIN2 merged → CC1 → JOIN1 waiting (1/2)")

            // Step 7: P2 approves H → arrives at JOIN1 → merges → I
            approvalFlowEngine.handleTask(taskH.id, p2, true, null, null)
            pendingTasks = getPendingTasks(instance.id)
            assertEquals(1, pendingTasks.size)
            val taskI = pendingTasks.first { it.nodeId == nodeI.id }
            assertEquals(p7, taskI.assigneeId)
            println("[complex] P2 approved H → JOIN1 merged → I(P7) pending")

            // Step 8: P7 approves I → CC2 → END
            approvalFlowEngine.handleTask(taskI.id, p7, true, null, null)
            inst = instanceService.getByIdOrThrow(instance.id)
            assertEquals(ApprovalFlowInstanceStatus.APPROVED.typeId, inst.status)

            val activeTokens = getActiveTokens(instance.id)
            assertTrue(activeTokens.isEmpty())
            println("[complex] P7 approved I → CC2 → END. Flow completed!")
        }
    }
}

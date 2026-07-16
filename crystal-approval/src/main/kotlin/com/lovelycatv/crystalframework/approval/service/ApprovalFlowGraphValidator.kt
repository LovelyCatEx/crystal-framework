package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO.GraphEdgeDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO.GraphNodeDTO
import com.lovelycatv.crystalframework.approval.types.*
import com.lovelycatv.crystalframework.shared.utils.parseObject

/**
 * Validates the structural correctness of an approval flow graph before persistence.
 */
object ApprovalFlowGraphValidator {

    /**
     * Validates the given graph DTO against the following 36 rules:
     *
     * --- Node basics ---
     * 1.  Must have exactly 1 START node
     * 2.  Must have exactly 1 END node
     * 3.  nodeKey must not be blank
     * 4.  nodeKey must be unique within the graph
     * 5.  type must be a valid enum value (0-6)
     *
     * --- Node configs ---
     * 6.  APPROVAL node must have parseable ApprovalNodeConfig
     * 7.  APPROVAL node strategy must be valid (0-4)
     * 8.  APPROVAL node with SPECIFIED_USER strategy must have at least 1 userId
     * 9.  CONDITION node must have parseable ConditionNodeConfig
     * 10. CONDITION node routes must not be empty
     * 11. CONDITION node route targetNodeId must reference existing node (deferred)
     * 12. CONDITION node route condition tree must not be empty
     * 13. CC node must have parseable CcNodeConfig
     * 14. FORK/JOIN nodes must not have config (or config is null)
     *
     * --- Edge basics ---
     * 15. Edge sourceNodeKey and targetNodeKey must exist in node list
     * 16. START node must have no incoming edges
     * 17. START node must have exactly 1 outgoing edge
     * 18. END node must have no outgoing edges
     * 19. END node must have at least 1 incoming edge
     * 20. No self-loops allowed (source == target)
     * 21. No duplicate edges (same source-target pair)
     *
     * --- Connectivity ---
     * 22. All nodes must be reachable from START (forward BFS)
     * 23. All nodes must be able to reach END (reverse BFS)
     * 24. Graph must be acyclic (topological sort / Kahn's algorithm)
     *
     * --- FORK/JOIN structure ---
     * 25. FORK node must have at least 2 outgoing edges
     * 26. FORK node must have exactly 1 incoming edge
     * 27. JOIN node must have at least 2 incoming edges
     * 28. JOIN node must have exactly 1 outgoing edge
     * 29. Each FORK's branches must converge to the same JOIN
     * 30. FORK/JOIN pairs must not cross-nest (strict nesting like brackets)
     * 31. No cross-edges between branches of the same FORK
     *
     * --- CONDITION routes ---
     * 32. CONDITION node outgoing edge count must >= route count
     * 33. CONDITION route targets must be direct successors (deferred)
     *
     * --- Node degree ---
     * 34. APPROVAL node must have exactly 1 outgoing edge
     * 35. CC node must have exactly 1 outgoing edge
     * 36. APPROVAL/CC/CONDITION nodes must have at least 1 incoming edge
     */
    fun validate(dto: ManagerUpdateApprovalFlowGraphDTO): List<String> {
        val errors = mutableListOf<String>()
        val nodes = dto.nodes
        val edges = dto.edges

        errors += validateNodeBasics(nodes)
        errors += validateNodeConfigs(nodes)
        errors += validateEdgeBasics(nodes, edges)
        errors += validateConnectivity(nodes, edges)
        errors += validateForkJoinStructure(nodes, edges)
        errors += validateConditionRoutes(nodes, edges)
        errors += validateNodeDegrees(nodes, edges)

        return errors
    }

    private fun validateNodeBasics(nodes: List<GraphNodeDTO>): List<String> {
        val errors = mutableListOf<String>()

        // Rule 1: Must have exactly 1 START node
        val startCount = nodes.count { it.type == ApprovalFlowNodeType.START.typeId }
        if (startCount == 0) errors += "Graph must have a START node"
        if (startCount > 1) errors += "Graph must have exactly 1 START node, found $startCount"

        // Rule 2: Must have exactly 1 END node
        val endCount = nodes.count { it.type == ApprovalFlowNodeType.END.typeId }
        if (endCount == 0) errors += "Graph must have an END node"
        if (endCount > 1) errors += "Graph must have exactly 1 END node, found $endCount"

        // Rule 3: nodeKey must not be blank
        nodes.forEachIndexed { i, node ->
            if (node.nodeKey.isBlank()) {
                errors += "Node at index $i has blank nodeKey"
            }
        }

        // Rule 4: nodeKey must be unique within the graph
        val duplicateKeys = nodes.groupBy { it.nodeKey }.filter { it.value.size > 1 }.keys
        if (duplicateKeys.isNotEmpty()) {
            errors += "Duplicate nodeKeys: ${duplicateKeys.joinToString(", ")}"
        }

        // Rule 5: type must be a valid enum value (0-6)
        val validTypeIds = ApprovalFlowNodeType.entries.map { it.typeId }.toSet()
        nodes.forEach { node ->
            if (node.type !in validTypeIds) {
                errors += "Node '${node.nodeKey}' has invalid type: ${node.type}"
            }
        }

        return errors
    }

    private fun validateNodeConfigs(nodes: List<GraphNodeDTO>): List<String> {
        val errors = mutableListOf<String>()

        nodes.forEach { node ->
            when (node.type) {
                ApprovalFlowNodeType.APPROVAL.typeId -> {
                    // Rule 6: APPROVAL node must have parseable ApprovalNodeConfig
                    val config = runCatching { node.config?.parseObject<ApprovalNodeConfig>() }.getOrNull()
                    if (config == null) {
                        errors += "APPROVAL node '${node.nodeKey}' has invalid or missing config"
                    } else {
                        // Rule 7: APPROVAL node strategy must be valid (0-4)
                        if (ApprovalFlowApproverStrategy.getById(config.strategy) == null) {
                            errors += "APPROVAL node '${node.nodeKey}' has invalid strategy: ${config.strategy}"
                        }
                        // Rule 8: APPROVAL node with SPECIFIED_USER strategy must have at least 1 userId or memberIds
                        if (ApprovalFlowApproverStrategy.getById(config.strategy) == ApprovalFlowApproverStrategy.SPECIFIED_USER) {
                            @Suppress("UNCHECKED_CAST")
                            val userIds = config.strategyParams["userIds"] as? List<*> ?: emptyList<Any>()
                            @Suppress("UNCHECKED_CAST")
                            val memberIds = config.strategyParams["memberIds"] as? List<*> ?: emptyList<Any>()
                            if (userIds.isEmpty() && memberIds.isEmpty()) {
                                errors += "APPROVAL node '${node.nodeKey}' with SPECIFIED_USER strategy must have at least 1 userId or memberIds"
                            }
                        }
                    }
                }
                ApprovalFlowNodeType.CONDITION.typeId -> {
                    // Rule 9: CONDITION node must have parseable ConditionNodeConfig
                    val config = runCatching { node.config?.parseObject<ConditionNodeConfig>() }.getOrNull()
                    if (config == null) {
                        errors += "CONDITION node '${node.nodeKey}' has invalid or missing config"
                    } else {
                        // Rule 10: CONDITION node routes must not be empty
                        if (config.routes.isEmpty()) {
                            errors += "CONDITION node '${node.nodeKey}' must have at least 1 route"
                        }
                        // Rule 12: CONDITION node route condition tree must not be empty
                        config.routes.forEachIndexed { i, route ->
                            if (!isConditionTreeNonEmpty(route.condition)) {
                                errors += "CONDITION node '${node.nodeKey}' route[$i] has empty condition tree"
                            }
                        }
                    }
                }
                ApprovalFlowNodeType.CC.typeId -> {
                    // Rule 13: CC node must have parseable CcNodeConfig with non-empty channelIds
                    if (node.config != null) {
                        val config = runCatching { node.config!!.parseObject<CcNodeConfig>() }.getOrNull()
                        if (config == null) {
                            errors += "CC node '${node.nodeKey}' has invalid config"
                        } else if (config.channelIds.isEmpty()) {
                            errors += "CC node '${node.nodeKey}' must have at least one channelId"
                        }
                    } else {
                        errors += "CC node '${node.nodeKey}' must have config"
                    }
                }
                ApprovalFlowNodeType.FORK.typeId, ApprovalFlowNodeType.JOIN.typeId -> {
                    // Rule 14: FORK/JOIN nodes must not have config
                    if (node.config != null && node.config!!.isNotBlank()) {
                        errors += "${ApprovalFlowNodeType.getById(node.type)?.name} node '${node.nodeKey}' must not have config"
                    }
                }
            }
        }

        return errors
    }

    private fun isConditionTreeNonEmpty(node: ConditionNode): Boolean {
        return when (node) {
            is ConditionLeaf -> true
            is ConditionGroup -> node.children.isNotEmpty() && node.children.any { isConditionTreeNonEmpty(it) }
        }
    }

    private fun validateEdgeBasics(nodes: List<GraphNodeDTO>, edges: List<GraphEdgeDTO>): List<String> {
        val errors = mutableListOf<String>()
        val nodeKeys = nodes.map { it.nodeKey }.toSet()

        edges.forEach { edge ->
            // Rule 15: Edge sourceNodeKey and targetNodeKey must exist in node list
            if (edge.sourceNodeKey !in nodeKeys) {
                errors += "Edge references unknown source nodeKey: '${edge.sourceNodeKey}'"
            }
            if (edge.targetNodeKey !in nodeKeys) {
                errors += "Edge references unknown target nodeKey: '${edge.targetNodeKey}'"
            }
            // Rule 20: No self-loops allowed
            if (edge.sourceNodeKey == edge.targetNodeKey) {
                errors += "Self-loop detected on node '${edge.sourceNodeKey}'"
            }
        }

        // Rule 21: No duplicate edges
        val duplicates = edges.groupBy { "${it.sourceNodeKey}->${it.targetNodeKey}" }.filter { it.value.size > 1 }
        duplicates.keys.forEach { errors += "Duplicate edge: $it" }

        val startKey = nodes.firstOrNull { it.type == ApprovalFlowNodeType.START.typeId }?.nodeKey
        val endKey = nodes.firstOrNull { it.type == ApprovalFlowNodeType.END.typeId }?.nodeKey

        if (startKey != null) {
            // Rule 16: START node must have no incoming edges
            val startIncoming = edges.count { it.targetNodeKey == startKey }
            if (startIncoming > 0) errors += "START node must have no incoming edges, found $startIncoming"
            // Rule 17: START node must have exactly 1 outgoing edge
            val startOutgoing = edges.count { it.sourceNodeKey == startKey }
            if (startOutgoing != 1) errors += "START node must have exactly 1 outgoing edge, found $startOutgoing"
        }

        if (endKey != null) {
            // Rule 18: END node must have no outgoing edges
            val endOutgoing = edges.count { it.sourceNodeKey == endKey }
            if (endOutgoing > 0) errors += "END node must have no outgoing edges, found $endOutgoing"
            // Rule 19: END node must have at least 1 incoming edge
            val endIncoming = edges.count { it.targetNodeKey == endKey }
            if (endIncoming < 1) errors += "END node must have at least 1 incoming edge"
        }

        return errors
    }

    private fun validateConnectivity(nodes: List<GraphNodeDTO>, edges: List<GraphEdgeDTO>): List<String> {
        val errors = mutableListOf<String>()
        val nodeKeys = nodes.map { it.nodeKey }.toSet()
        val startKey = nodes.firstOrNull { it.type == ApprovalFlowNodeType.START.typeId }?.nodeKey ?: return errors
        val endKey = nodes.firstOrNull { it.type == ApprovalFlowNodeType.END.typeId }?.nodeKey ?: return errors

        val adjacency = mutableMapOf<String, MutableList<String>>()
        val reverseAdjacency = mutableMapOf<String, MutableList<String>>()
        nodeKeys.forEach { adjacency[it] = mutableListOf(); reverseAdjacency[it] = mutableListOf() }
        edges.forEach { edge ->
            adjacency[edge.sourceNodeKey]?.add(edge.targetNodeKey)
            reverseAdjacency[edge.targetNodeKey]?.add(edge.sourceNodeKey)
        }

        // Rule 22: All nodes must be reachable from START
        val forwardReachable = bfs(startKey, adjacency)
        val unreachableFromStart = nodeKeys - forwardReachable
        if (unreachableFromStart.isNotEmpty()) {
            errors += "Nodes not reachable from START: ${unreachableFromStart.joinToString(", ")}"
        }

        // Rule 23: All nodes must be able to reach END
        val backwardReachable = bfs(endKey, reverseAdjacency)
        val cannotReachEnd = nodeKeys - backwardReachable
        if (cannotReachEnd.isNotEmpty()) {
            errors += "Nodes that cannot reach END: ${cannotReachEnd.joinToString(", ")}"
        }

        // Rule 24: Graph must be acyclic (Kahn's algorithm)
        if (!isAcyclic(nodeKeys, edges)) {
            errors += "Graph contains a cycle"
        }

        return errors
    }

    private fun bfs(start: String, adjacency: Map<String, List<String>>): Set<String> {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(start)
        visited.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            adjacency[current]?.forEach { neighbor ->
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited
    }

    private fun isAcyclic(nodeKeys: Set<String>, edges: List<GraphEdgeDTO>): Boolean {
        val inDegree = nodeKeys.associateWith { 0 }.toMutableMap()
        val adjacency = nodeKeys.associateWith { mutableListOf<String>() }.toMutableMap()
        edges.forEach { edge ->
            if (edge.sourceNodeKey in nodeKeys && edge.targetNodeKey in nodeKeys) {
                adjacency[edge.sourceNodeKey]!!.add(edge.targetNodeKey)
                inDegree[edge.targetNodeKey] = inDegree[edge.targetNodeKey]!! + 1
            }
        }
        val queue = ArrayDeque(inDegree.filter { it.value == 0 }.keys)
        var processed = 0
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            processed++
            adjacency[current]!!.forEach { neighbor ->
                inDegree[neighbor] = inDegree[neighbor]!! - 1
                if (inDegree[neighbor] == 0) queue.add(neighbor)
            }
        }
        return processed == nodeKeys.size
    }

    // PLACEHOLDER_VALIDATE_FORK_JOIN

    private fun validateForkJoinStructure(nodes: List<GraphNodeDTO>, edges: List<GraphEdgeDTO>): List<String> {
        val errors = mutableListOf<String>()
        val nodeByKey = nodes.associateBy { it.nodeKey }

        val forkNodes = nodes.filter { it.type == ApprovalFlowNodeType.FORK.typeId }
        val joinNodes = nodes.filter { it.type == ApprovalFlowNodeType.JOIN.typeId }

        forkNodes.forEach { fork ->
            // Rule 25: FORK node must have at least 2 outgoing edges
            val outgoing = edges.count { it.sourceNodeKey == fork.nodeKey }
            if (outgoing < 2) errors += "FORK node '${fork.nodeKey}' must have at least 2 outgoing edges, found $outgoing"
            // Rule 26: FORK node must have exactly 1 incoming edge
            val incoming = edges.count { it.targetNodeKey == fork.nodeKey }
            if (incoming != 1) errors += "FORK node '${fork.nodeKey}' must have exactly 1 incoming edge, found $incoming"
        }

        joinNodes.forEach { join ->
            // Rule 27: JOIN node must have at least 2 incoming edges
            val incoming = edges.count { it.targetNodeKey == join.nodeKey }
            if (incoming < 2) errors += "JOIN node '${join.nodeKey}' must have at least 2 incoming edges, found $incoming"
            // Rule 28: JOIN node must have exactly 1 outgoing edge
            val outgoing = edges.count { it.sourceNodeKey == join.nodeKey }
            if (outgoing != 1) errors += "JOIN node '${join.nodeKey}' must have exactly 1 outgoing edge, found $outgoing"
        }

        val adjacency = mutableMapOf<String, MutableList<String>>()
        nodes.forEach { adjacency[it.nodeKey] = mutableListOf() }
        edges.forEach { adjacency[it.sourceNodeKey]?.add(it.targetNodeKey) }

        forkNodes.forEach { fork ->
            val branchTargets = edges.filter { it.sourceNodeKey == fork.nodeKey }.map { it.targetNodeKey }

            // Rule 29: Each FORK's branches must converge to the same JOIN
            val joinNodeKey = findMatchingJoin(fork.nodeKey, branchTargets, adjacency, nodeByKey)
            if (joinNodeKey == null) {
                errors += "FORK node '${fork.nodeKey}' branches do not converge to a single JOIN"
            } else {
                // Rule 30 + 31: No cross-nesting and no cross-edges between branches
                val branchNodeSets = branchTargets.map { branchStart ->
                    collectBranchNodes(branchStart, joinNodeKey, adjacency)
                }
                for (i in branchNodeSets.indices) {
                    for (j in i + 1 until branchNodeSets.size) {
                        val overlap = branchNodeSets[i].intersect(branchNodeSets[j])
                        val overlapWithoutJoin = overlap - joinNodeKey
                        if (overlapWithoutJoin.isNotEmpty()) {
                            errors += "FORK '${fork.nodeKey}' has cross-edges between branches at nodes: ${overlapWithoutJoin.joinToString(", ")}"
                        }
                    }
                }
            }
        }

        return errors
    }

    private fun findMatchingJoin(
        forkKey: String,
        branchStarts: List<String>,
        adjacency: Map<String, List<String>>,
        nodeByKey: Map<String, GraphNodeDTO>
    ): String? {
        val convergencePoints = branchStarts.map { start ->
            collectReachable(start, adjacency)
        }
        if (convergencePoints.isEmpty()) return null
        val common = convergencePoints.reduce { acc, set -> acc.intersect(set).toMutableSet() }
        val joinCandidates = common.filter { nodeByKey[it]?.type == ApprovalFlowNodeType.JOIN.typeId }
        if (joinCandidates.isEmpty()) return null
        return findClosestJoin(branchStarts, joinCandidates, adjacency)
    }

    private fun findClosestJoin(
        branchStarts: List<String>,
        joinCandidates: List<String>,
        adjacency: Map<String, List<String>>
    ): String? {
        for (candidate in joinCandidates) {
            val allReachDirectly = branchStarts.all { start ->
                canReachWithout(start, candidate, adjacency, joinCandidates.toSet() - candidate)
            }
            if (allReachDirectly) return candidate
        }
        return joinCandidates.firstOrNull()
    }

    private fun canReachWithout(
        from: String,
        target: String,
        adjacency: Map<String, List<String>>,
        forbidden: Set<String>
    ): Boolean {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(from)
        visited.add(from)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == target) return true
            adjacency[current]?.forEach { neighbor ->
                if (neighbor !in visited && neighbor !in forbidden) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return false
    }

    private fun collectBranchNodes(
        start: String,
        stopAt: String,
        adjacency: Map<String, List<String>>
    ): Set<String> {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(start)
        visited.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == stopAt) continue
            adjacency[current]?.forEach { neighbor ->
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited
    }

    private fun collectReachable(start: String, adjacency: Map<String, List<String>>): Set<String> {
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(start)
        visited.add(start)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            adjacency[current]?.forEach { neighbor ->
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }
        return visited
    }

    // PLACEHOLDER_VALIDATE_CONDITION_DEGREES

    private fun validateConditionRoutes(nodes: List<GraphNodeDTO>, edges: List<GraphEdgeDTO>): List<String> {
        val errors = mutableListOf<String>()

        nodes.filter { it.type == ApprovalFlowNodeType.CONDITION.typeId }.forEach { node ->
            val config = runCatching { node.config?.parseObject<ConditionNodeConfig>() }.getOrNull() ?: return@forEach
            val outgoingCount = edges.count { it.sourceNodeKey == node.nodeKey }
            // Rule 32: CONDITION node outgoing edge count must >= route count
            if (outgoingCount < config.routes.size) {
                errors += "CONDITION node '${node.nodeKey}' has ${config.routes.size} routes but only $outgoingCount outgoing edges"
            }
        }

        return errors
    }

    private fun validateNodeDegrees(nodes: List<GraphNodeDTO>, edges: List<GraphEdgeDTO>): List<String> {
        val errors = mutableListOf<String>()

        nodes.forEach { node ->
            val outgoing = edges.count { it.sourceNodeKey == node.nodeKey }
            val incoming = edges.count { it.targetNodeKey == node.nodeKey }

            when (node.type) {
                ApprovalFlowNodeType.APPROVAL.typeId -> {
                    // Rule 34: APPROVAL node must have exactly 1 outgoing edge
                    if (outgoing != 1) errors += "APPROVAL node '${node.nodeKey}' must have exactly 1 outgoing edge, found $outgoing"
                    // Rule 36: APPROVAL node must have at least 1 incoming edge
                    if (incoming < 1) errors += "APPROVAL node '${node.nodeKey}' must have at least 1 incoming edge"
                }
                ApprovalFlowNodeType.CC.typeId -> {
                    // Rule 35: CC node must have exactly 1 outgoing edge
                    if (outgoing != 1) errors += "CC node '${node.nodeKey}' must have exactly 1 outgoing edge, found $outgoing"
                    // Rule 36: CC node must have at least 1 incoming edge
                    if (incoming < 1) errors += "CC node '${node.nodeKey}' must have at least 1 incoming edge"
                }
                ApprovalFlowNodeType.CONDITION.typeId -> {
                    // Rule 36: CONDITION node must have at least 1 incoming edge
                    if (incoming < 1) errors += "CONDITION node '${node.nodeKey}' must have at least 1 incoming edge"
                }
            }
        }

        return errors
    }
}

package com.lovelycatv.crystalframework.approval.controller.manager.dto

class ManagerUpdateApprovalFlowGraphDTO(
    var definitionId: Long = 0,
    var nodes: List<GraphNodeDTO> = emptyList(),
    var edges: List<GraphEdgeDTO> = emptyList(),
) {
    class GraphNodeDTO(
        var nodeKey: String = "",
        var type: Int = 0,
        var name: String = "",
        var config: String? = null,
        var formSchema: String? = null,
        var positionX: Int = 0,
        var positionY: Int = 0,
    )

    class GraphEdgeDTO(
        var sourceNodeKey: String = "",
        var targetNodeKey: String = "",
    )
}

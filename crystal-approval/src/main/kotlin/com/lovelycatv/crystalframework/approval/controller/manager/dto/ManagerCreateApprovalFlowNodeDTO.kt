package com.lovelycatv.crystalframework.approval.controller.manager.dto

class ManagerCreateApprovalFlowNodeDTO(
    var definitionId: Long = 0,
    var definitionVersion: Int = 0,
    var nodeKey: String = "",
    var type: Int = 0,
    var name: String = "",
    var config: String? = null,
    var formSchema: String? = null,
    var positionX: Int = 0,
    var positionY: Int = 0,
)

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowNodeType(val typeId: Int) {
    START(0),
    END(1),
    APPROVAL(2),
    CONDITION(3),
    CC(4);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

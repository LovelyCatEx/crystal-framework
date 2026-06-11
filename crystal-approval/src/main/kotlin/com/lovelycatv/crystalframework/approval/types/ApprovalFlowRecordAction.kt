package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowRecordAction(val typeId: Int) {
    INITIATE(0),
    APPROVE(1),
    REJECT(2),
    SYSTEM_FORWARD(3);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowTokenStatus(val typeId: Int) {
    ACTIVE(0),
    WAITING(1),
    COMPLETED(2);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

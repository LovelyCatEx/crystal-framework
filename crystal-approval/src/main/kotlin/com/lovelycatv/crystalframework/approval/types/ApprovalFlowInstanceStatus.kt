package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowInstanceStatus(val typeId: Int) {
    IN_PROGRESS(0),
    APPROVED(1),
    REJECTED(2),
    CANCELLED(3);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

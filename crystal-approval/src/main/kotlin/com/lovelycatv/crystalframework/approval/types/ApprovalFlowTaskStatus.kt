package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowTaskStatus(val typeId: Int) {
    PENDING(0),
    APPROVED(1),
    REJECTED(2),
    SKIPPED(3);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

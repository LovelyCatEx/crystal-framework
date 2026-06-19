package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowApproveMode(val typeId: Int) {
    AND(0),
    OR(1);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

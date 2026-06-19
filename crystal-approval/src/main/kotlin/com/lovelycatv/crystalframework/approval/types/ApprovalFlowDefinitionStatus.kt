package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowDefinitionStatus(val typeId: Int) {
    DRAFT(0),
    PUBLISHED(1),
    DISABLED(2);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}

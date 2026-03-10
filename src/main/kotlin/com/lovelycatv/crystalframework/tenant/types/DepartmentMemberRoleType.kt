package com.lovelycatv.crystalframework.tenant.types

enum class DepartmentMemberRoleType(val typeId: Int) {
    MEMBER(0),
    LEADER(1);

    companion object {
        fun getById(id: Int): DepartmentMemberRoleType? {
            return entries.find { it.typeId == id }
        }
    }
}

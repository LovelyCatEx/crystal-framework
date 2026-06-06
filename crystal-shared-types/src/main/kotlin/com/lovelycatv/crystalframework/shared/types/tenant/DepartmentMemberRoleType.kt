package com.lovelycatv.crystalframework.shared.types.tenant

enum class DepartmentMemberRoleType(val typeId: Int) {
    MEMBER(0),
    ADMIN(1),
    SUPER_ADMIN(2);

    companion object {
        fun getById(id: Int): DepartmentMemberRoleType? {
            return entries.find { it.typeId == id }
        }
    }
}

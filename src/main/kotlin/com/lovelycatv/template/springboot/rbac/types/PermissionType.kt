package com.lovelycatv.template.springboot.rbac.types

enum class PermissionType(val typeId: Int) {
    ACTION(0),
    MENU(1);

    companion object {
        fun getById(id: Int): PermissionType? {
            return entries.find { it.typeId == id }
        }
    }
}
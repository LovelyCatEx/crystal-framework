package com.lovelycatv.crystalframework.sdk.rbac.types

data class RbacPermissionDeclaration(
    val name: String,
    val description: String = name,
    val type: RbacPermissionType,
    val path: String? = null,
    val module: String? = null,
) {
    companion object {
        fun action(
            name: String,
            description: String = name,
            module: String? = null,
        ): RbacPermissionDeclaration {
            return RbacPermissionDeclaration(
                name = name,
                description = description,
                type = RbacPermissionType.ACTION,
                module = module,
            )
        }

        fun menu(
            name: String,
            path: String,
            description: String = name,
            module: String? = null,
        ): RbacPermissionDeclaration {
            return RbacPermissionDeclaration(
                name = name,
                description = description,
                type = RbacPermissionType.MENU,
                path = path,
                module = module,
            )
        }

        fun component(
            name: String,
            path: String,
            description: String = name,
            module: String? = null,
        ): RbacPermissionDeclaration {
            return RbacPermissionDeclaration(
                name = name,
                description = description,
                type = RbacPermissionType.COMPONENT,
                path = path,
                module = module,
            )
        }
    }
}

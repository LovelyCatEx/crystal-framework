package com.lovelycatv.crystalframework.sdk.rbac.system.types

data class SystemRbacPermissionDeclaration(
    val name: String,
    val description: String = name,
    val type: SystemPermissionType,
    val path: String? = null,
    val module: String? = null,
) {
    companion object {
        fun action(
            name: String,
            description: String = name,
            module: String? = null,
        ): SystemRbacPermissionDeclaration {
            return SystemRbacPermissionDeclaration(
                name = name,
                description = description,
                type = SystemPermissionType.ACTION,
                module = module,
            )
        }

        fun menu(
            name: String,
            path: String,
            description: String = name,
            module: String? = null,
        ): SystemRbacPermissionDeclaration {
            return SystemRbacPermissionDeclaration(
                name = name,
                description = description,
                type = SystemPermissionType.MENU,
                path = path,
                module = module,
            )
        }

        fun component(
            name: String,
            path: String,
            description: String = name,
            module: String? = null,
        ): SystemRbacPermissionDeclaration {
            return SystemRbacPermissionDeclaration(
                name = name,
                description = description,
                type = SystemPermissionType.COMPONENT,
                path = path,
                module = module,
            )
        }
    }
}

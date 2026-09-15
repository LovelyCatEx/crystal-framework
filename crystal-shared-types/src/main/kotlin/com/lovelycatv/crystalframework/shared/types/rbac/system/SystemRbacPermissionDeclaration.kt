/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.rbac.system

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

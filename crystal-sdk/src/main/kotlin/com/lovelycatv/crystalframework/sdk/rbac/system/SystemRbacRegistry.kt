package com.lovelycatv.crystalframework.sdk.rbac.system

import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRoleDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.system.types.SystemRolePermissionBindingDeclaration

class SystemRbacRegistry {
    private val permissions = linkedMapOf<String, SystemRbacPermissionDeclaration>()
    private val roles = linkedMapOf<String, SystemRoleDeclaration>()
    private val rolePermissionBindings = linkedMapOf<String, LinkedHashSet<String>>()
    private val grantAllRoles = linkedSetOf<String>()

    fun permission(permission: SystemRbacPermissionDeclaration) {
        val permissionName = permission.name.trim()
        if (permissionName.isBlank()) {
            return
        }

        if (permissions.putIfAbsent(permissionName, permission.copy(name = permissionName)) != null) {
            throw IllegalStateException("SystemRbacRegistry: duplicate permission name '$permissionName'")
        }
    }

    fun permissions(permissions: Iterable<SystemRbacPermissionDeclaration>) {
        permissions.forEach { permission(it) }
    }

    fun role(role: SystemRoleDeclaration) {
        val roleName = role.name.trim()
        if (roleName.isBlank()) {
            return
        }

        if (roles.putIfAbsent(roleName, role.copy(name = roleName)) != null) {
            throw IllegalStateException("SystemRbacRegistry: duplicate role name '$roleName'")
        }
    }

    fun roles(roles: Iterable<SystemRoleDeclaration>) {
        roles.forEach { role(it) }
    }

    /**
     * Bind permissions to a role.
     *
     * @param roleName role key/name registered by [role].
     * @param permissionNames permission keys/names registered by [permission], not raw menu/component declarations.
     * For example, use `dashboard.business.statistics` instead of
     * `dashboard.business.statistics@dashboard.business.statistics`.
     */
    fun bind(roleName: String, vararg permissionNames: String) {
        bind(roleName, permissionNames.asIterable())
    }

    /**
     * Bind permissions to a role.
     *
     * @param roleName role key/name registered by [role].
     * @param permissionNames permission keys/names registered by [permission], not raw menu/component declarations.
     * For example, use `dashboard.business.statistics` instead of
     * `dashboard.business.statistics@dashboard.business.statistics`.
     */
    fun bind(roleName: String, permissionNames: Iterable<String>) {
        val normalizedRoleName = roleName.trim()
        if (normalizedRoleName.isBlank()) {
            return
        }

        val bindings = rolePermissionBindings.getOrPut(normalizedRoleName) { linkedSetOf() }
        permissionNames
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { bindings.add(it) }
    }

    fun grantAll(roleName: String) {
        val normalizedRoleName = roleName.trim()
        if (normalizedRoleName.isNotBlank()) {
            grantAllRoles.add(normalizedRoleName)
        }
    }

    fun permissionDeclarations(): List<SystemRbacPermissionDeclaration> {
        return permissions.values.toList()
    }

    fun roleDeclarations(): List<SystemRoleDeclaration> {
        return roles.values.toList()
    }

    fun rolePermissionBindings(): List<SystemRolePermissionBindingDeclaration> {
        return rolePermissionBindings.map { (roleName, permissionNames) ->
            SystemRolePermissionBindingDeclaration(
                roleName = roleName,
                permissionNames = permissionNames.toSet(),
            )
        }
    }

    fun grantAllRoleNames(): Set<String> {
        return grantAllRoles.toSet()
    }
}

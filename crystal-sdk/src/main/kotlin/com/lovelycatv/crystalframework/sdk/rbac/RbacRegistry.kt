package com.lovelycatv.crystalframework.sdk.rbac

import com.lovelycatv.crystalframework.sdk.rbac.types.RbacPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.types.RbacRoleDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.types.RbacRolePermissionBindingDeclaration

class RbacRegistry {
    private val permissions = linkedMapOf<String, RbacPermissionDeclaration>()
    private val roles = linkedMapOf<String, RbacRoleDeclaration>()
    private val rolePermissionBindings = linkedMapOf<String, LinkedHashSet<String>>()
    private val grantAllRoles = linkedSetOf<String>()

    fun permission(permission: RbacPermissionDeclaration) {
        val permissionName = permission.name.trim()
        if (permissionName.isBlank()) {
            return
        }

        permissions.putIfAbsent(
            permissionName,
            permission.copy(name = permissionName)
        )
    }

    fun permissions(permissions: Iterable<RbacPermissionDeclaration>) {
        permissions.forEach { permission(it) }
    }

    fun role(role: RbacRoleDeclaration) {
        val roleName = role.name.trim()
        if (roleName.isBlank()) {
            return
        }

        roles.putIfAbsent(
            roleName,
            role.copy(name = roleName)
        )
    }

    fun roles(roles: Iterable<RbacRoleDeclaration>) {
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

    fun permissionDeclarations(): List<RbacPermissionDeclaration> {
        return permissions.values.toList()
    }

    fun roleDeclarations(): List<RbacRoleDeclaration> {
        return roles.values.toList()
    }

    fun rolePermissionBindings(): List<RbacRolePermissionBindingDeclaration> {
        return rolePermissionBindings.map { (roleName, permissionNames) ->
            RbacRolePermissionBindingDeclaration(
                roleName = roleName,
                permissionNames = permissionNames.toSet(),
            )
        }
    }

    fun grantAllRoleNames(): Set<String> {
        return grantAllRoles.toSet()
    }
}
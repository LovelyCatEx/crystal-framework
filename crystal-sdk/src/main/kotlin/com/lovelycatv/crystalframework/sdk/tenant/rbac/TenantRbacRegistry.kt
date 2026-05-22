package com.lovelycatv.crystalframework.sdk.tenant.rbac

import com.lovelycatv.crystalframework.sdk.tenant.rbac.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.tenant.rbac.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.sdk.tenant.rbac.types.TenantRolePermissionBindingDeclaration

class TenantRbacRegistry {
    private val permissions = linkedMapOf<String, TenantPermissionDeclaration>()
    private val roles = linkedMapOf<String, TenantRoleDeclaration>()
    private val rolePermissionBindings = linkedMapOf<String, LinkedHashSet<String>>()
    private var defaultOwnerRoleName: String? = null
    private var defaultMemberRoleName: String? = null

    fun permission(permission: TenantPermissionDeclaration) {
        val permissionName = permission.name.trim()
        if (permissionName.isBlank()) {
            return
        }

        permissions.putIfAbsent(
            permissionName,
            permission.copy(name = permissionName)
        )
    }

    fun permissions(permissions: Iterable<TenantPermissionDeclaration>) {
        permissions.forEach { permission(it) }
    }

    fun role(role: TenantRoleDeclaration) {
        val roleName = role.name.trim()
        if (roleName.isBlank()) {
            return
        }

        roles.putIfAbsent(
            roleName,
            role.copy(
                name = roleName,
                parentRoleName = role.parentRoleName?.trim()?.takeIf { it.isNotBlank() }
            )
        )
    }

    fun roles(roles: Iterable<TenantRoleDeclaration>) {
        roles.forEach { role(it) }
    }

    fun bind(roleName: String, vararg permissionNames: String) {
        bind(roleName, permissionNames.asIterable())
    }

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

    fun defaultOwnerRole(roleName: String) {
        defaultOwnerRoleName = roleName.trim().takeIf { it.isNotBlank() }
    }

    fun defaultMemberRole(roleName: String) {
        defaultMemberRoleName = roleName.trim().takeIf { it.isNotBlank() }
    }

    fun permissionDeclarations(): List<TenantPermissionDeclaration> {
        return permissions.values.toList()
    }

    fun roleDeclarations(): List<TenantRoleDeclaration> {
        return roles.values.toList()
    }

    fun rolePermissionBindings(): List<TenantRolePermissionBindingDeclaration> {
        return rolePermissionBindings.map { (roleName, permissionNames) ->
            TenantRolePermissionBindingDeclaration(
                roleName = roleName,
                permissionNames = permissionNames.toSet(),
            )
        }
    }

    fun defaultOwnerRoleName(): String? {
        return defaultOwnerRoleName
    }

    fun defaultMemberRoleName(): String? {
        return defaultMemberRoleName
    }
}

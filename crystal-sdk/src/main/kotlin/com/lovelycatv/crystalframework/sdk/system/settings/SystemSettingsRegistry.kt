package com.lovelycatv.crystalframework.sdk.system.settings

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemDeclaration

class SystemSettingsRegistry {
    private val settings = linkedMapOf<String, SystemSettingsItemDeclaration>()

    fun setting(declaration: SystemSettingsItemDeclaration) {
        val settingKey = declaration.key.trim()
        if (settingKey.isBlank()) {
            return
        }

        if (settings.putIfAbsent(settingKey, declaration.copy(key = settingKey)) != null) {
            throw IllegalStateException("SystemSettingsRegistry: duplicate setting key '$settingKey'")
        }
    }

    fun settings(declarations: Iterable<SystemSettingsItemDeclaration>) {
        declarations.forEach { setting(it) }
    }

    fun settingDeclarations(): List<SystemSettingsItemDeclaration> {
        return settings.values.toList()
    }

    fun declarationMap(): Map<String, SystemSettingsItemDeclaration> {
        return settings.toMap()
    }
}

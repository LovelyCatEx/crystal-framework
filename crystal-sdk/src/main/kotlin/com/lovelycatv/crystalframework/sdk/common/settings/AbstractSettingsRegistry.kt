package com.lovelycatv.crystalframework.sdk.common.settings

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration

open class AbstractSettingsRegistry {
    private val settings = linkedMapOf<String, SettingsItemDeclaration>()

    fun setting(declaration: SettingsItemDeclaration) {
        val settingKey = declaration.key.trim()
        if (settingKey.isBlank()) {
            return
        }

        if (settings.putIfAbsent(settingKey, declaration.copy(key = settingKey)) != null) {
            throw IllegalStateException("${this::class.simpleName}: duplicate setting key '$settingKey'")
        }
    }

    fun settings(declarations: Iterable<SettingsItemDeclaration>) {
        declarations.forEach { setting(it) }
    }

    fun settingDeclarations(): List<SettingsItemDeclaration> {
        return settings.values.toList()
    }

    fun declarationMap(): Map<String, SettingsItemDeclaration> {
        return settings.toMap()
    }
}

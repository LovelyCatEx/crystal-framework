package com.lovelycatv.crystalframework.sdk.database

class TableRegistry {

    private val registrations = linkedMapOf<String, TableRegistration>()

    fun register(registration: TableRegistration) {
        registrations[registration.tableName.lowercase()] = registration
    }

    fun register(tableName: String, isBaseEntity: Boolean = true) {
        register(TableRegistration(tableName, isBaseEntity))
    }

    fun getRegistrations(): List<TableRegistration> {
        return registrations.values.toList()
    }
}

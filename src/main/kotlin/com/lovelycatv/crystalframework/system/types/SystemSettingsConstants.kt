package com.lovelycatv.crystalframework.system.types

import kotlin.reflect.full.memberProperties

object SystemSettingsConstants {
    object Bootstrap {
        val AUTO_CHECK_RBAC_TABLE_DATA = SystemSettingsItemDeclaration(
            "bootstrap.autoCheckRbacTableData",
            SystemSettingsItemValueType.BOOLEAN,
            true.toString()
        )
    }

    object Mail {
        object SMTP {
            val HOST = SystemSettingsItemDeclaration(
                "mail.smtp.host",
                SystemSettingsItemValueType.STRING,
                "127.0.0.1",
                1
            )

            val PORT = SystemSettingsItemDeclaration(
                "mail.smtp.port",
                SystemSettingsItemValueType.NUMBER,
                465.toString(),
                2
            )

            val USERNAME = SystemSettingsItemDeclaration(
                "mail.smtp.username",
                SystemSettingsItemValueType.STRING,
                "username",
                3
            )

            val PASSWORD = SystemSettingsItemDeclaration(
                "mail.smtp.password",
                SystemSettingsItemValueType.STRING,
                "password",
                4
            )

            val FROM_EMAIL = SystemSettingsItemDeclaration(
                "mail.smtp.fromEmail",
                SystemSettingsItemValueType.STRING,
                "user@example.com",
                5
            )

            val SSL = SystemSettingsItemDeclaration(
                "mail.smtp.ssl",
                SystemSettingsItemValueType.BOOLEAN,
                true.toString(),
                6
            )
        }
    }

    fun getAllDeclarations(): List<SystemSettingsItemDeclaration> {
        val declarations = mutableListOf<SystemSettingsItemDeclaration>()
        var nested = SystemSettingsConstants::class.nestedClasses

        while (nested.isNotEmpty()) {
            nested.forEach {
                val instance = it.objectInstance
                it.memberProperties.forEach { prop ->
                    val value = if (instance != null) {
                        prop.call(instance)
                    } else {
                        prop.getter.call()
                    }

                    if (value is SystemSettingsItemDeclaration) {
                        declarations.add(value)
                    }
                }
            }

            nested = nested.flatMap { it.nestedClasses }
        }

        return declarations
    }
}
package com.lovelycatv.crystalframework.system.types

import kotlin.reflect.full.memberProperties

object SystemSettingsConstants {
    object Basic {
        val BASE_URL = SystemSettingsItemDeclaration(
            key = "basic.baseUrl",
            valueType = SystemSettingsItemValueType.STRING,
            defaultValue = "http://localhost:8080/api/v1",
        )
    }

    object Bootstrap {
        val AUTO_CHECK_RBAC_TABLE_DATA = SystemSettingsItemDeclaration(
            key = "bootstrap.autoCheckRbacTableData",
            valueType = SystemSettingsItemValueType.BOOLEAN,
            defaultValue = true.toString()
        )
    }

    object Mail {
        object SMTP {
            val HOST = SystemSettingsItemDeclaration(
                key = "mail.smtp.host",
                valueType = SystemSettingsItemValueType.STRING,
                defaultValue = "127.0.0.1",
                sort = 1
            )

            val PORT = SystemSettingsItemDeclaration(
                key = "mail.smtp.port",
                valueType = SystemSettingsItemValueType.NUMBER,
                defaultValue = 465.toString(),
                sort = 2
            )

            val USERNAME = SystemSettingsItemDeclaration(
                key = "mail.smtp.username",
                valueType = SystemSettingsItemValueType.STRING,
                defaultValue = "username",
                sort = 3
            )

            val PASSWORD = SystemSettingsItemDeclaration(
                key = "mail.smtp.password",
                valueType = SystemSettingsItemValueType.STRING,
                defaultValue = "password",
                sort = 4
            )

            val FROM_EMAIL = SystemSettingsItemDeclaration(
                key = "mail.smtp.fromEmail",
                valueType = SystemSettingsItemValueType.STRING,
                defaultValue = "user@example.com",
                sort = 5
            )

            val SSL = SystemSettingsItemDeclaration(
                key = "mail.smtp.ssl",
                valueType = SystemSettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 6
            )
        }
    }

    object Security {
        object Api {
            object Encrypt {
                val ENABLE = SystemSettingsItemDeclaration(
                    key = "security.api.encrypt.enabled",
                    valueType =  SystemSettingsItemValueType.BOOLEAN,
                    defaultValue = true.toString(),
                    sort = 1
                )

                val SCOPE = SystemSettingsItemDeclaration(
                    key = "security.api.encrypt.scope",
                    valueType =  SystemSettingsItemValueType.ENUM_SINGLE,
                    defaultValue = "ALL",
                    sort = 2,
                    enumValues = listOf("ALL", "ALL_ANNOTATED", "BY_ANNOTATED_LEVEL")
                )

                val SECURITY_LEVEL = SystemSettingsItemDeclaration(
                    key = "security.api.encrypt.securityLevel",
                    valueType = SystemSettingsItemValueType.NUMBER,
                    defaultValue = 1.toString(),
                    sort = 3
                )
            }
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
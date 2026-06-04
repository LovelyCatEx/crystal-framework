package com.lovelycatv.crystalframework.system.types

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType

object SystemSettingsConstants {
    object Basic {
        val BASE_URL = SettingsItemDeclaration(
            key = "basic.baseUrl",
            valueType = SettingsItemValueType.STRING,
            defaultValue = "http://localhost:8080/api/v1",
        )

        object WaterMark {
            val ENABLED = SettingsItemDeclaration(
                key = "basic.waterMark.enabled",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = false.toString(),
                sort = 1
            )

            val TYPE = SettingsItemDeclaration(
                key = "basic.waterMark.type",
                valueType = SettingsItemValueType.ENUM_SINGLE,
                defaultValue = "SYSTEM_NAME",
                sort = 2,
                enumValues = listOf("SYSTEM_NAME", "USER_NAME", "CUSTOM")
            )

            val CUSTOM_VALUE = SettingsItemDeclaration(
                key = "basic.waterMark.customValue",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 3
            )

            val FONT_COLOR = SettingsItemDeclaration(
                key = "basic.waterMark.fontColor",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "#00000018",
                sort = 4
            )
        }
    }

    object Bootstrap {
        val AUTO_CHECK_RBAC_TABLE_DATA = SettingsItemDeclaration(
            key = "bootstrap.autoCheckRbacTableData",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString()
        )
    }

    object Mail {
        object SMTP {
            val HOST = SettingsItemDeclaration(
                key = "mail.smtp.host",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "127.0.0.1",
                sort = 1
            )

            val PORT = SettingsItemDeclaration(
                key = "mail.smtp.port",
                valueType = SettingsItemValueType.NUMBER,
                defaultValue = 465.toString(),
                sort = 2
            )

            val USERNAME = SettingsItemDeclaration(
                key = "mail.smtp.username",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "username",
                sort = 3
            )

            val PASSWORD = SettingsItemDeclaration(
                key = "mail.smtp.password",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "password",
                sort = 4
            )

            val FROM_EMAIL = SettingsItemDeclaration(
                key = "mail.smtp.fromEmail",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "user@example.com",
                sort = 5
            )

            val SSL = SettingsItemDeclaration(
                key = "mail.smtp.ssl",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 6
            )
        }
    }

    object MessageChannel {
        object Lark {
            val APP_ID = SettingsItemDeclaration(
                key = "messageChannel.lark.appId",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 1
            )

            val APP_SECRET = SettingsItemDeclaration(
                key = "messageChannel.lark.appSecret",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 2
            )

            val BASE_URL = SettingsItemDeclaration(
                key = "messageChannel.lark.baseUrl",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://open.feishu.cn",
                sort = 3
            )
        }
    }

    object Security {
        object Api {
            object Encrypt {
                val ENABLE = SettingsItemDeclaration(
                    key = "security.api.encrypt.enabled",
                    valueType =  SettingsItemValueType.BOOLEAN,
                    defaultValue = true.toString(),
                    sort = 1
                )

                val SCOPE = SettingsItemDeclaration(
                    key = "security.api.encrypt.scope",
                    valueType =  SettingsItemValueType.ENUM_SINGLE,
                    defaultValue = "ALL",
                    sort = 2,
                    enumValues = listOf("ALL", "ALL_ANNOTATED", "BY_ANNOTATED_LEVEL")
                )

                val SECURITY_LEVEL = SettingsItemDeclaration(
                    key = "security.api.encrypt.securityLevel",
                    valueType = SettingsItemValueType.NUMBER,
                    defaultValue = 1.toString(),
                    sort = 3
                )
            }
        }

    }
}
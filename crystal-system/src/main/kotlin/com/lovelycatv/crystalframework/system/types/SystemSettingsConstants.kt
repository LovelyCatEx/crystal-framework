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

        val FRONTEND_BASE_URL = SettingsItemDeclaration(
            key = "basic.frontendBaseUrl",
            valueType = SettingsItemValueType.STRING,
            defaultValue = "http://localhost:5173",
            sort = 1
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

    object OAuth {
        object Github {
            val ENABLED = SettingsItemDeclaration(
                key = "oauth.github.enabled",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = false.toString(),
                sort = 1
            )

            val USE_DEFAULT = SettingsItemDeclaration(
                key = "oauth.github.useDefault",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 2
            )

            val AUTHORIZATION_URI = SettingsItemDeclaration(
                key = "oauth.github.authorizationUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://github.com/login/oauth/authorize",
                sort = 3
            )

            val TOKEN_URI = SettingsItemDeclaration(
                key = "oauth.github.tokenUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://github.com/login/oauth/access_token",
                sort = 4
            )

            val USER_INFO_URI = SettingsItemDeclaration(
                key = "oauth.github.userInfoUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://api.github.com/user",
                sort = 5
            )

            val USER_NAME_ATTRIBUTE = SettingsItemDeclaration(
                key = "oauth.github.userNameAttribute",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "id",
                sort = 6
            )

            val CLIENT_ID = SettingsItemDeclaration(
                key = "oauth.github.clientId",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 7
            )

            val CLIENT_SECRET = SettingsItemDeclaration(
                key = "oauth.github.clientSecret",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 8
            )

            val SCOPE = SettingsItemDeclaration(
                key = "oauth.github.scope",
                valueType = SettingsItemValueType.STRING_ARRAY,
                defaultValue = """["read:user"]""",
                sort = 9
            )
        }

        object Google {
            val ENABLED = SettingsItemDeclaration(
                key = "oauth.google.enabled",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = false.toString(),
                sort = 1
            )

            val USE_DEFAULT = SettingsItemDeclaration(
                key = "oauth.google.useDefault",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = true.toString(),
                sort = 2
            )

            val AUTHORIZATION_URI = SettingsItemDeclaration(
                key = "oauth.google.authorizationUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://accounts.google.com/o/oauth2/v2/auth",
                sort = 3
            )

            val TOKEN_URI = SettingsItemDeclaration(
                key = "oauth.google.tokenUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://www.googleapis.com/oauth2/v4/token",
                sort = 4
            )

            val USER_INFO_URI = SettingsItemDeclaration(
                key = "oauth.google.userInfoUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://www.googleapis.com/oauth2/v3/userinfo",
                sort = 5
            )

            val USER_NAME_ATTRIBUTE = SettingsItemDeclaration(
                key = "oauth.google.userNameAttribute",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "sub",
                sort = 6
            )

            val CLIENT_ID = SettingsItemDeclaration(
                key = "oauth.google.clientId",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 7
            )

            val CLIENT_SECRET = SettingsItemDeclaration(
                key = "oauth.google.clientSecret",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 8
            )

            val SCOPE = SettingsItemDeclaration(
                key = "oauth.google.scope",
                valueType = SettingsItemValueType.STRING_ARRAY,
                defaultValue = """["openid","profile","email"]""",
                sort = 9
            )
        }

        object Oicq {
            val ENABLED = SettingsItemDeclaration(
                key = "oauth.oicq.enabled",
                valueType = SettingsItemValueType.BOOLEAN,
                defaultValue = false.toString(),
                sort = 1
            )

            val AUTHORIZATION_URI = SettingsItemDeclaration(
                key = "oauth.oicq.authorizationUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://graph.qq.com/oauth2.0/authorize",
                sort = 2
            )

            val TOKEN_URI = SettingsItemDeclaration(
                key = "oauth.oicq.tokenUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://graph.qq.com/oauth2.0/token",
                sort = 3
            )

            val USER_INFO_URI = SettingsItemDeclaration(
                key = "oauth.oicq.userInfoUri",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "https://graph.qq.com/user/get_user_info",
                sort = 4
            )

            val USER_NAME_ATTRIBUTE = SettingsItemDeclaration(
                key = "oauth.oicq.userNameAttribute",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "openid",
                sort = 5
            )

            val CLIENT_ID = SettingsItemDeclaration(
                key = "oauth.oicq.clientId",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 6
            )

            val CLIENT_SECRET = SettingsItemDeclaration(
                key = "oauth.oicq.clientSecret",
                valueType = SettingsItemValueType.STRING,
                defaultValue = "",
                sort = 7
            )

            val SCOPE = SettingsItemDeclaration(
                key = "oauth.oicq.scope",
                valueType = SettingsItemValueType.STRING_ARRAY,
                defaultValue = """["get_user_info"]""",
                sort = 8
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

    object Module {
        val TENANT_ENABLED = SettingsItemDeclaration(
            key = "module.tenant.enabled",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 0
        )

        val APPROVAL_ENABLED = SettingsItemDeclaration(
            key = "module.approval.enabled",
            valueType = SettingsItemValueType.BOOLEAN,
            defaultValue = true.toString(),
            sort = 1
        )
    }
}
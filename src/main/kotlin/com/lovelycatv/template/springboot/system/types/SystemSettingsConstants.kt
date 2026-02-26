package com.lovelycatv.template.springboot.system.types

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
            )

            val PORT = SystemSettingsItemDeclaration(
                "mail.smtp.port",
                SystemSettingsItemValueType.NUMBER,
                465.toString()
            )

            val USERNAME = SystemSettingsItemDeclaration(
                "mail.smtp.username",
                SystemSettingsItemValueType.STRING,
                "username"
            )

            val PASSWORD = SystemSettingsItemDeclaration(
                "mail.smtp.password",
                SystemSettingsItemValueType.STRING,
                "password"
            )

            val SSL = SystemSettingsItemDeclaration(
                "mail.smtp.ssl",
                SystemSettingsItemValueType.BOOLEAN,
                true.toString()
            )

            val FROM_EMAIL = SystemSettingsItemDeclaration(
                "mail.smtp.fromEmail",
                SystemSettingsItemValueType.STRING,
                "user@example.com"
            )
        }
    }
}
package com.lovelycatv.crystalframework.system.types

data class SystemSettings(
    val bootstrap: Bootstrap,
    val mail: Mail,
) {
    data class Bootstrap(
        val autoCheckRbacTableData: Boolean
    )

    data class Mail(
        val smtp: SMTP,
    ) {
        data class SMTP(
            val host: String,
            val port: Int,
            val username: String,
            val password: String,
            val ssl: Boolean,
            val fromEmail: String,
        )
    }
}
package com.lovelycatv.template.springboot.system.types

data class SystemSettings(
    val mail: Mail,
) {
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
package com.lovelycatv.crystalframework.shared.types.system

import com.lovelycatv.crystalframework.shared.types.encrypt.ApiEncryptionScope

data class SystemSettings(
    val basic: Basic,
    val bootstrap: Bootstrap,
    val mail: Mail,
    val lark: Lark,
    val security: Security,
) {
    data class Basic(
        val baseUrl: String,
        val waterMark: WaterMark,
    ) {
        data class WaterMark(
            val enabled: Boolean,
            val type: String,
            val customValue: String,
            val fontColor: String,
        )

        fun getNormalizedBaseUrl(withSuffix: Boolean = false): String {
            return baseUrl.removeSuffix("/").run {
                if (withSuffix) {
                    "$this/"
                } else {
                    this
                }
            }
        }
    }

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

    data class Lark(
        val app: App,
    ) {
        data class App(
            val appId: String,
            val appSecret: String,
            val baseUrl: String,
        )
    }

    data class Security(
        val api: Api
    ) {
        data class Api(
            val encrypt: Encrypt
        ) {
            data class Encrypt(
                val enabled: Boolean,
                val scope: ApiEncryptionScope,
                val securityLevel: Int,
            )
        }
    }
}
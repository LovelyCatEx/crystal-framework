package com.lovelycatv.crystalframework.shared.types.system

import com.lovelycatv.crystalframework.shared.types.encrypt.ApiEncryptionScope

data class SystemSettings(
    val basic: Basic,
    val bootstrap: Bootstrap,
    val mail: Mail,
    val messageChannel: MessageChannel,
    val security: Security,
    val oauth: OAuth,
    val module: Module,
) {
    data class Basic(
        val baseUrl: String,
        val frontendBaseUrl: String,
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

    data class MessageChannel(
        val lark: Lark,
    ) {
        data class Lark(
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

    data class OAuth(
        val github: OAuthPlatformSettings,
        val google: OAuthPlatformSettings,
        val oicq: OAuthPlatformSettings,
    ) {
        data class OAuthPlatformSettings(
            val enabled: Boolean,
            val useDefault: Boolean?,
            val authorizationUri: String,
            val tokenUri: String,
            val userInfoUri: String,
            val userNameAttribute: String,
            val clientId: String,
            val clientSecret: String,
            val scope: List<String>,
        )
    }

    data class Module(
        val tenantEnabled: Boolean,
        val approvalEnabled: Boolean,
    )
}
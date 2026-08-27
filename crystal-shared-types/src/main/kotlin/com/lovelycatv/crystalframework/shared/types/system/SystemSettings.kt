package com.lovelycatv.crystalframework.shared.types.system

import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.crystalframework.shared.types.encrypt.ApiEncryptionScope

data class SystemSettings(
    val basic: Basic,
    val bootstrap: Bootstrap,
    val mail: Mail,
    val messageChannel: MessageChannel,
    val security: Security,
    val oauth: OAuth,
    val resource: Resource,
    val module: Module,
) {
    data class Resource(
        val signedUrl: SignedUrl,
        val visibility: Visibility,
    ) {
        data class SignedUrl(
            val ttlSeconds: Long,
        )

        /**
         * Visibility overrides keyed by [com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration.key].
         * Every registered file type contributes one entry at startup; runtime lookup is
         * `overrides[decl.key] ?: decl.defaultVisibility`, so third-party types are honored on
         * equal footing with the built-ins without hardcoding names here.
         */
        data class Visibility(
            val overrides: Map<String, ResourceVisibility>,
        )
    }

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
        ) {
            override fun toString(): String {
                return "SMTP(host=$host, port=$port, username=$username, password=***, ssl=$ssl, fromEmail=$fromEmail)"
            }
        }
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
        val api: Api,
        val loginRateLimit: LoginRateLimit,
        val emailCodeRateLimit: EmailCodeRateLimit,
        val outbound: Outbound,
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

        data class LoginRateLimit(
            val enabled: Boolean,
            val windowSeconds: Int,
            val maxAttemptsPerIp: Int,
            val maxAttemptsPerAccount: Int,
            val lockThreshold: Int,
            val lockBaseSeconds: Int,
            val lockMaxSeconds: Int,
        )

        data class EmailCodeRateLimit(
            val enabled: Boolean,
            val windowSeconds: Int,
            val maxPerIp: Int,
            val maxPerEmail: Int,
            val maxGlobal: Int,
        )

        data class Outbound(
            val allowedHosts: List<String>,
            val allowedSmtpHosts: List<String>,
        )
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
        val messageSystemPeerEnabled: Boolean,
        val messageTenantScopeEnabled: Boolean,
        val messageTenantDeskEnabled: Boolean,
    )
}
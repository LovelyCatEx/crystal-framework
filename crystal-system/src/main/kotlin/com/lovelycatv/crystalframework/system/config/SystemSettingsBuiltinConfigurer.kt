package com.lovelycatv.crystalframework.system.config

import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.system.settings.config.SystemSettingsConfigurer
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SystemSettingsBuiltinConfigurer : SystemSettingsConfigurer {
    override fun configure(registry: SystemSettingsRegistry) {
        registry.settings(
            listOf(
                SystemSettingsConstants.Basic.BASE_URL,
                SystemSettingsConstants.Basic.FRONTEND_BASE_URL,
                SystemSettingsConstants.Basic.WaterMark.ENABLED,
                SystemSettingsConstants.Basic.WaterMark.TYPE,
                SystemSettingsConstants.Basic.WaterMark.CUSTOM_VALUE,
                SystemSettingsConstants.Basic.WaterMark.FONT_COLOR,
                SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA,
                SystemSettingsConstants.Mail.SMTP.HOST,
                SystemSettingsConstants.Mail.SMTP.PORT,
                SystemSettingsConstants.Mail.SMTP.USERNAME,
                SystemSettingsConstants.Mail.SMTP.PASSWORD,
                SystemSettingsConstants.Mail.SMTP.FROM_EMAIL,
                SystemSettingsConstants.Mail.SMTP.SSL,
                SystemSettingsConstants.MessageChannel.Lark.APP_ID,
                SystemSettingsConstants.MessageChannel.Lark.APP_SECRET,
                SystemSettingsConstants.MessageChannel.Lark.BASE_URL,
                SystemSettingsConstants.Security.Api.Encrypt.ENABLE,
                SystemSettingsConstants.Security.Api.Encrypt.SCOPE,
                SystemSettingsConstants.Security.Api.Encrypt.SECURITY_LEVEL,
                SystemSettingsConstants.Security.LoginRateLimit.ENABLED,
                SystemSettingsConstants.Security.LoginRateLimit.WINDOW_SECONDS,
                SystemSettingsConstants.Security.LoginRateLimit.MAX_ATTEMPTS_PER_IP,
                SystemSettingsConstants.Security.LoginRateLimit.MAX_ATTEMPTS_PER_ACCOUNT,
                SystemSettingsConstants.Security.LoginRateLimit.LOCK_THRESHOLD,
                SystemSettingsConstants.Security.LoginRateLimit.LOCK_BASE_SECONDS,
                SystemSettingsConstants.Security.LoginRateLimit.LOCK_MAX_SECONDS,
                SystemSettingsConstants.Security.EmailCodeRateLimit.ENABLED,
                SystemSettingsConstants.Security.EmailCodeRateLimit.WINDOW_SECONDS,
                SystemSettingsConstants.Security.EmailCodeRateLimit.MAX_PER_IP,
                SystemSettingsConstants.Security.EmailCodeRateLimit.MAX_PER_EMAIL,
                SystemSettingsConstants.Security.EmailCodeRateLimit.MAX_GLOBAL,
                SystemSettingsConstants.OAuth.Github.ENABLED,
                SystemSettingsConstants.OAuth.Github.USE_DEFAULT,
                SystemSettingsConstants.OAuth.Github.AUTHORIZATION_URI,
                SystemSettingsConstants.OAuth.Github.TOKEN_URI,
                SystemSettingsConstants.OAuth.Github.USER_INFO_URI,
                SystemSettingsConstants.OAuth.Github.USER_NAME_ATTRIBUTE,
                SystemSettingsConstants.OAuth.Github.CLIENT_ID,
                SystemSettingsConstants.OAuth.Github.CLIENT_SECRET,
                SystemSettingsConstants.OAuth.Github.SCOPE,
                SystemSettingsConstants.OAuth.Google.ENABLED,
                SystemSettingsConstants.OAuth.Google.USE_DEFAULT,
                SystemSettingsConstants.OAuth.Google.AUTHORIZATION_URI,
                SystemSettingsConstants.OAuth.Google.TOKEN_URI,
                SystemSettingsConstants.OAuth.Google.USER_INFO_URI,
                SystemSettingsConstants.OAuth.Google.USER_NAME_ATTRIBUTE,
                SystemSettingsConstants.OAuth.Google.CLIENT_ID,
                SystemSettingsConstants.OAuth.Google.CLIENT_SECRET,
                SystemSettingsConstants.OAuth.Google.SCOPE,
                SystemSettingsConstants.OAuth.Oicq.ENABLED,
                SystemSettingsConstants.OAuth.Oicq.AUTHORIZATION_URI,
                SystemSettingsConstants.OAuth.Oicq.TOKEN_URI,
                SystemSettingsConstants.OAuth.Oicq.USER_INFO_URI,
                SystemSettingsConstants.OAuth.Oicq.USER_NAME_ATTRIBUTE,
                SystemSettingsConstants.OAuth.Oicq.CLIENT_ID,
                SystemSettingsConstants.OAuth.Oicq.CLIENT_SECRET,
                SystemSettingsConstants.OAuth.Oicq.SCOPE,
                SystemSettingsConstants.Module.TENANT_ENABLED,
                SystemSettingsConstants.Module.APPROVAL_ENABLED,
                SystemSettingsConstants.Resource.Visibility.USER_AVATAR,
                SystemSettingsConstants.Resource.Visibility.TENANT_ICON,
                SystemSettingsConstants.Resource.Visibility.TENANT_MEMBER_AVATAR,
            )
        )
    }
}

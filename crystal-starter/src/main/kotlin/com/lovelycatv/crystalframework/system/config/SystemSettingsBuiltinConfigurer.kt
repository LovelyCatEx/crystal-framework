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
                SystemSettingsConstants.Lark.App.APP_ID,
                SystemSettingsConstants.Lark.App.APP_SECRET,
                SystemSettingsConstants.Lark.App.BASE_URL,
                SystemSettingsConstants.Security.Api.Encrypt.ENABLE,
                SystemSettingsConstants.Security.Api.Encrypt.SCOPE,
                SystemSettingsConstants.Security.Api.Encrypt.SECURITY_LEVEL,
            )
        )
    }
}

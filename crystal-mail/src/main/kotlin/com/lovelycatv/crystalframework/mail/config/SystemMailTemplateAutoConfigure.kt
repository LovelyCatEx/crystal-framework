package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.sdk.mail.config.SystemMailTemplateConfigure
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SystemMailTemplateAutoConfigure {
    @Bean
    @ConditionalOnMissingBean(SystemMailTemplateConfigure::class)
    fun systemMailTemplateConfigure(): SystemMailTemplateConfigure {
        return DefaultSystemMailTemplateConfigure()
    }
}
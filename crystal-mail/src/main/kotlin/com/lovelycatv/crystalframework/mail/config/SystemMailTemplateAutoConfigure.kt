/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
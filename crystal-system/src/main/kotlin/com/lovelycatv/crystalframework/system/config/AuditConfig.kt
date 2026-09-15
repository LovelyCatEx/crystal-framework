/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.config

import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class AuditConfig {
    @Bean
    fun auditEventRepository(): AuditEventRepository {
        val delegate = InMemoryAuditEventRepository()
        return object : AuditEventRepository by delegate {}
    }
}
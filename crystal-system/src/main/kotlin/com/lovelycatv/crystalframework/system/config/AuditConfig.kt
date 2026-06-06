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
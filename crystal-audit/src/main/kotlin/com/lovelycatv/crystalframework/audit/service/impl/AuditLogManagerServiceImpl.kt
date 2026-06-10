package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.audit.service.AuditLogManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AuditLogManagerServiceImpl(
    private val auditLogRepository: AuditLogRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AuditLogManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AuditLogEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AuditLogEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AuditLogEntity> = AuditLogEntity::class

    override fun getRepository(): AuditLogRepository {
        return this.auditLogRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAuditLogDTO): AuditLogEntity {
        throw BusinessException("Audit logs cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateAuditLogDTO, original: AuditLogEntity): AuditLogEntity {
        throw BusinessException("Audit logs cannot be updated")
    }
}

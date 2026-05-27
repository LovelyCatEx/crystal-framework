package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.audit.service.AuditLogManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AuditLogManagerServiceImpl(
    private val auditLogRepository: AuditLogRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AuditLogManagerService {
    override val cacheStore: ExpiringKVStore<String, AuditLogEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<AuditLogEntity>>
        get() = redisService.asKVStore()
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

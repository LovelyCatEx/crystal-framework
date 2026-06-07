package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.service.manager.TenantInvitationManagerService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import kotlin.reflect.KClass

@Service
class TenantInvitationManagerServiceImpl(
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val tenantBenefitService: TenantBenefitService,
    private val tenantService: TenantService,
    private val reactiveRedisService: ReactiveRedisService,
    private val snowIdGenerator: SnowIdGenerator,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantInvitationManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantInvitationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantInvitationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantInvitationEntity> = TenantInvitationEntity::class

    override fun getRepository(): TenantInvitationRepository {
        return this.tenantInvitationRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun create(dto: ManagerCreateInvitationDTO): TenantInvitationEntity {
        val tireTypeId = tenantService.getByIdOrThrow(dto.tenantId).tireTypeId
        val now = System.currentTimeMillis()

        // Check invitation feature is enabled for this tenant's tire type
        if (!tenantBenefitService.hasBenefit(tireTypeId, TenantBenefit.INVITATION_ENABLED.featureKey)) {
            throw BusinessException("Invitations are not enabled for your plan")
        }

        // Check total invitation count limit
        val totalLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_MAX_COUNT.featureKey)
        val existingTotal = tenantInvitationRepository.countByTenantId(dto.tenantId).awaitFirstOrNull() ?: 0
        if (existingTotal >= totalLimit) {
            throw BusinessException("Total invitation codes limit reached ($totalLimit)")
        }

        // Check daily creation limit
        val dailyLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey)
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayCount = tenantInvitationRepository
            .countByTenantIdAndCreatedTimeGreaterThanEqual(dto.tenantId, startOfToday)
            .awaitFirstOrNull() ?: 0
        if (todayCount >= dailyLimit) {
            throw BusinessException("Daily invitation creation limit reached ($dailyLimit)")
        }

        // Check per-code usage limit (cap the invitationCount field)
        val perCodeLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey)
        if (dto.invitationCount > perCodeLimit) {
            throw BusinessException("Per-code usage limit is $perCodeLimit, configured value ${dto.invitationCount} exceeds it")
        }

        // Check max validity days
        val maxValidityDays = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey)
        if (dto.expiresTime != null && dto.expiresTime > now + maxValidityDays * 86400000L) {
            throw BusinessException("Invitation validity period cannot exceed $maxValidityDays days")
        }

        val entity = TenantInvitationEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            creatorMemberId = dto.creatorMemberId ?: throw BusinessException("invitation creator id must be specified"),
            departmentId = dto.departmentId,
            invitationCode = generateInvitationCode(dto.tenantId),
            invitationCount = dto.invitationCount,
            expiresTime = dto.expiresTime,
            requiresReviewing = dto.requiresReviewing
        ).apply { newEntity() }

        return tenantInvitationRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create invitation")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateInvitationDTO,
        original: TenantInvitationEntity
    ): TenantInvitationEntity {
        val tireTypeId = tenantService.getByIdOrThrow(original.tenantId).tireTypeId
        val now = System.currentTimeMillis()

        dto.invitationCount?.let { newCount ->
            val perCodeLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey)
            if (newCount > perCodeLimit) {
                throw BusinessException("Per-code usage limit is $perCodeLimit, configured value $newCount exceeds it")
            }
        }

        dto.expiresTime?.let { newExpiresTime ->
            val maxValidityDays = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey)
            if (newExpiresTime > now + maxValidityDays * 86400000L) {
                throw BusinessException("Invitation validity period cannot exceed $maxValidityDays days")
            }
        }

        return original.apply {
            departmentId = dto.departmentId
            dto.invitationCount?.let { invitationCount = it }
            dto.expiresTime?.let { expiresTime = it }
            dto.requiresReviewing?.let { requiresReviewing = it }
            dto.creatorMemberId?.let { creatorMemberId = it }
        }
    }

    private fun generateInvitationCode(tenantId: Long): String {
        val chars = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ-$tenantId"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantInvitationEntity> {
        return getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }
}
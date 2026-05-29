package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantMemberManagerServiceImpl(
    private val tenantMemberRepository: TenantMemberRepository,
    private val userManagerService: UserManagerService,
    private val tenantBenefitService: TenantBenefitService,
    private val redisService: RedisService,
    private val snowIdGenerator: SnowIdGenerator,
    override val eventPublisher: ApplicationEventPublisher,
    @Lazy
    private val tenantService: TenantService,
    @Lazy
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantDepartmentMemberRelationService: TenantDepartmentMemberRelationService,
    private val tenantMemberService: TenantMemberService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantMemberManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantMemberEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantMemberEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantMemberEntity> = TenantMemberEntity::class

    override fun getRepository(): TenantMemberRepository {
        return this.tenantMemberRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun create(dto: ManagerCreateTenantMemberDTO): TenantMemberEntity {
        userManagerService.getByIdOrNull(dto.memberUserId)
            ?: throw BusinessException("User with ID ${dto.memberUserId} not found")

        val existingMember = tenantMemberRepository.findByTenantIdAndMemberUserId(
            dto.tenantId,
            dto.memberUserId
        ).awaitFirstOrNull()

        if (existingMember != null) {
            throw BusinessException("Member already exists in this tenant")
        }

        // Check member count limit
        val tireTypeId = tenantService.getByIdOrThrow(dto.tenantId).tireTypeId
        val memberLimit = tenantBenefitService.getBenefitLimit(tireTypeId, "member.max_count")
        val memberCount = tenantMemberRepository.countByTenantId(dto.tenantId).awaitFirstOrNull() ?: 0
        if (memberCount >= memberLimit) {
            throw BusinessException("Member limit reached ($memberLimit)")
        }

        val entity = TenantMemberEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            memberUserId = dto.memberUserId,
            status = dto.status ?: TenantMemberStatus.ACTIVE.typeId
        ).apply { newEntity() }

        // Save member
        val savedMember = tenantMemberRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create tenant member")

        // Add default member role
        val defaultMemberRoleId = tenantService
            .getByIdOrThrow(dto.tenantId)
            .getSettingsObject()
            ?.defaultMemberRoleId

        if (defaultMemberRoleId != null) {
            tenantMemberRoleRelationService.setMemberRoles(
                savedMember.id,
                listOf(defaultMemberRoleId)
            )
        }

        return savedMember
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateTenantMemberDTO,
        original: TenantMemberEntity
    ): TenantMemberEntity {
        return original.apply {
            dto.status?.let { status = it }
        }
    }

    override suspend fun queryVO(dto: ManagerReadTenantMemberDTO): PaginatedResponseData<TenantMemberVO> {
        val entityResult = query(dto)

        val vos = entityResult.records.map { entity ->
            tenantMemberService.transformTenantMemberVO(entity)
        }

        return PaginatedResponseData(
            page = entityResult.page,
            pageSize = entityResult.pageSize,
            total = entityResult.total,
            totalPages = entityResult.totalPages,
            records = vos
        )
    }

    override suspend fun batchDelete(ids: List<Long>) {
        tenantMemberRoleRelationService.deleteByMemberIdIn(ids)

        tenantDepartmentMemberRelationService.deleteByMemberIdIn(ids)

        super.batchDelete(ids)
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantMemberEntity> {
        return this.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }
}
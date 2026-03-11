package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantMemberManagerServiceImpl(
    private val tenantMemberRepository: TenantMemberRepository,
    private val userManagerService: UserManagerService,
    private val redisService: RedisService,
    private val snowIdGenerator: SnowIdGenerator,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantMemberManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantMemberEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantMemberEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantMemberEntity> = TenantMemberEntity::class

    override fun getRepository(): TenantMemberRepository {
        return this.tenantMemberRepository
    }

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

        val entity = TenantMemberEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            memberUserId = dto.memberUserId,
            status = dto.status ?: TenantMemberStatus.ACTIVE.ordinal
        ).apply { newEntity() }

        return tenantMemberRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create tenant member")
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
            val user = userManagerService.getByIdOrNull(entity.memberUserId)
            TenantMemberVO.fromEntity(entity, user)
        }

        return PaginatedResponseData(
            page = entityResult.page,
            pageSize = entityResult.pageSize,
            total = entityResult.total,
            totalPages = entityResult.totalPages,
            records = vos
        )
    }
}

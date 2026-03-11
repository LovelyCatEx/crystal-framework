package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.DepartmentMemberRoleType
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentMemberManagerServiceImpl(
    private val tenantDepartmentMemberRelationRepository: TenantDepartmentMemberRelationRepository,
    private val tenantMemberRepository: TenantMemberRepository,
    private val userManagerService: UserManagerService,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentMemberManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantDepartmentMemberRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantDepartmentMemberRelationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantDepartmentMemberRelationEntity> = TenantDepartmentMemberRelationEntity::class

    override fun getRepository(): TenantDepartmentMemberRelationRepository {
        return this.tenantDepartmentMemberRelationRepository
    }

    override suspend fun create(dto: ManagerCreateTenantDepartmentMemberDTO): TenantDepartmentMemberRelationEntity {
        // Check if member exists
        val member = tenantMemberRepository.findById(dto.memberId).awaitFirstOrNull()
            ?: throw BusinessException("Member with ID ${dto.memberId} not found")

        // Check if relation already exists
        val existingRelation = tenantDepartmentMemberRelationRepository
            .findByDepartmentIdAndMemberId(dto.departmentId, dto.memberId)
            .awaitFirstOrNull()

        if (existingRelation != null) {
            throw BusinessException("Member already exists in this department")
        }

        val entity = TenantDepartmentMemberRelationEntity(
            id = snowIdGenerator.nextId(),
            departmentId = dto.departmentId,
            memberId = dto.memberId,
            roleType = dto.roleType ?: DepartmentMemberRoleType.MEMBER.typeId
        ).apply { newEntity() }

        return tenantDepartmentMemberRelationRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create department member relation")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateTenantDepartmentMemberDTO,
        original: TenantDepartmentMemberRelationEntity
    ): TenantDepartmentMemberRelationEntity {
        return original.apply {
            departmentId = dto.departmentId
            memberId = dto.memberId
            roleType = dto.roleType
        }
    }

    override suspend fun queryVO(dto: ManagerReadTenantDepartmentMemberDTO): PaginatedResponseData<TenantDepartmentMemberVO> {
        val entityResult = query(dto)

        val vos = entityResult.records.map { entity ->
            val member = tenantMemberRepository.findById(entity.memberId).awaitFirstOrNull()
            val user = member?.let { userManagerService.getByIdOrNull(it.memberUserId) }

            TenantDepartmentMemberVO(
                id = entity.id,
                member = member?.let { TenantMemberVO.fromEntity(it, user) }
                    ?: throw BusinessException("Member with ID ${entity.memberId} not found"),
                roleType = entity.roleType,
                createdTime = entity.createdTime,
                modifiedTime = entity.modifiedTime,
            )
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

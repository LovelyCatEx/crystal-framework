package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.lockRowForUpdate
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.rbac.user.service.UserForceLogoutService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberProfileService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.shared.types.tenant.TenantMemberStatus
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
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
    private val reactiveRedisService: ReactiveRedisService,
    private val snowIdGenerator: SnowIdGenerator,
    override val eventPublisher: ApplicationEventPublisher,
    @Lazy
    private val tenantService: TenantService,
    @Lazy
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantDepartmentMemberRelationService: TenantDepartmentMemberRelationService,
    private val tenantMemberService: TenantMemberService,
    @Lazy
    private val tenantMemberProfileService: TenantMemberProfileService,
    private val userForceLogoutService: UserForceLogoutService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantMemberManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantMemberEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantMemberEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantMemberEntity> = TenantMemberEntity::class

    override fun getRepository(): TenantMemberRepository {
        return this.tenantMemberRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun create(dto: ManagerCreateTenantMemberDTO): TenantMemberEntity {
        // Serialize concurrent member creation for this tenant so the count-then-insert member-limit
        // check below cannot be bypassed by parallel transactions (M-12). The lock on the tenant row
        // is held until this transaction commits, so contenders re-read the count only after commit.
        getEntityTemplate().lockRowForUpdate(TableConstants.TABLE_TENANTS, dto.tenantId)

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
        val memberLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.MEMBER_MAX_COUNT.featureKey)
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

        // Persist tenant-scoped profile in the same transaction so every member always has a profile.
        // `name` is resolved inside upsertProfile from the system user (nickname -> username) as fallback.
        tenantMemberProfileService.upsertProfile(
            tenantId = savedMember.tenantId,
            tenantMemberId = savedMember.id,
            memberUserId = savedMember.memberUserId,
        )

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

    /**
     * When a member is deactivated (status changes away from ACTIVE), the member's still-valid JWT
     * and cached authorities are not revoked by the plain status update. Force-logout the underlying
     * user so the existing token is rejected on the next request; re-login is already blocked by the
     * member-status check on the token-issuing path (CustomUserDetailsService.checkIsTenantValid).
     *
     * Note: force-logout is user-scoped, so a user who belongs to multiple tenants will be logged out
     * of all of them. This is the accepted tradeoff of reusing the existing user-level mechanism.
     */
    @Transactional(rollbackFor = [Exception::class])
    override suspend fun update(dto: ManagerUpdateTenantMemberDTO): TenantMemberEntity? {
        val before = getByIdOrNull(dto.id)
        val result = super.update(dto)
        if (result != null && before != null &&
            before.getRealStatus() == TenantMemberStatus.ACTIVE &&
            result.getRealStatus() != TenantMemberStatus.ACTIVE
        ) {
            userForceLogoutService.markForceLogout(result.memberUserId)
        }
        return result
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

    /**
     * Deleting a member does not revoke the underlying user's still-valid JWT or cached authorities.
     * Resolve the member users before deletion and force-logout them so existing tokens are rejected
     * on the next request, matching the deactivation path in [update].
     *
     * Note: force-logout is user-scoped, so a user who belongs to multiple tenants will be logged out
     * of all of them. This is the accepted tradeoff of reusing the existing user-level mechanism.
     */
    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        val memberUserIds = ids.mapNotNull { getByIdOrNull(it)?.memberUserId }

        tenantMemberRoleRelationService.deleteByMemberIdIn(ids)

        tenantDepartmentMemberRelationService.deleteByMemberIdIn(ids)

        super.batchDelete(ids)

        memberUserIds.forEach { userForceLogoutService.markForceLogout(it) }
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantMemberEntity> {
        return this.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }
}
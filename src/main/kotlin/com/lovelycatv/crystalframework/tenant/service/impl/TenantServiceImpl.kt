package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantServiceImpl(
    private val tenantRepository: TenantRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val tenantMemberRelationService: TenantMemberRelationService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantMemberService: TenantMemberService,
    private val tenantMemberManagerService: TenantMemberManagerService,
) : TenantService {
    private val logger = logger()

    override val cacheStore: ExpiringKVStore<String, TenantEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantEntity> = TenantEntity::class

    override fun getRepository(): TenantRepository {
        return this.tenantRepository
    }

    override suspend fun getUserTenants(userId: Long): List<TenantEntity> {
        return tenantMemberRelationService
            .getUserTenantMembers(userId)
            .mapNotNull {
                this.getByIdOrNull(it.tenantId)
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun transferOwnership(tenantId: Long, targetUserId: Long) {
        val tenant = this.getByIdOrThrow(tenantId)

        val tenantSettings = tenant.getSettingsObject()
            ?: throw BusinessException("Tenant settings not found")

        val originalOwnerMember = tenantMemberService
            .getRepository()
            .findByTenantIdAndMemberUserId(tenantId, tenant.ownerUserId)
            .awaitFirstOrNull()
            ?: throw BusinessException("Unexpected error, tenant original owner member not found")

        val targetOwnerMember = tenantMemberService
            .getRepository()
            .findByTenantIdAndMemberUserId(tenantId, targetUserId)
            .awaitFirstOrNull()
            ?: tenantMemberManagerService
                .create(
                    ManagerCreateTenantMemberDTO(
                        tenantId = tenant.id,
                        memberUserId = targetUserId,
                        status = TenantMemberStatus.ACTIVE.typeId,
                    )
                )

        // Reset role of original owner
        tenantMemberRoleRelationService.setMemberRoles(
            originalOwnerMember.id,
            listOf(tenantSettings.defaultMemberRoleId)
        )

        // Reset role of target member role
        tenantMemberRoleRelationService.setMemberRoles(
            targetOwnerMember.id,
            listOf(tenantSettings.defaultOwnerRoleId)
        )

        withUpdateEntityContext(tenant) {
            this.getRepository().save(
                tenant.apply {
                    this.ownerUserId = targetOwnerMember.memberUserId
                }
            ).awaitFirstOrNull() ?: throw BusinessException("Could not transfer ownership of tenant")
        }

        logger.info("Tenant ownership transferred successfully, " +
                "tenant: ${tenant.name} - ${tenant.id}, " +
                "from [user: ${originalOwnerMember.memberUserId}, member: ${originalOwnerMember.id}] " +
                "to [user: ${targetOwnerMember.memberUserId}, member: ${targetOwnerMember.id}]"
        )
    }
}

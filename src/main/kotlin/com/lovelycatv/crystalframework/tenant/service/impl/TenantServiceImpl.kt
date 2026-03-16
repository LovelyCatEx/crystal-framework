package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.controller.dto.UpdateTenantProfileDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.vo.TenantProfileVO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.tenant.utils.toProfileVO
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
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
    private val fileResourceService: FileResourceService,
    private val fileResourceServiceManager: FileResourceServiceManager,
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

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun updateTenantProfile(tenantId: Long, dto: UpdateTenantProfileDTO) {
        val tenant = this.getByIdOrThrow(tenantId)

        // Update fields if provided
        dto.name?.let { tenant.name = it }
        dto.description?.let { tenant.description = it }
        dto.contactName?.let { tenant.contactName = it }
        dto.contactEmail?.let { tenant.contactEmail = it }
        dto.contactPhone?.let { tenant.contactPhone = it }
        dto.address?.let { tenant.address = it }

        withUpdateEntityContext(tenant) {
            this.getRepository().save(tenant).awaitFirstOrNull()
                ?: throw BusinessException("Could not update tenant profile")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun uploadTenantIcon(userId: Long, tenantId: Long, file: FilePart) {
        val tenant = this.getByIdOrThrow(tenantId)

        val (_, extension) = file.filename().split(".")
        val targetFileName = UUID.randomUUID().toString() + "." + extension

        val service = fileResourceServiceManager
            .getService(tenantId, ResourceFileType.TENANT_ICON, targetFileName)

        val result = service.uploadFile(
            userId,
            ResourceFileType.TENANT_ICON,
            file,
            targetFileName
        )

        if (!result.success || result.fileResourceEntity == null) {
            logger.error("could not upload icon for tenant: $tenantId, fileResource: ${result.fileResourceEntity}", result.exception)
            throw BusinessException("could not upload tenant icon", result.exception)
        }

        val updatedTenant = withUpdateEntityContext(tenant) {
            tenant.icon = result.fileResourceEntity.id
            this.getRepository().save(tenant).awaitFirstOrNull()
                ?: throw BusinessException("Could not update tenant icon")
        }

        logger.info("Tenant $tenantId uploaded icon, resource details: ${result.fileResourceEntity.id}")
    }
}

package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.tenant.constants.TenantRole
import com.lovelycatv.crystalframework.tenant.constants.TenantRolePermissionRelation
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.service.TenantInitializeService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.tenant.types.TenantSettings
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TenantInitializeServiceImpl(
    private val tenantRoleManagerService: TenantRoleManagerService,
    private val tenantPermissionManagerService: TenantPermissionManagerService,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantMemberManagerService: TenantMemberManagerService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantService: TenantService
) : TenantInitializeService {
    private val logger = logger()

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun initializeTenant(tenantId: Long, ownerUserId: Long) {
        val tenant = tenantService.getByIdOrNull(tenantId)
            ?: throw BusinessException("Tenant not found")

        logger.info("Initializing Tenant: ${tenant.name}, details: ${tenant.toJSONString()}")

        val rolePermissionEntityMap = TenantRolePermissionRelation
            .mapping
            .map { (role, permissions) ->
                val roleEntity = tenantRoleManagerService.createFromDeclaration(tenant.id, role)

                val permissionEntities = permissions
                    .mapNotNull { permission ->
                        tenantPermissionManagerService
                            .getRepository()
                            .findByName(permission.name)
                            .awaitFirstOrNull()
                    }

                tenantRolePermissionRelationService.setRolePermissions(
                    roleId = roleEntity.id,
                    permissionIds = permissionEntities.map { it.id }
                )

                role to (roleEntity to permissionEntities)
            }
            .toMap()

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: roles and permissions initialized")

        val settings = TenantSettings(
            defaultOwnerRoleId = rolePermissionEntityMap[TenantRole.ROOT]!!.first.id,
            defaultMemberRoleId = rolePermissionEntityMap[TenantRole.MEMBER]!!.first.id,
        )


        tenantService.withUpdateEntityContext(tenant) {
            tenantService.getRepository().save(
                tenant.apply {
                    this.settings = settings.toJSONString()
                }
            ).awaitFirstOrNull() ?: throw BusinessException("Could not save tenant settings when initializing")
        }

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: settings initialized, data: ${tenant.settings}")

        val ownerMember = tenantMemberManagerService.create(
            ManagerCreateTenantMemberDTO(
                tenantId = tenant.id,
                memberUserId = ownerUserId,
                status = TenantMemberStatus.ACTIVE.typeId,
            )
        )

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: owner member relation initialized, data: ${tenant.settings}")

        tenantMemberRoleRelationService.setMemberRoles(
            memberId = ownerMember.id,
            roleIds = listOf(settings.defaultOwnerRoleId)
        )

        logger.info("Tenant ${tenant.name} - ${tenant.id} initialization: owner member role initialized, roleId: ${settings.defaultOwnerRoleId}")
    }
}
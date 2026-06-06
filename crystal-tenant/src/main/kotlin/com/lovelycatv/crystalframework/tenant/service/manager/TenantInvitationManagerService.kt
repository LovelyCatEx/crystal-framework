package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerDeleteInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerReadInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository

interface TenantInvitationManagerService : BaseTenantResourceManagerService<
        TenantInvitationRepository,
        TenantInvitationEntity,
        ManagerCreateInvitationDTO,
        ManagerReadInvitationDTO,
        ManagerUpdateInvitationDTO,
        ManagerDeleteInvitationDTO
        >

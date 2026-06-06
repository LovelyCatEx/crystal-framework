package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerCreateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerDeleteTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerReadTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerUpdateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantMessageChannelEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMessageChannelRepository

interface TenantMessageChannelManagerService : BaseTenantResourceManagerService<
        TenantMessageChannelRepository,
        TenantMessageChannelEntity,
        ManagerCreateTenantMessageChannelDTO,
        ManagerReadTenantMessageChannelDTO,
        ManagerUpdateTenantMessageChannelDTO,
        ManagerDeleteTenantMessageChannelDTO
        > {
    /**
     * Resolves the persisted record at [channelId] into a strongly-typed, decrypted
     * [ChannelConfig] ready to feed into
     * [com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService].
     */
    suspend fun resolveConfig(channelId: Long): ChannelConfig
}

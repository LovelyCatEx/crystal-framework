package com.lovelycatv.crystalframework.messagechannel.service.manager

import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerCreateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerDeleteMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerReadMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerUpdateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.entity.MessageChannelEntity
import com.lovelycatv.crystalframework.messagechannel.repository.MessageChannelRepository
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

interface MessageChannelManagerService : BaseScopedManagerService<
        MessageChannelRepository,
        MessageChannelEntity,
        ManagerCreateMessageChannelDTO,
        ManagerReadMessageChannelDTO,
        ManagerUpdateMessageChannelDTO,
        ManagerDeleteMessageChannelDTO
        > {
    /**
     * Resolves the persisted record at [channelId] into a strongly-typed, decrypted
     * [ChannelConfig] ready to feed into
     * [com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService].
     *
     * The caller must declare which scope the lookup is happening under via
     * [expectedScope] / [expectedScopeId]; the channel entity is rejected when its own
     * scope does not match, preventing cross-tenant reads of encrypted config
     * (SMTP passwords, Lark bot tokens, ...).
     */
    suspend fun resolveConfig(
        channelId: Long,
        expectedScope: ResourceScope,
        expectedScopeId: Long?,
    ): ChannelConfig
}

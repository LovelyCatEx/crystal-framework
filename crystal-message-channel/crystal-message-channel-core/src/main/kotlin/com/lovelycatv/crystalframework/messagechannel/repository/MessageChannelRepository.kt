package com.lovelycatv.crystalframework.messagechannel.repository

import com.lovelycatv.crystalframework.messagechannel.entity.MessageChannelEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MessageChannelRepository : BaseRepository<MessageChannelEntity> {
    fun findAllByScopeId(scopeId: Long): Flux<MessageChannelEntity>

    fun findAllByScopeAndScopeId(scope: Int, scopeId: Long): Flux<MessageChannelEntity>

    fun findAllByScopeAndScopeIdAndChannelType(
        scope: Int,
        scopeId: Long,
        channelType: Int,
    ): Flux<MessageChannelEntity>

    fun findByScopeAndScopeIdAndChannelTypeAndName(
        scope: Int,
        scopeId: Long,
        channelType: Int,
        name: String,
    ): Mono<MessageChannelEntity>
}

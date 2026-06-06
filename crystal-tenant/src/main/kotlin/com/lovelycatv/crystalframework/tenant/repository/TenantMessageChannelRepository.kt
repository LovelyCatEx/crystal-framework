package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMessageChannelEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantMessageChannelRepository : BaseRepository<TenantMessageChannelEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantMessageChannelEntity>

    fun findAllByTenantIdAndChannelType(tenantId: Long, channelType: Int): Flux<TenantMessageChannelEntity>

    fun findByTenantIdAndChannelTypeAndName(
        tenantId: Long,
        channelType: Int,
        name: String,
    ): Mono<TenantMessageChannelEntity>
}

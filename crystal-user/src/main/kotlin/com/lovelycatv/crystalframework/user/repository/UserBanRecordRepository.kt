package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.UserBanRecordEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserBanRecordRepository : BaseRepository<UserBanRecordEntity> {
    fun findByUserId(userId: Long): Flux<UserBanRecordEntity>

    fun findByUserIdIn(userIds: Collection<Long>): Flux<UserBanRecordEntity>
}

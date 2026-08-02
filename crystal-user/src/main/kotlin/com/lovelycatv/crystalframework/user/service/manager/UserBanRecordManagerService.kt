package com.lovelycatv.crystalframework.user.service.manager

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerCreateUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerDeleteUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerReadUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerUpdateUserBanRecordDTO
import com.lovelycatv.crystalframework.user.entity.UserBanRecordEntity
import com.lovelycatv.crystalframework.user.repository.UserBanRecordRepository

interface UserBanRecordManagerService : CachedBaseManagerService<
        UserBanRecordRepository,
        UserBanRecordEntity,
        ManagerCreateUserBanRecordDTO,
        ManagerReadUserBanRecordDTO,
        ManagerUpdateUserBanRecordDTO,
        ManagerDeleteUserBanRecordDTO
> {
    override suspend fun create(dto: ManagerCreateUserBanRecordDTO): UserBanRecordEntity {
        throw BusinessException("Ban records cannot be created through the standard manager endpoint")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateUserBanRecordDTO,
        original: UserBanRecordEntity
    ): UserBanRecordEntity {
        throw BusinessException("Ban records cannot be updated")
    }

    /**
     * Create an effective ban record for the given user.
     */
    suspend fun banUser(userId: Long, reason: String, banUntil: Long?, operatorUserId: Long)

    /**
     * Lift the currently effective ban of the given user by stamping its [UserBanRecordEntity.liftedTime].
     */
    suspend fun unbanUser(userId: Long)

    /**
     * Return the currently effective ban record of the given user, or null when the user is not banned.
     *
     * A ban is effective when it has not been lifted ([UserBanRecordEntity.liftedTime] is null) and either
     * has no expiry or the expiry is still in the future.
     */
    suspend fun getActiveBan(userId: Long): UserBanRecordEntity?

    /**
     * Return the subset of [userIds] that currently have an effective ban. Batched to avoid N+1 when
     * enriching a page of users with their ban status.
     */
    suspend fun getActivelyBannedUserIds(userIds: Collection<Long>): Set<Long>
}

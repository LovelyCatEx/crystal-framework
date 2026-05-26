package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface UserManagerService : CachedBaseManagerService<
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
> {
    override suspend fun query(
        dto: ManagerReadUserDTO,
        isAdvanceQuery: suspend (dto: ManagerReadUserDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadUserDTO, limit: Int, offset: Int) -> PaginatedResponseData<UserEntity>
    ): PaginatedResponseData<UserEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { d ->
                d.searchKeyword != null || d.username != null || d.email != null || d.nickname != null || d.startTime != null || d.endTime != null
            },
            doAdvanceQuery = { d, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        d.searchKeyword,
                        d.username,
                        d.email,
                        d.nickname,
                        d.startTime,
                        d.endTime,
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    d.searchKeyword,
                    d.username,
                    d.email,
                    d.nickname,
                    d.startTime,
                    d.endTime,
                    limit,
                    offset
                ).awaitListWithTimeout()

                d.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        )
    }
}

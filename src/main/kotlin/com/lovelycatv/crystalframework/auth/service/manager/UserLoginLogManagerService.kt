package com.lovelycatv.crystalframework.auth.service.manager

import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerCreateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerDeleteUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerReadUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerUpdateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface UserLoginLogManagerService : CachedBaseManagerService<
        UserLoginLogRepository,
        UserLoginLogEntity,
        ManagerCreateUserLoginLogDTO,
        ManagerReadUserLoginLogDTO,
        ManagerUpdateUserLoginLogDTO,
        ManagerDeleteUserLoginLogDTO
        > {
    override suspend fun query(
        dto: ManagerReadUserLoginLogDTO,
        isAdvanceQuery: suspend (dto: ManagerReadUserLoginLogDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadUserLoginLogDTO, limit: Int, offset: Int) -> PaginatedResponseData<UserLoginLogEntity>
    ): PaginatedResponseData<UserLoginLogEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.userId != null || dto.username != null || dto.tenantId != null || dto.loginMethod != null || dto.oauth2Type != null || dto.success != null || dto.remoteIp != null || dto.startTime != null || dto.endTime != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.userId,
                        dto.username,
                        dto.tenantId,
                        dto.loginMethod,
                        dto.oauth2Type,
                        dto.success,
                        dto.remoteIp,
                        dto.startTime,
                        dto.endTime
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.userId,
                    dto.username,
                    dto.tenantId,
                    dto.loginMethod,
                    dto.oauth2Type,
                    dto.success,
                    dto.remoteIp,
                    dto.startTime,
                    dto.endTime,
                    limit,
                    offset
                ).awaitListWithTimeout()

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        )
    }

    override suspend fun create(dto: ManagerCreateUserLoginLogDTO): UserLoginLogEntity {
        throw BusinessException("User login logs cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateUserLoginLogDTO, original: UserLoginLogEntity): UserLoginLogEntity {
        throw BusinessException("User login logs cannot be updated")
    }
}
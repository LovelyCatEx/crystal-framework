package com.lovelycatv.crystalframework.auth.service.manager

import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerCreateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerDeleteUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerReadUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerUpdateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface UserLoginLogManagerService : CachedBaseManagerService<
        UserLoginLogRepository,
        UserLoginLogEntity,
        ManagerCreateUserLoginLogDTO,
        ManagerReadUserLoginLogDTO,
        ManagerUpdateUserLoginLogDTO,
        ManagerDeleteUserLoginLogDTO
> {
    override suspend fun create(dto: ManagerCreateUserLoginLogDTO): UserLoginLogEntity {
        throw BusinessException("User login logs cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateUserLoginLogDTO, original: UserLoginLogEntity): UserLoginLogEntity {
        throw BusinessException("User login logs cannot be updated")
    }
}

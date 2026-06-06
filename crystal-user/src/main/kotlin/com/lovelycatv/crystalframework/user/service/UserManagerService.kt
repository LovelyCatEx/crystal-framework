package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository

interface UserManagerService : CachedBaseManagerService<
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
>

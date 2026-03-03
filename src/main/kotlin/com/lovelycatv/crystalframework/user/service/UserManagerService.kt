package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerUpdateUserDTO
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

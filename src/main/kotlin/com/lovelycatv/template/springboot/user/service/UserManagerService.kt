package com.lovelycatv.template.springboot.user.service

import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.template.springboot.user.entity.UserEntity
import com.lovelycatv.template.springboot.user.repository.UserRepository
import com.lovelycatv.template.springboot.shared.service.BaseManagerService

interface UserManagerService : BaseManagerService<
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
>

package com.lovelycatv.template.springboot.user.service.impl

import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.template.springboot.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.template.springboot.user.entity.UserEntity
import com.lovelycatv.template.springboot.user.repository.UserRepository
import com.lovelycatv.template.springboot.user.service.UserManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserManagerServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val snowIdGenerator: SnowIdGenerator
) : UserManagerService {
    override fun getRepository(): UserRepository {
        return userRepository
    }

    override suspend fun create(dto: ManagerCreateUserDTO): UserEntity {
        val entity = UserEntity(
            id = snowIdGenerator.nextId(),
            username = dto.username,
            password = passwordEncoder.encode(dto.password)!!,
            email = dto.email,
            nickname = dto.nickname
        ).apply { newEntity() }
        return userRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create user")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateUserDTO, original: UserEntity): UserEntity {
        return original.apply {
            dto.email?.let { email = it }
            dto.nickname?.let { nickname = it }
        }
    }
}

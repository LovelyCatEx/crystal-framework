package com.lovelycatv.template.springboot.user.service

import com.lovelycatv.template.springboot.shared.service.BaseService
import com.lovelycatv.template.springboot.user.entity.UserEntity
import com.lovelycatv.template.springboot.user.repository.UserRepository
import com.lovelycatv.template.springboot.user.service.result.UserRbacQueryResult
import org.springframework.security.core.userdetails.ReactiveUserDetailsService

interface UserService : BaseService<UserRepository, UserEntity, Long>, ReactiveUserDetailsService {
    suspend fun register(
        username: String,
        password: String,
        email: String,
        emailConfirmationCode: String
    )

    suspend fun requestRegisterEmailConfirmationCode(email: String)

    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult
}
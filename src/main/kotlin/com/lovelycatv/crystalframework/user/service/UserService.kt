package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.BaseService
import com.lovelycatv.crystalframework.user.controller.vo.UserProfileVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import org.springframework.security.core.userdetails.ReactiveUserDetailsService

interface UserService : BaseService<UserRepository, UserEntity>, ReactiveUserDetailsService {
    suspend fun register(
        username: String,
        password: String,
        email: String,
        emailConfirmationCode: String
    )

    suspend fun requestRegisterEmailConfirmationCode(email: String)

    suspend fun resetPassword(email: String, emailCode: String, newPassword: String)

    suspend fun requestResetPasswordEmailConfirmationCode(email: String)

    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult

    suspend fun getUserProfileVO(userId: Long, fullAccess: Boolean): UserProfileVO
}
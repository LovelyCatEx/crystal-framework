package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.BaseService
import com.lovelycatv.crystalframework.user.controller.dto.UpdateUserProfileDTO
import com.lovelycatv.crystalframework.user.controller.vo.UserProfileVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import org.springframework.http.codec.multipart.FilePart
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

    suspend fun requestResetPasswordEmailConfirmationCode(userId: Long) {
        this.requestResetPasswordEmailConfirmationCode(
            this.getByIdOrThrow(userId).email
        )
    }

    suspend fun requestResetPasswordEmailConfirmationCode(email: String)

    suspend fun resetEmailAddress(userId: Long, emailCode: String, newEmail: String)

    suspend fun requestResetEmailAddressEmailConfirmationCode(email: String)

    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult

    suspend fun getUserProfileVO(userId: Long, fullAccess: Boolean): UserProfileVO

    suspend fun updateUserProfile(userId: Long, dto: UpdateUserProfileDTO)

    suspend fun uploadAvatar(userId: Long, file: FilePart)
}
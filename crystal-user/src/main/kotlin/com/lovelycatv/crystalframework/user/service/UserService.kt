package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.user.controller.dto.UpdateUserProfileDTO
import com.lovelycatv.crystalframework.user.controller.vo.UserProfileVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import org.springframework.http.codec.multipart.FilePart
import org.springframework.transaction.annotation.Transactional

interface UserService : CachedBaseService<UserRepository, UserEntity> {
    @Transactional(rollbackFor = [Exception::class])
    suspend fun register(
        username: String,
        password: String,
        email: String,
        emailConfirmationCode: String
    )

    suspend fun requestRegisterEmailConfirmationCode(email: String)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun resetPassword(email: String, emailCode: String, newPassword: String)

    suspend fun requestResetPasswordEmailConfirmationCode(email: String)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun resetEmailAddress(userId: Long, emailCode: String, newEmail: String)

    suspend fun requestResetEmailAddressEmailConfirmationCode(email: String)

    suspend fun getUserProfileVO(userId: Long, fullAccess: Boolean): UserProfileVO

    @Transactional(rollbackFor = [Exception::class])
    suspend fun updateUserProfile(userId: Long, dto: UpdateUserProfileDTO)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun uploadAvatar(userId: Long, file: FilePart)

    @Transactional(rollbackFor = [Exception::class])
    suspend fun bindUserFromOAuthAccount(oauthAccountId: Long, username: String, password: String): UserEntity

    @Transactional(rollbackFor = [Exception::class])
    suspend fun bindUserFromOAuthAccount(oauthAccountId: Long, userId: Long): UserEntity

    @Transactional(rollbackFor = [Exception::class])
    suspend fun registerFromOAuthAccount(
        oauthAccountId: Long,
        username: String,
        password: String,
        nickname: String
    ): UserEntity
}
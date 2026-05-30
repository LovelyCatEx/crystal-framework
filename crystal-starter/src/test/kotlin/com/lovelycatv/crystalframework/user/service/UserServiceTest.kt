package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.user.entity.UserEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals

class UserServiceTest(
    @Autowired
    private val userService: UserService
) : CrystalFrameworkApplicationTests() {
    @Test
    fun register() {
        withTransactionalRollback("user-register") {
            mockRegisteredUser()
        }
    }

    suspend fun mockRegisteredUser(): UserEntity {
        val suffix = java.util.UUID.randomUUID().toString().substring(0, 8)
        val username = "testuser-$suffix"
        val password = "testpassword"
        val email = "test-$suffix@crystalframework.com"
        val emailCode = "123456"

        userService.requestRegisterEmailConfirmationCode(email)

        userService.register(username, password, email, emailCode)

        val user = userService.findByUsername(username).awaitSingleOrNull() as UserEntity
        assertNotNull(user)
        assertEquals(username, user.username)
        assertEquals(email, user.email)

        return user
    }

}
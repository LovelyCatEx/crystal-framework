package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class UserRbacQueryServiceTest(
    @Autowired
    private val userRbacQueryService: UserRbacQueryService,
    @Autowired
    private val applicationContext: ApplicationContext
) : CrystalFrameworkApplicationTests() {
    private val userServiceTest: UserServiceTest = getTestClassInstance(applicationContext)

    @Test
    fun getUserRbacAccessInfo() {
        withTransactionalRollback("getUserRbacAccessInfo") {
            val user = userServiceTest.mockRegisteredUser()

            val result = userRbacQueryService.getUserRbacAccessInfo(user.id)
            println(result.toPrettierJSONString())
            assertTrue {
                result.roles.any { it.name == SystemRole.ROLE_USER }
            }
        }
    }

}
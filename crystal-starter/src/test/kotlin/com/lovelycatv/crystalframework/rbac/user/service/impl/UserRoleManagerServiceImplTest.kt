package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleService
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class UserRoleManagerServiceImplTest(
    @Autowired private val userRoleManagerService: UserRoleManagerService,
    @Autowired private val userRoleService: UserRoleService,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    @Test
    fun authenticatedUserCannotCreateReservedRole() {
        withTransactionalRollback("user-role-create-reserved") {
            val user = setupUnauthorizedUser()

            withAuthenticatedUser(user) {
                SystemRole.PROTECTED_ROLE_NAMES.forEach { protectedName ->
                    assertProtectedRoleException(
                        runCatching {
                            userRoleManagerService.create(ManagerCreateRoleDTO(name = protectedName))
                        }.exceptionOrNull(),
                    )
                }
            }
        }
    }

    @Test
    fun authenticatedUserCannotRenameRoleToReservedName() {
        withTransactionalRollback("user-role-rename-reserved") {
            val role = userRoleManagerService.create(ManagerCreateRoleDTO(name = ROLE_NAME))
            val user = setupUnauthorizedUser()

            withAuthenticatedUser(user) {
                SystemRole.PROTECTED_ROLE_NAMES.forEach { protectedName ->
                    assertProtectedRoleException(
                        runCatching {
                            userRoleManagerService.update(ManagerUpdateRoleDTO(id = role.id, name = protectedName))
                        }.exceptionOrNull(),
                    )
                }
            }

            assertEquals(ROLE_NAME, userRoleManagerService.getByIdOrNull(role.id)?.name)
        }
    }

    @Test
    fun protectedRoleCannotBeRenamedOrDeleted() {
        withTransactionalRollback("user-role-mutate-protected") {
            val protectedRole = userRoleService.getAllRoles()
                .first { it.name == SystemRole.ROLE_ROOT }
            val ordinaryRole = userRoleManagerService.create(ManagerCreateRoleDTO(name = ROLE_NAME))
            val user = setupUnauthorizedUser()

            withAuthenticatedUser(user) {
                assertProtectedRoleException(
                    runCatching {
                        userRoleManagerService.update(
                            ManagerUpdateRoleDTO(id = protectedRole.id, name = RENAMED_ROLE_NAME),
                        )
                    }.exceptionOrNull(),
                )
                assertProtectedRoleException(
                    runCatching {
                        userRoleManagerService.batchDelete(listOf(protectedRole.id, ordinaryRole.id))
                    }.exceptionOrNull(),
                )
            }

            assertEquals(SystemRole.ROLE_ROOT, userRoleManagerService.getByIdOrNull(protectedRole.id)?.name)
            assertNotNull(userRoleManagerService.getByIdOrNull(ordinaryRole.id))
        }
    }

    private fun assertProtectedRoleException(exception: Throwable?) {
        val forbidden = assertIs<ForbiddenException>(exception)
        assertEquals(ForbiddenReason.ROLE_PROTECTED, forbidden.context?.reason)
        assertEquals(ResourceScope.SYSTEM, forbidden.context?.scope)
    }

    companion object {
        private const val ROLE_NAME = "custom-role"
        private const val RENAMED_ROLE_NAME = "renamed-role"
    }
}

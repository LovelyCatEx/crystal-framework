package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.interfaces.ResourceTenantMembershipChecker
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pure unit test for the read-authorization decision logic in [ResourceAccessServiceImpl].
 *
 * SYSTEM_ADMIN is intentionally not exercised here: it delegates to RbacUtils/ReactiveSecurityContext
 * which requires a Spring reactive security context (integration-level concern).
 */
class ResourceAccessServiceImplTest {

    /** Placeholder TTL; irrelevant to the visibility decision logic under test. */
    private val SIGNED_URL_TTL_SECONDS = 3600L

    private fun systemModuleClientReturning(visibility: ResourceVisibility): SystemModuleClient {
        val settings = mock<SystemSettings>()
        whenever(settings.resource).thenReturn(
            SystemSettings.Resource(
                signedUrl = SystemSettings.Resource.SignedUrl(ttlSeconds = SIGNED_URL_TTL_SECONDS),
                visibility = SystemSettings.Resource.Visibility(
                    userAvatar = visibility,
                    tenantIcon = visibility,
                    tenantMemberAvatar = visibility,
                )
            )
        )
        val client = mock<SystemModuleClient>()
        whenever(client.getSystemSettings(anyOrNull())).thenReturn(settings)
        return client
    }

    private fun entity(
        type: ResourceFileType,
        scope: ResourceScope,
        scopeId: Long = 0,
        userId: Long = 0,
    ): FileResourceEntity {
        return FileResourceEntity(
            id = 1L,
            scope = scope.typeId,
            scopeId = scopeId,
            userId = userId,
            type = type.typeId,
        )
    }

    private fun viewer(userId: Long, tenantId: Long? = null): UserAuthentication {
        return UserAuthentication(
            userId = userId,
            username = "u$userId",
            tenantId = tenantId,
            tenantMemberId = tenantId?.let { 1L },
        )
    }

    @Test
    fun publicIsReadableByAnonymous() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.PUBLIC),
            membershipChecker = null,
        )
        assertTrue(service.isReadable(entity(ResourceFileType.TENANT_ICON, ResourceScope.TENANT), viewer = null))
    }

    @Test
    fun authenticatedRejectsAnonymousButAllowsLoggedIn() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.AUTHENTICATED),
            membershipChecker = null,
        )
        val entity = entity(ResourceFileType.USER_AVATAR, ResourceScope.SYSTEM)
        assertFalse(service.isReadable(entity, viewer = null))
        assertTrue(service.isReadable(entity, viewer(userId = 7)))
    }

    @Test
    fun ownerOnlyAllowsOwnerRejectsOthers() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.OWNER_ONLY),
            membershipChecker = null,
        )
        val entity = entity(ResourceFileType.USER_AVATAR, ResourceScope.SYSTEM, userId = 42)
        assertTrue(service.isReadable(entity, viewer(userId = 42)))
        assertFalse(service.isReadable(entity, viewer(userId = 99)))
    }

    @Test
    fun scopeMemberTenantChecksMembership() = runTest {
        val checker = ResourceTenantMembershipChecker { tenantId, userId ->
            tenantId == 100L && userId == 5L
        }
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.SCOPE_MEMBER),
            membershipChecker = checker,
        )
        val entity = entity(ResourceFileType.TENANT_MEMBER_AVATAR, ResourceScope.TENANT, scopeId = 100L)
        assertTrue(service.isReadable(entity, viewer(userId = 5, tenantId = 100L)))
        assertFalse(service.isReadable(entity, viewer(userId = 6, tenantId = 100L)))
    }

    @Test
    fun scopeMemberFailsClosedWhenNoChecker() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.SCOPE_MEMBER),
            membershipChecker = null,
        )
        val entity = entity(ResourceFileType.TENANT_MEMBER_AVATAR, ResourceScope.TENANT, scopeId = 100L)
        assertFalse(service.isReadable(entity, viewer(userId = 5, tenantId = 100L)))
    }

    @Test
    fun scopeMemberSystemScopeDegradesToAuthenticated() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.SCOPE_MEMBER),
            membershipChecker = null,
        )
        val entity = entity(ResourceFileType.USER_AVATAR, ResourceScope.SYSTEM)
        assertTrue(service.isReadable(entity, viewer(userId = 5)))
        assertFalse(service.isReadable(entity, viewer = null))
    }

    @Test
    fun assertReadableThrowsForbiddenWhenDenied() = runTest {
        val service = ResourceAccessServiceImpl(
            systemModuleClientReturning(ResourceVisibility.OWNER_ONLY),
            membershipChecker = null,
        )
        val entity = entity(ResourceFileType.USER_AVATAR, ResourceScope.SYSTEM, userId = 42)
        assertFailsWith<ForbiddenException> {
            service.assertReadable(entity, viewer(userId = 99))
        }
    }
}

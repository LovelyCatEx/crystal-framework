package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.constants.RbacConstants
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UserRbacQueryServiceTest(
    @Autowired
    private val userRbacQueryService: UserRbacQueryService,
    @Autowired
    private val redisService: ReactiveRedisService,
    @Autowired
    private val applicationContext: ApplicationContext
) : CrystalFrameworkApplicationTests() {
    private val userServiceTest: UserServiceTest = getTestClassInstance(applicationContext)
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }

    /** Reuses the production key builders so the test asserts the exact key layout, no magic values. */
    private fun sliceKey(userId: Long, tenantId: Long?): String =
        RedisConstants.getUserAuthoritiesCacheKey(userId, tenantId)

    private fun indexKey(userId: Long): String =
        RedisConstants.getUserAuthoritiesIndexKey(userId)

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

    /**
     * H-1 regression: the authority cache is keyed per tenant slice, so a token scoped to one tenant
     * can never read the authorities computed for another. Before the fix the key was `userAuthorities:$userId`
     * alone, so the tenant call would overwrite the system slice and the system re-read would leak tenant
     * authorities (`I_ROLE_*` / `i.tenant.*`).
     */
    @Test
    fun getUserAuthorities_cachesPerTenantSliceWithoutCrossTenantBleed() {
        withTransactionalRollback("authorities-per-tenant-slice") {
            val user = userServiceTest.mockRegisteredUser()
            try {
                val tireType = tireTypeServiceTest.mockTireType()
                val (tenant, ownerMember) = tenantServiceTest.mockTenant(user.id, tireType.id)

                val systemAuthorities = userRbacQueryService
                    .getUserAuthorities(user.id, null, null, false)
                    .mapNotNull { it.authority }
                    .toSet()

                val tenantAuthorities = userRbacQueryService
                    .getUserAuthorities(user.id, tenant.id, ownerMember.id, false)
                    .mapNotNull { it.authority }
                    .toSet()

                // Both slices live under distinct keys, both indexed for later invalidation.
                assertTrue(redisService.hasKey(sliceKey(user.id, null)).awaitFirstOrNull() == true)
                assertTrue(redisService.hasKey(sliceKey(user.id, tenant.id)).awaitFirstOrNull() == true)
                val indexed = redisService.opsForSet<String>()
                    .members(indexKey(user.id))
                    .collectList()
                    .awaitFirstOrNull()
                    .orEmpty()
                    .toSet()
                assertEquals(setOf(sliceKey(user.id, null), sliceKey(user.id, tenant.id)), indexed)

                // The tenant slice carries system authorities plus its own tenant authorities.
                assertTrue(tenantAuthorities.containsAll(systemAuthorities))

                // Re-reading the system slice from cache must NOT return the tenant slice's authorities.
                val systemAuthoritiesAgain = userRbacQueryService
                    .getUserAuthorities(user.id, null, null, false)
                    .mapNotNull { it.authority }
                    .toSet()
                assertEquals(systemAuthorities, systemAuthoritiesAgain)
                assertFalse(systemAuthoritiesAgain.any { it.startsWith(RbacConstants.TENANT_ROLE_PREFIX) })
            } finally {
                // Redis writes are not covered by the transactional rollback; drop them explicitly.
                userRbacQueryService.clearUserAuthoritiesCache(user.id)
            }
        }
    }

    /** Clearing a user's cache must drop every tenant slice and the index Set itself. */
    @Test
    fun clearUserAuthoritiesCache_dropsAllTenantSlices() {
        withTransactionalRollback("authorities-clear-all-slices") {
            val user = userServiceTest.mockRegisteredUser()
            try {
                val tireType = tireTypeServiceTest.mockTireType()
                val (tenant, ownerMember) = tenantServiceTest.mockTenant(user.id, tireType.id)

                userRbacQueryService.getUserAuthorities(user.id, null, null, false)
                userRbacQueryService.getUserAuthorities(user.id, tenant.id, ownerMember.id, false)

                userRbacQueryService.clearUserAuthoritiesCache(user.id)

                assertFalse(redisService.hasKey(sliceKey(user.id, null)).awaitFirstOrNull() == true)
                assertFalse(redisService.hasKey(sliceKey(user.id, tenant.id)).awaitFirstOrNull() == true)
                assertFalse(redisService.hasKey(indexKey(user.id)).awaitFirstOrNull() == true)
            } finally {
                userRbacQueryService.clearUserAuthoritiesCache(user.id)
            }
        }
    }
}

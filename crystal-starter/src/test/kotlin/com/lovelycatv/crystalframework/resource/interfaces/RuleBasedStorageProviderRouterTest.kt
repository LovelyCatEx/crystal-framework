package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.service.impl.StorageProviderManagerServiceImplTest
import com.lovelycatv.crystalframework.resource.service.manager.impl.StorageProviderRoutingRuleManagerServiceImplTest
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.RuleDistributionType
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Verifies [RuleBasedStorageProviderRouter] — the bean wired by default in
 * `StorageProviderRouterConfiguration`, so autowiring [StorageProviderRouter] here resolves to it.
 */
class RuleBasedStorageProviderRouterTest(
    @Autowired private val storageProviderRouter: StorageProviderRouter,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val providerTest: StorageProviderManagerServiceImplTest by lazy { getTestClassInstance(applicationContext) }
    private val ruleTest: StorageProviderRoutingRuleManagerServiceImplTest by lazy { getTestClassInstance(applicationContext) }

    private fun avatarContext() = RoutingContext.of(
        userId = 1L,
        fileType = ResourceFileType.USER_AVATAR,
        fileName = "avatar.png",
    )

    private fun iconContext() = RoutingContext.of(
        userId = 2L,
        fileType = ResourceFileType.TENANT_ICON,
        fileName = "icon.png",
    )

    private fun fileTypeCondition(fileType: ResourceFileType): String =
        GroupNode(
            QueryLogic.AND,
            listOf(ConditionNode("fileType", QueryOperator.EQ, fileType.typeId))
        ).toJSONString()

    @Test
    fun `first matching rule by priority order wins over a later broader rule`() {
        withTransactionalRollback("router-priority-order") {
            val avatarProvider = providerTest.mockStorageProvider()
            val fallbackProvider = providerTest.mockStorageProvider()

            // Created first -> priority 0, only matches USER_AVATAR uploads.
            ruleTest.mockRoutingRule(
                name = "avatar-rule",
                conditionTree = fileTypeCondition(ResourceFileType.USER_AVATAR),
                targetProviderIds = listOf(avatarProvider.id),
            )
            // Created second -> priority 1, match-all fallback.
            ruleTest.mockRoutingRule(
                name = "fallback-rule",
                conditionTree = null,
                targetProviderIds = listOf(fallbackProvider.id),
            )

            val resolved = storageProviderRouter.get(avatarContext())
            assertEquals(avatarProvider.id, resolved.id)
        }
    }

    @Test
    fun `falls through to match-all fallback rule when no earlier rule matches`() {
        withTransactionalRollback("router-fallback") {
            val avatarProvider = providerTest.mockStorageProvider()
            val fallbackProvider = providerTest.mockStorageProvider()

            ruleTest.mockRoutingRule(
                name = "avatar-rule",
                conditionTree = fileTypeCondition(ResourceFileType.USER_AVATAR),
                targetProviderIds = listOf(avatarProvider.id),
            )
            ruleTest.mockRoutingRule(
                name = "fallback-rule",
                conditionTree = null,
                targetProviderIds = listOf(fallbackProvider.id),
            )

            // TENANT_ICON doesn't match the first rule's condition, so it must fall through.
            val resolved = storageProviderRouter.get(iconContext())
            assertEquals(fallbackProvider.id, resolved.id)
        }
    }

    @Test
    fun `disabled target provider is skipped, falls through to next rule`() {
        withTransactionalRollback("router-disabled-provider") {
            val disabledProvider = providerTest.mockStorageProvider(active = false)
            val activeFallback = providerTest.mockStorageProvider()

            ruleTest.mockRoutingRule(
                name = "disabled-only-rule",
                conditionTree = null,
                targetProviderIds = listOf(disabledProvider.id),
            )
            ruleTest.mockRoutingRule(
                name = "active-fallback-rule",
                conditionTree = null,
                targetProviderIds = listOf(activeFallback.id),
            )

            val resolved = storageProviderRouter.get(avatarContext())
            assertEquals(activeFallback.id, resolved.id)
        }
    }

    @Test
    fun `no rule matches at all throws BusinessException`() {
        withTransactionalRollback("router-no-match") {
            val provider = providerTest.mockStorageProvider()
            ruleTest.mockRoutingRule(
                name = "icon-only-rule",
                conditionTree = fileTypeCondition(ResourceFileType.TENANT_ICON),
                targetProviderIds = listOf(provider.id),
            )

            val result = runCatching { storageProviderRouter.get(avatarContext()) }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue(ex.message!!.contains("no storage provider routing rule matched"))
        }
    }

    @Test
    fun `FIRST_AVAILABLE distribution always picks the first candidate`() {
        withTransactionalRollback("router-first-available") {
            val first = providerTest.mockStorageProvider()
            val second = providerTest.mockStorageProvider()

            ruleTest.mockRoutingRule(
                name = "first-available-rule",
                conditionTree = null,
                targetProviderIds = listOf(first.id, second.id),
                distributionType = RuleDistributionType.FIRST_AVAILABLE,
            )

            repeat(5) {
                assertEquals(first.id, storageProviderRouter.get(avatarContext()).id)
            }
        }
    }

    @Test
    fun `RANDOM distribution picks one of the candidate providers`() {
        withTransactionalRollback("router-random") {
            val first = providerTest.mockStorageProvider()
            val second = providerTest.mockStorageProvider()
            val candidateIds = setOf(first.id, second.id)

            ruleTest.mockRoutingRule(
                name = "random-rule",
                conditionTree = null,
                targetProviderIds = listOf(first.id, second.id),
                distributionType = RuleDistributionType.RANDOM,
            )

            repeat(10) {
                val resolved = storageProviderRouter.get(avatarContext())
                assertTrue(resolved.id in candidateIds)
            }
        }
    }
}

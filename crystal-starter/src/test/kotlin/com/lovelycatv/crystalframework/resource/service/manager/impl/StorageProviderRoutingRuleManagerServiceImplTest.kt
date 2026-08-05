package com.lovelycatv.crystalframework.resource.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderRoutingRuleManagerService
import com.lovelycatv.crystalframework.resource.types.RuleDistributionType
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StorageProviderRoutingRuleManagerServiceImplTest(
    @Autowired private val storageProviderRoutingRuleManagerService: StorageProviderRoutingRuleManagerService,
    @Autowired private val storageProviderRouter: StorageProviderRouter,
) : CrystalFrameworkApplicationTests() {

    /**
     * Creates a rule then invalidates the router's list cache so callers (including
     * [com.lovelycatv.crystalframework.resource.interfaces.RuleBasedStorageProviderRouterTest])
     * see it immediately — the manager service's `create()` does not itself invalidate the
     * router's list cache, only `update()` does (via [com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService]'s
     * entity-cache eviction, which [com.lovelycatv.crystalframework.resource.event.StorageProviderRouterCacheInvalidator] listens to).
     */
    suspend fun mockRoutingRule(
        name: String,
        conditionTree: String? = null,
        targetProviderIds: List<Long>,
        distributionType: RuleDistributionType = RuleDistributionType.FIRST_AVAILABLE,
        enabled: Boolean = true,
    ): StorageProviderRoutingRuleEntity {
        val created = storageProviderRoutingRuleManagerService.create(
            ManagerCreateStorageProviderRoutingRuleDTO(
                name = name,
                conditionTree = conditionTree,
                targetProviderIds = targetProviderIds.toJSONString(),
                distributionType = distributionType.typeId,
                enabled = enabled,
            )
        )
        storageProviderRouter.invalidateCache()
        return created
    }

    /**
     * Soft-deletes every pre-existing routing rule so a test observes only the rules it creates itself.
     * These integration tests run against a shared database that may already hold real rules; without this
     * the router's priority scan and `create()`'s `maxPriority` calculation would both be skewed by them.
     * The delete runs inside the caller's [withTransactionalRollback], so the real rows are restored on rollback.
     */
    suspend fun clearExistingRules() {
        val existingIds = storageProviderRoutingRuleManagerService.getRepository()
            .findAll()
            .awaitListWithTimeout()
            .map { it.id }
        if (existingIds.isNotEmpty()) {
            storageProviderRoutingRuleManagerService.batchDelete(existingIds)
        }
        storageProviderRouter.invalidateCache()
    }

    @Test
    fun createRoutingRule() {
        withTransactionalRollback("routing-rule-create") {
            val rule = mockRoutingRule(name = "test-rule", targetProviderIds = listOf(1L))
            assertNotNull(rule)
            println(rule.toPrettierJSONString())
        }
    }

    @Test
    fun reorderAssignsPriorityByIndex() {
        withTransactionalRollback("routing-rule-reorder") {
            clearExistingRules()
            val ruleA = mockRoutingRule(name = "rule-a", targetProviderIds = listOf(1L))
            val ruleB = mockRoutingRule(name = "rule-b", targetProviderIds = listOf(2L))

            storageProviderRoutingRuleManagerService.reorder(listOf(ruleB.id, ruleA.id))

            // reorder writes the new priority to the DB, but its post-commit cache eviction never fires inside
            // a rollback-only test transaction, so the entity cache still holds each rule's create-time priority.
            // Evict manually to force the assertion reads below to hit the freshly-written rows.
            storageProviderRoutingRuleManagerService.removeCache(ruleB.id)
            storageProviderRoutingRuleManagerService.removeCache(ruleA.id)

            val reorderedB = storageProviderRoutingRuleManagerService.getByIdOrNull(ruleB.id)
            val reorderedA = storageProviderRoutingRuleManagerService.getByIdOrNull(ruleA.id)
            assertEquals(0, reorderedB?.priority)
            assertEquals(1, reorderedA?.priority)
        }
    }
}

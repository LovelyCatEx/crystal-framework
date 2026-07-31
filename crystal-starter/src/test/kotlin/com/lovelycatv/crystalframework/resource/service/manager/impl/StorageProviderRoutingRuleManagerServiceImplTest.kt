package com.lovelycatv.crystalframework.resource.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderRoutingRuleManagerService
import com.lovelycatv.crystalframework.resource.types.RuleDistributionType
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
            val ruleA = mockRoutingRule(name = "rule-a", targetProviderIds = listOf(1L))
            val ruleB = mockRoutingRule(name = "rule-b", targetProviderIds = listOf(2L))

            storageProviderRoutingRuleManagerService.reorder(listOf(ruleB.id, ruleA.id))

            val reorderedB = storageProviderRoutingRuleManagerService.getByIdOrNull(ruleB.id)
            val reorderedA = storageProviderRoutingRuleManagerService.getByIdOrNull(ruleA.id)
            assertEquals(0, reorderedB?.priority)
            assertEquals(1, reorderedA?.priority)
        }
    }
}

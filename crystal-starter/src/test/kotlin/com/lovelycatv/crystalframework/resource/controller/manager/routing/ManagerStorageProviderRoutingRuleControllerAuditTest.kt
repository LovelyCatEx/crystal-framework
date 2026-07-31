package com.lovelycatv.crystalframework.resource.controller.manager.routing

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.SimulateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ManagerStorageProviderRoutingRuleControllerAuditTest {
    @Test
    fun `simulate endpoint declares audit metadata`() {
        val method = ManagerStorageProviderRoutingRuleController::class.java.declaredMethods.single {
            it.name == "simulate" &&
                it.parameterTypes.take(2) == listOf(
                    UserAuthentication::class.java,
                    SimulateStorageProviderRoutingRuleDTO::class.java,
                )
        }
        val audit = method.getAnnotation(Audit::class.java)

        assertNotNull(audit)
        assertEquals(AuditAction.READ, audit.action)
        assertEquals(TableConstants.TABLE_STORAGE_PROVIDER_ROUTING_RULES, audit.resourceType)
        assertEquals("", audit.resourceIds)
    }
}
